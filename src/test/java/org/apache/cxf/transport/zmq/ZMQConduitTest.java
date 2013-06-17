package org.apache.cxf.transport.zmq;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.org.apache.cxf.hello_world_zmq.HelloWorldImpl;
import org.org.apache.cxf.hello_world_zmq.HelloWorldPortType;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class ZMQConduitTest extends AbstractZMQTransportTest {

    @Test
    public void testDecoupledEndpoint() throws Exception {

        JaxWsServerFactoryBean serverFactory = new JaxWsServerFactoryBean();
        serverFactory.setAddress("zmq:(tcp://*:" + ZMQ_TEST_PORT + "?socketOperation=bind&socketType=pull)");
        serverFactory.setServiceClass(HelloWorldImpl.class);
        Server server = serverFactory.create();

        JaxWsProxyFactoryBean clientFactory = new JaxWsProxyFactoryBean();
        clientFactory.setServiceClass(HelloWorldPortType.class);
        clientFactory.setAddress("zmq:(tcp://localhost:" + ZMQ_TEST_PORT + "?socketOperation=connect&socketType=push)");
        clientFactory.getFeatures().add(new WSAddressingFeature());

        HelloWorldPortType client = (HelloWorldPortType) clientFactory.create();

        ClientProxy.getClient(client).getEndpoint()
                                     .getEndpointInfo()
                                     .setProperty("org.apache.cxf.ws.addressing.replyto",
                                                  "zmq:(tcp://127.0.0.1:5555?socketOperation=connect&socketType=push)");

        String reply = client.sayHello("Claude");

        server.stop();

        assertEquals("Hello Claude", reply);
    }

    @Test
    public void testConfigurationFromSpring() throws Exception {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"/spring/zmq-test-client-config.xml"});
        HelloWorldPortType client = (HelloWorldPortType) ctx.getBean("helloWorldClient");

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
    public void testConfigurationFromAPIRouterDealer() throws Exception {

        new Thread(new Runnable() {
            public void run() {
                ZMQ.Socket zmqSocket = zmqContext.socket(ZMQ.ROUTER);
                zmqSocket.bind("tcp://*:" + ZMQ_TEST_PORT);
                //byte[] request = zmqSocket.recv(0);
                ZMsg msg = ZMsg.recvMsg(zmqSocket);
                byte [] address = msg.pop().getData();
                
                try {
                    
                	XMLAssert.assertXMLEqual(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-request.xml").toURI())), new String(msg.getLast().getData()));
                    
                    ZMsg outGoingMessage = new ZMsg();
                    outGoingMessage.add(address);//copy address/identity
                    outGoingMessage.add(FileUtils.readFileToString(new File(getClass().getResource("/samples/soap-reply.xml").toURI())));
                    outGoingMessage.send(zmqSocket);
                    
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                zmqSocket.close();
            }
        }).start();

        String address = "zmq:(tcp://localhost:" + ZMQ_TEST_PORT + "?socketOperation=connect&socketType=dealer)";

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
