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

import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents storage account credentials, based on storage account and access key, for accessing the Microsoft Azure
 * storage services.
 */
public final class StorageCredentialsAccountAndKey extends StorageCredentials {

    /**
     * The internal Credentials associated with the StorageCredentials.
     */
    @SuppressWarnings("deprecation")
    private Credentials credentials;

    /**
     * Creates an instance of the <code>StorageCredentialsAccountAndKey</code> class, using the specified storage
     * account name and access key; the specified access key is in the form of a byte array.
     * 
     * @param accountName
     *            A <code>String</code> that represents the name of the storage account.
     * @param key
     *            An array of bytes that represent the account access key.
     */
    @SuppressWarnings("deprecation")
    public StorageCredentialsAccountAndKey(final String accountName, final byte[] key) {
        this.credentials = new Credentials(accountName, key);
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
    @SuppressWarnings("deprecation")
    public StorageCredentialsAccountAndKey(final String accountName, final String key) {
        this.credentials = new Credentials(accountName, key);
    }

    /**
     * Gets the account name.
     * 
     * @return A <code>String</code> that contains the account name.
     */
    @SuppressWarnings("deprecation")
    @Override
    public String getAccountName() {
        return this.credentials.getAccountName();
    }
    
    /**
     * Exports the value of the access key to a Base64-encoded string.
     * 
     * @return A <code>String</code> that represents the Base64-encoded access key.
     */
    @SuppressWarnings("deprecation")
    public String exportBase64EncodedKey() {
        return this.credentials.getKey().getBase64EncodedKey();
    }

    /**
     * Exports the value of the access key to an array of bytes.
     * 
     * @return A byte array that represents the access key.
     */
    @SuppressWarnings("deprecation")
    public byte[] exportKey() {
        return this.credentials.getKey().getKey();
    }
    
    /**
     * Sets the account name.
     * 
     * @param accountName
     *          A <code>String</code> that contains the account name.
     */
    @SuppressWarnings("deprecation")
    public void setAccountName(String accountName) {
        this.credentials.setAccountName(accountName);
    }
    
    /**
     * Sets the name of the access key to be used when signing the request.
     * 
     * @param key
     *        A <code>String</code> that represents the name of the access key to be used when signing the request.
     */
    @SuppressWarnings("deprecation")
    public void updateKey(final String key) {
        if (Utility.isNullOrEmptyOrWhitespace(key) || !Base64.validateIsBase64String(key)) {
            throw new IllegalArgumentException(SR.INVALID_KEY);
        }

        this.credentials.setKey(new StorageKey(Base64.decode(key)));
    }
    
    /**
     * Sets the name of the access key to be used when signing the request.
     * 
     * @param key
     *        A <code>String</code> that represents the name of the access key to be used when signing the request.
     */
    @SuppressWarnings("deprecation")
    public void updateKey(final byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException(SR.INVALID_KEY);
        }

        this.credentials.setKey(new StorageKey(key));
    }

    /**
     * Gets the name of the key used by these credentials.
     * @deprecated as of 3.0.0. The key name property is only useful internally.
     */
    @Deprecated
    public String getAccountKeyName() {
        return this.credentials.getKeyName();
    }

    /**
     * Returns the internal credentials associated with the storage credentials.
     * 
     * @return A <code>Credentials</code> object that contains the internal credentials associated with this instance of
     *         the <code>StorageCredentialsAccountAndKey</code> class.
     * @deprecated as of 3.0.0. Please use {@link #getAccountName()}, {@link #exportKey()}, or 
     *          {@link #exportBase64EncodedKey()}
     */
    @Deprecated
    public Credentials getCredentials() {
        return this.credentials;
    }

    /**
     * Sets the credentials.
     * 
     * @param credentials
     *            A <code>Credentials</code> object that represents the credentials to set for this instance of the
     *            <code>StorageCredentialsAccountAndKey</code> class.
     * @deprecated as of 3.0.0. Please use {@link #setAccountName(String)}, {@link #updateKey(String)}, or 
     *          {@link #updateKey(byte[])}
     */
    @Deprecated
    public void setCredentials(final Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Returns a <code>String</code> that represents this instance, optionally including sensitive data.
     * 
     * @param exportSecrets
     *            <code>true</code> to include sensitive data in the return string; otherwise, <code>false</code>.
     * 
     * @return A <code>String</code> that represents this object, optionally including sensitive data.
     */
    @SuppressWarnings("deprecation")
    @Override
    public String toString(final boolean exportSecrets) {
        return String.format("%s=%s;%s=%s", CloudStorageAccount.ACCOUNT_NAME_NAME, this.getAccountName(),
                CloudStorageAccount.ACCOUNT_KEY_NAME, exportSecrets ? this.credentials.getKey().getBase64EncodedKey()
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
}
