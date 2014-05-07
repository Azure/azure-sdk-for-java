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
package com.microsoft.azure.storage.core;

import java.security.InvalidKeyException;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageKey;

/**
 * RESERVED FOR INTERNAL USE. A helper method for StorageCredentials.
 */
public class StorageCredentialsHelper {

    //
    // RESERVED, for internal use only. Gets a value indicating whether a
    // request can be signed under the Shared Key authentication scheme using
    // the specified credentials.
    //
    // @return <Code>True</Code> if a request can be signed with these
    // credentials; otherwise, <Code>false</Code>
    //
    /** Reserved. */
    public static boolean canCredentialsSignRequest(final StorageCredentials creds) {
        if (creds.getClass().equals(StorageCredentialsAccountAndKey.class)) {
            return true;
        }
        else {
            return false;
        }
    }

    //
    // RESERVED, for internal use only. Gets a value indicating whether a
    // request can be signed under the Shared Key Lite authentication scheme
    // using the specified credentials.
    //
    // @return <code>true</code> if a request can be signed with these
    // credentials; otherwise, <code>false</code>
    //
    /** Reserved. */
    public static boolean canCredentialsSignRequestLite(final StorageCredentials creds) {
        if (creds.getClass().equals(StorageCredentialsAccountAndKey.class)) {
            return true;
        }
        else {
            return false;
        }
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
    public static String computeHmac256(final StorageCredentials creds, final String value) throws InvalidKeyException {
        return computeHmac256(creds, value, null);
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
    public static String computeHmac256(final StorageCredentials creds, final String value,
            final OperationContext opContext) throws InvalidKeyException {
        if (creds.getClass().equals(StorageCredentialsAccountAndKey.class)) {
            return StorageKey.computeMacSha256(((StorageCredentialsAccountAndKey) creds).getCredentials().getKey(),
                    value);
        }
        else {
            return null;
        }
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
    public static void signBlobAndQueueRequest(final StorageCredentials creds,
            final java.net.HttpURLConnection request, final long contentLength) throws InvalidKeyException,
            StorageException {
        signBlobAndQueueRequest(creds, request, contentLength, null);
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
    public static void signBlobAndQueueRequest(final StorageCredentials creds,
            final java.net.HttpURLConnection request, final long contentLength, OperationContext opContext)
            throws InvalidKeyException, StorageException {
        if (creds.getClass().equals(StorageCredentialsAccountAndKey.class)) {
            opContext = opContext == null ? new OperationContext() : opContext;
            BaseRequest.signRequestForBlobAndQueue(request, ((StorageCredentialsAccountAndKey) creds).getCredentials(),
                    contentLength, opContext);
        }
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
    public static void signBlobAndQueueRequestLite(final StorageCredentials creds,
            final java.net.HttpURLConnection request, final long contentLength) throws InvalidKeyException,
            StorageException {
        signBlobAndQueueRequestLite(creds, request, contentLength, null);
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
    public static void signBlobAndQueueRequestLite(final StorageCredentials creds,
            final java.net.HttpURLConnection request, final long contentLength, OperationContext opContext)
            throws StorageException, InvalidKeyException {
        if (creds.getClass().equals(StorageCredentialsAccountAndKey.class)) {
            opContext = opContext == null ? new OperationContext() : opContext;
            BaseRequest.signRequestForBlobAndQueueSharedKeyLite(request,
                    ((StorageCredentialsAccountAndKey) creds).getCredentials(), contentLength, opContext);
        }
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
    public static void signTableRequest(final StorageCredentials creds, final java.net.HttpURLConnection request,
            final long contentLength) throws InvalidKeyException, StorageException {
        signTableRequest(creds, request, contentLength, null);
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
    public static void signTableRequest(final StorageCredentials creds, final java.net.HttpURLConnection request,
            final long contentLength, OperationContext opContext) throws InvalidKeyException, StorageException {
        if (creds.getClass().equals(StorageCredentialsAccountAndKey.class)) {
            opContext = opContext == null ? new OperationContext() : opContext;
            BaseRequest.signRequestForTableSharedKey(request,
                    ((StorageCredentialsAccountAndKey) creds).getCredentials(), contentLength, opContext);
        }
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
    public static void signTableRequestLite(final StorageCredentials creds, final java.net.HttpURLConnection request,
            final long contentLength) throws InvalidKeyException, StorageException {
        signTableRequestLite(creds, request, contentLength, null);
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
    public static void signTableRequestLite(final StorageCredentials creds, final java.net.HttpURLConnection request,
            final long contentLength, OperationContext opContext) throws StorageException, InvalidKeyException {
        if (creds.getClass().equals(StorageCredentialsAccountAndKey.class)) {
            opContext = opContext == null ? new OperationContext() : opContext;
            BaseRequest.signRequestForTableSharedKeyLite(request,
                    ((StorageCredentialsAccountAndKey) creds).getCredentials(), contentLength, opContext);
        }
    }
}
