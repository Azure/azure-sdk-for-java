// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.tls;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultKeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Security;

/**
 * The ServerSSL sample.
 */
public class ServerSSLSample {

    public static void main(String[] args) throws Exception {
        // BEGIN: readme-sample-serverSSL
        System.setProperty("azure.keyvault.uri", "<your-azure-keyvault-uri>");
        System.setProperty("azure.keyvault.tenant-id", "<your-azure-keyvault-tenant-id>");
        System.setProperty("azure.keyvault.client-id", "<your-azure-keyvault-client-id>");
        System.setProperty("azure.keyvault.client-secret", "<your-azure-keyvault-client-secret>");

        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        // Register the provider before requesting its KeyStore implementation.
        Security.addProvider(provider);

        // Load the certificate and private key that identify this server to connecting clients.
        KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        // Key managers select the server certificate and private key during each TLS handshake.
        KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        managerFactory.init(keyStore, "".toCharArray());

        // Configure one-way TLS: clients aren't required to present a certificate.
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(managerFactory.getKeyManagers(), null, null);

        SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(8765);

        while (true) {
            // Accept a TLS connection and write a minimal HTTP response over it.
            SSLSocket socket = (SSLSocket) serverSocket.accept();
            System.out.println("Client connected: " + socket.getInetAddress());
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String body = "Hello, this is server.";
            // Build a minimal HTTP response and calculate Content-Length from the UTF-8 body bytes.
            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: "
                + body.getBytes(StandardCharsets.UTF_8).length + "\r\nConnection: close\r\n\r\n" + body;

            out.write(response);
            out.flush();
            socket.close();
        }
        // END: readme-sample-serverSSL
    }

}
