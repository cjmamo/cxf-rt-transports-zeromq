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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace = "http://cxf.apache.org/hello_world_zmq", name = "HelloWorldPortType", serviceName = "HelloWorldService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface HelloWorldPortType {

    @WebResult(name = "greeting", targetNamespace = "http://cxf.apache.org/hello_world_zmq", partName = "greeting")
    @WebMethod(action = "sayHello")
    public String sayHello(
            @WebParam(partName = "firstName", name = "firstName")
            String firstName
    );
}
