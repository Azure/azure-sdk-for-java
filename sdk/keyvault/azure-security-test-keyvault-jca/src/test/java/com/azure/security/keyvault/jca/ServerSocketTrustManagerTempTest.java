// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Security;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "0myalias")
public class ServerSocketTrustManagerTempTest {

    @Test
    public void testServerSocketWithSelfSignedTrustManager() throws Exception {

        /*
         * Add JCA provider.
         */
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);
        Security.insertProviderAt(new KeyVaultTrustManagerFactoryProvider(), 1);

        /*
         * Setup server side.
         *
         *  - Create an Azure Key Vault specific instance of a KeyStore.
         *  - Set the KeyManagerFactory to use that KeyStore.
         *  - Set the SSL context to use the KeyManagerFactory.
         *  - Create the SSLServerSocket using th SSL context.
         */
        PropertyConvertorUtils.putEnvironmentPropertyToSystemProperty(
            Arrays.asList("AZURE_KEYVAULT_URI",
                "AZURE_KEYVAULT_TENANT_ID",
                "AZURE_KEYVAULT_CLIENT_ID",
                "AZURE_KEYVAULT_CLIENT_SECRET")
        );
        KeyStore ks = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("AZURE_KEYVAULT_URI"),
            System.getenv("AZURE_KEYVAULT_TENANT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_SECRET"));
        ks.load(parameter);


        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLServerSocketFactory factory = context.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(8867);
        serverSocket.setNeedClientAuth(true);

        Thread server = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    try (OutputStream outputStream = socket.getOutputStream()) {
                        outputStream.write("HTTP/1.1 204\r\n".getBytes());
                        outputStream.flush();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
        server.start();

        /*
         * Setup client side
         *
         * - Create an SSL context.
         * - Set SSL context to trust any certificate.
         * - Create SSL connection factory.
         * - Set hostname verifier to trust any hostname.
         */

        SSLContext sslContext = SSLContexts
            .custom()
            .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
            .loadKeyMaterial(ks, "".toCharArray(), new ClientPrivateKeyStrategy())
            .build();

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
            sslContext, (hostname, session) -> true);

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .build());

        /*
         * And now execute the test.
         */
        String result = null;

        try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).build()) {
            HttpGet httpGet = new HttpGet("https://localhost:8867");
            ResponseHandler<String> responseHandler = (HttpResponse response) -> {
                int status = response.getStatusLine().getStatusCode();
                String result1 = null;
                if (status == 204) {
                    result1 = "Success";
                }
                return result1;
            };
            result = client.execute(httpGet, responseHandler);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        /*
         * And verify all went well.
         */
        assertEquals("Success", result);
    }

    @Test
    public void testServerSocketWithDefaultTrustManager() throws Exception {

        /*
         * Add JCA provider.
         */
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);

        /*
         * Setup server side.
         *
         *  - Create an Azure Key Vault specific instance of a KeyStore.
         *  - Set the KeyManagerFactory to use that KeyStore.
         *  - Set the SSL context to use the KeyManagerFactory.
         *  - Create the SSLServerSocket using th SSL context.
         */
        PropertyConvertorUtils.putEnvironmentPropertyToSystemProperty(
            Arrays.asList("AZURE_KEYVAULT_URI",
                "AZURE_KEYVAULT_TENANT_ID",
                "AZURE_KEYVAULT_CLIENT_ID",
                "AZURE_KEYVAULT_CLIENT_SECRET")
        );
        KeyStore ks = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("AZURE_KEYVAULT_URI"),
            System.getenv("AZURE_KEYVAULT_TENANT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_SECRET"));
        ks.load(parameter);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLServerSocketFactory factory = context.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(8868);
        serverSocket.setNeedClientAuth(true);

        Thread server = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    try (OutputStream outputStream = socket.getOutputStream()) {
                        outputStream.write("HTTP/1.1 204\r\n".getBytes());
                        outputStream.flush();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
        server.start();

        /*
         * Setup client side
         *
         * - Create an SSL context.
         * - Set SSL context to trust any certificate.
         * - Create SSL connection factory.
         * - Set hostname verifier to trust any hostname.
         */

        SSLContext sslContext = SSLContexts
            .custom()
            .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
            .loadKeyMaterial(ks, "".toCharArray(), new ClientPrivateKeyStrategy())
            .build();

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
            sslContext, (hostname, session) -> true);

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .build());

        /*
         * And now execute the test.
         */
        String result = null;

        try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).build()) {
            HttpGet httpGet = new HttpGet("https://localhost:8868");
            ResponseHandler<String> responseHandler = (HttpResponse response) -> {
                int status = response.getStatusLine().getStatusCode();
                String result1 = null;
                if (status == 204) {
                    result1 = "Success";
                }
                return result1;
            };
            result = client.execute(httpGet, responseHandler);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        /*
         * And verify all went well.
         */
        assertEquals("Success", result);
    }


    private static class ClientPrivateKeyStrategy implements PrivateKeyStrategy {
        @Override
        public String chooseAlias(Map<String, PrivateKeyDetails> map, Socket socket) {
            return "0myalias"; // It should be your certificate alias used in client-side
        }
    }

}
