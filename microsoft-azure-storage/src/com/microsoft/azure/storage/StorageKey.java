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
package com.microsoft.azure.storage;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.azure.storage.core.Base64;

/**
 * Represents a container for a storage key.
 */
public final class StorageKey {
    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm.
     * 
     * @param storageKey
     *            A <code>StorageKey</code> object that represents the storage key to use.
     * @param stringToSign
     *            The UTF-8-encoded string to sign.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA256-encoded signature.
     * 
     * @throws IllegalArgumentException
     *             If the string to sign is not a valid Base64-encoded string.
     * @throws InvalidKeyException
     *             If the key is not a valid storage key.
     */
    public static synchronized String computeMacSha256(final StorageKey storageKey, final String stringToSign)
            throws InvalidKeyException {
        if (storageKey.hmacSha256 == null) {
            storageKey.initHmacSha256();
        }

        byte[] utf8Bytes = null;
        try {
            utf8Bytes = stringToSign.getBytes(Constants.UTF8_CHARSET);
        }
        catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }

        return Base64.encode(storageKey.hmacSha256.doFinal(utf8Bytes));
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA512 algorithm.
     * 
     * @param storageKey
     *            A <code>StorageKey</code> object that represents the storage key to use.
     * @param stringToSign
     *            The UTF-8-encoded string to sign.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA512-encoded signature.
     * 
     * @throws IllegalArgumentException
     *             If the string to sign is not a valid Base64-encoded string.
     * @throws InvalidKeyException
     *             If the key is not a valid storage key.
     */
    public static synchronized String computeMacSha512(final StorageKey storageKey, final String stringToSign)
            throws InvalidKeyException {
        if (storageKey.hmacSha512 == null) {
            storageKey.initHmacSha512();
        }

        byte[] utf8Bytes = null;
        try {
            utf8Bytes = stringToSign.getBytes(Constants.UTF8_CHARSET);
        }
        catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }

        return Base64.encode(storageKey.hmacSha512.doFinal(utf8Bytes));
    }

    /**
     * Stores a reference to the hmacsha256 Mac.
     */
    private Mac hmacSha256;

    /**
     * Stores a reference to the hmacsha512 Mac.
     */
    private Mac hmacSha512;

    /**
     * Stores a reference to the hmacsha256 SecretKey.
     */
    private SecretKey key256;

    /**
     * Stores a reference to the hmacsha512 SecretKey.
     */
    private SecretKey key512;

    /**
     * Stores the key.
     */
    private byte[] key;

    /**
     * Creates an instance of the <code>StorageKey</code> class.
     * 
     * @param key
     *            An array of bytes that represent the storage key.
     */
    public StorageKey(final byte[] key) {
        this.setKey(key);
    }

    /**
     * Returns the Base64-encoded key.
     * 
     * @return A <code>String</code> that represents the Base64-encoded key.
     */
    public String getBase64EncodedKey() {
        return Base64.encode(this.key);
    }

    /**
     * Returns the key.
     * 
     * @return A byte array that represents the key.
     */
    public byte[] getKey() {
        final byte[] copy = this.key.clone();
        return copy;
    }

    /**
     * Initializes the HMAC-SHA256 Mac and SecretKey.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid SecretKey according to specification.
     */
    private void initHmacSha256() throws InvalidKeyException {
        this.key256 = new SecretKeySpec(this.key, "HmacSHA256");
        try {
            this.hmacSha256 = Mac.getInstance("HmacSHA256");
        }
        catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException();
        }
        this.hmacSha256.init(this.key256);
    }

    /**
     * Initializes the HMAC-SHA256 Mac and SecretKey.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid SecretKey according to specification.
     */
    private void initHmacSha512() throws InvalidKeyException {
        this.key512 = new SecretKeySpec(this.key, "HmacSHA512");
        try {
            this.hmacSha512 = Mac.getInstance("HmacSHA512");
        }
        catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException();
        }
        this.hmacSha512.init(this.key512);
    }

    /**
     * Sets the key to be used, using the specified byte array as the key.
     * <p/>
     * This method is provided to support key rotation. This method is not thread-safe.
     * 
     * @param key
     *            A <code>byte</code> array that represents the key being assigned.
     */
    public void setKey(final byte[] key) {
        this.key = key;
        this.hmacSha256 = null;
        this.hmacSha512 = null;
        this.key256 = null;
        this.key512 = null;
    }

    /**
     * Sets the key to be used, using the specified <code>String</code> as the key.
     * <p/>
     * This method is provided to support key rotation. This method is not thread-safe.
     * 
     * @param key
     *            A <code>String</code> that represents the key being assigned.
     */
    public void setKey(final String key) {
        this.key = Base64.decode(key);
    }
}
