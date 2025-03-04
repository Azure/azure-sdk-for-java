// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.mtls;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultKeyStore;
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
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

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
            ResponseHandler<String> responseHandler = (HttpResponse response) -> {
                int status = response.getStatusLine().getStatusCode();
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
