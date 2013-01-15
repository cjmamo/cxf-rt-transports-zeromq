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
package org.org.apache.cxf.hello_world_zmq;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.URL;

@WebServiceClient(name = "HelloWorldService",
        wsdlLocation = "zmq_test.wsdl",
        targetNamespace = "http://cxf.apache.org/hello_world_zmq")
public class HelloWorldService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://cxf.apache.org/hello_world_zmq", "HelloWorldService");
    public final static QName HelloWorldRequestResponsePort = new QName("http://cxf.apache.org/hello_world_zmq", "HelloWorldRequestResponsePort");
    public final static QName HelloWorldPullPort = new QName("http://cxf.apache.org/hello_world_zmq", "HelloWorldPullPort");

    static {
        URL url = HelloWorldService.class.getResource("zmq_test.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(HelloWorldService.class.getName())
                    .log(java.util.logging.Level.INFO,
                            "Can not initialize the default wsdl from {0}", "zmq_test.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public HelloWorldService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public HelloWorldService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public HelloWorldService() {
        super(WSDL_LOCATION, SERVICE);
    }


    /**
     * @return returns HelloWorldPortType
     */
    @WebEndpoint(name = "HelloWorldRequestResponsePort")
    public HelloWorldPortType getHelloWorldRequestResponsePort() {
        return super.getPort(HelloWorldRequestResponsePort, HelloWorldPortType.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns HelloWorldPortType
     */
    @WebEndpoint(name = "HelloWorldRequestResponsePort")
    public HelloWorldPortType getHelloWorldRequestResponsePort(WebServiceFeature... features) {
        return super.getPort(HelloWorldRequestResponsePort, HelloWorldPortType.class, features);
    }

    /**
     * @return returns HelloWorldPortType
     */
    @WebEndpoint(name = "HelloWorldPullPort")
    public HelloWorldPortType getHelloWorldPullPort() {
        return super.getPort(HelloWorldPullPort, HelloWorldPortType.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns HelloWorldPortType
     */
    @WebEndpoint(name = "HelloWorldPullPort")
    public HelloWorldPortType getHelloWorldPullPort(WebServiceFeature... features) {
        return super.getPort(HelloWorldPullPort, HelloWorldPortType.class, features);
    }

}
