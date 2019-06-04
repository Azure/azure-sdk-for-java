package com.azure.eventhubs.implementation.handler;

import com.azure.eventhubs.implementation.ClientConstants;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.azure.eventhubs.implementation.handler.ConnectionHandler.AMQPS_PORT;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.FRAMEWORK;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.MAX_FRAME_SIZE;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.PLATFORM;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.PRODUCT;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.USER_AGENT;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.VERSION;
import static org.mockito.Mockito.mock;

public class ConnectionHandlerTest {
    private static final String CONNECTION_ID = "some-random-id";
    private static final String HOSTNAME = "hostname-random";
    private ConnectionHandler handler;

    @Before
    public void setup() {
        handler = new ConnectionHandler(CONNECTION_ID, HOSTNAME);
    }

    @Test
    public void createHandler() {
        // Assert
        Assert.assertEquals(HOSTNAME, handler.getHostname());
        Assert.assertEquals(MAX_FRAME_SIZE, handler.getMaxFrameSize());
        Assert.assertEquals(AMQPS_PORT, handler.getProtocolPort());

        final Map<String, Object> properties = handler.getConnectionProperties();
        Assert.assertTrue(properties.containsKey(PRODUCT.toString()));
        Assert.assertTrue(properties.containsKey(VERSION.toString()));
        Assert.assertTrue(properties.containsKey(PLATFORM.toString()));
        Assert.assertTrue(properties.containsKey(FRAMEWORK.toString()));
        Assert.assertTrue(properties.containsKey(USER_AGENT.toString()));
    }

    @Test
    public void addsSslLayer() {
        // Arrange
        final Event event = mock(Event.class);
        final Transport transport = mock(Transport.class);
    }
}
