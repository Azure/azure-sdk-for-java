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
package com.microsoft.windowsazure.services.blob.client;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseResponse;

/**
 * RESERVED FOR INTERNAL USE. A class used to parse the response from container
 * operations
 */
final class ContainerResponse extends BaseResponse
{

    /**
     * Gets the ACL for the container from the response.
     * 
     * @param request
     *            the request object for this operation
     * @return the ACL value indicating the public access level for the
     *         container
     */
    public static String getAcl(final HttpURLConnection request)
    {
        return request.getHeaderField(BlobConstants.BLOB_PUBLIC_ACCESS_HEADER);
    }

    /**
     * Gets the BlobContainerAttributes from the given request.
     * 
     * @param request
     *            the request to get attributes from.
     * @param usePathStyleUris
     *            a value indicating if the account is using pathSytleUris.
     * @return the BlobContainerAttributes from the given request.
     * @throws StorageException
     */
    public static BlobContainerAttributes getAttributes(
            final HttpURLConnection request, final boolean usePathStyleUris)
            throws StorageException
    {
        final BlobContainerAttributes containerAttributes = new BlobContainerAttributes();
        URI tempURI;
        try
        {
            tempURI = PathUtility.stripURIQueryAndFragment(request.getURL()
                    .toURI());
        } catch (final URISyntaxException e)
        {
            final StorageException wrappedUnexpectedException = Utility
                    .generateNewUnexpectedStorageException(e);
            throw wrappedUnexpectedException;
        }

        containerAttributes.setUri(tempURI);
        containerAttributes.setName(PathUtility.getContainerNameFromUri(
                tempURI, usePathStyleUris));

        final BlobContainerProperties containerProperties = containerAttributes
                .getProperties();
        containerProperties.setEtag(BaseResponse.getEtag(request));
        containerProperties
                .setLastModified(new Date(request.getLastModified()));
        containerAttributes.setMetadata(getMetadata(request));

        containerProperties
                .setLeaseStatus(BaseResponse.getLeaseStatus(request));
        containerProperties.setLeaseState(BaseResponse.getLeaseState(request));
        containerProperties.setLeaseDuration(BaseResponse
                .getLeaseDuration(request));

        return containerAttributes;
    }
}
