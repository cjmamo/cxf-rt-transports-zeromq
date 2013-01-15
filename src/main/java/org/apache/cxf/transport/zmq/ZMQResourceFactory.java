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

import org.apache.cxf.transport.zmq.uri.ZMQURIConstants;
import org.zeromq.ZMQ;

public class ZMQResourceFactory {

    private ZMQResourceFactory() {

    }

    public static ZMQ.Context createContext(int ioThreads) {
        return ZMQ.context(ioThreads);
    }

    public static ZMQ.Socket createSocket(EndpointConfig endpointConfig, ZMQ.Context zmqContext) {
        int socketType = socketTypeToInteger(endpointConfig.getSocketType());
        ZMQ.Socket zmqSocket = zmqContext.socket(socketType);

        if (endpointConfig.getIdentity() != null) {
            zmqSocket.setIdentity(endpointConfig.getIdentity().getBytes());
        }

        if (socketType == ZMQ.PUB) {
            String[] subscribers = endpointConfig.getEndpointUri().split(";");

            for (String subscriber : subscribers) {
                prepare(zmqSocket, endpointConfig.getSocketOperation(), subscriber);
            }
        } else {
            prepare(zmqSocket, endpointConfig.getSocketOperation(), endpointConfig.getEndpointUri());
        }

        if (socketType == ZMQ.SUB) {
            if (endpointConfig.getFilter() != null) {
                zmqSocket.subscribe(endpointConfig.getFilter().getBytes());
            } else {
                zmqSocket.subscribe(new byte[]{});
            }
        }

        return zmqSocket;
    }

    private static void prepare(ZMQ.Socket zmqSocket, ZMQURIConstants.SocketOperation socketOperation, String address) {
        if (socketOperation.equals(ZMQURIConstants.SocketOperation.BIND)) {
            zmqSocket.bind(address);
        } else {
            zmqSocket.connect(address);
        }
    }

    public static ZMQ.Poller createPoller(ZMQ.Socket zmqSocket, ZMQ.Context zmqContext) {
        ZMQ.Poller poller = zmqContext.poller(1);
        poller.register(zmqSocket);

        return poller;
    }

    private static int socketTypeToInteger(ZMQURIConstants.SocketType socketType) {

        switch (socketType) {
            case REP:
                return ZMQ.REP;

            case REQ:
                return ZMQ.REQ;

            case SUB:
                return ZMQ.SUB;

            case XSUB:
                return ZMQ.XSUB;

            case PUB:
                return ZMQ.PUB;

            case XPUB:
                return ZMQ.XPUB;

            case PULL:
                return ZMQ.PULL;

            case PUSH:
                return ZMQ.PUSH;

            case DEALER:
                return ZMQ.DEALER;

            case ROUTER:
                return ZMQ.ROUTER;

            case PAIR:
                return ZMQ.PAIR;

            default:
                throw new UnsupportedOperationException();
        }
    }

}
