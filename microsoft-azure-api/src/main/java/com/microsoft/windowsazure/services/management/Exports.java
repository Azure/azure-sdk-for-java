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
package com.microsoft.windowsazure.services.management;

import static com.microsoft.windowsazure.services.core.utils.ExportUtils.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import com.microsoft.windowsazure.services.core.Builder;
import com.microsoft.windowsazure.services.core.UserAgentFilter;
import com.microsoft.windowsazure.services.management.implementation.KeyStoreCredential;
import com.microsoft.windowsazure.services.management.implementation.KeyStoreType;
import com.microsoft.windowsazure.services.management.implementation.ManagementExceptionProcessor;
import com.microsoft.windowsazure.services.management.implementation.ManagementRestProxy;
import com.microsoft.windowsazure.services.management.implementation.SSLContextFactory;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * The Class Exports.
 */
public class Exports implements Builder.Exports {

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.core.Builder.Exports#register(com.microsoft.windowsazure.services.core.Builder.Registry)
     */
    @Override
    public void register(Builder.Registry registry) {

        // provide contract implementation
        registry.add(ManagementContract.class, ManagementExceptionProcessor.class);
        registry.add(ManagementRestProxy.class);
        registry.add(UserAgentFilter.class);

        // alter jersey client config for service management
        registry.alter(ClientConfig.class, new Builder.Alteration<ClientConfig>() {

            @Override
            public ClientConfig alter(String profile, ClientConfig clientConfig, Builder builder,
                    Map<String, Object> properties) {

                String keyStoreName = (String) getPropertyIfExists(profile, properties,
                        ManagementConfiguration.KEYSTORE_PATH);
                String keyStorePass = (String) getPropertyIfExists(profile, properties,
                        ManagementConfiguration.KEYSTORE_PASSWORD);

                KeyStoreCredential keyStoreCredential = null;
                try {
                    keyStoreCredential = new KeyStoreCredential(new FileInputStream(keyStoreName), keyStorePass,
                            KeyStoreType.jks);
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                SSLContext sslContext = null;
                try {
                    sslContext = SSLContextFactory.createSSLContext(keyStoreCredential);
                }
                catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                clientConfig.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                        new HTTPSProperties(new HostnameVerifier() {
                            @Override
                            public boolean verify(String arg0, SSLSession arg1) {
                                return true;
                            }
                        }, sslContext));
                return clientConfig;
            }
        });

    }
}
