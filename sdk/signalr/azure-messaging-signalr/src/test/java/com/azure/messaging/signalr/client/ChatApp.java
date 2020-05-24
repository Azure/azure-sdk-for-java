// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr.client;

import com.azure.core.util.Configuration;
import com.azure.messaging.signalr.SignalRClient;
import com.azure.messaging.signalr.SignalRClientBuilder;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatApp {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("SIGNALR_WS_ENDPOINT");
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("SIGNALR_CS");
    private static final String WEB_SOCKET_URL = ENDPOINT + "/ws/client/?user=JGApp";

    private SimpleChatClient wsClient;
    private SignalRClient signalrClient;

    private JRadioButton useWSBtn;
    private JRadioButton useSignalRBtn;

    public static void main(String[] args) {
        new ChatApp().run();
    }

    private void run() {
        // This is a direct websocket connection to the service
        wsClient = new SimpleChatClient();
        wsClient.connect(WEB_SOCKET_URL);

        // create a SignalR client that connects to the default hub with no group specified
        signalrClient = new SignalRClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildClient();

        JFrame frame = new JFrame("Chat App");
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.out.println("Closing connections...");
                wsClient.closeConnection();
//                signalrClient.closeConnection(); // TODO (jgiles) what connection ID to use?
                System.out.println("Done closing connections");
                System.exit(0);
            }
        });

        useWSBtn = new JRadioButton("Use WebSockets");
        useSignalRBtn = new JRadioButton("Use SignalR Client");
        useSignalRBtn.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(useWSBtn);
        group.add(useSignalRBtn);

        JPanel northPanel = new JPanel();
        northPanel.add(useWSBtn);
        northPanel.add(useSignalRBtn);
        frame.add(northPanel, BorderLayout.NORTH);

        // incoming messages always come via the websocket connection. The SignalR client library does not receive events.
        final DefaultListModel<String> listModel = new DefaultListModel<>();
        wsClient.addMessageListener(listModel::addElement);
        JList<String> list = new JList<>(listModel);
        frame.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        frame.add(southPanel, BorderLayout.SOUTH);

        final JTextField textField = new JTextField();
        textField.addActionListener(e -> sendMessage(textField.getText()));
        southPanel.add(textField, BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> {
            sendMessage(textField.getText());
            textField.setText("");
        });
        southPanel.add(sendBtn, BorderLayout.EAST);

        frame.setSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void sendMessage(String message) {
        if (useWSBtn.isSelected()) {
            System.out.println("Sending using web services");
            wsClient.sendMessage(message);
        } else {
            System.out.println("Sending using signalR");
            signalrClient.sendToAll(message);
        }
    }
}
