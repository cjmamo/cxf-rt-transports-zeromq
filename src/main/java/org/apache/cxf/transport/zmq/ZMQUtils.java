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
import org.apache.cxf.transport.zmq.uri.ZMQURIConstants;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ZMQUtils {

    private static final Logger LOG = LogUtils.getL7dLogger(ZMQUtils.class);

    public static void sendMessage(ZMQ.Socket zmqSocket, byte[] message) {
        zmqSocket.send(message, 0);
    }

    public static byte[] receiveMessage(ZMQ.Socket zmqSocket) {
        try {
            return zmqSocket.recv(0);
        } catch (ZMQException e) {
            if(e.getErrorCode() == ZMQURIConstants.ERR_ETERM) {
                LOG.log(Level.FINE, "ZeroMQ context terminated. Closing socket...");
                zmqSocket.close();
                return null;
            }
            else {
                throw e;
            }
        }
    }

	public static ZMsg receiveZMessage(ZMQ.Socket zmqSocket) {
		ZMsg msg = null;
		try {
			msg = ZMsg.recvMsg(zmqSocket);
		} catch (ZMQException e) {
			if (e.getErrorCode() == ZMQURIConstants.ERR_ETERM) {
				LOG.log(Level.FINE,
						"ZeroMQ context terminated. Closing socket...");
				zmqSocket.close();
				return null;
			} else {
				throw e;
			}
		}

		return msg;
	}
            
	public static void sendMessage(Socket zmqSocket, byte[] identifier,
			byte[] byteArray) {
           	ZMsg outGoingMessage = new ZMsg();
      		outGoingMessage.add(identifier);
      		outGoingMessage.add(byteArray);

      		outGoingMessage.send(zmqSocket);
	}
}
