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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sun.jersey.core.util.ReaderWriter;

/**
 * The Class KeyStoreCredential.
 */
public class KeyStoreCredential {

    /** The keystore. */
    private final byte[] keyStore;

    /** The password of the keystore. */
    private final String keystorePassword;

    /** The key store type. */
    private final KeyStoreType keyStoreType;

    /**
     * Creates a <code>KeyStoreCredential</code> instance from a keyStore.
     * 
     * @param keyStore
     *            - an InputStream probably a FileInputStream from a keyStore, the jks containing
     *            your management cert
     * @param keyPass
     *            - keyStore password, key for store and the internal private key must be
     *            symmetric
     * @param type
     *            the type
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public KeyStoreCredential(String keyStorePath, String keyPass, KeyStoreType type) throws IOException {
        keystorePassword = keyPass;
        InputStream keyStoreInputStream = new FileInputStream(keyStorePath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ReaderWriter.writeTo(keyStoreInputStream, byteArrayOutputStream);
        keyStore = byteArrayOutputStream.toByteArray();
        keyStoreType = type;
    }

    /**
     * Gets the key store type.
     * 
     * @return the key store type
     */
    public KeyStoreType getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * Gets the key store.
     * 
     * @return the key store
     */
    public InputStream getKeyStore() {
        return new ByteArrayInputStream(keyStore);
    }

    /**
     * Gets the keystore password.
     * 
     * @return the keystore password
     */
    public String getKeystorePassword() {
        return keystorePassword;
    }

}
