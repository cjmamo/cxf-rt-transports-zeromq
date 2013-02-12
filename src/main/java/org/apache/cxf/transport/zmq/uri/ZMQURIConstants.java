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
package org.apache.cxf.transport.zmq.uri;

public final class ZMQURIConstants {

    public static final String SOCKETOPERATION_PARAMETER_NAME = "socketOperation";
    public static final String SOCKETTYPE_PARAMETER_NAME = "socketType";
    public static final String FILTER_PARAMETER_NAME = "filter";
    public static final int ERR_EFSM = 156384763;
    public static final int ERR_ETERM = 156384765;

    public static enum SocketType {
        REQ, REP, PUB, XPUB, SUB, XSUB, PUSH, PULL, DEALER, ROUTER, PAIR
    }

    public static enum SocketOperation {
        CONNECT, BIND
    }

    private ZMQURIConstants() {
    }
}
