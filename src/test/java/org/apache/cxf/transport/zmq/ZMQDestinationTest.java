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

import org.apache.commons.io.FileUtils;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.org.apache.cxf.hello_world_zmq.HelloWorldImpl;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.zeromq.ZMQ;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ZMQDestinationTest extends AbstractZMQTransportTest {

    @Test
    public void testConfigurationFromSpring() throws Exception{
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"/spring/zmq-test-service-config.xml"});
        EndpointImpl endpoint = (EndpointImpl)ctx.getBean("helloWorld");

        ZMQ.Socket zmqSocket = zmqContext.socket(ZMQ.REQ);
        zmqSocket.connect("tcp://localhost:" + ZMQ_TEST_PORT);
        zmqSocket.send(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-request.xml").toURI())).getBytes(), 0);
        byte[] reply = zmqSocket.recv(0);
        zmqSocket.close();

        endpoint.stop();

        XMLAssert.assertXMLEqual(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-reply.xml").toURI())), new String(reply));
    }

    @Test
    public void testConfigurationFromAPI() throws Exception {

        JaxWsServerFactoryBean bean = new JaxWsServerFactoryBean();
        bean.setAddress("zmq:(tcp://*:" + ZMQ_TEST_PORT + "?socketOperation=bind&socketType=rep)");
        bean.setServiceClass(HelloWorldImpl.class);
        Server server = bean.create();

        ZMQ.Socket zmqSocket = zmqContext.socket(ZMQ.REQ);
        zmqSocket.connect("tcp://localhost:" + ZMQ_TEST_PORT);
        zmqSocket.send(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-request.xml").toURI())).getBytes(), 0);
        byte[] reply = zmqSocket.recv(0);
        zmqSocket.close();

        server.stop();

        XMLAssert.assertXMLEqual(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-reply.xml").toURI())), new String(reply));
    }

    @Test
    public void testReplyTo() throws Exception {

        JaxWsServerFactoryBean bean = new JaxWsServerFactoryBean();
        bean.setAddress("zmq:(tcp://*:" + ZMQ_TEST_PORT + "?socketOperation=bind&socketType=pull)");
        bean.setServiceClass(HelloWorldImpl.class);
        Server server = bean.create();

        ZMQ.Socket zmqSocket = zmqContext.socket(ZMQ.PUSH);
        zmqSocket.connect("tcp://localhost:" + ZMQ_TEST_PORT);
        zmqSocket.send(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-wa-request.xml").toURI())).getBytes(), 0);
        zmqSocket.close();

        ZMQ.Socket receiverSocket = zmqContext.socket(ZMQ.PULL);
        receiverSocket.bind("tcp://*:9001");
        byte[] reply = receiverSocket.recv(0);
        receiverSocket.close();

        server.stop();

        Diff myDiff = new Diff(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-wa-reply.xml").toURI())), new String(reply));
        DetailedDiff myDifferenceListener = new DetailedDiff(myDiff);
        List<Difference> diffs = myDifferenceListener.getAllDifferences();

        assertEquals(1, diffs.size());
        assertEquals("/Envelope[1]/Header[1]/MessageID[1]/text()[1]", diffs.get(0).getControlNodeDetail().getXpathLocation());
    }

    @Test
    public void testConfigurationFromWSDL() throws Exception {
        JaxWsServerFactoryBean bean = new JaxWsServerFactoryBean();
        bean.setWsdlLocation("/wsdl/zmq_test.wsdl");
        bean.setServiceClass(HelloWorldImpl.class);
        Server server = bean.create();

        ZMQ.Socket zmqSocket = zmqContext.socket(ZMQ.REQ);
        zmqSocket.connect("tcp://localhost:" + ZMQ_TEST_PORT);
        zmqSocket.send(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-request.xml").toURI())).getBytes(), 0);
        byte[] reply = zmqSocket.recv(0);
        zmqSocket.close();

        server.stop();

        XMLAssert.assertXMLEqual(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-reply.xml").toURI())), new String(reply));
    }

}
