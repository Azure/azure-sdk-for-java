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

import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/**
 * The Class KeyStoreCredential.
 */
public class KeyStoreCredential {

    /** The password of the keystore. */
    private final String keystorePassword;

    /** The key store path. */
    private final String keyStorePath;

    /** The key store type. */
    private final KeyStoreType keyStoreType;

    /**
     * Creates a <code>KeyStoreCredential</code> instance from a keyStore.
     * 
     * @param keyStorePath
     *            the path of the keystore.
     * @param keyStorePassword
     *            the password for the keystore.
     * @param keyStoreType
     *            the type of the keyStore.
     * @throws IOException
     *             when a I/O exception has occurred.
     */
    @Inject
    public KeyStoreCredential(@Named(ManagementConfiguration.KEYSTORE_PATH) String keyStorePath, @Named(ManagementConfiguration.KEYSTORE_PASSWORD) String keyStorePassword,
            @Named(ManagementConfiguration.KEYSTORE_TYPE) KeyStoreType keyStoreType) throws IOException {
        this.keystorePassword = keyStorePassword;
        this.keyStorePath = keyStorePath;
        this.keyStoreType = keyStoreType;
    }

    public KeyStoreCredential(String keyStorePath, String keyStorePassword)
            throws IOException {
        this(keyStorePath, keyStorePassword, KeyStoreType.jks);
    }

    /**
     * Gets the type of the key store.
     * 
     * @return A <code>KeyStoreType</code> representing the type of the key
     *         store.
     */
    public KeyStoreType getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * Gets the keystore password.
     * 
     * @return A <code>String</code> instance representing the password of the
     *         keystore.
     */
    public String getKeystorePassword() {
        return keystorePassword;
    }

    /**
     * Gets the key store path.
     * 
     * @return the key store path
     */
    public String getKeyStorePath() {
        return this.keyStorePath;
    }
}