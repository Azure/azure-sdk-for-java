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

/**
 * Represents storage account credentials, based on storage account and access key, for accessing the Microsoft Azure
 * storage services.
 */
public final class StorageCredentialsAccountAndKey extends StorageCredentials {

    /**
     * The internal Credentials associated with the StorageCredentials.
     */
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
    public StorageCredentialsAccountAndKey(final String accountName, final String key) {
        this(accountName, Base64.decode(key));
    }

    /**
     * Returns the associated account name for the credentials.
     * 
     * @return A <code>String</code> that contains the account name for the credentials.
     */
    @Override
    public String getAccountName() {
        return this.credentials.getAccountName();
    }

    /**
     * Gets the name of the key used by these credentials.
     */
    public String getAccountKeyName() {
        return this.credentials.getKeyName();
    }

    /**
     * Returns the internal credentials associated with the storage credentials.
     * 
     * @return A <code>Credentials</code> object that contains the internal credentials associated with this instance of
     *         the <code>StorageCredentialsAccountAndKey</code> class.
     */
    public Credentials getCredentials() {
        return this.credentials;
    }

    /**
     * Sets the credentials.
     * 
     * @param credentials
     *            A <code>Credentials</code> object that represents the credentials to set for this instance of the
     *            <code>StorageCredentialsAccountAndKey</code> class.
     */
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
