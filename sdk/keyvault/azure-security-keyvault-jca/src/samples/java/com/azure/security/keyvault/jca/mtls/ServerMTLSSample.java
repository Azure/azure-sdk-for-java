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
import java.security.KeyStore;
import java.security.Security;

/**
 * The ServerMTLS sample.
 */
public class ServerMTLSSample {

    public static void main(String[] args) throws Exception {
        // BEGIN: readme-sample-serverMTLS
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);

        System.setProperty("azure.keyvault.uri", "<server-azure-keyvault-uri>");
        System.setProperty("azure.keyvault.tenant-id", "<server-azure-keyvault-tenant-id>");
        System.setProperty("azure.keyvault.client-id", "<server-azure-keyvault-client-id>");
        System.setProperty("azure.keyvault.client-secret", "<server-azure-keyvault-client-secret>");
        KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "".toCharArray());

        System.setProperty("azure.keyvault.uri", "<client-azure-keyvault-uri>");
        System.setProperty("azure.keyvault.tenant-id", "<client-azure-keyvault-tenant-id>");
        System.setProperty("azure.keyvault.client-id", "<client-azure-keyvault-client-id>");
        System.setProperty("azure.keyvault.client-secret", "<client-azure-keyvault-client-secret>");
        KeyStore trustStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(8765);
        serverSocket.setNeedClientAuth(true);

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
        // END: readme-sample-serverMTLS
    }

}
