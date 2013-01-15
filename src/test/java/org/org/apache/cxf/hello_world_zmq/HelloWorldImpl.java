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

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;

@WebService(targetNamespace = "http://cxf.apache.org/hello_world_zmq", serviceName = "HelloWorldService", portName = "HelloWorldPort")
@Addressing(enabled = true)
public class HelloWorldImpl {

    @WebResult(name = "greeting")
    public String sayHello(@WebParam(name = "firstName") String firstName) {
        return "Hello " + firstName;
    }
}
