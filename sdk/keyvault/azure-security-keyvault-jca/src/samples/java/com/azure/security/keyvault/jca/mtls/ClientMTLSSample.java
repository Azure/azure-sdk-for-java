// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.mtls;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultKeyStore;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyStore;
import java.security.Security;

/**
 * The ClientMTLS sample.
 */
public class ClientMTLSSample {

    public static void main(String[] args) throws Exception {
        // BEGIN: readme-sample-clientMTLS
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);

        System.setProperty("azure.keyvault.uri", "<client-azure-keyvault-uri>");
        System.setProperty("azure.keyvault.tenant-id", "<client-azure-keyvault-tenant-id>");
        System.setProperty("azure.keyvault.client-id", "<client-azure-keyvault-client-id>");
        System.setProperty("azure.keyvault.client-secret", "<client-azure-keyvault-client-secret>");
        KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        System.setProperty("azure.keyvault.uri", "<server-azure-keyvault-uri>");
        System.setProperty("azure.keyvault.tenant-id", "<server-azure-keyvault-tenant-id>");
        System.setProperty("azure.keyvault.client-id", "<server-azure-keyvault-client-id>");
        System.setProperty("azure.keyvault.client-secret", "<server-azure-keyvault-client-secret>");
        KeyStore trustStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        SSLContext sslContext = SSLContexts
            .custom()
            .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
            .loadKeyMaterial(keyStore, "".toCharArray())
            .build();

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
            sslContext, (hostname, session) -> true);

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .build());

        String result = null;

        try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).build()) {
            HttpGet httpGet = new HttpGet("https://localhost:8765");
            HttpClientResponseHandler<String> responseHandler = (ClassicHttpResponse response) -> {
                int status = response.getCode();
                String result1 = "Not success";
                if (status == 200) {
                    result1 = EntityUtils.toString(response.getEntity());
                }
                return result1;
            };
            result = client.execute(httpGet, responseHandler);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println(result);
        // END: readme-sample-clientMTLS
    }

}
