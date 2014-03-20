/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.credentials;

import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreCredential;
import com.microsoft.windowsazure.core.utils.SSLContextFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

public class CertificateCloudCredentials extends SubscriptionCloudCredentials {
    private String subscriptionId;
    private KeyStoreCredential keyStoreCredential;

    public CertificateCloudCredentials(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public CertificateCloudCredentials(String subscriptionId,
            KeyStoreCredential keyStoreCredential) {
        this.subscriptionId = subscriptionId;
        this.keyStoreCredential = keyStoreCredential;
    }

    @Override
    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public KeyStoreCredential getKeyStoreCredential() {
        return keyStoreCredential;
    }

    public void setKeyStoreCredential(KeyStoreCredential keyStoreCredential) {
        this.keyStoreCredential = keyStoreCredential;
    }

    @Override
    public <T> void applyConfig(String profile, Map<String, Object> properties) {
        SSLContext sslcontext;
        try {
            sslcontext = SSLContextFactory.create(this.getKeyStoreCredential());
            properties
                    .put(profile
                            + ApacheConfigurationProperties.PROPERTY_SSL_CONNECTION_SOCKET_FACTORY,
                            new SSLConnectionSocketFactory(sslcontext));
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(CertificateCloudCredentials.class.getName()).log(
                    Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CertificateCloudCredentials.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
}
