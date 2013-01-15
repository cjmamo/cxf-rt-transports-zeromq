package org.apache.cxf.transport.zmq;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.org.apache.cxf.hello_world_zmq.HelloWorldPortType;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.zeromq.ZMQ;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class ZMQConduitTest extends AbstractZMQTransportTest {

    @Test
    public void testConfigurationFromSpring() throws Exception {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"/spring/zmq-test-client-config.xml"});
        HelloWorldPortType client = (HelloWorldPortType)ctx.getBean("helloWorldClient");

        new Thread(new Runnable() {
            @Override
            public void run() {
                ZMQ.Socket zmqSocket = zmqContext.socket(ZMQ.REP);
                zmqSocket.bind("tcp://*:" + ZMQ_TEST_PORT);
                byte[] request = zmqSocket.recv(0);
                try {
                    XMLAssert.assertXMLEqual(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-request.xml").toURI())), new String(request));
                    zmqSocket.send(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-reply.xml").toURI())));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                zmqSocket.close();
            }
        }).start();

        String reply = client.sayHello("Claude");
        assertEquals("Hello Claude", reply);
    }

    @Test
    public void testConfigurationFromAPI() throws Exception {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ZMQ.Socket zmqSocket = zmqContext.socket(ZMQ.REP);
                zmqSocket.bind("tcp://*:" + ZMQ_TEST_PORT);
                byte[] request = zmqSocket.recv(0);
                try {
                    XMLAssert.assertXMLEqual(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-request.xml").toURI())), new String(request));
                    zmqSocket.send(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-reply.xml").toURI())));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                zmqSocket.close();
            }
        }).start();

        String address = "zmq:(tcp://localhost:" + ZMQ_TEST_PORT + "?socketOperation=connect&socketType=req)";

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(HelloWorldPortType.class);
        factory.setAddress(address);
        HelloWorldPortType client = (HelloWorldPortType) factory.create();
        String reply = client.sayHello("Claude");
        assertEquals("Hello Claude", reply);
    }

    @Test
    public void testConfigurationFromWSDL() throws Exception {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ZMQ.Socket zmqSocket = zmqContext.socket(ZMQ.REP);
                zmqSocket.bind("tcp://*:" + ZMQ_TEST_PORT);
                byte[] request = zmqSocket.recv(0);
                try {
                    XMLAssert.assertXMLEqual(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-request.xml").toURI())), new String(request));
                    zmqSocket.send(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-reply.xml").toURI())));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                zmqSocket.close();
            }
        }).start();

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(HelloWorldPortType.class);
        factory.setWsdlLocation("/wsdl/zmq_test.wsdl");
        HelloWorldPortType client = (HelloWorldPortType) factory.create();
        String reply = client.sayHello("Claude");
        assertEquals("Hello Claude", reply);
    }
}
