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

import java.io.FileInputStream;
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
 * A factory for creating SSLContext instance.
 */
public class SSLContextFactory {

    /**
     * Creates a SSLContext with specified keystore credential.
     * 
     * @param keyStoreCredential
     *            the credential of the keystore.
     * @return a <code>SSLContext</code> instance.
     * @throws GeneralSecurityException
     *             the general security exception
     * @throws IOException
     *             when an I/O exception has occurred.
     */
    public static SSLContext createSSLContext(KeyStoreCredential keyStoreCredential) throws GeneralSecurityException,
            IOException {
        if (keyStoreCredential == null) {
            throw new IllegalArgumentException("KeyStoreCredential cannot be null.");
        }
        return create(keyStoreCredential.getKeyStorePath(), keyStoreCredential.getKeystorePassword(),
                keyStoreCredential.getKeyStoreType());
    }

    /**
     * Creates a SSLContext object with specified keystore stream and password.
     * 
     * @param keyStorePath
     *            the path of the keystore.
     * @param keyStorePassword
     *            the password of the keystore.
     * @param keyStoreType
     *            the type of the keystore.
     * @return An <code>SSLContext</code> instance.
     * @throws GeneralSecurityException
     *             the general security exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static SSLContext create(String keyStorePath, String keyStorePassword, KeyStoreType keyStoreType)
            throws GeneralSecurityException, IOException {

        if ((keyStorePath == null) || (keyStorePath.isEmpty())) {
            throw new IllegalArgumentException("The keystore path cannot be null or empty.");
        }

        if (keyStoreType == null) {
            throw new IllegalArgumentException("The type of the keystore cannot be null");
        }

        InputStream keyStoreInputStream = new FileInputStream(keyStorePath);
        KeyManager[] keyManagers = getKeyManagers(keyStoreInputStream, keyStorePassword, keyStoreType);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, null, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        keyStoreInputStream.close();
        return sslContext;
    }

    /**
     * Gets the key managers.
     * 
     * @param keyStoreStream
     *            the key store stream
     * @param keyStorePassword
     *            the key stream password
     * @param type
     *            the type
     * @return the key managers
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws GeneralSecurityException
     *             the general security exception
     */
    private static KeyManager[] getKeyManagers(InputStream keyStoreInputStream, String keyStorePassword,
            KeyStoreType keyStoreType) throws IOException, GeneralSecurityException {

        KeyStore keyStore = KeyStore.getInstance(keyStoreType.name());
        keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

        return keyManagerFactory.getKeyManagers();
    }

}