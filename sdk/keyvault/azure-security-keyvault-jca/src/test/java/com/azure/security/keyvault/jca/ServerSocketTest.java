// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.security.KeyStore;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The unit test validating the ServerSocket is created using a certificate from Azure Key Vault.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class ServerSocketTest {

    private static KeyStore ks;

    private static KeyManagerFactory kmf;

    private static String certificateName;

    @BeforeAll
    public static void beforeEach() throws Exception {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemPropertyForKeyVaultJca();
        /*
         * Add JCA provider.
         */
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);

        /*
         *  - Create an Azure Key Vault specific instance of a KeyStore.
         *  - Set the KeyManagerFactory to use that KeyStore.
         */
        ks = PropertyConvertorUtils.getKeyVaultKeyStore();
        kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "".toCharArray());
        certificateName = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    private void startSocket(SSLServerSocket serverSocket) {
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
    }

    @Test
    public void testHttpsConnectionWithoutClientTrust() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, JcaTestUtils.loadTrustMaterial(null, (ignoredChain, ignoredAuthType) -> true), null);
        testHttpsConnection(8765, sslContext);

    }

    @Test
    public void testHttpsConnectionWithSelfSignedClientTrust() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, JcaTestUtils.loadTrustMaterial(ks, (chain, ignored) -> chain.length == 1), null);
        testHttpsConnection(8766, sslContext);

    }

    @Test
    public void testServerSocketWithDefaultTrustManager() throws Exception {
        serverSocketWithTrustManager(8768);
    }

    /**
     * Test SSLServerSocket with key vault trust manager.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testServerSocketWithKeyVaultTrustManager() throws Exception {
        KeyVaultTrustManagerFactoryProvider provider = new KeyVaultTrustManagerFactoryProvider();
        Security.addProvider(provider);
        serverSocketWithTrustManager(8767);
    }

    private void testHttpsConnection(Integer port, SSLContext sslContext) throws Exception {
        /*
         * Setup server side.
         *
         *  - Set the SSL context to use the KeyManagerFactory.
         *  - Create the SSLServerSocket using th SSL context.
         */

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), null, null);

        SSLServerSocketFactory factory = context.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);

        startSocket(serverSocket);

        /*
         * And now execute the test.
         */
        String result = sendRequest(sslContext, port);

        /*
         * And verify all went well.
         */
        assertEquals("Success", result);
    }

    private void serverSocketWithTrustManager(int port) throws Exception {
        /*
         * Setup server side.
         *
         *  - Set the SSL context to use the KeyManagerFactory.
         *  - Create the SSLServerSocket using th SSL context.
         */

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLServerSocketFactory factory = context.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);
        serverSocket.setNeedClientAuth(true);

        startSocket(serverSocket);

        /*
         * Setup client side
         *
         * - Create an SSL context.
         * - Set SSL context to trust any certificate.
         */
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
            JcaTestUtils.loadKeyMaterial(ks, "".toCharArray(), (ignoredKeyTypes, ignoredIssuers) -> certificateName),
            JcaTestUtils.loadTrustMaterial(ks, (chain, ignored) -> chain.length == 1), null);

        /*
         * And now execute the test.
         */
        String result = sendRequest(sslContext, port);

        /*
         * And verify all went well.
         */
        assertEquals("Success", result);
    }

    private String sendRequest(SSLContext sslContext, int port) {

        /*
         * - Create SSL connection factory.
         * - Set hostname verifier to trust any hostname.
         */
        String result = null;
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) URI.create("https://localhost:" + port).toURL().openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier((hostname, session) -> true);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 204) {
                result = "Success";
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }
}
