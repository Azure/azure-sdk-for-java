/*
 * 
 * 
 * The author contributes this code to the public domain,
 * retaining no rights and incurring no responsibilities for its use in whole or in part.
 */

package com.microsoft.windowsazure.services.management;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

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
        // Apache IOUtils could be used instead of google.common.io,
        // or do a brute force read into List and then into an array.
        keyStore = ByteStreams.toByteArray(keys);
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
