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

package com.microsoft.windowsazure.services.management.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * A factory for creating SSLContext objects.
 */
public class SSLContextFactory {

    /**
     * Creates a new SSLContext object.
     * 
     * @param cred
     *            the cred
     * @return the SSL context
     * @throws GeneralSecurityException
     *             the general security exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static SSLContext createSSLContext(KeyStoreCredential keyStoreCredential) throws GeneralSecurityException,
            IOException {
        return createSSLContext(keyStoreCredential.getKeyStore(), keyStoreCredential.getKeystorePassword(),
                keyStoreCredential.getKeyStoreType());
    }

    /**
     * Creates a new SSLContext object.
     * 
     * @param keyStoreStream
     *            the key store stream
     * @param keyStorePassword
     *            the key stream passwd
     * @param keyStoreType
     *            the type
     * @return the SSL context
     * @throws GeneralSecurityException
     *             the general security exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static SSLContext createSSLContext(InputStream keyStoreStream, String keyStorePassword, KeyStoreType keyStoreType)
            throws GeneralSecurityException, IOException {

        KeyManager[] keyManagers = getKeyManagers(keyStoreStream, keyStorePassword, keyStoreType.name());
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagers, null, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

        return context;
    }

    /**
     * Gets the key managers.
     * 
     * @param keyStoreStream
     *            the key store stream
     * @param keyStorePassword
     *            the key stream passwd
     * @param type
     *            the type
     * @return the key managers
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws GeneralSecurityException
     *             the general security exception
     */
    private static KeyManager[] getKeyManagers(InputStream keyStoreStream, String keyStorePassword, String type)
            throws IOException, GeneralSecurityException {

        KeyStore ks = KeyStore.getInstance(type);
        ks.load(keyStoreStream, keyStorePassword.toCharArray());
        keyStoreStream.close();

        String alg = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory kmFact = KeyManagerFactory.getInstance(alg);
        kmFact.init(ks, keyStorePassword.toCharArray());

        return kmFact.getKeyManagers();
    }

}