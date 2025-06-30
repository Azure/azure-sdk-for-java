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

@ClientEndpoint
public class WebSocketTestClient {
    private ClientManager client;
    private Session userSession = null;

    public WebSocketTestClient() {
    }

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
        // Connection opened
    }

    @OnMessage
    public void onMessage(byte[] message) {
    }

    @OnMessage
    public void onMessage(String message) {
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        // Connection closed
    }

    public void close() {
        if (userSession != null && userSession.isOpen()) {
            try {
                userSession.close();
            } catch (IOException e) {
                // Closing a session that's already closing can throw an exception.
                // It's safe to ignore.
            }
        }
        if (client != null) {
            client.shutdown();
        }
    }
}
