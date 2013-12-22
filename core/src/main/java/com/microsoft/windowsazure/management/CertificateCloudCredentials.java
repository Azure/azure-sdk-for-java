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

package com.microsoft.windowsazure.management;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class CertificateCloudCredentials extends SubscriptionCloudCredentials {
    private String _subscriptionId;
    private KeyStoreCredential _keyStoreCredential;
    
    public CertificateCloudCredentials(String subscriptionId)
    {
        this._subscriptionId = subscriptionId;
    }
    
    public CertificateCloudCredentials(String subscriptionId, KeyStoreCredential keyStoreCredential)
    {
        this._subscriptionId = subscriptionId;
        this._keyStoreCredential = keyStoreCredential;
    }
    
    @Override
    public String getSubscriptionId()
    {
        return _subscriptionId;
    }
    
    public void setSubscriptionId(String subscriptionId)
    {
        _subscriptionId = subscriptionId;
    }
    
    public KeyStoreCredential getKeyStoreCredential()
    {
        return _keyStoreCredential;
    }
    
    public void setKeyStoreCredential(KeyStoreCredential keyStoreCredential)
    {
        _keyStoreCredential = keyStoreCredential;
    }
    
    @Override
    public CloseableHttpClient initializeClient()
    {
        try {
            SSLContext sslcontext = SSLContextFactory.create(this.getKeyStoreCredential());

            return HttpClients.custom()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(sslcontext))
                .build();
        }
        catch (IOException e)
        {
            return null;
        }
        catch (GeneralSecurityException e)
        {
            return null;
        }
    }
}