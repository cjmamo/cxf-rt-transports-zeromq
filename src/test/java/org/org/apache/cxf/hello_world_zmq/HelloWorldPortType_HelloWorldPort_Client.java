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

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public final class HelloWorldPortType_HelloWorldPort_Client {

    private static final QName SERVICE_NAME = new QName("http://cxf.apache.org/hello_world_zmq", "HelloWorldService");

    private HelloWorldPortType_HelloWorldPort_Client() {
    }

    public static void main(String args[]) throws Exception {
        URL wsdlURL = HelloWorldService.WSDL_LOCATION;
        if (args.length > 0 && args[0] != null && !"".equals(args[0])) {
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        HelloWorldService ss = new HelloWorldService(wsdlURL, SERVICE_NAME);
        HelloWorldPortType port = ss.getHelloWorldRequestResponsePort();

        {
            System.out.println("Invoking sayHello...");
            String _sayHello_firstName = "";
            String _sayHello__return = port.sayHello(_sayHello_firstName);
            System.out.println("sayHello.result=" + _sayHello__return);


        }

        System.exit(0);
    }

}
