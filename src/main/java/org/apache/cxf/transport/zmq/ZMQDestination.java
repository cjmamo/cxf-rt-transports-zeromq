/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Copyright 2012 Claude Mamo
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cxf.transport.zmq;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractConduit;
import org.apache.cxf.transport.AbstractMultiplexDestination;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.zmq.uri.ZMQURIConstants;
import org.apache.cxf.workqueue.WorkQueueManager;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.*;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZMQDestination extends AbstractMultiplexDestination implements MessageListener {

    private static final Logger LOG = LogUtils.getL7dLogger(ZMQDestination.class);
    private EndpointConfig endpointConfig;
    private ZMQ.Context zmqContext;
    private ZMQConfiguration zmqConfig;

    public ZMQConfiguration getZmqConfig() {
        return zmqConfig;
    }

    public void setZmqConfig(ZMQConfiguration zmqConfig) {
        this.zmqConfig = zmqConfig;
    }

    public ZMQDestination(Bus bus, EndpointInfo endpointInfo, EndpointConfig endpointConfig) {
        super(bus, getTargetReference(endpointInfo, bus), endpointInfo);
        this.endpointConfig = endpointConfig;
    }

    @Override
    public void shutdown() {
        getLogger().log(Level.FINE, "ZMQDestination shutdown()");
        this.deactivate();
    }

    @Override
    public void deactivate() {
        try {
            zmqContext.term();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Conduit getInbuiltBackChannel(Message inMessage) {
        return new BackChannelConduit(EndpointReferenceUtils.getAnonymousEndpointReference(), inMessage);
    }

    @Override
    public void activate() {
        getLogger().log(Level.FINE, "ZMQDestination activate().... ");
        zmqContext = ZMQResourceFactory.createContext(zmqConfig != null ? zmqConfig.getIoThreads() : 1);
        registerZMQListener(endpointConfig, this, bus, zmqContext);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
    
    @Override
    public void onMessage(ZMsg message, ZMQ.Socket zmqSocket)
    {
        getLogger().log(Level.FINE, "server received request: ", message);
       
        Message inMessage = new MessageImpl();
        if(zmqSocket.getType() == ZMQ.ROUTER)
        {
        	inMessage.put("identifier", message.pop().getData());
        }
        inMessage.setContent(InputStream.class, new ByteArrayInputStream(message.getLast().getData()));
        ((MessageImpl) inMessage).setDestination(this);
        inMessage.put("socket", zmqSocket);
        
        incomingObserver.onMessage(inMessage);
	
    }
    //Not in use after dealer router support
    @Override
    public void onMessage(byte[] message, ZMQ.Socket zmqSocket) {
        getLogger().log(Level.FINE, "server received request: ", message);
        
        Message inMessage = new MessageImpl();
        inMessage.setContent(InputStream.class, new ByteArrayInputStream(message));
        ((MessageImpl) inMessage).setDestination(this);
        inMessage.put("socket", zmqSocket);
        
        incomingObserver.onMessage(inMessage);
        
    }

    private void registerZMQListener(EndpointConfig endpointConfig,
                                     final MessageListener messageListener,
                                     Bus bus,
                                     final ZMQ.Context zmqContext) {

        WorkQueueManager queueManager = bus.getExtension(WorkQueueManager.class);
        final Executor executor = queueManager.getNamedWorkQueue("zmq-transport") != null ? queueManager.getNamedWorkQueue("zmq-transport") : queueManager.getAutomaticWorkQueue();
        final ZMQ.Socket zmqSocket = ZMQResourceFactory.createSocket(endpointConfig, zmqContext);
        final ZMQ.Poller poller = ZMQResourceFactory.createPoller(zmqSocket, zmqContext);

        executor.execute(new Runnable() {
           // @Override
            public void run() {
                boolean isContextTerm = false;

                while (!isContextTerm) {
                    poller.poll();

                    //final byte[] message = ZMQUtils.receiveMessage(zmqSocket);
                    final ZMsg message = ZMQUtils.receiveZMessage(zmqSocket);
                    if (message != null) {
                        if (zmqSocket.getType() == ZMQ.REP) {
                            messageListener.onMessage(message, zmqSocket);
                        } else {
                            executor.execute(new Runnable() {
                                public void run() {
                                    messageListener.onMessage(message, zmqSocket);
                                }
                            });
                        }
                    } else {
                        isContextTerm = true;
                    }
                }
            }
        });
    }

    /**
     * Determines if the current message has no response content.
     * The message has no response content if either:
     * - the request is oneway and the current message is no partial
     * response or an empty partial response.
     * - the request is not oneway but the current message is an empty partial
     * response.
     *
     * @param message
     * @return
     */
    private boolean hasNoResponseContent(Message message) {
        final boolean ow = isOneWay(message);
        final boolean pr = MessageUtils.isPartialResponse(message);
        final boolean epr = MessageUtils.isEmptyPartialResponse(message);

        //REVISIT may need to provide an option to choose other behavior?
        // old behavior not suppressing any responses  => ow && !pr
        // suppress empty responses for oneway calls   => ow && (!pr || epr)
        // suppress additionally empty responses for decoupled twoway calls =>
        return (ow && (!pr || epr)) || (!ow && epr);
    }

    /**
     * @param message the message under consideration
     * @return true iff the message has been marked as oneway
     */
    protected final boolean isOneWay(Message message) {
        Exchange ex = message.getExchange();
        return ex == null ? false : ex.isOneWay();
    }

    protected class BackChannelConduit extends AbstractConduit {

        protected Message inMessage;

        BackChannelConduit(EndpointReferenceType t, Message inMessage) {
            super(t);
            this.inMessage = inMessage;
        }

        @Override
        public void close(Message msg) throws IOException {
            super.close(msg);
        }

        public void prepare(final Message message) throws IOException {
            inMessage.getExchange().setOutMessage(message);
            final ZMQ.Socket zmqSocket = (ZMQ.Socket) inMessage.get("socket");

            Exchange exchange = inMessage.getExchange();
            exchange.setOutMessage(message);

            message.setContent(OutputStream.class, new ByteArrayOutputStream() {

                @Override
                public void close() throws IOException {
                    super.close();
                    if (endpointConfig.getSocketType() == ZMQURIConstants.SocketType.REP) {
                        getLogger().log(Level.FINE, "send out the message!");
                        if (hasNoResponseContent(message)) {
                            ZMQUtils.sendMessage(zmqSocket, new byte[]{0});
                        }
                        else {
                            ZMQUtils.sendMessage(zmqSocket, toByteArray());
                        }
                    }
                    else if(endpointConfig.getSocketType() == ZMQURIConstants.SocketType.ROUTER)
                    {
                    	final byte[] identifier = (byte[]) inMessage.get("identifier");
                    	if (hasNoResponseContent(message)) {
                    		 
                             ZMQUtils.sendMessage(zmqSocket, identifier, new byte[]{0});
                        }
                        else {
                             ZMQUtils.sendMessage(zmqSocket, identifier, toByteArray());
                        }
                    }
                }
            });
        }

        protected Logger getLogger() {
            return LOG;
        }

    }

}
