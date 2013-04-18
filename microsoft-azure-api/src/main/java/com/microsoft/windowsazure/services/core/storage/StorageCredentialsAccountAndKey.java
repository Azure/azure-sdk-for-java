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
package com.microsoft.windowsazure.services.core.storage;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseRequest;

/**
 * Represents storage account credentials, based on storage account and access key, for accessing the Windows Azure
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

    //
    // RESERVED, for internal use only. Gets a value indicating whether the
    // <Code>ComputeHmac</Code> method will return a valid HMAC-encoded
    // signature string when called using the specified credentials.
    //
    // @return <Code>True</Code>
    //
    /** Reserved. */
    @Override
    public boolean canCredentialsComputeHmac() {
        return true;
    }

    //
    // RESERVED, for internal use only. Gets a value indicating whether a
    // request can be signed under the Shared Key authentication scheme using
    // the specified credentials.
    //
    // @return <Code>True</Code>
    //
    /** Reserved. */
    @Override
    public boolean canCredentialsSignRequest() {
        return true;
    }

    //
    // RESERVED, for internal use only. Gets a value indicating whether a
    // request can be signed under the Shared Key Lite authentication scheme
    // using the specified credentials.
    //
    // @return <Code>True</Code>
    //
    /** Reserved. */
    @Override
    public boolean canCredentialsSignRequestLite() {
        return true;
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA256-encoded signature.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid Base64-encoded string.
     */
    @Override
    public String computeHmac256(final String value) throws InvalidKeyException {
        return this.computeHmac256(value, null);
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm with the specified operation
     * context.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA256-encoded signature.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid Base64-encoded string.
     */
    @Override
    public String computeHmac256(final String value, final OperationContext opContext) throws InvalidKeyException {
        return StorageKey.computeMacSha256(this.credentials.getKey(), value);
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA512 algorithm.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA512-encoded signature.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid Base64-encoded string.
     */
    @Override
    public String computeHmac512(final String value) throws InvalidKeyException {
        return this.computeHmac512(value, null);
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA512 algorithm with the specified operation
     * context.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA512-encoded signature.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid Base64-encoded string.
     */
    @Override
    public String computeHmac512(final String value, final OperationContext opContext) throws InvalidKeyException {
        return StorageKey.computeMacSha512(this.credentials.getKey(), value);
    }

    //
    // RESERVED, for internal use only. Gets a value indicating whether the
    // <Code>TransformUri</Code> method should be called to transform a resource
    // URI to a URI that includes a token for a shared access signature.
    //
    // @return <Code>False</Code>.
    //
    /** Reserved. */
    @Override
    public boolean doCredentialsNeedTransformUri() {
        return false;
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
     * Internal usage.
     * Gets the name of the key used by these credentials.
     */
    public String getAccountKeyName() {
        return this.credentials.getKeyName();
    }

    /**
     * Internal usage.
     * Sets the account name that owns the key to use when signing requests.
     */
    public void setSigningAccountName(final String signingAccountName) {
        this.credentials.setSigningAccountName(signingAccountName);
    }

    /**
     * Returns the Base64-encoded key for the credentials.
     * 
     * @return A <code>String</code> that contains the Base64-encoded key.
     */
    protected String getBase64EncodedKey() {
        return this.credentials.getKey().getBase64EncodedKey();
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
     * Signs a request under the Shared Key authentication scheme.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * 
     * @throws InvalidKeyException
     *             If the given key is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @Override
    public void signRequest(final java.net.HttpURLConnection request, final long contentLength)
            throws InvalidKeyException, StorageException {
        this.signRequest(request, contentLength, null);
    }

    /**
     * Signs a request using the specified operation context under the Shared Key authentication scheme.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws InvalidKeyException
     *             If the given key is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @Override
    public void signRequest(final java.net.HttpURLConnection request, final long contentLength,
            OperationContext opContext) throws InvalidKeyException, StorageException {
        opContext = opContext == null ? new OperationContext() : opContext;
        BaseRequest.signRequestForBlobAndQueue(request, this.credentials, contentLength, opContext);
    }

    /**
     * Signs a request using the Shared Key Lite authentication scheme.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * 
     * @throws InvalidKeyException
     *             If the given key is invalid.
     * @throws StorageException
     *             If an unspecified storage exception occurs.
     */
    @Override
    public void signRequestLite(final java.net.HttpURLConnection request, final long contentLength)
            throws InvalidKeyException, StorageException {
        this.signRequestLite(request, contentLength, null);
    }

    /**
     * Signs a request using the specified operation context under the Shared Key Lite authentication scheme.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws InvalidKeyException
     *             If the given key is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @Override
    public void signRequestLite(final java.net.HttpURLConnection request, final long contentLength,
            OperationContext opContext) throws StorageException, InvalidKeyException {
        opContext = opContext == null ? new OperationContext() : opContext;
        BaseRequest.signRequestForTableSharedKeyLite(request, this.credentials, contentLength, opContext);
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
    @Override
    public URI transformUri(final URI resourceUri) {
        return this.transformUri(resourceUri, null);
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
    @Override
    public URI transformUri(final URI resourceUri, final OperationContext opContext) {
        return resourceUri;
    }
}
