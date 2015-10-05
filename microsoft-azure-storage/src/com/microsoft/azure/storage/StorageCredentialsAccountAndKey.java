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

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents storage account credentials, based on storage account and access key, for accessing the Microsoft Azure
 * storage services.
 */
public final class StorageCredentialsAccountAndKey extends StorageCredentials {

    /**
     * Stores the Account name for the credentials.
     */
    private String accountName;

    /**
     * Stores a reference to the hmacsha256 Mac.
     */
    private Mac hmacSha256;

    /**
     * Stores the key.
     */
    private byte[] key;

    /**
     * Creates an instance of the <code>StorageCredentialsAccountAndKey</code> class, using the specified storage
     * account name and access key; the specified access key is in the form of a byte array.
     * 
     * @param accountName
     *            A <code>String</code> that represents the name of the storage account.
     * @param key
     *            An array of bytes that represent the account access key.
     */
    public StorageCredentialsAccountAndKey(final String accountName, final byte[] key) {
        if (Utility.isNullOrEmptyOrWhitespace(accountName)) {
            throw new IllegalArgumentException(SR.INVALID_ACCOUNT_NAME);
        }

        if (key == null || key.length == 0) {
            throw new IllegalArgumentException(SR.INVALID_KEY);
        }

        this.accountName = accountName;
        this.key = key;
    }

    /**
     * Creates an instance of the <code>StorageCredentialsAccountAndKey</code> class, using the specified storage
     * account name and access key; the specified access key is stored as a <code>String</code>.
     * 
     * @param accountName
     *            A <code>String</code> that represents the name of the storage account.
     * @param key
     *            A <code>String</code> that represents the Base-64-encoded account access key.
     */
    public StorageCredentialsAccountAndKey(final String accountName, final String key) {
        this(accountName, Base64.decode(key));
    }

    /**
     * Gets the account name.
     * 
     * @return A <code>String</code> that contains the account name.
     */
    @Override
    public String getAccountName() {
        return this.accountName;
    }
    
    /**
     * Exports the value of the access key to a Base64-encoded string.
     * 
     * @return A <code>String</code> that represents the Base64-encoded access key.
     */
    public String exportBase64EncodedKey() {
        return Base64.encode(this.key);
    }

    /**
     * Exports the value of the access key to an array of bytes.
     * 
     * @return A byte array that represents the access key.
     */
    public byte[] exportKey() {
        final byte[] copy = this.key.clone();
        return copy;
    }
    
    /**
     * Sets the account name.
     * 
     * @param accountName
     *          A <code>String</code> that contains the account name.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    /**
     * Sets the name of the access key to be used when signing the request.
     * 
     * @param key
     *        A <code>String</code> that represents the name of the access key to be used when signing the request.
     */
    public synchronized void updateKey(final String key) {
        this.updateKey(Base64.decode(key));
    }
    
    /**
     * Sets the name of the access key to be used when signing the request.
     * 
     * @param key
     *        A <code>String</code> that represents the name of the access key to be used when signing the request.
     */
    public synchronized void updateKey(final byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException(SR.INVALID_KEY);
        }

        this.key = key;
        this.hmacSha256 = null;
    }

    /**
     * Returns a <code>String</code> that represents this instance, optionally including sensitive data.
     * 
     * @param exportSecrets
     *            <code>true</code> to include sensitive data in the return string; otherwise, <code>false</code>.
     * 
     * @return A <code>String</code> that represents this object, optionally including sensitive data.
     */
    @Override
    public String toString(final boolean exportSecrets) {
        return String.format("%s=%s;%s=%s", CloudStorageAccount.ACCOUNT_NAME_NAME, this.getAccountName(),
                CloudStorageAccount.ACCOUNT_KEY_NAME, exportSecrets ? this.exportBase64EncodedKey()
                        : "[key hidden]");
    }

    @Override
    public URI transformUri(URI resourceUri, OperationContext opContext) {
        return resourceUri;
    }

    @Override
    public StorageUri transformUri(StorageUri resourceUri, OperationContext opContext) {
        return resourceUri;
    }
    
    /**
     * Gets the HmacSha256 associated with the account key.
     * 
     * @return A <code>MAC</code> created with the account key.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid storage key.
     */
    public synchronized Mac getHmac256() throws InvalidKeyException {
        if (this.hmacSha256 == null) {
            // Initializes the HMAC-SHA256 Mac and SecretKey.
            try {
                this.hmacSha256 = Mac.getInstance("HmacSHA256");
            }
            catch (final NoSuchAlgorithmException e) {
                throw new IllegalArgumentException();
            }
            this.hmacSha256.init(new SecretKeySpec(this.key, "HmacSHA256"));
        }
        return this.hmacSha256;
    }
}
