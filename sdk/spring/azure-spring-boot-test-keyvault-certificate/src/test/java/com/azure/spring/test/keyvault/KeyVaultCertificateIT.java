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
import org.junit.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AZURE_KEYVAULT_URI;
import static com.azure.spring.test.EnvironmentVariable.SPRING_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.SPRING_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.SPRING_TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyVaultCertificateIT {

    private RestTemplate restTemplate;

    public void setRestTemplate() throws Exception{
        KeyStore trustStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        trustStore.load(parameter);
        SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(trustStore, null)
            .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
            (hostname, session) -> true);
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        restTemplate = new RestTemplate(requestFactory);
    }

    public void setMtlsRestTemplate() throws Exception{
        KeyStore trustStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        trustStore.load(parameter);
        SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(trustStore, null)
            .loadKeyMaterial(trustStore, "".toCharArray(), new ClientPrivateKeyStrategy())
            .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
            (hostname, session) -> true);
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        restTemplate = new RestTemplate(requestFactory);
    }


    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootWebApplication() throws Exception {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemProperty(
            Arrays.asList("AZURE_KEYVAULT_URI",
                "AZURE_KEYVAULT_TENANT_ID",
                "AZURE_KEYVAULT_CLIENT_ID",
                "AZURE_KEYVAULT_CLIENT_SECRET")
        );

        try (AppRunner app = new AppRunner(DummyApp.class)) {
            app.property("azure.keyvault.uri", AZURE_KEYVAULT_URI);
            app.property("azure.keyvault.client-id", SPRING_CLIENT_ID);
            app.property("azure.keyvault.client-secret", SPRING_CLIENT_SECRET);
            app.property("azure.keyvault.tenant-id", SPRING_TENANT_ID);
            app.property("server.ssl.key-alias", "myalias");
            app.property("server.ssl.key-store-type", "AzureKeyVault");
            app.start();
            setRestTemplate();

            final String response = restTemplate.getForObject(
                "https://localhost:" + app.port() + "", String.class);
            assertEquals(response, "Hello World");
        }
    }

    /**
     * Test the Spring Boot Health indicator integration.
     */
    @Test
    public void testSpringBootMtlsWebApplication() throws Exception {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemProperty(
            Arrays.asList("AZURE_KEYVAULT_URI",
                "AZURE_KEYVAULT_TENANT_ID",
                "AZURE_KEYVAULT_CLIENT_ID",
                "AZURE_KEYVAULT_CLIENT_SECRET")
        );

        try (AppRunner app = new AppRunner(DummyApp.class)) {
            app.property("azure.keyvault.uri", AZURE_KEYVAULT_URI);
            app.property("azure.keyvault.client-id", SPRING_CLIENT_ID);
            app.property("azure.keyvault.client-secret", SPRING_CLIENT_SECRET);
            app.property("azure.keyvault.tenant-id", SPRING_TENANT_ID);
            app.property("server.port", "8443");
            app.property("server.ssl.key-alias", "myalias");
            app.property("server.ssl.key-store-type", "AzureKeyVault");
            app.property("server.ssl.client-auth", "need");
            app.property("server.ssl.trust-store-type", "AzureKeyVault");
            app.start();
            setMtlsRestTemplate();
            final String response = restTemplate.getForObject(
                "https://localhost:" + app.port() + "", String.class);
            assertEquals(response, "Hello World");
        }
    }

    private static class ClientPrivateKeyStrategy implements PrivateKeyStrategy {
        @Override
        public String chooseAlias(Map<String, PrivateKeyDetails> map, Socket socket) {
            return "myalias"; // It should be your certificate alias used in client-side
        }
    }

}
