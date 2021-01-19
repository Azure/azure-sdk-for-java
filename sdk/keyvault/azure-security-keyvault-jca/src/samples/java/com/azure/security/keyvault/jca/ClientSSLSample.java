// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyStore;
import java.security.Security;

/**
 * The ClientSSL sample.
 */
public class ClientSSLSample {
    public void clientSSLSample() throws Exception {
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);

        KeyStore ks = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.aad-authentication-url"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        ks.load(parameter);

        SSLContext sslContext = SSLContexts
            .custom()
            .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
            .build();

        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder
            .create()
            .setSslContext(sslContext)
            .setHostnameVerifier((hostname, session) -> true)
            .build();

        PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder
            .create()
            .setSSLSocketFactory(sslSocketFactory)
            .build();

        String result = null;

        try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).build()) {
            HttpGet httpGet = new HttpGet("https://localhost:8766");
            HttpClientResponseHandler<String> responseHandler = (ClassicHttpResponse response) -> {
                int status = response.getCode();
                String result1 = "Not success";
                if (status == 204) {
                    result1 = "Success";
                }
                return result1;
            };
            result = client.execute(httpGet, responseHandler);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
