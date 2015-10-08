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

package com.microsoft.windowsazure.core.utils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;

/**
 * A factory for creating SSLContext instance.
 */
public final class SSLContextFactory {
    private SSLContextFactory() {
    }

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
    public static SSLContext create(KeyStoreCredential keyStoreCredential)
            throws GeneralSecurityException, IOException {
        if (keyStoreCredential == null) {
            throw new IllegalArgumentException(
                    "KeyStoreCredential cannot be null.");
        }
        return create(keyStoreCredential.getKeyStorePath(),
                keyStoreCredential.getKeystorePassword(),
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
     * @throws ClassNotFoundException
     */
    public static SSLContext create(String keyStorePath,
            String keyStorePassword, KeyStoreType keyStoreType)
            throws GeneralSecurityException, IOException {

        if ((keyStorePath == null) || (keyStorePath.isEmpty())) {
            throw new IllegalArgumentException(
                    "The keystore path cannot be null or empty.");
        }

        if (keyStoreType == null) {
            throw new IllegalArgumentException(
                    "The type of the keystore cannot be null");
        }

        InputStream keyStoreInputStream = new FileInputStream(new File(
                keyStorePath));

        SSLContext sslContext = SSLContext.getInstance("TLS");

        // Using bouncy castle only in case of java6 and if keystore is having
        // empty password
        if (Float.valueOf(System.getProperty("java.specification.version")) < 1.7
                && (keyStorePassword != null && keyStorePassword.trim()
                        .length() == 0)) {
            try {
                String defaultAlgorithm = KeyManagerFactory
                        .getDefaultAlgorithm();
                KeyManagerFactory keyManagerFactory = KeyManagerFactory
                        .getInstance(defaultAlgorithm);

                // use bouncy castle provider
                Class<?> providerClass = Class
                        .forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                Security.addProvider((Provider) providerClass.newInstance());

                Field field = providerClass.getField("PROVIDER_NAME");
                KeyStore store = KeyStore.getInstance("PKCS12", field.get(null)
                        .toString());
                store.load(keyStoreInputStream, keyStorePassword.toCharArray());

                keyManagerFactory.init(store, keyStorePassword.toCharArray());
                sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            } catch (Exception e) {
                // Using catch all exception class to avoid repeated code in
                // different catch blocks
                throw new RuntimeException(
                        "Exception occured while initiatlizing SSL context"
                                + "Make sure java versions less than 1.7 has bouncycastle jar in classpath",
                        e);
            }
        } else {
            KeyManager[] keyManagers = getKeyManagers(keyStoreInputStream,
                    keyStorePassword, keyStoreType);

            sslContext.init(keyManagers,
                    new TrustManager[] {
                        new X509TrustManager() {
                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                // System.out.println("getAcceptedIssuers =============");
                                return null;
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] certs,
                                    String authType) {
                                // System.out.println("checkClientTrusted =============");
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] certs,
                                    String authType) {
                                // System.out.println("checkServerTrusted =============");
                            }
                        }
                    }, new SecureRandom());

        }

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
    private static KeyManager[] getKeyManagers(InputStream keyStoreInputStream,
            String keyStorePassword, KeyStoreType keyStoreType)
            throws IOException, GeneralSecurityException {

        KeyStore keyStore = KeyStore.getInstance(keyStoreType.name());
        keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
        String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory keyManagerFactory = KeyManagerFactory
                .getInstance(defaultAlgorithm);
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

        return keyManagerFactory.getKeyManagers();
    }

}