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

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transport.AbstractConduit;
import org.apache.cxf.transport.zmq.uri.ZMQURIConstants;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.zeromq.ZMQ;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZMQConduit extends AbstractConduit {

    private static final Logger LOG = LogUtils.getL7dLogger(ZMQConduit.class);
    private ZMQ.Socket zmqSocket;
    private EndpointConfig endpointConfig;
    private ZMQ.Context zmqContext;
    private ZMQConfiguration zmqConfig;

    public ZMQConfiguration getZmqConfig() {
        return zmqConfig;
    }

    public void setZmqConfig(ZMQConfiguration zmqConfig) {
        this.zmqConfig = zmqConfig;
    }

    public ZMQConduit(EndpointReferenceType target,
                      EndpointConfig endpointConfig) {
        super(target);
        this.endpointConfig = endpointConfig;
    }

    @Override
    public void activate() {
        getLogger().log(Level.FINE, "ZMQConduit activate().... ");
        zmqContext = ZMQResourceFactory.createContext(zmqConfig != null ? zmqConfig.getIoThreads() : 1);
        zmqSocket = ZMQResourceFactory.createSocket(endpointConfig, zmqContext);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public void prepare(final Message message) throws IOException {
        message.setContent(OutputStream.class, new ByteArrayOutputStream() {

            @Override
            public void close() throws IOException {
                super.close();
                sendExchange(message.getExchange(), toByteArray());
            }
        });
    }

    public void sendExchange(final Exchange exchange, final byte[] request) {
        LOG.log(Level.FINE, "ZMQConduit send message");

        final Message outMessage = exchange.getOutMessage() == null
                ? exchange.getOutFaultMessage()
                : exchange.getOutMessage();
        if (outMessage == null) {
            throw new RuntimeException("Exchange to be sent has no outMessage");
        }

        if (!exchange.isOneWay() && endpointConfig.getSocketType().equals(ZMQURIConstants.SocketType.REQ)) {

            synchronized (exchange) {
                ZMQUtils.sendMessage(zmqSocket, request);
                byte[] reply = ZMQUtils.receiveMessage(zmqSocket);
                Message inMessage = new MessageImpl();
                exchange.setInMessage(inMessage);
                inMessage.setContent(InputStream.class, new ByteArrayInputStream(reply));
                if (exchange.isSynchronous()) {
                    exchange.notifyAll();
                }

            }

            if (incomingObserver != null) {
                incomingObserver.onMessage(exchange.getInMessage());
            }

        } else {
            ZMQUtils.sendMessage(zmqSocket, request);
        }

    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public synchronized void close() {
        zmqContext.term();
        LOG.log(Level.FINE, "ZMQConduit closed ");
    }

}
