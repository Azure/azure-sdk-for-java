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
package com.microsoft.windowsazure.storage.blob;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.windowsazure.storage.AccessCondition;
import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.Constants.HeaderConstants;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.core.BaseRequest;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.UriQueryBuilder;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. Provides a set of methods for constructing requests for blob operations.
 */
final class BlobRequest {

    private static final String BLOCK_QUERY_ELEMENT_NAME = "block";

    private static final String BLOCK_ID_QUERY_ELEMENT_NAME = "blockid";

    private static final String BLOCK_LIST_QUERY_ELEMENT_NAME = "blocklist";

    private static final String BLOCK_LIST_TYPE_QUERY_ELEMENT_NAME = "blocklisttype";

    private static final String COPY_QUERY_ELEMENT_NAME = "copy";

    private static final String PAGE_QUERY_ELEMENT_NAME = "page";

    private static final String PAGE_LIST_QUERY_ELEMENT_NAME = "pagelist";

    private static final String SNAPSHOTS_QUERY_ELEMENT_NAME = "snapshots";

    private static final String UNCOMMITTED_BLOBS_QUERY_ELEMENT_NAME = "uncommittedblobs";

    /**
     * Adds the metadata.
     * 
     * @param request
     *            The request.
     * @param metadata
     *            The metadata.
     */
    public static void addMetadata(final HttpURLConnection request, final HashMap<String, String> metadata,
            final OperationContext opContext) {
        BaseRequest.addMetadata(request, metadata, opContext);
    }

    /**
     * Adds the metadata.
     * 
     * @param request
     *            The request.
     * @param name
     *            The metadata name.
     * @param value
     *            The metadata value.
     */
    public static void addMetadata(final HttpURLConnection request, final String name, final String value,
            final OperationContext opContext) {
        BaseRequest.addMetadata(request, name, value, opContext);
    }

    /**
     * Creates a request to copy a blob, Sign with 0 length.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @param source
     *            The canonical path to the source blob, in the form /<account-name>/<container-name>/<blob-name>.
     * @param sourceSnapshotID
     *            The snapshot version, if the source blob is a snapshot.
     * @param sourceAccessConditionType
     *            A type of condition to check on the source blob.
     * @param sourceAccessConditionValue
     *            The value of the condition to check on the source blob
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpURLConnection copyFrom(final URI uri, final int timeout, String source,
            final String sourceSnapshotID, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws StorageException, IOException, URISyntaxException {

        if (sourceSnapshotID != null) {
            source = source.concat("?snapshot=");
            source = source.concat(sourceSnapshotID);
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, null, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        request.setRequestProperty(Constants.HeaderConstants.COPY_SOURCE_HEADER, source);

        if (sourceAccessCondition != null) {
            sourceAccessCondition.applyConditionToRequest(request, true);
        }

        if (destinationAccessCondition != null) {
            destinationAccessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Generates a web request to abort a copy operation.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @param copyId
     *            A <code>String</code> object that identifying the copy operation.
     * @param accessCondition
     *            The access condition to apply to the request. Only lease conditions are supported for this operation.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpURLConnection abortCopy(final URI uri, final int timeout, final String copyId,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws StorageException, IOException, URISyntaxException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.COPY);
        builder.add(Constants.QueryConstants.COPY_ID, copyId);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        request.setRequestProperty(Constants.HeaderConstants.COPY_ACTION_HEADER,
                Constants.HeaderConstants.COPY_ACTION_ABORT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request, true);
        }

        return request;
    }

    /**
     * Creates the web request.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @param query
     *            The query builder to use.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    private static HttpURLConnection createURLConnection(final URI uri, final int timeout, final UriQueryBuilder query,
            final BlobRequestOptions blobOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        return BaseRequest.createURLConnection(uri, timeout, query, opContext);
    }

    /**
     * Constructs a HttpURLConnection to delete the blob, Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @param deleteSnapshotsOption
     *            A set of options indicating whether to delete only blobs, only snapshots, or both.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection delete(final URI uri, final int timeout, final String snapshotVersion,
            final DeleteSnapshotsOption deleteSnapshotsOption, final AccessCondition accessCondition,
            final BlobRequestOptions blobOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {

        if (snapshotVersion != null && deleteSnapshotsOption != DeleteSnapshotsOption.NONE) {
            throw new IllegalArgumentException(String.format(SR.DELETE_SNAPSHOT_NOT_VALID_ERROR,
                    "deleteSnapshotsOption", "snapshot"));
        }

        final UriQueryBuilder builder = new UriQueryBuilder();
        BaseRequest.addSnapshot(builder, snapshotVersion);
        final HttpURLConnection request = BaseRequest.delete(uri, timeout, builder, opContext);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        switch (deleteSnapshotsOption) {
            case NONE:
                // nop
                break;
            case INCLUDE_SNAPSHOTS:
                request.setRequestProperty(Constants.HeaderConstants.DELETE_SNAPSHOT_HEADER,
                        BlobConstants.INCLUDE_SNAPSHOTS_VALUE);
                break;
            case DELETE_SNAPSHOTS_ONLY:
                request.setRequestProperty(Constants.HeaderConstants.DELETE_SNAPSHOT_HEADER,
                        BlobConstants.SNAPSHOTS_ONLY_VALUE);
                break;
            default:
                break;
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to download the blob, Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @param offset
     *            The offset at which to begin returning content.
     * @param count
     *            The number of bytes to return.
     * @param requestRangeContentMD5
     *            If set to true, request an MD5 header for the specified range.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection get(final URI uri, final int timeout, final String snapshotVersion,
            final Long offset, final Long count, boolean requestRangeContentMD5, final AccessCondition accessCondition,
            final BlobRequestOptions blobOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {

        if (offset != null && requestRangeContentMD5) {
            Utility.assertNotNull("count", count);
            Utility.assertInBounds("count", count, 1, Constants.MAX_BLOCK_SIZE);
        }

        final HttpURLConnection request = get(uri, timeout, snapshotVersion, accessCondition, blobOptions, opContext);

        if (offset != null) {
            long rangeStart = offset;
            long rangeEnd;
            if (count != null) {
                rangeEnd = offset + count - 1;
                request.setRequestProperty(Constants.HeaderConstants.STORAGE_RANGE_HEADER, String.format(
                        Utility.LOCALE_US, Constants.HeaderConstants.RANGE_HEADER_FORMAT, rangeStart, rangeEnd));
            }
            else {
                request.setRequestProperty(Constants.HeaderConstants.STORAGE_RANGE_HEADER, String.format(
                        Utility.LOCALE_US, Constants.HeaderConstants.BEGIN_RANGE_HEADER_FORMAT, rangeStart));
            }
        }

        if (offset != null && requestRangeContentMD5) {
            request.setRequestProperty(Constants.HeaderConstants.RANGE_GET_CONTENT_MD5, Constants.TRUE);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to download the blob, Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection get(final URI uri, final int timeout, final String snapshotVersion,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        BaseRequest.addSnapshot(builder, snapshotVersion);
        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);
        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to return a list of the block blobs blocks. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @param blockFilter
     *            The types of blocks to include in the list: committed, uncommitted, or both.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection getBlockList(final URI uri, final int timeout, final String snapshotVersion,
            final BlockListingFilter blockFilter, final AccessCondition accessCondition,
            final BlobRequestOptions blobOptions, final OperationContext opContext) throws StorageException,
            IOException, URISyntaxException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        builder.add(Constants.QueryConstants.COMPONENT, BLOCK_LIST_QUERY_ELEMENT_NAME);
        builder.add(BLOCK_LIST_TYPE_QUERY_ELEMENT_NAME, blockFilter.toString());
        BaseRequest.addSnapshot(builder, snapshotVersion);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);
        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to return a list of the PageBlob's page ranges. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection getPageRanges(final URI uri, final int timeout, final String snapshotVersion,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws StorageException, IOException, URISyntaxException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, PAGE_LIST_QUERY_ELEMENT_NAME);
        BaseRequest.addSnapshot(builder, snapshotVersion);

        final HttpURLConnection request = BlobRequest
                .createURLConnection(uri, timeout, builder, blobOptions, opContext);
        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        BaseRequest.addOptionalHeader(request, BlobConstants.SNAPSHOT, snapshotVersion);
        return request;
    }

    /**
     * Constructs a HttpURLConnection to return the blob's system properties, Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection getProperties(final URI uri, final int timeout, final String snapshotVersion,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws StorageException, IOException, URISyntaxException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        BaseRequest.addSnapshot(builder, snapshotVersion);
        final HttpURLConnection request = BaseRequest.getProperties(uri, timeout, builder, opContext);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to Acquire,Release,Break, or Renew a blob lease. Sign with 0 length.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param action
     *            the LeaseAction to perform
     * 
     * @param visibilityTimeoutInSeconds
     *            Specifies the the span of time for which to acquire the lease, in seconds.
     *            If null, an infinite lease will be acquired. If not null, this must be greater than zero.
     * 
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     * 
     * @param breakPeriodInSeconds
     *            Specifies the amount of time to allow the lease to remain, in seconds.
     *            If null, the break period is the remainder of the current lease, or zero for infinite leases.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection lease(final URI uri, final int timeout, final LeaseAction action,
            final Integer leaseTimeInSeconds, final String proposedLeaseId, final Integer breakPeriodInSeconds,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BlobConstants.LEASE);

        final HttpURLConnection request = BlobRequest
                .createURLConnection(uri, timeout, builder, blobOptions, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);
        request.setFixedLengthStreamingMode(0);
        request.setRequestProperty(HeaderConstants.LEASE_ACTION_HEADER, action.toString());

        request.setRequestProperty(HeaderConstants.LEASE_DURATION, leaseTimeInSeconds == null ? "-1"
                : leaseTimeInSeconds.toString());

        if (proposedLeaseId != null) {
            request.setRequestProperty(HeaderConstants.PROPOSED_LEASE_ID_HEADER, proposedLeaseId);
        }

        if (breakPeriodInSeconds != null) {
            request.setRequestProperty(HeaderConstants.LEASE_BREAK_PERIOD_HEADER, breakPeriodInSeconds.toString());
        }

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }
        return request;
    }

    /**
     * Constructs a HttpURLConnection to list blobs. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param listingContext
     *            A set of parameters for the listing operation.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     * */
    public static HttpURLConnection list(final URI uri, final int timeout, final BlobListingContext listingContext,
            final BlobRequestOptions blobOptions, final OperationContext opContext) throws URISyntaxException,
            IOException, StorageException {

        final UriQueryBuilder builder = ContainerRequest.getContainerUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.LIST);

        if (listingContext != null) {
            if (!Utility.isNullOrEmpty(listingContext.getPrefix())) {
                builder.add(Constants.QueryConstants.PREFIX, listingContext.getPrefix());
            }

            if (!Utility.isNullOrEmpty(listingContext.getDelimiter())) {
                builder.add(Constants.QueryConstants.DELIMITER, listingContext.getDelimiter());
            }

            if (!Utility.isNullOrEmpty(listingContext.getMarker())) {
                builder.add(Constants.QueryConstants.MARKER, listingContext.getMarker());
            }

            if (listingContext.getMaxResults() != null && listingContext.getMaxResults() > 0) {
                builder.add(Constants.QueryConstants.MAX_RESULTS, listingContext.getMaxResults().toString());
            }

            if (listingContext.getListingDetails().size() > 0) {
                final StringBuilder sb = new StringBuilder();

                boolean started = false;

                if (listingContext.getListingDetails().contains(BlobListingDetails.SNAPSHOTS)) {
                    if (!started) {
                        started = true;
                    }
                    else {
                        sb.append(",");
                    }

                    sb.append(SNAPSHOTS_QUERY_ELEMENT_NAME);
                }

                if (listingContext.getListingDetails().contains(BlobListingDetails.UNCOMMITTED_BLOBS)) {
                    if (!started) {
                        started = true;
                    }
                    else {
                        sb.append(",");
                    }

                    sb.append(UNCOMMITTED_BLOBS_QUERY_ELEMENT_NAME);
                }

                if (listingContext.getListingDetails().contains(BlobListingDetails.COPY)) {
                    if (!started) {
                        started = true;
                    }
                    else {
                        sb.append(",");
                    }

                    sb.append(COPY_QUERY_ELEMENT_NAME);
                }

                if (listingContext.getListingDetails().contains(BlobListingDetails.METADATA)) {
                    if (!started) {
                        started = true;
                    }
                    else {
                        sb.append(",");
                    }

                    sb.append(Constants.QueryConstants.METADATA);
                }

                builder.add(Constants.QueryConstants.INCLUDE, sb.toString());
            }
        }

        final HttpURLConnection request = BlobRequest
                .createURLConnection(uri, timeout, builder, blobOptions, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        return request;
    }

    /**
     * Constructs a HttpURLConnection to upload a blob. Sign with blob length, or -1 for pageblob create.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param properties
     *            The properties to set for the blob.
     * @param blobType
     *            The type of the blob.
     * @param pageBlobSize
     *            For a page blob, the size of the blob. This parameter is ignored for block blobs.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection put(final URI uri, final int timeout, final BlobProperties properties,
            final BlobType blobType, final long pageBlobSize, final AccessCondition accessCondition,
            final BlobRequestOptions blobOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        if (blobType == BlobType.UNSPECIFIED) {
            throw new IllegalArgumentException(SR.BLOB_TYPE_NOT_DEFINED);
        }

        final HttpURLConnection request = BlobRequest.createURLConnection(uri, timeout, null, blobOptions, opContext);

        request.setDoOutput(true);

        request.setRequestMethod(Constants.HTTP_PUT);

        // use set optional header
        BaseRequest.addOptionalHeader(request, Constants.HeaderConstants.CACHE_CONTROL, properties.getCacheControl());
        BaseRequest.addOptionalHeader(request, Constants.HeaderConstants.CONTENT_TYPE, properties.getContentType());
        BaseRequest.addOptionalHeader(request, Constants.HeaderConstants.CONTENT_MD5, properties.getContentMD5());
        BaseRequest.addOptionalHeader(request, Constants.HeaderConstants.CONTENT_LANGUAGE,
                properties.getContentLanguage());
        BaseRequest.addOptionalHeader(request, Constants.HeaderConstants.CONTENT_ENCODING,
                properties.getContentEncoding());

        if (blobType == BlobType.PAGE_BLOB) {
            request.setFixedLengthStreamingMode(0);
            request.setRequestProperty(Constants.HeaderConstants.CONTENT_LENGTH, "0");

            request.setRequestProperty(BlobConstants.BLOB_TYPE_HEADER, BlobConstants.PAGE_BLOB);
            request.setRequestProperty(BlobConstants.SIZE, String.valueOf(pageBlobSize));

            properties.setLength(pageBlobSize);
        }
        else {
            request.setRequestProperty(BlobConstants.BLOB_TYPE_HEADER, BlobConstants.BLOCK_BLOB);
        }

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to upload a block. Sign with length of block data.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param blockId
     *            the Base64 ID for the block
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putBlock(final URI uri, final int timeout, final String blockId,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BLOCK_QUERY_ELEMENT_NAME);
        builder.add(BLOCK_ID_QUERY_ELEMENT_NAME, blockId);

        final HttpURLConnection request = BlobRequest
                .createURLConnection(uri, timeout, builder, blobOptions, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to set the blob's properties, Sign with zero length specified.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putBlockList(final URI uri, final int timeout, final BlobProperties properties,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BLOCK_LIST_QUERY_ELEMENT_NAME);

        final HttpURLConnection request = BlobRequest
                .createURLConnection(uri, timeout, builder, blobOptions, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        BaseRequest.addOptionalHeader(request, Constants.HeaderConstants.CACHE_CONTROL_HEADER,
                properties.getCacheControl());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_ENCODING_HEADER, properties.getContentEncoding());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_LANGUAGE_HEADER, properties.getContentLanguage());
        BaseRequest.addOptionalHeader(request, BlobConstants.BLOB_CONTENT_MD5_HEADER, properties.getContentMD5());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_TYPE_HEADER, properties.getContentType());

        return request;
    }

    /**
     * Constructs a HttpURLConnection to upload a block. Sign with page length for update, or 0 for clear.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param properties
     *            the page properties
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putPage(final URI uri, final int timeout, final PageProperties properties,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, PAGE_QUERY_ELEMENT_NAME);

        final HttpURLConnection request = BlobRequest
                .createURLConnection(uri, timeout, builder, blobOptions, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (properties.getPageOperation() == PageOperationType.CLEAR) {
            request.setFixedLengthStreamingMode(0);
        }

        // Page write is either update or clean; required
        request.setRequestProperty(BlobConstants.PAGE_WRITE, properties.getPageOperation().toString());
        request.setRequestProperty(Constants.HeaderConstants.STORAGE_RANGE_HEADER, properties.getRange().toString());

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to set the blob's metadata, Sign with 0 length.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection setMetadata(final URI uri, final int timeout,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final HttpURLConnection request = BaseRequest.setMetadata(uri, timeout, null, opContext);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to set the blob's properties, Sign with zero length specified.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param newBlobSize
     *            The new blob size, if the blob is a page blob. Set this parameter to null to keep the existing blob
     *            size.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection setProperties(final URI uri, final int timeout, final BlobProperties properties,
            final Long newBlobSize, final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.PROPERTIES);

        final HttpURLConnection request = BlobRequest
                .createURLConnection(uri, timeout, builder, blobOptions, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        if (newBlobSize != null) {
            request.setRequestProperty(BlobConstants.SIZE, newBlobSize.toString());
            properties.setLength(newBlobSize);
        }

        BaseRequest.addOptionalHeader(request, Constants.HeaderConstants.CACHE_CONTROL_HEADER,
                properties.getCacheControl());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_ENCODING_HEADER, properties.getContentEncoding());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_LANGUAGE_HEADER, properties.getContentLanguage());
        BaseRequest.addOptionalHeader(request, BlobConstants.BLOB_CONTENT_MD5_HEADER, properties.getContentMD5());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_TYPE_HEADER, properties.getContentType());

        return request;
    }

    /**
     * Constructs a HttpURLConnection to create a snapshot of the blob. Sign with 0 length.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection snapshot(final URI uri, final int timeout, final AccessCondition accessCondition,
            final BlobRequestOptions blobOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BlobConstants.SNAPSHOT);
        final HttpURLConnection request = BlobRequest
                .createURLConnection(uri, timeout, builder, blobOptions, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Writes a Block List and returns the corresponding UTF8 bytes.
     * 
     * @param blockList
     *            the Iterable of BlockEntry to write
     * @param opContext
     *            a tracking object for the request
     * @return a byte array of the UTF8 bytes representing the serialized block list.
     * @throws XMLStreamException
     *             if there is an error writing the block list.
     * @throws StorageException
     */
    public static byte[] writeBlockListToStream(final Iterable<BlockEntry> blockList, final OperationContext opContext)
            throws XMLStreamException, StorageException {

        final StringWriter outWriter = new StringWriter();
        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outWriter);

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(BlobConstants.BLOCK_LIST_ELEMENT);

        for (final BlockEntry block : blockList) {
            if (block.searchMode == BlockSearchMode.COMMITTED) {
                xmlw.writeStartElement(BlobConstants.COMMITTED_ELEMENT);
            }
            else if (block.searchMode == BlockSearchMode.UNCOMMITTED) {
                xmlw.writeStartElement(BlobConstants.UNCOMMITTED_ELEMENT);
            }
            else if (block.searchMode == BlockSearchMode.LATEST) {
                xmlw.writeStartElement(BlobConstants.LATEST_ELEMENT);
            }

            xmlw.writeCharacters(block.getId());
            xmlw.writeEndElement();
        }

        // end BlockListElement
        xmlw.writeEndElement();

        // end doc
        xmlw.writeEndDocument();
        try {
            return outWriter.toString().getBytes("UTF8");
        }
        catch (final UnsupportedEncodingException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }

    /**
     * Private Default Ctor
     */
    private BlobRequest() {
        // No op
    }
}
