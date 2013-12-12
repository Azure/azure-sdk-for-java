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

import com.microsoft.windowsazure.KeyStoreCredential;
import com.microsoft.windowsazure.KeyStoreType;
import static com.microsoft.windowsazure.services.core.utils.ExportUtils.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import com.microsoft.windowsazure.services.core.Builder;
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
        registry.add(ManagementClient.class, ManagementClientImpl.class);

        /*
        // alter jersey client config for service management. 
        registry.alter(ManagementClient.class, ClientConfig.class, new Builder.Alteration<ClientConfig>() {

            @Override
            public ClientConfig alter(String profile, ClientConfig clientConfig, Builder builder,
                    Map<String, Object> properties) {

                String keyStorePath = (String) getPropertyIfExists(profile, properties,
                        ManagementConfiguration.KEYSTORE_PATH);

                String keyStorePass = (String) getPropertyIfExists(profile, properties,
                        ManagementConfiguration.KEYSTORE_PASSWORD);

                KeyStoreType keyStoreType = KeyStoreType.valueOf((String) getPropertyIfExists(profile, properties,
                        ManagementConfiguration.KEYSTORE_TYPE));

                KeyStoreCredential keyStoreCredential = null;
                try {
                    keyStoreCredential = new KeyStoreCredential(keyStorePath, keyStorePass, keyStoreType);
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
*/
    }
}
