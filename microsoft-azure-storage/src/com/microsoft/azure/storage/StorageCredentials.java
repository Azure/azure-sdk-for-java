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
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Map;

import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a set of credentials used to authenticate access to a Microsoft Azure storage account. This is the base
 * class for the {@link StorageCredentialsAccountAndKey} and {@link StorageCredentialsSharedAccessSignature} classes.
 */
public abstract class StorageCredentials {

    /**
     * Tries to determine the storage credentials from a collection of name/value pairs.
     * 
     * @param settings
     *            A <code>Map</code> object of the name/value pairs that represent the settings to use to configure
     *            the credentials.
     *            <p>
     *            Either include an account name with an account key (specifying values for
     *            {@link CloudStorageAccount#ACCOUNT_NAME_NAME} and {@link CloudStorageAccount#ACCOUNT_KEY_NAME} ), or a
     *            shared access signature (specifying a value for
     *            {@link CloudStorageAccount#SHARED_ACCESS_SIGNATURE_NAME} ). If you use an account name and account
     *            key, do not include a shared access signature, and vice versa.
     * 
     * @return A {@link StorageCredentials} object representing the storage credentials determined from the name/value
     *         pairs.
     * 
     * @throws InvalidKeyException
     *             If the key value specified for {@link CloudStorageAccount#ACCOUNT_KEY_NAME} is not a valid
     *             Base64-encoded string.
     */
    protected static StorageCredentials tryParseCredentials(final Map<String, String> settings)
            throws InvalidKeyException {
        final String accountName = settings.get(CloudStorageAccount.ACCOUNT_NAME_NAME) != null ?
                settings.get(CloudStorageAccount.ACCOUNT_NAME_NAME) : null;

        final String accountKey = settings.get(CloudStorageAccount.ACCOUNT_KEY_NAME) != null ?
                settings.get(CloudStorageAccount.ACCOUNT_KEY_NAME) : null;

        final String sasSignature = settings.get(CloudStorageAccount.SHARED_ACCESS_SIGNATURE_NAME) != null ?
                settings.get(CloudStorageAccount.SHARED_ACCESS_SIGNATURE_NAME) : null;

        if (accountName != null && accountKey != null && sasSignature == null) {
            if (Base64.validateIsBase64String(accountKey)) {
                return new StorageCredentialsAccountAndKey(accountName, accountKey);
            }
            else {
                throw new InvalidKeyException(SR.INVALID_KEY);
            }
        }
        if (accountName == null && accountKey == null && sasSignature != null) {
            return new StorageCredentialsSharedAccessSignature(sasSignature);
        }

        return null;
    }

    /**
     * Tries to determine the storage credentials from a connection string.
     * <p>
     * The format for the connection string is in the pattern "<i>keyname=value</i>". Multiple key/value pairs can be
     * separated by a semi-colon, for example, "<i>keyname1=value1;keyname2=value2</i>". Either include an account name
     * with an account key or a shared access signature. If you use an account name and account key, do not include a
     * shared access signature, and vice versa.
     * <p>
     * The same connection string can be used as for {@link CloudStorageAccount#parse(String)} but here only the account
     * name, account key, and sas key/value pairs will be examined.
     * 
     * @param connectionString
     *            A <code>String</code> that contains the key/value pairs that represent the storage credentials.
     * 
     * @return A {@link StorageCredentials} object representing the storage credentials determined from the connection
     *         string.
     * 
     * @throws InvalidKeyException
     *             If the account key specified in <code>connectionString</code> is not valid.
     * @throws StorageException 
     */
    public static StorageCredentials tryParseCredentials(final String connectionString)
            throws InvalidKeyException, StorageException {
        
        return tryParseCredentials(Utility.parseAccountString(connectionString));
    }
    
    /**
     * A <code>boolean</code> representing whether this <code>StorageCredentials</code> object only allows access via HTTPS.
     */
    private boolean httpsOnly = false;
    
    /**
     * Gets whether this <code>StorageCredentials</code> object only allows access via HTTPS.
     * 
     * @return A <code>boolean</code> representing whether this <code>StorageCredentials</code>
     *         object only allows access via HTTPS.
     */
    public boolean isHttpsOnly() {
        return this.httpsOnly;
    }

    /**
     * Returns the associated account name for the credentials. This is null for anonymous and shared access signature
     * credentials.
     * 
     * @return A <code>String</code> that represents the associated account name for the credentials
     */
    public String getAccountName() {
        return null;
    }
    
    /**
     * Sets whether this <code>StorageCredentials</code> object only allows access via HTTPS.
     * @param httpsOnly
     *            A <code>boolean</code> representing whether this <code>StorageCredentials</code>
     *            object only allows access via HTTPS.
     */
    protected void setHttpsOnly(boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
    }

    /**
     * Returns a <code>String</code> that represents this instance.
     * 
     * @param exportSecrets
     *            <code>true</code> to include sensitive data in the return string; otherwise, <code>false</code>.
     * @return A <code>String</code> that represents this object, optionally including sensitive data.
     */
    public abstract String toString(boolean exportSecrets);

    /**
     * Transforms a resource URI into a shared access signature URI, by appending a shared access token.
     * 
     * @param resourceUri
     *            A <code>java.net.URI</code> object that represents the resource URI to be transformed.
     * 
     * @return A <code>java.net.URI</code> object that represents the signature, including the resource URI and the
     *         shared access token.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is not properly formatted.
     */
    public URI transformUri(final URI resourceUri) throws URISyntaxException, StorageException {
        return this.transformUri(resourceUri, null);
    }

    /**
     * Transforms a resource URI into a shared access signature URI, by appending a shared access token.
     * 
     * @param resourceUri
     *            A <code>StorageUri</code> object that represents the resource URI to be transformed.
     * 
     * @return A <code>StorageUri</code> object that represents the signature, including the resource URI and the
     *         shared access token.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is not properly formatted.
     */
    public StorageUri transformUri(StorageUri resourceUri) throws URISyntaxException, StorageException {
        return this.transformUri(resourceUri, null /* opContext */);
    }

    /**
     * Transforms a resource URI into a shared access signature URI, by appending a shared access token and using the
     * specified operation context.
     * 
     * @param resourceUri
     *            A <code>java.net.URI</code> object that represents the resource URI to be transformed.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>java.net.URI</code> object that represents the signature, including the resource URI and the
     *         shared access token.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is not properly formatted.
     */
    public abstract URI transformUri(URI resourceUri, OperationContext opContext) throws URISyntaxException,
            StorageException;

    /**
     * Transforms a resource URI into a shared access signature URI, by appending a shared access token and using the
     * specified operation context.
     * 
     * @param resourceUri
     *            A <code>StorageUri</code> object that represents the resource URI to be transformed.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>StorageUri</code> object that represents the signature, including the resource URI and the
     *         shared access token.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is not properly formatted.
     */
    public abstract StorageUri transformUri(StorageUri resourceUri, OperationContext opContext)
            throws URISyntaxException, StorageException;
}
