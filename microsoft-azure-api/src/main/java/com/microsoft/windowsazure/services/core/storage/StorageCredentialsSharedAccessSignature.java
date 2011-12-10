/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.core.storage;

import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;

/**
 * Represents storage credentials for delegated access to Blob service resources via a shared access signature.
 */
public final class StorageCredentialsSharedAccessSignature extends StorageCredentials {

    /**
     * Stores the shared access signature token.
     */
    private final String token;

    /**
     * Creates an instance of the <code>StorageCredentialsSharedAccessSignature</code> class using the specified shared
     * access signature token.
     * 
     * @param token
     *            A <code>String</code> that represents shared access signature token.
     */
    public StorageCredentialsSharedAccessSignature(final String token) {
        this.token = token;
    }

    //
    // RESERVED, for internal use only. Gets a value indicating whether the
    // <Code>ComputeHmac</Code> method will return a valid HMAC-encoded
    // signature string when called using the specified credentials.
    //
    // @return <Code>False</Code>
    //
    /** Reserved. */
    @Override
    public boolean canCredentialsComputeHmac() {
        return false;
    }

    //
    // RESERVED, for internal use only. Gets a value indicating whether a
    // request can be signed under the Shared Key authentication scheme using
    // the specified credentials.
    //
    // @return <Code>False</Code>
    //
    /** Reserved. */
    @Override
    public boolean canCredentialsSignRequest() {
        return false;
    }

    //
    // RESERVED, for internal use only. Gets a value indicating whether a
    // request can be signed under the Shared Key Lite authentication scheme
    // using the specified credentials.
    //
    // @return <Code>False</Code>
    //
    /** Reserved. */
    @Override
    public boolean canCredentialsSignRequestLite() {
        return false;
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm. This is not a valid operation for
     * objects of type <code>StorageCredentialsSharedAccessSignature</code> so the method merely returns
     * <code>null</code>.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * 
     * @return <code>null</code> for objects of type <code>StorageCredentialsSharedAccessSignature</code>.
     */
    @Override
    public String computeHmac256(final String value) {
        return null;
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm with the specified operation
     * context. This is not a valid operation for objects of type <code>StorageCredentialsSharedAccessSignature</code>
     * so the method merely returns <code>null</code>.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>null</code> for objects of type <code>StorageCredentialsSharedAccessSignature</code>.
     */
    @Override
    public String computeHmac256(final String value, final OperationContext opContext) {
        return null;
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA512 algorithm. This is not a valid operation for
     * objects of type <code>StorageCredentialsSharedAccessSignature</code> so the method merely returns
     * <code>null</code>.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * 
     * @return <code>null</code> for objects of type <code>StorageCredentialsSharedAccessSignature</code>.
     */
    @Override
    public String computeHmac512(final String value) {
        return null;
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA512 algorithm with the specified operation
     * context. This is not a valid operation for objects of type <code>StorageCredentialsSharedAccessSignature</code>
     * so the method merely returns <code>null</code>.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>null</code> for objects of type <code>StorageCredentialsSharedAccessSignature</code>.
     */
    @Override
    public String computeHmac512(final String value, final OperationContext opContext) {
        return null;
    }

    //
    // RESERVED, for internal use only. Gets a value indicating whether the
    // <Code>TransformUri</Code> method should be called to transform a resource
    // URI to a URI that includes a token for a shared access signature.
    //
    // @return <Code>True</Code>.
    //
    /** Reserved. */
    @Override
    public boolean doCredentialsNeedTransformUri() {
        return true;
    }

    /**
     * Returns the associated account name for the credentials. This is not a valid operation for objects of type
     * <code>StorageCredentialsSharedAccessSignature</code> so the method merely returns <code>null</code>.
     * 
     * @return <code>null</code> for objects of type <code>StorageCredentialsSharedAccessSignature</code>.
     */
    @Override
    public String getAccountName() {
        return null;
    }

    /**
     * Returns the shared access signature token.
     * 
     * @return A <code>String</code> that contains the token.
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Signs a request under the Shared Key authentication scheme. This is not a valid operation for objects of type
     * <code>StorageCredentialsSharedAccessSignature</code> so the method performs a no-op.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     */
    @Override
    public void signRequest(final java.net.HttpURLConnection request, final long contentLength) {
        // No op
    }

    /**
     * Signs a request using the specified operation context under the Shared Key authentication scheme. This is not a
     * valid operation for objects of type <code>StorageCredentialsSharedAccessSignature</code> so the method performs a
     * no-op.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     */
    @Override
    public void signRequest(final java.net.HttpURLConnection request, final long contentLength,
            final OperationContext opContext) {
        // No op
    }

    /**
     * Signs a request under the Shared Key Lite authentication scheme. This is not a valid operation for objects of
     * type <code>StorageCredentialsSharedAccessSignature</code> so the method performs a no-op.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     */
    @Override
    public void signRequestLite(final java.net.HttpURLConnection request, final long contentLength) {
        // No op
    }

    /**
     * Signs a request using the specified operation context under the Shared Key Lite authentication scheme. This is
     * not a valid operation for objects of type <code>StorageCredentialsSharedAccessSignature</code> so the method
     * performs a no-op.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     */
    @Override
    public void signRequestLite(final java.net.HttpURLConnection request, final long contentLength,
            final OperationContext opContext) {
        // No op
    }

    /**
     * Returns a <code>String</code> that represents this instance, optionally including sensitive data.
     * 
     * @param exportSecrets
     *            <code>true</code> to include sensitive data in the return string; otherwise, <code>false</code>.
     * @return A <code>String</code> that represents this object, optionally including sensitive data.
     */
    @Override
    public String toString(final boolean exportSecrets) {
        return String.format("%s=%s", CloudStorageAccount.SHARED_ACCESS_SIGNATURE_NAME, exportSecrets ? this.token
                : "[signature hidden]");
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
     * @throws IllegalArgumentException
     *             If a parameter is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is not properly formatted.
     */
    @Override
    public URI transformUri(final URI resourceUri) throws URISyntaxException, StorageException {
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
    public URI transformUri(final URI resourceUri, final OperationContext opContext) throws URISyntaxException,
            StorageException {
        return PathUtility.addToQuery(resourceUri, this.token);
    }
}
