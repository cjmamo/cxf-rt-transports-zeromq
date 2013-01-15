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
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.ConfigurationException;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.springframework.beans.factory.annotation.Required;

import java.util.logging.Logger;

@NoJSR250Annotations
public class ZMQConfigFeature extends AbstractFeature {
    static final Logger LOG = LogUtils.getL7dLogger(ZMQConfigFeature.class);

    ZMQConfiguration zmqConfig;

    @Override
    public void initialize(Client client, Bus bus) {
        checkZmqConfig();
        Conduit conduit = client.getConduit();
        if (!(conduit instanceof ZMQConduit)) {
            throw new ConfigurationException(new Message("ZMQCONFIGFEATURE_ONLY_ZMQ", LOG));
        }
        Endpoint ep = client.getEndpoint();
        changeTransportUriToZmq(ep);
        ZMQConduit zmqConduit = (ZMQConduit)conduit;
        zmqConduit.setZmqConfig(zmqConfig);
        super.initialize(client, bus);
    }

    @Override
    public void initialize(Server server, Bus bus) {
        checkZmqConfig();
        Destination destination = server.getDestination();
        if (!(destination instanceof ZMQDestination)) {
            throw new ConfigurationException(new Message("ZMQCONFIGFEATURE_ONLY_ZMQ", LOG));
        }
        Endpoint ep = server.getEndpoint();
        changeTransportUriToZmq(ep);
        ZMQDestination zmqDestination = (ZMQDestination)destination;
        zmqDestination.setZmqConfig(zmqConfig);
        super.initialize(server, bus);
    }

    private void changeTransportUriToZmq(Endpoint ep) {
        if (ep.getBinding() == null) {
            return;
        }
        if (ep.getBinding().getBindingInfo() == null) {
            return;
        }
        BindingInfo bindingInfo = ep.getBinding().getBindingInfo();
        if (bindingInfo instanceof SoapBindingInfo) {
            SoapBindingInfo soapBindingInfo = (SoapBindingInfo) bindingInfo;
            soapBindingInfo.setTransportURI("http://schemas.xmlsoap.org/soap/zmq");
        }
    }

    public ZMQConfiguration getZmqConfig() {
        return zmqConfig;
    }

    @Required
    public void setZmqConfig(ZMQConfiguration zmqConfig) {
        this.zmqConfig = zmqConfig;
    }

    private void checkZmqConfig() {
        if (zmqConfig == null) {
            throw new ConfigurationException(new Message("ZMQCONFIG_REQUIRED", LOG));
        }
    }
}
