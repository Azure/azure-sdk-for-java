// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.test.keyvault;

import com.azure.security.keyvault.jca.KeyVaultLoadStoreParameter;
import com.azure.spring.test.AppRunner;
import com.azure.spring.test.keyvault.app.DummyApp;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.spring.test.keyvault.PropertyConvertorUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyVaultCertificateIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultCertificateIT.class);

    private RestTemplate restTemplate;

    private static AppRunner app;

    @BeforeAll
    public static void setEnvironmentProperty() {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemProperty(
            Arrays.asList("CERTIFICATE_AZURE_KEYVAULT_URI",
                "CERTIFICATE_AZURE_KEYVAULT_TENANT_ID",
                "CERTIFICATE_AZURE_KEYVAULT_CLIENT_ID",
                "CERTIFICATE_AZURE_KEYVAULT_CLIENT_SECRET")
        );
    }

    public static KeyStore getAzureKeyVaultKeyStore() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        trustStore.load(parameter);
        return trustStore;
    }

    private void setRestTemplate(SSLContext sslContext) {
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
            (hostname, session) -> true);
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        restTemplate = new RestTemplate(requestFactory);
    }

    public void setRestTemplate() throws Exception {
        KeyStore keyStore = getAzureKeyVaultKeyStore();
        SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(keyStore, null)
            .build();
        setRestTemplate(sslContext);
    }

    public void setMTLSRestTemplate(String certificateName) {
        try {
            KeyStore keyStore = getAzureKeyVaultKeyStore();
            SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(keyStore, null)
                .loadKeyMaterial(keyStore, "".toCharArray(), new ClientPrivateKeyStrategy(certificateName))
                .build();
            setRestTemplate(sslContext);
        }catch (Exception e) {
            LOGGER.error("Exception happened when create MTLSRestTemplate", e);
        }
    }

    public void startAppRunner(Map<String, String> properties) {
        app = new AppRunner(DummyApp.class);
        properties.forEach(app::property);
        app.start();
    }

    public Map<String, String> getDefaultMap() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.keyvault.uri", AZURE_KEYVAULT_URI);
        properties.put("azure.keyvault.client-id", SPRING_CLIENT_ID);
        properties.put("azure.keyvault.client-secret", SPRING_CLIENT_SECRET);
        properties.put("azure.keyvault.tenant-id", SPRING_TENANT_ID);
        properties.put("server.ssl.key-alias", "myalias");
        properties.put("server.ssl.key-store-type", "AzureKeyVault");
        return properties;
    }

    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootWebApplication() throws Exception {
        startSpringBootWebApplication(null);
    }

    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootWebApplicationWithRSAKeyLess() throws Exception {
        startSpringBootWebApplication(new HashMap<String, String>() {{ put("server.ssl.key-alias", "myaliasForRSAKeyLess");}});
    }

    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootWebApplicationWithEC256KeyLess() throws Exception {
        startSpringBootWebApplication(new HashMap<String, String>() {{ put("server.ssl.key-alias", "myaliasForEC256KeyLess");}});
    }

    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootWebApplicationWithEC384KeyLess() throws Exception {
        startSpringBootWebApplication(new HashMap<String, String>() {{ put("server.ssl.key-alias", "myaliasForEC384KeyLess");}});
    }

    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootWebApplicationWithEC521KeyLess() throws Exception {
        startSpringBootWebApplication(new HashMap<String, String>() {{ put("server.ssl.key-alias", "myaliasForEC521KeyLess");}});
    }

    private void startSpringBootWebApplication(Map<String, String> additionalProperties) throws Exception {
        Map<String, String> properties = getDefaultMap();
        properties.putAll(additionalProperties);
        startAppRunner(properties);
        setRestTemplate();
        sendRequest();
    }


    @AfterAll
    public static void destroy() {
        app.close();
    }

    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootMTLSWebApplication() {
        Map<String, String> properties = getDefaultMap();
        properties.put("server.ssl.client-auth", "need");
        properties.put("server.ssl.trust-store-type", "AzureKeyVault");
        startAppRunner(properties);
        setMTLSRestTemplate("myalias");
        sendRequest();
    }

    @Test
    public void testSpringBootMTLSWebApplicationWithKeyLess() {
        Map<String, String> properties = getDefaultMap();
        properties.put("server.ssl.client-auth", "need");
        properties.put("server.ssl.trust-store-type", "AzureKeyVault");
        startAppRunner(properties);

        Stream.of(
            "myaliasForRSAKeyLess",
            "myaliasForEC256KeyLess",
            "myaliasForEC384KeyLess",
            "myaliasForEC521KeyLess")
            .forEach(certificateName -> {
                setMTLSRestTemplate(certificateName);
                sendRequest();
            });
    }


    public void sendRequest() {
        final String response = restTemplate.getForObject(
            "https://localhost:" + app.port() + "", String.class);
        assertEquals(response, "Hello World");
    }

    private static class ClientPrivateKeyStrategy implements PrivateKeyStrategy {

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
