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
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.*;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZMQTransportFactory extends AbstractTransportFactory implements ConduitInitiator, DestinationFactory {

    public static final List<String> DEFAULT_NAMESPACES
            = Arrays.asList(
            "http://cxf.apache.org/transports/zmq"
    );

    private static final Set<String> URI_PREFIXES = new HashSet<String>();

    static {
        URI_PREFIXES.add("zmq:");
    }

    public ZMQTransportFactory() {
        super(DEFAULT_NAMESPACES);
    }

    public ZMQTransportFactory(Bus b) {
        super(DEFAULT_NAMESPACES, b);
    }

    @Resource(name = "cxf")
    public void setBus(Bus bus) {
        super.setBus(bus);
    }

    @Override
    public Conduit getConduit(EndpointInfo endpointInfo) throws IOException {
        return getConduit(endpointInfo, endpointInfo.getTarget());
    }

    @Override
    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType target) throws IOException {
        ConfigurationFactory configFactory = new ConfigurationFactory();
        EndpointConfig endpointConfig = configFactory.createConfiguration(bus, endpointInfo, target, true);
        return new ZMQConduit(target, endpointConfig, endpointInfo, bus);
    }

    @Override
    public Destination getDestination(EndpointInfo endpointInfo) throws IOException {
        ConfigurationFactory configFactory = new ConfigurationFactory();
        EndpointConfig endpointConfig = configFactory.createConfiguration(bus, endpointInfo, null, false);
        return new ZMQDestination(bus, endpointInfo, endpointConfig);
    }

    @Override
    public Set<String> getUriPrefixes() {
        return URI_PREFIXES;
    }
}
