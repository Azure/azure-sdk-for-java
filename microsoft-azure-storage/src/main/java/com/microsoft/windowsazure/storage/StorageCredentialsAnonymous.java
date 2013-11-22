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
package com.microsoft.windowsazure.storage;

import java.net.URI;

/**
 * *RESERVED FOR INTERNAL USE* Represents credentials for anonymous access. This class is used by the internal
 * implementation (not a public class, so its comments are not built into the Javadoc output).
 */
public final class StorageCredentialsAnonymous extends StorageCredentials {

    /**
     * Stores the singleton instance of this class.
     */
    public static final StorageCredentials ANONYMOUS = new StorageCredentialsAnonymous();

    /**
     * Returns the singleton instance of the <code>StorageCredentials</code> class.
     * 
     * @return the singleton instance of this class
     */
    protected static StorageCredentials getInstance() {
        return StorageCredentialsAnonymous.ANONYMOUS;
    }

    /**
     * Enforces the singleton pattern via a private constructor.
     */
    protected StorageCredentialsAnonymous() {
        // Empty Default Ctor
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
     * Encodes a Shared Key or Shared Key Lite signature string by using the HMAC-SHA256 algorithm over a UTF-8-encoded
     * string-to-sign. This is not a valid operation for objects of type <code>StorageCredentialsAnonymous</code> so the
     * method merely returns null.
     * 
     * @param value
     *            the UTF-8-encoded string-to-sign
     * @return null for objects of type <code>StorageCredentialsAnonymous</code>
     */
    @Override
    public String computeHmac256(final String value) {
        return null;
    }

    /**
     * Encodes a Shared Key or Shared Key Lite signature string by using the HMAC-SHA256 algorithm over a UTF-8-encoded
     * string-to-sign and the specified operation context. This is not a valid operation for objects of type
     * <code>StorageCredentialsAnonymous</code> so the method merely returns null.
     * 
     * @param value
     *            the UTF-8-encoded string-to-sign
     * @param opContext
     *            an operation context, as a {@link com.microsoft.windowsazure.storage.OperationContext} object,
     *            that represents the current operation
     * @return null for objects of type <code>StorageCredentialsAnonymous</code>
     */
    @Override
    public String computeHmac256(final String value, final OperationContext opContext) {
        return null;
    }

    /**
     * Encodes a Shared Key signature string by using the HMAC-SHA512 algorithm over a UTF-8-encoded string-to-sign.
     * This is not a valid operation for objects of type <code>StorageCredentialsAnonymous</code> so the method merely
     * returns null.
     * 
     * @param value
     *            the UTF-8-encoded string-to-sign
     * @return null for objects of type <code>StorageCredentialsAnonymous</code>
     */
    @Override
    public String computeHmac512(final String value) {
        return null;
    }

    /**
     * Encodes a Shared Key signature string by using the HMAC-SHA256 algorithm over a UTF-8-encoded string-to-sign.
     * This is not a valid operation for objects of type <code>StorageCredentialsAnonymous</code> so the method merely
     * returns null.
     * 
     * @param value
     *            the UTF-8-encoded string-to-sign
     * @param opContext
     *            an operation context, as a {@link com.microsoft.windowsazure.storage.OperationContext} object,
     *            that represents the current operation
     * 
     * @return null for objects of type <code>StorageCredentialsAnonymous</code>
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
    // @return <Code>False</Code>.
    //
    /** Reserved. */
    @Override
    public boolean doCredentialsNeedTransformUri() {
        return false;
    }

    /**
     * Returns the associated account name for the credentials. This is null for anonymous credentials.
     * 
     * @return null for anonymous credentials
     */
    @Override
    public String getAccountName() {
        return null;
    }

    /**
     * Signs a request using the specified credentials under the Shared Key authentication scheme. This is not a valid
     * operation for objects of type <code>StorageCredentialsAnonymous</code> so the method performs a no-op.
     * 
     * @deprecated This method has been deprecated. Please use either {@link signBlobAndQueueRequest} or
     *             {@link signBlobAndQueueRequestLite}, depending on your desired shared key authentication scheme.
     * 
     * @param connection
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     * @param contentLength
     *            the length of the content written to the output stream. If unknown, specify -1.
     */
    @Override
    @Deprecated
    public void signRequest(final java.net.HttpURLConnection connection, final long contentLength) {
        // No op
    }

    /**
     * Signs a request using the specified credentials and operation context under the Shared Key authentication scheme.
     * This is not a valid operation for objects of type <code>StorageCredentialsAnonymous</code> so the method performs
     * a no-op.
     * 
     * @deprecated This method has been deprecated. Please use either {@link signBlobAndQueueRequest} or
     *             {@link signBlobAndQueueRequestLite}, depending on your desired shared key authentication scheme.
     * 
     * @param request
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     * @param contentLength
     *            the length of the content written to the output stream. If unknown, specify -1.
     * @param opContext
     *            an operation context, as a {@link com.microsoft.windowsazure.storage.OperationContext} object,
     *            that represents the current operation
     */
    @Override
    @Deprecated
    public void signRequest(final java.net.HttpURLConnection request, final long contentLength,
            final OperationContext opContext) {
        // No op
    }

    /**
     * Signs a request using the specified credentials under the Shared Key Lite authentication scheme. This is not a
     * valid operation for objects of type <code>StorageCredentialsAnonymous</code> so the method performs a no-op.
     * 
     * @deprecated This method has been deprecated. Please use either {@link signTableRequest} or
     *             {@link signTableRequestLite}, depending on your desired shared key authentication scheme.
     * 
     * @param connection
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     */
    @Override
    @Deprecated
    public void signRequestLite(final java.net.HttpURLConnection connection, final long contentLength) {
        // No op
    }

    /**
     * Signs a request using the specified credentials under the Shared Key Lite authentication scheme. This is not a
     * valid operation for objects of type <code>StorageCredentialsSharedAccessSignature</code> so the method performs a
     * no-op.
     * 
     * @deprecated This method has been deprecated. Please use either {@link signTableRequest} or
     *             {@link signTableRequestLite}, depending on your desired shared key authentication scheme.
     * 
     * @param request
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     * @param opContext
     *            an operation context, as a {@link com.microsoft.windowsazure.storage.OperationContext} object,
     *            that represents the current operation
     */
    @Override
    @Deprecated
    public void signRequestLite(final java.net.HttpURLConnection request, final long contentLength,
            final OperationContext opContext) {
        // No op
    }

    /**
     * Signs a request using the specified credentials under the Shared Key authentication scheme. This is not a valid
     * operation for objects of type <code>StorageCredentialsAnonymous</code> so the method performs a no-op.
     * 
     * @param connection
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     * @param contentLength
     *            the length of the content written to the output stream. If unknown, specify -1.
     */
    @Override
    public void signBlobAndQueueRequest(final java.net.HttpURLConnection connection, final long contentLength) {
        // No op
    }

    /**
     * Signs a request using the specified credentials and operation context under the Shared Key authentication scheme.
     * This is not a valid operation for objects of type <code>StorageCredentialsAnonymous</code> so the method performs
     * a no-op.
     * 
     * @param request
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     * @param contentLength
     *            the length of the content written to the output stream. If unknown, specify -1.
     * @param opContext
     *            an operation context, as a {@link com.microsoft.windowsazure.storage.OperationContext} object,
     *            that represents the current operation
     */
    @Override
    public void signBlobAndQueueRequest(final java.net.HttpURLConnection request, final long contentLength,
            final OperationContext opContext) {
        // No op
    }

    /**
     * Signs a request using the specified credentials under the Shared Key Lite authentication scheme. This is not a
     * valid operation for objects of type <code>StorageCredentialsAnonymous</code> so the method performs a no-op.
     * 
     * @param connection
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     */
    @Override
    public void signBlobAndQueueRequestLite(final java.net.HttpURLConnection connection, final long contentLength) {
        // No op
    }

    /**
     * Signs a request using the specified credentials under the Shared Key Lite authentication scheme. This is not a
     * valid operation for objects of type <code>StorageCredentialsSharedAccessSignature</code> so the method performs a
     * no-op.
     * 
     * @param request
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     * @param opContext
     *            an operation context, as a {@link com.microsoft.windowsazure.storage.OperationContext} object,
     *            that represents the current operation
     */
    @Override
    public void signBlobAndQueueRequestLite(final java.net.HttpURLConnection request, final long contentLength,
            final OperationContext opContext) {
        // No op
    }

    /**
     * Signs a request using the specified credentials under the Shared Key authentication scheme. This is not a valid
     * operation for objects of type <code>StorageCredentialsAnonymous</code> so the method performs a no-op.
     * 
     * @param connection
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     * @param contentLength
     *            the length of the content written to the output stream. If unknown, specify -1.
     */
    @Override
    public void signTableRequest(final java.net.HttpURLConnection connection, final long contentLength) {
        // No op
    }

    /**
     * Signs a request using the specified credentials and operation context under the Shared Key authentication scheme.
     * This is not a valid operation for objects of type <code>StorageCredentialsAnonymous</code> so the method performs
     * a no-op.
     * 
     * @param request
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     * @param contentLength
     *            the length of the content written to the output stream. If unknown, specify -1.
     * @param opContext
     *            an operation context, as a {@link com.microsoft.windowsazure.storage.OperationContext} object,
     *            that represents the current operation
     */
    @Override
    public void signTableRequest(final java.net.HttpURLConnection request, final long contentLength,
            final OperationContext opContext) {
        // No op
    }

    /**
     * Signs a request using the specified credentials under the Shared Key Lite authentication scheme. This is not a
     * valid operation for objects of type <code>StorageCredentialsAnonymous</code> so the method performs a no-op.
     * 
     * @param connection
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     */
    @Override
    public void signTableRequestLite(final java.net.HttpURLConnection connection, final long contentLength) {
        // No op
    }

    /**
     * Signs a request using the specified credentials under the Shared Key Lite authentication scheme. This is not a
     * valid operation for objects of type <code>StorageCredentialsSharedAccessSignature</code> so the method performs a
     * no-op.
     * 
     * @param request
     *            the request, as an <code>HttpURLConnection</code> object, to sign
     * @param opContext
     *            an operation context, as a {@link com.microsoft.windowsazure.storage.OperationContext} object,
     *            that represents the current operation
     */
    @Override
    public void signTableRequestLite(final java.net.HttpURLConnection request, final long contentLength,
            final OperationContext opContext) {
        // No op
    }

    /**
     * Returns a <code>String</code> object that represents this instance.
     * 
     * @param exportSecrets
     *            <code>true</code> to include sensitive data in the string; otherwise, <code>false</code>
     * @return a string representation of the credentials, optionally including sensitive data.
     */
    @Override
    public String toString(final boolean exportSecrets) {
        return Constants.EMPTY_STRING;
    }

    /**
     * Transforms a resource URI into a shared access signature URI, by appending a shared access token. No transform
     * occurs for anonymous credentials, so this method returns the same URI that is passed in.
     * 
     * @param resourceUri
     *            the resource URI to be transformed
     * @return the unmodified value passed in for the <code>resourceUri</code> parameter
     */
    @Override
    public URI transformUri(final URI resourceUri) {
        return this.transformUri(resourceUri, null);
    }

    /**
     * Transforms a resource URI into a shared access signature URI, by appending a shared access token. No transform
     * occurs for anonymous credentials, so this method returns the same URI that is passed in.
     * 
     * @param resourceUri
     *            the resource URI to be transformed
     * @param opContext
     *            the an operation context, as a {@link com.microsoft.windowsazure.storage.OperationContext}
     *            object, that represents the
     *            current operation
     * 
     * @return the unmodified value passed in for the <code>resourceUri</code> parameter
     */
    @Override
    public URI transformUri(final URI resourceUri, final OperationContext opContext) {
        return resourceUri;
    }
}
