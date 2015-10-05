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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageException;

/**
 * RESERVED FOR INTERNAL USE. A helper method for StorageCredentials.
 */
public final class StorageCredentialsHelper {

    /**
     *  RESERVED, for internal use only. Gets a value indicating whether a
     *  request can be signed under the Shared Key authentication scheme using
     *  the specified credentials.
    
     *  @return <Code>true</Code> if a request can be signed with these
     *  credentials; otherwise, <Code>false</Code>
     */
    public static boolean canCredentialsSignRequest(final StorageCredentials creds) {
        return creds.getClass().equals(StorageCredentialsAccountAndKey.class);
    }

    /**
     *  RESERVED, for internal use only. Gets a value indicating whether a
     *  client can be generated under the Shared Key or Shared Access Signature
     *  authentication schemes using the specified credentials.
     *  @return <Code>true</Code> if a client can be generated with these
     *  credentials; otherwise, <Code>false</Code>
     */
    public static boolean canCredentialsGenerateClient(final StorageCredentials creds) {
        return canCredentialsSignRequest(creds) || creds.getClass().equals(StorageCredentialsSharedAccessSignature.class);
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
    public static synchronized String computeHmac256(final StorageCredentials creds, final String value) throws InvalidKeyException {
        if (creds.getClass().equals(StorageCredentialsAccountAndKey.class)) {
            byte[] utf8Bytes = null;
            try {
                utf8Bytes = value.getBytes(Constants.UTF8_CHARSET);
            }
            catch (final UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
            return Base64.encode(((StorageCredentialsAccountAndKey) creds).getHmac256().doFinal(utf8Bytes));
        }
        else {
            return null;
        }
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
    public static void signBlobQueueAndFileRequest(final StorageCredentials creds,
            final java.net.HttpURLConnection request, final long contentLength, OperationContext opContext)
            throws InvalidKeyException, StorageException {
        
        if (creds.getClass().equals(StorageCredentialsAccountAndKey.class)) {
            opContext = opContext == null ? new OperationContext() : opContext;
            request.setRequestProperty(Constants.HeaderConstants.DATE, Utility.getGMTTime());
            final Canonicalizer canonicalizer = CanonicalizerFactory.getBlobQueueFileCanonicalizer(request);

            final String stringToSign = canonicalizer.canonicalize(request, creds.getAccountName(), contentLength);

            final String computedBase64Signature = StorageCredentialsHelper.computeHmac256(creds, stringToSign);

            Logger.trace(opContext, LogConstants.SIGNING, stringToSign);
            
            request.setRequestProperty(Constants.HeaderConstants.AUTHORIZATION,
                    String.format("%s %s:%s", "SharedKey", creds.getAccountName(), computedBase64Signature));
        }
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
            request.setRequestProperty(Constants.HeaderConstants.DATE, Utility.getGMTTime());

            final Canonicalizer canonicalizer = CanonicalizerFactory.getTableCanonicalizer(request);

            final String stringToSign = canonicalizer.canonicalize(request, creds.getAccountName(), contentLength);

            final String computedBase64Signature = StorageCredentialsHelper.computeHmac256(creds, stringToSign);
            
            Logger.trace(opContext, LogConstants.SIGNING, stringToSign);

            request.setRequestProperty(Constants.HeaderConstants.AUTHORIZATION,
                    String.format("%s %s:%s", "SharedKey", creds.getAccountName(), computedBase64Signature));
        }
    }
    
    /**
     * A private default constructor. All methods of this class are static so no instances of it should ever be created.
     */
    private StorageCredentialsHelper() {
        //No op
    }
}
