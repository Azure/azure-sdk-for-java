// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.tls;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultKeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        // Register the provider before requesting its KeyStore implementation.
        Security.addProvider(provider);

        KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        // Create trust managers from the certificates in the Key Vault-backed KeyStore.
        TrustManagerFactory trustManagerFactory
            = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        // The local server may use a self-signed certificate. Accept a one-certificate server chain while delegating
        // validation of all other chains to the platform trust manager. Do not use this behavior in production.
        for (int i = 0; i < trustManagers.length; i++) {
            if (trustManagers[i] instanceof X509TrustManager) {
                X509TrustManager delegate = (X509TrustManager) trustManagers[i];
                trustManagers[i] = new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                        delegate.checkClientTrusted(chain, authType);
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                        if (chain.length != 1) {
                            delegate.checkServerTrusted(chain, authType);
                        }
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return delegate.getAcceptedIssuers();
                    }
                };
            }
        }

        // Configure one-way TLS: the client validates the server but doesn't present a client certificate.
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, null);

        String result = null;
        HttpsURLConnection connection = null;
        try {
            // openConnection will return HttpsURLConnection when the protocol is 'https'.
            connection = (HttpsURLConnection) URI.create("https://localhost:8765").toURL().openConnection();

            // Apply the custom trust configuration to this HTTPS connection.
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            // Allow the sample certificate to use a hostname other than localhost. Do not do this in production.
            connection.setHostnameVerifier((hostname, session) -> true);

            connection.setRequestMethod("GET");
            int status = connection.getResponseCode();
            if (status == 200) {
                // Decode the response using its declared charset, or UTF-8 when no charset is present.
                Charset responseCharset = StandardCharsets.UTF_8;
                String contentType = connection.getContentType();
                if (contentType != null) {
                    Matcher matcher = Pattern.compile("(?i)\\bcharset\\s*=\\s*\"?([^;\\s\"]+)")
                        .matcher(contentType);
                    if (matcher.find()) {
                        responseCharset = Charset.forName(matcher.group(1));
                    }
                }

                // Read the complete body without changing its line endings.
                try (Reader reader = new InputStreamReader(connection.getInputStream(), responseCharset)) {
                    StringBuilder responseBody = new StringBuilder();
                    char[] buffer = new char[1024];
                    int read;
                    while ((read = reader.read(buffer)) != -1) {
                        responseBody.append(buffer, 0, read);
                    }
                    result = responseBody.toString();
                }
            } else {
                result = "Not success";
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        System.out.println(result);
        // END: readme-sample-clientSSL
    }

}
