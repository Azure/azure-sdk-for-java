// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ClientEndpoint
public class SimpleChatClient {
    private ClientManager client;
    private Session userSession = null;
    private List<Consumer<String>> messageListeners;

    public SimpleChatClient() {    }

    public void connect(String sServer) {
        try {
            client = ClientManager.createClient();
            userSession = client.connectToServer(this, new URI(sServer));
        } catch (DeploymentException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected ... " + session.getId());

        try {
            session.getBasicRemote().sendText("Hello from Java");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        if (message == null || message.equals("OK")) {
            return null;
        }

        if (messageListeners != null) {
            messageListeners.forEach(consumer -> consumer.accept(message));
        }
        return null;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.printf("Session %s close because of %s", session.getId(), closeReason);
//        latch.countDown();
    }

    public void sendMessage(String message) {
        try {
            userSession.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMessageListener(Consumer<String> listener) {
        if (messageListeners == null) {
            messageListeners = new ArrayList<>();
        }
        messageListeners.add(listener);
    }

    public void closeConnection() {
//        latch.countDown();
        client.shutdown();
    }
}
