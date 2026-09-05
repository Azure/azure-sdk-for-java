// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.certificates.JreCertificates;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class JreKeyStoreTest {
    @BeforeAll
    public static void init() {
        /*
         * Set system properties.
         */
        PropertyConvertorUtils.putEnvironmentPropertyToSystemPropertyForKeyVaultJca();
        /*
         * Add JCA provider.
         */
        PropertyConvertorUtils.addKeyVaultJcaProvider();
    }

    @Test
    public void testJreKsEntries() {
        JreCertificates jreCertificates = JreCertificates.getInstance();
        assertNotNull(jreCertificates);
        assertNotNull(jreCertificates.getAliases());
        Map<String, Certificate> certs = jreCertificates.getCertificates();
        assertFalse(certs.isEmpty());
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
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, JcaTestUtils.loadTrustMaterial(ks, null), null);

        /*
         * And now execute the test.
         */
        String result = null;
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) URI.create("https://google.com:443").toURL().openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                result = "Success";
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        /*
         * And verify all went well.
         */
        assertEquals("Success", result);
    }
}
