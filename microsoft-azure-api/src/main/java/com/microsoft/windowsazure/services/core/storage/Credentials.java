/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage;

import com.microsoft.windowsazure.services.core.storage.utils.Base64;

/**
 * Represents the credentials used to sign a request against the storage services.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
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
}
