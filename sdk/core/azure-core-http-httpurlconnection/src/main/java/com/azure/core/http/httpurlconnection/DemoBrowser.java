package com.azure.core.http.httpurlconnection;

import com.azure.core.http.*;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Context;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DemoBrowser implements ActionListener {
    static HttpClient httpClient;
    static JEditorPane jep;
    static JTextField t1, t2;

    public void setup() {
        String title = "Demo Browser - ";

//        httpClient = new NettyAsyncHttpClientBuilder().build(); title += "NettyClient";
        httpClient = new HttpUrlConnectionClientBuilder().build(); title += "HttpUrlConnectionClient";

        jep = new JEditorPane();
        jep.setEditable(false);

        JButton b1 = new JButton("GET");
        b1.setHorizontalTextPosition(AbstractButton.CENTER);
        b1.setActionCommand("sendGET");
        b1.addActionListener((ActionListener) this);

        t1 = new JTextField();
        t1.setColumns(8);

        JButton b2 = new JButton("POST");
        b2.setHorizontalTextPosition(AbstractButton.CENTER);
        b2.setActionCommand("sendPOST");
        b2.addActionListener(this);

        JButton b3 = new JButton("DELETE");
        b3.setHorizontalTextPosition(AbstractButton.CENTER);
        b3.setActionCommand("sendDELETE");
        b3.addActionListener(this);

        t2 = new JTextField();
        t2.setColumns(8);

        JButton b4 = new JButton("PATCH");
        b4.setHorizontalTextPosition(AbstractButton.CENTER);
        b4.setActionCommand("sendPATCH");
        b4.addActionListener(this);

        JButton b5 = new JButton("RESET");
        b5.setHorizontalTextPosition(AbstractButton.CENTER);
        b5.setActionCommand("sendRESET");
        b5.addActionListener(this);

        JScrollPane scrollPane = new JScrollPane(jep);
        scrollPane.setLocation(0, 50);
        scrollPane.setSize(600, 400);

        JPanel panel = new JPanel();
        panel.add(b1);
        panel.add(t1);
        panel.add(b2);
        panel.add(b3);
        panel.add(t2);
        panel.add(b4);
        panel.add(b5);
        panel.setVisible(true);
        panel.setSize(500,50);


        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(scrollPane);
        f.getContentPane().add(panel);

        Dimension size = new Dimension(600,450);

        f.setPreferredSize(size);
        f.setSize(size);

        f.setVisible(true);
    }

    static void loadPage(HttpResponse response) {
        jep.setContentType("text/html");

        response.getBodyAsByteArray()
            .map(i -> new String(i, StandardCharsets.UTF_8))
            .map(i -> i.replaceAll("\n", "<br />"))
            .map(i -> i.replaceAll("125.237.75.107", "&lt;IP ADDRESS&gt;"))
            .subscribe(
                value -> {
                    jep.setText(value);
                }
            );
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        HttpRequest request = null;
        HttpResponse response;
        HttpHeaders headers;

        switch(action) {
            case "sendGET":
                try {
                    request = new HttpRequest(
                        HttpMethod.GET,
                        new URL("http://localhost:5000/get")
                    );
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
                request.setHeaders(new HttpHeaders());
                response = httpClient.sendSync(request, Context.NONE);

                loadPage(response);
                break;
            case "sendPOST":
                try {
                    request = new HttpRequest(
                        HttpMethod.POST,
                        new URL("http://localhost:5000/add")
                    );
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
                headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                request.setHeaders(headers);
                request.setBody("{\"name\": \""+ t1.getText() +"\"}");

                response = httpClient.sendSync(request, Context.NONE);

                loadPage(response);
                break;
            case "sendDELETE":
                try {
                    request = new HttpRequest(
                        HttpMethod.DELETE,
                        new URL("http://localhost:5000/delete/"+t1.getText())
                    );
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
                request.setHeaders(new HttpHeaders());
                response = httpClient.sendSync(request, Context.NONE);
                loadPage(response);
                break;
            case "sendPATCH":
                try {
                    request = new HttpRequest(
                        HttpMethod.PATCH,
                        new URL("http://localhost:5000/patch/"+t1.getText())
                    );
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
                headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                request.setHeaders(headers);
                request.setBody("{\"name\": \""+ t2.getText() +"\"}");

                response = httpClient.sendSync(request, Context.NONE);

                loadPage(response);
                break;
            case "sendRESET":
                try {
                    request = new HttpRequest(
                        HttpMethod.GET,
                        new URL("http://localhost:5000/reset")
                    );
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
                request.setHeaders(new HttpHeaders());
                response = httpClient.sendSync(request, Context.NONE);

                loadPage(response);
                break;
        }
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(() -> {
            DemoBrowser browser = new DemoBrowser();
            browser.setup();
        });
    }
}
