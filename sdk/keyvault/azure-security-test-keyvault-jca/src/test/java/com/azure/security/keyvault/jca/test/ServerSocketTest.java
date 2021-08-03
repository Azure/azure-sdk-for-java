// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.test;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultTrustManagerFactoryProvider;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The unit test validating the ServerSocket is created using a certificate from Azure Key Vault.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class ServerSocketTest {

    private static KeyStore ks;

    private static KeyManagerFactory kmf;

    @BeforeAll
    public static void beforeEach() throws Exception {

        PropertyConvertorUtils.putEnvironmentPropertyToSystemPropertyForKeyVaultJca();
        /*
         * Add JCA provider.
         */
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);

        /**
         *  - Create an Azure Key Vault specific instance of a KeyStore.
         *  - Set the KeyManagerFactory to use that KeyStore.
         */
        ks = PropertyConvertorUtils.getKeyVaultKeyStore();
        kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "".toCharArray());

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
        SSLContext sslContext = SSLContexts
            .custom()
            .loadTrustMaterial((final X509Certificate[] chain, final String authType) -> true)
            .build();
        testHttpsConnection(8765, sslContext);

    }

    @Test
    public void testHttpsConnectionWithSelfSignedClientTrust() throws Exception {
        SSLContext sslContext = SSLContexts
            .custom()
            .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
            .build();
        testHttpsConnection(8766, sslContext);
    }

    @Test
    public void testServerSocketWithDefaultTrustManager() throws Exception {
        String certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
        serverSocketWithTrustManager(8768, getSSlContext(Arrays.asList(certificateName)));
    }

    @Test
    public void testServerSocketWithKeyLessDefaultTrustManager() throws Exception {
        List<String> certificateNames = Arrays.asList("myaliasForRSAKeyLess", "myaliasForEC256KeyLess", "myaliasForEC384KeyLess", "myaliasForEC521KeyLess");
        serverSocketWithTrustManager(8769, getSSlContext(certificateNames));
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
        String certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
        serverSocketWithTrustManager(8767, getSSlContext(Arrays.asList(certificateName)));
    }

    @Test
    public void testServerSocketWithLeyLessKeyVaultTrustManager() throws Exception {
        KeyVaultTrustManagerFactoryProvider provider = new KeyVaultTrustManagerFactoryProvider();
        Security.addProvider(provider);
        List<String> certificateNames = Arrays.asList("myaliasForRSAKeyLess", "myaliasForEC256KeyLess", "myaliasForEC384KeyLess", "myaliasForEC521KeyLess");
        serverSocketWithTrustManager(8770, getSSlContext(certificateNames));
    }

    private List<SSLContext> getSSlContext(List<String> certificateNames) throws Exception {
        List<SSLContext> result = new ArrayList<>();
        for (String certificateName : certificateNames) {
            /*
             * Setup client sides with different certificate
             *
             * - Create an SSL context.
             * - Set SSL context to trust any certificate.
             */
            result.add(SSLContexts
                  .custom()
                  .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
                  .loadKeyMaterial(ks, "".toCharArray(), new ClientPrivateKeyStrategy(certificateName))
                  .build());
        }
        return result;
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

    private void serverSocketWithTrustManager(Integer port, List<SSLContext> sslContexts) throws Exception {
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

        sslContexts.forEach(sslContext -> {
            /*
             * And now execute the test.
             */
            String result = sendRequest(sslContext, port);
            /*
             * And verify all went well.
             */
            assertEquals("Success", result);
        });
    }

    private String sendRequest(SSLContext sslContext, Integer port) {

        /**
         * - Create SSL connection factory.
         * - Set hostname verifier to trust any hostname.
         */
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
            sslContext, (hostname, session) -> true);

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .build());

        String result = null;

        try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).build()) {
            HttpGet httpGet = new HttpGet("https://localhost:" + port);
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
        return result;
    }

    private static final class ClientPrivateKeyStrategy implements PrivateKeyStrategy {

        String certificateName;

        private ClientPrivateKeyStrategy(String certificateName) {
            this.certificateName = certificateName;
        }

        @Override
        public String chooseAlias(Map<String, PrivateKeyDetails> map, Socket socket) {
            return certificateName; // It should be your certificate alias used in client-side
        }
    }

}
