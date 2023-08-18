package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DemoBrowser implements ActionListener {
    static HttpUrlConnectionClient httpClient;
    static JEditorPane jep;

    public void setup() {
        jep = new JEditorPane();
        jep.setEditable(false);
        httpClient = new HttpUrlConnectionClient();

        JButton b1 = new JButton("Send GET Request");
        b1.setHorizontalTextPosition(AbstractButton.CENTER);
        b1.setActionCommand("sendGET");
        b1.addActionListener((ActionListener) this);

        JButton b2 = new JButton("Send POST Request");
        b2.setHorizontalTextPosition(AbstractButton.CENTER);
        b2.setActionCommand("sendPOST");
        b2.addActionListener((ActionListener) this);

        JScrollPane scrollPane = new JScrollPane(jep);
        scrollPane.setLocation(0, 50);
        scrollPane.setSize(500, 400);

        JPanel panel = new JPanel();
        panel.add(b1);
        panel.add(b2);
        panel.setVisible(true);
        panel.setSize(500,50);


        JFrame f = new JFrame("Test HTML");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(scrollPane);
        f.getContentPane().add(panel);

        Dimension size = new Dimension(500,450);

        f.setPreferredSize(size);
        f.setSize(size);

        f.setVisible(true);
    }

    static void loadPage(HttpUrlConnectionResponse response) {
        jep.setContentType("text/html");

        response.getBodyAsByteArray()
            .map(i -> new String(i, StandardCharsets.UTF_8))
            .map(i -> i.replaceAll("\n", "<br />"))
            .map(i -> i.replaceAll("125.237.75.107", "&lt;IP ADDRESS&gt;"))
            .subscribe(
            value -> {
                System.out.println("setText");
                System.out.println(value);
                jep.setText(value);
            },
            error -> System.out.println("wtf?"),
            () -> System.out.println("completed without a value???")
        );
    }

    public void actionPerformed(ActionEvent e) {
        if ("sendGET".equals(e.getActionCommand())) {
            HttpRequest request = null;
            try {
                request = new HttpRequest(
                    HttpMethod.GET,
                    new URL("https://http-url-connect-client-web.vjr4ig.easypanel.host/home")
                );
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
            request.setHeaders(new HttpHeaders());
            HttpUrlConnectionResponse response = (HttpUrlConnectionResponse) httpClient.send(request).block();

            loadPage(response);
        }
        else {
            HttpRequest request = null;
            try {
                request = new HttpRequest(
                    HttpMethod.POST,
                    new URL("https://http-url-connect-client-web.vjr4ig.easypanel.host/login")
                );
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
            request.setHeaders(new HttpHeaders());
            request.setBody("{\"username\": \"CraigM\", \"password\": \"asdf1234\"}");

            HttpUrlConnectionResponse response = (HttpUrlConnectionResponse) httpClient.send(request).block();

            loadPage(response);
        }
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DemoBrowser browser = new DemoBrowser();
                browser.setup();
            }
        });
    }
}
