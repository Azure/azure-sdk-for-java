// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.azure.security.jca;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyStore;
import java.security.Security;

/**
 * The ClientSSL sample.
 */
public class ClientSSLSample {

    public static void main(String[] args) throws Exception {
        AzureKeyManagerFactoryProvider provider = new AzureKeyManagerFactoryProvider();
        Security.addProvider(provider);

        KeyStore keyStore = KeyStore.getInstance("AzureKeyVault");
        AzureLoadStoreParameter parameter = new AzureLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keyStore.load(parameter);

        SSLContext sslContext = SSLContexts
            .custom()
            .loadTrustMaterial(keyStore, new TrustSelfSignedStrategy())
            .build();

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
            sslContext, (hostname, session) -> true);

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .build());

        String result = null;

        try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).build()) {
            HttpGet httpGet = new HttpGet("https://localhost:8766");
            ResponseHandler<String> responseHandler = (HttpResponse response) -> {
                int status = response.getStatusLine().getStatusCode();
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
