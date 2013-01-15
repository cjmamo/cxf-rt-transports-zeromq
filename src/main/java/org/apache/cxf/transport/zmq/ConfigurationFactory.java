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
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.zmq.uri.ZMQEndpointParser;
import org.apache.cxf.transport.zmq.uri.ZMQURIConstants;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

import java.io.IOException;

public class ConfigurationFactory {

    public EndpointConfig createConfiguration(Bus bus,
                                              EndpointInfo endpointInfo,
                                              EndpointReferenceType target,
                                              boolean isConduit) throws IOException {
        EndpointConfig endpointConfig;

        try {

            String adr = target == null ? endpointInfo.getAddress() : target.getAddress().getValue();

            if (StringUtils.isEmpty(adr)) {
                endpointConfig = new EndpointConfig();
                org.apache.cxf.transport.zmq.AddressType address = null;

                address = endpointInfo.getTraversedExtensor(address, org.apache.cxf.transport.zmq.AddressType.class);

                if (isConduit) {
                    org.apache.cxf.transport.zmq.ClientConfig clientConfig = null;
                    clientConfig = endpointInfo.getTraversedExtensor(clientConfig, org.apache.cxf.transport.zmq.ClientConfig.class);
                    endpointConfig.setSocketType(Enum.valueOf(ZMQURIConstants.SocketType.class, clientConfig.getSocketType().value().toUpperCase()));
                    endpointConfig.setSocketOperation(Enum.valueOf(ZMQURIConstants.SocketOperation.class, clientConfig.getSocketOperation().value().toUpperCase()));
                } else {
                    org.apache.cxf.transport.zmq.ServiceConfig serviceConfig = null;
                    serviceConfig = endpointInfo.getTraversedExtensor(serviceConfig, org.apache.cxf.transport.zmq.ServiceConfig.class);
                    endpointConfig.setSocketType(Enum.valueOf(ZMQURIConstants.SocketType.class, serviceConfig.getSocketType().value().toUpperCase().replace("-", "_")));
                    endpointConfig.setSocketOperation(Enum.valueOf(ZMQURIConstants.SocketOperation.class, serviceConfig.getSocketOperation().value().toUpperCase()));
                }

                endpointConfig.setEndpointUri(address.getLocation());

            } else {
                endpointConfig = ZMQEndpointParser.createEndpointConfig(adr);
            }
        } catch (Exception e) {
            IOException e2 = new IOException(e.getMessage());
            e2.initCause(e);
            throw e2;
        }

        return endpointConfig;
    }

}
