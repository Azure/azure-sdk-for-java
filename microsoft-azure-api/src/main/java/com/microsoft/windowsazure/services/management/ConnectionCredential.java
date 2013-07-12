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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sun.jersey.core.util.ReaderWriter;

public class ConnectionCredential {
    private final byte[] keyStore;

    private final String keyPasswd;

    private final KeyStoreType keyStoreType;

    /**
     * Creates a Credential from a keyStore.
     * 
     * @param keyPass
     *            - keyStore password, key for store and the internal private key must be
     *            symmetric
     * @param keys
     *            - an InputStream probably a FileInputStream from a keyStore, the jks containing
     *            your management cert
     * @throws IOException
     */
    ConnectionCredential(InputStream keys, String keyPass, KeyStoreType type) throws IOException {
        keyPasswd = keyPass;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ReaderWriter.writeTo(keys, byteArrayOutputStream);
        keyStore = byteArrayOutputStream.toByteArray();
        keyStoreType = type;
    }

    public KeyStoreType getKeyStoreType() {
        return keyStoreType;
    }

    public InputStream getKeyStore() {
        return new ByteArrayInputStream(keyStore);
    }

    public String getKeyPasswd() {
        return keyPasswd;
    }

}
