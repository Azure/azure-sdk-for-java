// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.certificates.JreCertificates;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class JreKeyStoreTest {
    @BeforeAll
    public static void init() {
        /*
         * Add JCA provider.
         */
        PropertyConvertorUtils.addKeyVaultJcaProvider();
        /*
         * Set system properties.
         */
        PropertyConvertorUtils.putEnvironmentPropertyToSystemPropertyForKeyVaultJca();
    }

    @Test
    public void testJreKsEntries() {
        JreCertificates jreCertificates = JreCertificates.getInstance();
        assertNotNull(jreCertificates);
        assertNotNull(jreCertificates.getAliases());
        Map<String, Certificate> certs = jreCertificates.getCertificates();
        assertTrue(certs.size() > 0);
        assertNotNull(jreCertificates.getCertificateKeys());
    }

    @Test
    public void testJreKsTrustPeer() throws Exception {

        KeyStore ks = KeyStore.getInstance("AzureKeyVault");
        ks.load(null);
        /*
         * Setup client side
         *
         * - Create an SSL context.
         * - Create SSL connection factory.
         * - Set hostname verifier to trust any hostname.
         */

        SSLContext sslContext = SSLContexts
            .custom()
            .loadTrustMaterial(ks, null)
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
            HttpGet httpGet = new HttpGet("https://google.com:443");
            ResponseHandler<String> responseHandler = (HttpResponse response) -> {
                int status = response.getStatusLine().getStatusCode();
                String result1 = null;
                if (status == 200) {
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
}
