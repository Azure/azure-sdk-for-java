// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.tls;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultKeyStore;
import com.azure.security.keyvault.jca.SampleUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.Security;

/**
 * The ClientSSL sample.
 */
public class ClientSSLSample {

    public static void main(String[] args) throws Exception {
        // BEGIN: readme-sample-clientSSL
        System.setProperty("azure.keyvault.uri", "<your-azure-keyvault-uri>");
        System.setProperty("azure.keyvault.tenant-id", "<your-azure-keyvault-tenant-id>");
        System.setProperty("azure.keyvault.client-id", "<your-azure-keyvault-client-id>");
        System.setProperty("azure.keyvault.client-secret", "<your-azure-keyvault-client-secret>");

        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);

        KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        // This section initializing SSLContext can be replaced with implementation specific consumption of 'KeyStore',
        // if the library being used has convenience methods for that.
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = SampleUtils.loadTrustMaterial(keyStore);
        sslContext.init(null, trustManagers, null);

        String result = null;
        HttpsURLConnection connection = null;
        try {
            // openConnection will return HttpsURLConnection when the protocol is 'https'.
            connection = (HttpsURLConnection) URI.create("https://localhost:8765").toURL().openConnection();

            // Have the HttpsURLConnection use the SSLSocketFactory returned by SSLContext.
            connection.setSSLSocketFactory(sslContext.getSocketFactory());

            connection.setRequestMethod("GET");
            int status = connection.getResponseCode();
            if (status == 200) {
                result = SampleUtils.readResponse(connection);
            } else {
                result = "Not success";
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            result = "Not success";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        System.out.println(result);
        // END: readme-sample-clientSSL
    }

}
