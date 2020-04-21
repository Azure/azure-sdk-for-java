package com.azure.messaging.signalr;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SignalRClientTests {

    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("SIGNALR_CS");
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("SIGNALR_ENDPOINT");

    private SignalRClient client;
    private SignalRAsyncClient asyncClient;

    @BeforeEach
    public void setup() {
        this.client = new SignalRClientBuilder()
            .connectionString(CONNECTION_STRING)
//            .endpoint(endpoint)
            .buildClient();

        this.asyncClient = new SignalRClientBuilder()
          .connectionString(CONNECTION_STRING)
          .buildAsyncClient();
    }

    @Test
    public void assertClientNotNull() {
        assertNotNull(client);
    }

    @Test
    public void testStatus() {
        assertTrue(client.getStatus().isAvailable());
    }

    @Test
    public void testBroadcast() {
        Response<Void> response = client.sendToAll("Hello World - Broadcast test!");
        assertNotNull(response);
        assertEquals(202, response.getStatusCode());
    }

    @Test
    public void testSendToUser() {
        Response<Void> response = client.sendToUser("test_user", "Hello World!");
        assertNotNull(response);
        assertEquals(202, response.getStatusCode());
    }

    @Test
    public void testSendToConnection() {
        Response<Void> response = client.sendToConnection("test_connection", "Hello World!");
        assertNotNull(response);
        assertEquals(202, response.getStatusCode());
    }

    @Test
    public void testAddAndRemoveUserToGroup() {
        SignalRHubClient simpleChat = client.getHubClient("test");
        SignalRGroupClient javaGroup = simpleChat.getGroupClient("java");

        // FIXME don't block
        assertFalse(javaGroup.doesUserExist("Jonathan"));
        Response<Void> addUserResponse = javaGroup.addUser("Jonathan", Context.NONE);
        assertEquals(202, addUserResponse.getStatusCode());
        assertTrue(javaGroup.doesUserExist("Jonathan"));
        Response<Void> removeUserResponse = javaGroup.removeUser("Jonathan", Context.NONE);
        assertEquals(202, removeUserResponse.getStatusCode());
        assertFalse(javaGroup.doesUserExist("Jonathan"));
    }


    /*****************************************************************************************************************
     * Async Tests
     ****************************************************************************************************************/

//    @Test
//    public void testAsyncAddAndRemoveUserToGroup() {
//        SignalRHubAsyncClient simpleChat = asyncClient.getHubClient("simplechat");
//        SignalRGroupAsyncClient javaGroup = simpleChat.getGroupClient("java");
//
//        // FIXME don't block
//        assertFalse(javaGroup.doesUserExist("Jonathan").block().getValue());
//        javaGroup.addUser("Jonathan").block();
//        assertTrue(javaGroup.doesUserExist("Jonathan").block().getValue());
//        javaGroup.removeUser("Jonathan").block();
//        assertFalse(javaGroup.doesUserExist("Jonathan").block().getValue());
//    }
}
