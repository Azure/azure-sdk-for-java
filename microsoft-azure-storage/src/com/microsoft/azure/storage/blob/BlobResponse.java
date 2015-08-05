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
package com.microsoft.azure.storage.blob;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.BaseResponse;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to parse the response from blob and container operations.
 */
final class BlobResponse extends BaseResponse {

    /**
     * Gets the ACL for the container from the response.
     * 
     * @param request
     *            the request object for this operation
     * @return the ACL value indicating the public access level for the container
     */
    public static String getAcl(final HttpURLConnection request) {
        return request.getHeaderField(BlobConstants.BLOB_PUBLIC_ACCESS_HEADER);
    }

    /**
     * Gets the BlobAttributes from the given request
     * 
     * @param request
     *            The response from server.
     * @param resourceURI
     *            The blob uri to set.
     * @param snapshotID
     *            The snapshot version, if the blob is a snapshot.
     * @return the BlobAttributes from the given request
     * @throws ParseException
     * @throws URISyntaxException
     */
    public static BlobAttributes getBlobAttributes(final HttpURLConnection request, final StorageUri resourceURI,
            final String snapshotID) throws URISyntaxException, ParseException {

        final String blobType = request.getHeaderField(BlobConstants.BLOB_TYPE_HEADER);
        final BlobAttributes attributes = new BlobAttributes(BlobType.parse(blobType));
        final BlobProperties properties = attributes.getProperties();

        properties.setCacheControl(request.getHeaderField(Constants.HeaderConstants.CACHE_CONTROL));
        properties.setContentDisposition(request.getHeaderField(Constants.HeaderConstants.CONTENT_DISPOSITION));
        properties.setContentEncoding(request.getHeaderField(Constants.HeaderConstants.CONTENT_ENCODING));
        properties.setContentLanguage(request.getHeaderField(Constants.HeaderConstants.CONTENT_LANGUAGE));
        properties.setContentMD5(request.getHeaderField(Constants.HeaderConstants.CONTENT_MD5));
        properties.setContentType(request.getHeaderField(Constants.HeaderConstants.CONTENT_TYPE));
        properties.setEtag(BaseResponse.getEtag(request));

        final Calendar lastModifiedCalendar = Calendar.getInstance(Utility.LOCALE_US);
        lastModifiedCalendar.setTimeZone(Utility.UTC_ZONE);
        lastModifiedCalendar.setTime(new Date(request.getLastModified()));
        properties.setLastModified(lastModifiedCalendar.getTime());

        properties.setLeaseStatus(getLeaseStatus(request));
        properties.setLeaseState(getLeaseState(request));
        properties.setLeaseDuration(getLeaseDuration(request));

        final String rangeHeader = request.getHeaderField(Constants.HeaderConstants.CONTENT_RANGE);
        final String xContentLengthHeader = request.getHeaderField(BlobConstants.CONTENT_LENGTH_HEADER);

        if (!Utility.isNullOrEmpty(rangeHeader)) {
            properties.setLength(Long.parseLong(rangeHeader.split("/")[1]));
        }
        else if (!Utility.isNullOrEmpty(xContentLengthHeader)) {
            properties.setLength(Long.parseLong(xContentLengthHeader));
        }
        else {
            // using this instead of the request property since the request
            // property only returns an int.
            final String contentLength = request.getHeaderField(Constants.HeaderConstants.CONTENT_LENGTH);

            if (!Utility.isNullOrEmpty(contentLength)) {
                properties.setLength(Long.parseLong(contentLength));
            }
        }
        
        // Get sequence number
        final String sequenceNumber = request.getHeaderField(Constants.HeaderConstants.BLOB_SEQUENCE_NUMBER);
        if (!Utility.isNullOrEmpty(sequenceNumber)) {
            properties.setPageBlobSequenceNumber(Long.parseLong(sequenceNumber));
        }
        
        // Get committed block count
        final String comittedBlockCount = request.getHeaderField(Constants.HeaderConstants.BLOB_COMMITTED_BLOCK_COUNT);
        if (!Utility.isNullOrEmpty(comittedBlockCount))
        {
            properties.setAppendBlobCommittedBlockCount(Integer.parseInt(comittedBlockCount));
        }

        attributes.setStorageUri(resourceURI);
        attributes.setSnapshotID(snapshotID);

        attributes.setMetadata(BaseResponse.getMetadata(request));
        properties.setCopyState(getCopyState(request));
        attributes.setProperties(properties);
        return attributes;
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
    public static BlobContainerAttributes getBlobContainerAttributes(final HttpURLConnection request,
            final boolean usePathStyleUris) throws StorageException {
        final BlobContainerAttributes containerAttributes = new BlobContainerAttributes();
        URI tempURI;
        try {
            tempURI = PathUtility.stripSingleURIQueryAndFragment(request.getURL().toURI());
        }
        catch (final URISyntaxException e) {
            final StorageException wrappedUnexpectedException = Utility.generateNewUnexpectedStorageException(e);
            throw wrappedUnexpectedException;
        }

        containerAttributes.setName(PathUtility.getContainerNameFromUri(tempURI, usePathStyleUris));

        final BlobContainerProperties containerProperties = containerAttributes.getProperties();
        containerProperties.setEtag(BaseResponse.getEtag(request));
        containerProperties.setLastModified(new Date(request.getLastModified()));
        containerAttributes.setMetadata(getMetadata(request));

        containerProperties.setLeaseStatus(getLeaseStatus(request));
        containerProperties.setLeaseState(getLeaseState(request));
        containerProperties.setLeaseDuration(getLeaseDuration(request));

        return containerAttributes;
    }

    /**
     * Gets the copyState
     * 
     * @param request
     *            The response from server.
     * @return The CopyState.
     * @throws URISyntaxException
     * @throws ParseException
     */
    public static CopyState getCopyState(final HttpURLConnection request) throws URISyntaxException, ParseException {
        String copyStatusString = request.getHeaderField(Constants.HeaderConstants.COPY_STATUS);
        if (!Utility.isNullOrEmpty(copyStatusString)) {
            final CopyState copyState = new CopyState();
            
            copyState.setStatus(CopyStatus.parse(copyStatusString));
            copyState.setCopyId(request.getHeaderField(Constants.HeaderConstants.COPY_ID));
            copyState.setStatusDescription(request.getHeaderField(Constants.HeaderConstants.COPY_STATUS_DESCRIPTION));

            final String copyProgressString = request.getHeaderField(Constants.HeaderConstants.COPY_PROGRESS);
            if (!Utility.isNullOrEmpty(copyProgressString)) {
                String[] progressSequence = copyProgressString.split("/");
                copyState.setBytesCopied(Long.parseLong(progressSequence[0]));
                copyState.setTotalBytes(Long.parseLong(progressSequence[1]));
            }

            final String copySourceString = request.getHeaderField(Constants.HeaderConstants.COPY_SOURCE);
            if (!Utility.isNullOrEmpty(copySourceString)) {
                copyState.setSource(new URI(copySourceString));
            }

            final String copyCompletionTimeString =
                    request.getHeaderField(Constants.HeaderConstants.COPY_COMPLETION_TIME);
            if (!Utility.isNullOrEmpty(copyCompletionTimeString)) {
                copyState.setCompletionTime(Utility.parseRFC1123DateFromStringInGMT(copyCompletionTimeString));
            }
            
            return copyState;
        }
        else {
            return null;
        }
    }

    /**
     * Gets the LeaseDuration
     * 
     * @param request
     *            The response from server.
     * @return The LeaseDuration.
     */
    public static LeaseDuration getLeaseDuration(final HttpURLConnection request) {
        final String leaseDuration = request.getHeaderField(Constants.HeaderConstants.LEASE_DURATION);
        if (!Utility.isNullOrEmpty(leaseDuration)) {
            return LeaseDuration.parse(leaseDuration);
        }

        return LeaseDuration.UNSPECIFIED;
    }

    /**
     * Gets the lease id from the request header.
     * 
     * @param request
     *            The response from server.
     * @return the lease id from the request header.
     */
    public static String getLeaseID(final HttpURLConnection request) {
        return request.getHeaderField(Constants.HeaderConstants.LEASE_ID_HEADER);
    }

    /**
     * Gets the LeaseState
     * 
     * @param request
     *            The response from server.
     * @return The LeaseState.
     */
    public static LeaseState getLeaseState(final HttpURLConnection request) {
        final String leaseState = request.getHeaderField(Constants.HeaderConstants.LEASE_STATE);
        if (!Utility.isNullOrEmpty(leaseState)) {
            return LeaseState.parse(leaseState);
        }

        return LeaseState.UNSPECIFIED;
    }

    /**
     * Gets the LeaseStatus
     * 
     * @param request
     *            The response from server.
     * @return The Etag.
     */
    public static LeaseStatus getLeaseStatus(final HttpURLConnection request) {
        final String leaseStatus = request.getHeaderField(Constants.HeaderConstants.LEASE_STATUS);
        if (!Utility.isNullOrEmpty(leaseStatus)) {
            return LeaseStatus.parse(leaseStatus);
        }

        return LeaseStatus.UNSPECIFIED;
    }

    /**
     * Gets the lease Time from the request header.
     * 
     * @param request
     *            The response from server.
     * @return the lease Time from the request header.
     */
    public static String getLeaseTime(final HttpURLConnection request) {
        return request.getHeaderField(Constants.HeaderConstants.LEASE_TIME_HEADER);
    }

    /**
     * Gets the snapshot ID from the request header.
     * 
     * @param request
     *            The response from server.
     * @return the snapshot ID from the request header.
     */
    public static String getSnapshotTime(final HttpURLConnection request) {
        return request.getHeaderField(Constants.HeaderConstants.SNAPSHOT_ID_HEADER);
    }
}
