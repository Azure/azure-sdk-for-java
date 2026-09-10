// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.mtls;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultKeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Security;

/**
 * The ServerMTLS sample.
 */
public class ServerMTLSSample {

    public static void main(String[] args) throws Exception {
        // BEGIN: readme-sample-serverMTLS
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        // Register the provider before requesting its KeyStore implementation.
        Security.addProvider(provider);

        System.setProperty("azure.keyvault.uri", "<server-azure-keyvault-uri>");
        System.setProperty("azure.keyvault.tenant-id", "<server-azure-keyvault-tenant-id>");
        System.setProperty("azure.keyvault.client-id", "<server-azure-keyvault-client-id>");
        System.setProperty("azure.keyvault.client-secret", "<server-azure-keyvault-client-secret>");
        // Load the certificate and private key that identify this server to connecting clients.
        KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        // Key managers select the server certificate and private key during each mTLS handshake.
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "".toCharArray());

        System.setProperty("azure.keyvault.uri", "<client-azure-keyvault-uri>");
        System.setProperty("azure.keyvault.tenant-id", "<client-azure-keyvault-tenant-id>");
        System.setProperty("azure.keyvault.client-id", "<client-azure-keyvault-client-id>");
        System.setProperty("azure.keyvault.client-secret", "<client-azure-keyvault-client-secret>");
        // Load the client certificates that this server trusts.
        KeyStore trustStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        // Trust managers validate the certificate presented by each client.
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Combine the server identity with the client trust configuration.
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(8765);
        // Require every client to present a trusted certificate during the TLS handshake.
        serverSocket.setNeedClientAuth(true);

        while (true) {
            // Accept an mTLS connection and write a minimal HTTP response over it.
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
        // END: readme-sample-serverMTLS
    }

}
