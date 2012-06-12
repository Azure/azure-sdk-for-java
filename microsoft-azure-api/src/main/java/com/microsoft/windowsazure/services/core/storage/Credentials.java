/**
 * Copyright 2011 Microsoft Corporation
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
/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage;

import com.microsoft.windowsazure.services.core.storage.utils.Base64;

/**
 * Represents the credentials used to sign a request against the storage services.
 */
public final class Credentials {
    /**
     * Stores the Account name for the credentials.
     */
    private String accountName;

    /**
     * Stores the StorageKey for the credentials.
     */
    private final StorageKey key;

    /**
     * Stores the name of the access key to be used when signing the request.
     */
    private String keyName;

    /**
     * Stores the account name whose key is used to sign requests.
     */
    private String signingAccountName;

    /**
     * Creates an instance of the <code>Credentials</code> class, using the specified storage account name and access
     * key; the specified access key is in the form of a byte array.
     * 
     * @param accountName
     *            A <code>String</code> that represents the name of the storage account.
     * @param key
     *            An array of bytes that represent the account access key.
     * 
     */
    public Credentials(final String accountName, final byte[] key) {
        if (accountName == null || accountName.length() == 0) {
            throw new IllegalArgumentException("Invalid accountName");
        }

        if (key == null) {
            throw new IllegalArgumentException("Invalid key");
        }

        this.accountName = accountName;
        this.key = new StorageKey(key);
        this.signingAccountName = accountName;
    }

    /**
     * Creates an instance of the <code>Credentials</code> class, using the specified storage account name and access
     * key; the specified access key is stored as a <code>String</code>.
     * 
     * @param accountName
     *            A <code>String</code> that represents the name of the storage account.
     * @param key
     *            A <code>String</code> that represents the Base-64-encoded account access key.
     * 
     */
    public Credentials(final String accountName, final String key) {
        this(accountName, Base64.decode(key));
    }

    /**
     * Exports the value of the access key to a Base64-encoded string.
     * 
     * @return A <code>String</code> that represents the Base64-encoded access key.
     */
    public String exportBase64EncodedKey() {
        return this.getKey().getBase64EncodedKey();
    }

    /**
     * Exports the value of the access key to an array of bytes.
     * 
     * @return A byte array that represents the access key.
     */
    public byte[] exportKey() {
        return this.getKey().getKey();
    }

    /**
     * Returns the account name to be used in signing the request.
     * 
     * @return A <code>String</code> that represents the account name to be used in signing the request.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Returns the account name whose key is used to sign requests.
     * Internal use only.
     */
    public String getSigningAccountName() {
        return this.signingAccountName;
    }

    /**
     * Returns the name of the access key to be used when signing the request.
     * Internal use only.
     */
    public String getKeyName() {
        return this.keyName;
    }

    /**
     * Returns the access key to be used in signing the request.
     * 
     * @return A <code>String</code> that represents the access key to be used in signing the request.
     */
    public StorageKey getKey() {
        return this.key;
    }

    /**
     * Sets the account name to be used in signing the request.
     * 
     * @param accountName
     *            A <code>String</code> that represents the account name being set.
     */
    protected void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    /**
     * Sets the account name whose key is used to sign requests.
     * 
     * @param signingAccountName
     *            A <code>String</code> that represents the account name whose key is used to sign requests.
     */
    protected void setSigningAccountName(final String signingAccountName) {
        this.signingAccountName = signingAccountName;
    }

    /**
     * Sets the name of the access key to be used when signing the request.
     * 
     * @param keyName
     *            A <code>String</code> that represents the name of the access key to be used when signing the request.
     */
    protected void setKeyName(final String keyName) {
        this.keyName = keyName;
    }
}
