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
        Security.addProvider(provider);

        KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        managerFactory.init(keyStore, "".toCharArray());

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(managerFactory.getKeyManagers(), null, null);

        SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(8765);

        while (true) {
            SSLSocket socket = (SSLSocket) serverSocket.accept();
            System.out.println("Client connected: " + socket.getInetAddress());
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String body = "Hello, this is server.";
            String response =
                "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n" + "Connection: close\r\n" + "\r\n" + body;

            out.write(response);
            out.flush();
            socket.close();
        }
        // END: readme-sample-serverSSL
    }

}
