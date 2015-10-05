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
import java.util.Map;

import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.SR;

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
        
        if (token == null) {
            this.setHttpsOnly(false);
        }
        else {
            try {
                Map<String, String[]> queryParams = PathUtility.parseQueryString(token);
                final String[] protocols = queryParams.get(Constants.QueryConstants.SIGNED_PROTOCOLS);
                this.setHttpsOnly((protocols != null) && Constants.HTTPS.equals(protocols[0]));
            }
            catch (StorageException e) {
                this.setHttpsOnly(false);
            }
        }
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
    public URI transformUri(final URI resourceUri, final OperationContext opContext)
            throws URISyntaxException, StorageException {
        if (resourceUri == null) {
            return null;
        }
        
        if(this.isHttpsOnly() && !resourceUri.getScheme().equals(Constants.HTTPS)) {
            throw new IllegalArgumentException(SR.CANNOT_TRANSFORM_NON_HTTPS_URI_WITH_HTTPS_ONLY_CREDENTIALS);
        }
            
        // append the sas token to the resource uri
        URI sasUri = PathUtility.addToQuery(resourceUri, this.token);
        
        // append the api version parameter to the sas uri
        String apiVersion = Constants.QueryConstants.API_VERSION + "=" + Constants.HeaderConstants.TARGET_STORAGE_VERSION;
        return PathUtility.addToQuery(sasUri, apiVersion);
    }

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
    @Override
    public StorageUri transformUri(StorageUri resourceUri, OperationContext opContext) throws URISyntaxException,
            StorageException {
        return new StorageUri(this.transformUri(resourceUri.getPrimaryUri(), opContext), this.transformUri(
                resourceUri.getSecondaryUri(), opContext));
    }
}
