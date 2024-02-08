package com.azure.communication.messages;

import com.azure.core.util.BinaryData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DownloadMediaSample {
    private static final String connectionString = System.getenv("ACS_CONNECTION_STRING");

    public static void main(String[] args) throws IOException {

        NotificationMessagesClient messagesClient = new NotificationMessagesClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        BinaryData data = messagesClient.downloadMedia("<MEDIA_ID>");
        BufferedImage image = ImageIO.read(data.toStream());
        ImageIcon icon = new ImageIcon(image);
        JLabel label  = new JLabel(icon);
        JFrame frame = new JFrame();
        frame.add(label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
