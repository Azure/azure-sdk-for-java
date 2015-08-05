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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.Constants.HeaderConstants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.BaseRequest;
import com.microsoft.azure.storage.core.ListingContext;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.UriQueryBuilder;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. Provides a set of methods for constructing requests for blob operations.
 */
final class BlobRequest {

    private static final String APPEND_BLOCK_QUERY_ELEMENT_NAME = "appendblock";

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
     * Generates a web request to abort a copy operation.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            The access condition to apply to the request. Only lease conditions are supported for this operation.
     * @param copyId
     *            A <code>String</code> object that identifying the copy operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpURLConnection abortCopy(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final String copyId)
            throws StorageException, IOException, URISyntaxException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.COPY);
        builder.add(Constants.QueryConstants.COPY_ID, copyId);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, blobOptions, builder, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        request.setRequestProperty(Constants.HeaderConstants.COPY_ACTION_HEADER,
                Constants.HeaderConstants.COPY_ACTION_ABORT);

        if (accessCondition != null) {
            accessCondition.applyLeaseConditionToRequest(request);
        }

        return request;
    }

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
     * Adds the properties.
     * 
     * @param request
     *            The request.
     * @param properties
     *            The properties object.
     */
    private static void addProperties(final HttpURLConnection request, BlobProperties properties) {
        BaseRequest.addOptionalHeader(request, Constants.HeaderConstants.CACHE_CONTROL_HEADER,
                properties.getCacheControl());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_DISPOSITION_HEADER,
                properties.getContentDisposition());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_ENCODING_HEADER, properties.getContentEncoding());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_LANGUAGE_HEADER, properties.getContentLanguage());
        BaseRequest.addOptionalHeader(request, BlobConstants.BLOB_CONTENT_MD5_HEADER, properties.getContentMD5());
        BaseRequest.addOptionalHeader(request, BlobConstants.CONTENT_TYPE_HEADER, properties.getContentType());
    }

    /**
     * Adds the snapshot.
     * 
     * @param builder
     *            a query builder.
     * @param snapshotVersion
     *            the snapshot version to the query builder.
     * @throws StorageException
     */
    private static void addSnapshot(final UriQueryBuilder builder, final String snapshotVersion)
            throws StorageException {
        if (snapshotVersion != null) {
            builder.add(Constants.QueryConstants.SNAPSHOT, snapshotVersion);
        }
    }
    
    /**
     * Constructs a web request to commit a block to an append blob.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    public static HttpURLConnection appendBlock(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition) 
                    throws StorageException, IOException, URISyntaxException
    {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, APPEND_BLOCK_QUERY_ELEMENT_NAME);

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
            accessCondition.applyAppendConditionToRequest(request);
        }

        return request;
    }

    /**
     * Creates a request to copy a blob, Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param source
     *            The canonical path to the source blob, in the form /<account-name>/<container-name>/<blob-name>.
     * @param sourceSnapshotID
     *            The snapshot version, if the source blob is a snapshot.
     * @param sourceAccessConditionType
     *            A type of condition to check on the source blob.
     * @param sourceAccessConditionValue
     *            The value of the condition to check on the source blob
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpURLConnection copyFrom(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, String source, final String sourceSnapshotID)
            throws StorageException, IOException, URISyntaxException {

        if (sourceSnapshotID != null) {
            source = source.concat("?snapshot=");
            source = source.concat(sourceSnapshotID);
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, blobOptions, null, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        request.setRequestProperty(Constants.HeaderConstants.COPY_SOURCE_HEADER, source);

        if (sourceAccessCondition != null) {
            sourceAccessCondition.applySourceConditionToRequest(request);
        }

        if (destinationAccessCondition != null) {
            destinationAccessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a web request to create a new container. Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection createContainer(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        return BaseRequest.create(uri, blobOptions, containerBuilder, opContext);
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
    private static HttpURLConnection createURLConnection(final URI uri, final UriQueryBuilder query,
            final BlobRequestOptions blobOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        return BaseRequest.createURLConnection(uri, blobOptions, query, opContext);
    }

    /**
     * Constructs a HttpURLConnection to delete the blob, Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @param deleteSnapshotsOption
     *            A set of options indicating whether to delete only blobs, only snapshots, or both.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection deleteBlob(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final String snapshotVersion,
            final DeleteSnapshotsOption deleteSnapshotsOption) throws IOException, URISyntaxException, StorageException {

        if (snapshotVersion != null && deleteSnapshotsOption != DeleteSnapshotsOption.NONE) {
            throw new IllegalArgumentException(String.format(SR.DELETE_SNAPSHOT_NOT_VALID_ERROR,
                    "deleteSnapshotsOption", "snapshot"));
        }

        final UriQueryBuilder builder = new UriQueryBuilder();
        BlobRequest.addSnapshot(builder, snapshotVersion);
        final HttpURLConnection request = BaseRequest.delete(uri, blobOptions, builder, opContext);

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
     * Constructs a web request to delete the container and all of blobs within it. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection deleteContainer(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        HttpURLConnection request = BaseRequest.delete(uri, blobOptions, containerBuilder, opContext);
        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a web request to return the ACL for this container. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     */
    public static HttpURLConnection getAcl(final URI uri, final BlobRequestOptions blobOptions,
            final AccessCondition accessCondition, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder builder = getContainerUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.ACL);

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null) {
            accessCondition.applyLeaseConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to download the blob, Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @param offset
     *            The offset at which to begin returning content.
     * @param count
     *            The number of bytes to return.
     * @param requestRangeContentMD5
     *            If set to true, request an MD5 header for the specified range.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection getBlob(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final String snapshotVersion,
            final Long offset, final Long count, boolean requestRangeContentMD5) throws IOException,
            URISyntaxException, StorageException {

        if (offset != null && requestRangeContentMD5) {
            Utility.assertNotNull("count", count);
            Utility.assertInBounds("count", count, 1, Constants.MAX_BLOCK_SIZE);
        }

        final UriQueryBuilder builder = new UriQueryBuilder();
        BlobRequest.addSnapshot(builder, snapshotVersion);
        final HttpURLConnection request = BaseRequest.createURLConnection(uri, blobOptions, builder, opContext);
        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

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
     * Constructs a HttpURLConnection to return the blob's system properties, Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection getBlobProperties(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final String snapshotVersion)
            throws StorageException, IOException, URISyntaxException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        BlobRequest.addSnapshot(builder, snapshotVersion);
        return getProperties(uri, blobOptions, opContext, accessCondition, builder);
    }

    /**
     * Constructs a HttpURLConnection to return a list of the block blobs blocks. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @param blockFilter
     *            The types of blocks to include in the list: committed, uncommitted, or both.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection getBlockList(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final String snapshotVersion,
            final BlockListingFilter blockFilter) throws StorageException, IOException, URISyntaxException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        builder.add(Constants.QueryConstants.COMPONENT, BLOCK_LIST_QUERY_ELEMENT_NAME);
        builder.add(BLOCK_LIST_TYPE_QUERY_ELEMENT_NAME, blockFilter.toString());
        BlobRequest.addSnapshot(builder, snapshotVersion);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, blobOptions, builder, opContext);
        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a web request to return the user-defined metadata for this container. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection getContainerProperties(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, AccessCondition accessCondition) throws IOException, URISyntaxException,
            StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        return getProperties(uri, blobOptions, opContext, accessCondition, containerBuilder);
    }

    /**
     * Gets the container Uri query builder.
     * 
     * A <CODE>UriQueryBuilder</CODE> for the container.
     * 
     * @throws StorageException
     */
    private static UriQueryBuilder getContainerUriQueryBuilder() throws StorageException {
        final UriQueryBuilder uriBuilder = new UriQueryBuilder();
        try {
            uriBuilder.add(Constants.QueryConstants.RESOURCETYPE, "container");
        }
        catch (final IllegalArgumentException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
        return uriBuilder;
    }

    /**
     * Constructs a HttpURLConnection to return a list of the PageBlob's page ranges. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param snapshotVersion
     *            The snapshot version, if the blob is a snapshot.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection getPageRanges(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final String snapshotVersion)
            throws StorageException, IOException, URISyntaxException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, PAGE_LIST_QUERY_ELEMENT_NAME);
        BlobRequest.addSnapshot(builder, snapshotVersion);

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);
        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        BaseRequest.addOptionalHeader(request, BlobConstants.SNAPSHOT, snapshotVersion);
        return request;
    }

    /**
     * Constructs a web request to return the user-defined metadata for this container. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    private static HttpURLConnection getProperties(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, AccessCondition accessCondition, final UriQueryBuilder builder)
            throws IOException, URISyntaxException, StorageException {
        HttpURLConnection request = BaseRequest.getProperties(uri, blobOptions, builder, opContext);

        if (accessCondition != null) {
            accessCondition.applyLeaseConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to Acquire,Release,Break, or Renew a blob/container lease. Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param action
     *            the LeaseAction to perform
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     * @param breakPeriodInSeconds
     *            Specifies the amount of time to allow the lease to remain, in seconds.
     *            If null, the break period is the remainder of the current lease, or zero for infinite leases.
     * @param visibilityTimeoutInSeconds
     *            Specifies the the span of time for which to acquire the lease, in seconds.
     *            If null, an infinite lease will be acquired. If not null, this must be greater than zero.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    private static HttpURLConnection lease(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final LeaseAction action,
            final Integer leaseTimeInSeconds, final String proposedLeaseId, final Integer breakPeriodInSeconds,
            final UriQueryBuilder builder) throws IOException, URISyntaxException, StorageException {
        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);
        request.setFixedLengthStreamingMode(0);
        request.setRequestProperty(HeaderConstants.LEASE_ACTION_HEADER, action.toString());

        // Lease duration should only be sent for acquire.
        if (action == LeaseAction.ACQUIRE) {
            // Assert lease duration is in bounds
            if (leaseTimeInSeconds != null && leaseTimeInSeconds != -1) {
                Utility.assertInBounds("leaseTimeInSeconds", leaseTimeInSeconds, Constants.LEASE_DURATION_MIN,
                        Constants.LEASE_DURATION_MAX);
            }

            request.setRequestProperty(HeaderConstants.LEASE_DURATION, leaseTimeInSeconds == null ? "-1"
                    : leaseTimeInSeconds.toString());
        }

        if (proposedLeaseId != null) {
            request.setRequestProperty(HeaderConstants.PROPOSED_LEASE_ID_HEADER, proposedLeaseId);
        }

        if (breakPeriodInSeconds != null) {
            // Assert lease break period is in bounds
            Utility.assertInBounds("breakPeriodInSeconds", breakPeriodInSeconds, Constants.LEASE_BREAK_PERIOD_MIN,
                    Constants.LEASE_BREAK_PERIOD_MAX);
            request.setRequestProperty(HeaderConstants.LEASE_BREAK_PERIOD_HEADER, breakPeriodInSeconds.toString());
        }

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }
        return request;
    }

    /**
     * Constructs a HttpURLConnection to Acquire,Release,Break, or Renew a blob lease. Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param action
     *            the LeaseAction to perform
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     * @param breakPeriodInSeconds
     *            Specifies the amount of time to allow the lease to remain, in seconds.
     *            If null, the break period is the remainder of the current lease, or zero for infinite leases.
     * @param visibilityTimeoutInSeconds
     *            Specifies the the span of time for which to acquire the lease, in seconds.
     *            If null, an infinite lease will be acquired. If not null, this must be greater than zero.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection leaseBlob(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final LeaseAction action,
            final Integer leaseTimeInSeconds, final String proposedLeaseId, final Integer breakPeriodInSeconds)
            throws IOException, URISyntaxException, StorageException {
        UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BlobConstants.LEASE);

        return lease(uri, blobOptions, opContext, accessCondition, action, leaseTimeInSeconds, proposedLeaseId,
                breakPeriodInSeconds, builder);
    }

    /**
     * Constructs a HttpURLConnection to Acquire,Release,Break, or Renew a container lease. Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @param action
     *            the LeaseAction to perform
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     * @param breakPeriodInSeconds
     *            Specifies the amount of time to allow the lease to remain, in seconds.
     *            If null, the break period is the remainder of the current lease, or zero for infinite leases.
     * @param visibilityTimeoutInSeconds
     *            Specifies the the span of time for which to acquire the lease, in seconds.
     *            If null, an infinite lease will be acquired. If not null, this must be greater than zero.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection leaseContainer(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final LeaseAction action,
            final Integer leaseTimeInSeconds, final String proposedLeaseId, final Integer breakPeriodInSeconds)
            throws IOException, URISyntaxException, StorageException {

        final UriQueryBuilder builder = getContainerUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BlobConstants.LEASE);

        return lease(uri, blobOptions, opContext, accessCondition, action, leaseTimeInSeconds, proposedLeaseId,
                breakPeriodInSeconds, builder);
    }

    /**
     * Constructs a HttpURLConnection to list blobs. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param listingContext
     *            A set of parameters for the listing operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     * */
    public static HttpURLConnection listBlobs(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final BlobListingContext listingContext) throws URISyntaxException,
            IOException, StorageException {

        final UriQueryBuilder builder = getContainerUriQueryBuilder();
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

            if (listingContext.getListingDetails() != null && listingContext.getListingDetails().size() > 0) {
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

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        return request;
    }

    /**
     * Constructs a request to return a listing of all containers in this storage account. Sign with no length
     * specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param listingContext
     *            A set of parameters for the listing operation.
     * @param detailsIncluded
     *            Additional details to return with the listing.
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection listContainers(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final ListingContext listingContext,
            final ContainerListingDetails detailsIncluded) throws URISyntaxException, IOException, StorageException {
        final UriQueryBuilder builder = BaseRequest.getListUriQueryBuilder(listingContext);
        
        if (detailsIncluded == ContainerListingDetails.ALL || detailsIncluded == ContainerListingDetails.METADATA) {
            builder.add(Constants.QueryConstants.INCLUDE, Constants.QueryConstants.METADATA);
        }

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        return request;
    }

    /**
     * Constructs a HttpURLConnection to upload a blob. Sign with blob length, or -1 for pageblob create.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param properties
     *            The properties to set for the blob.
     * @param blobType
     *            The type of the blob.
     * @param pageBlobSize
     *            For a page blob, the size of the blob. This parameter is ignored for block blobs.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putBlob(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final BlobProperties properties,
            final BlobType blobType, final long pageBlobSize) throws IOException, URISyntaxException, StorageException {
        if (blobType == BlobType.UNSPECIFIED) {
            throw new IllegalArgumentException(SR.BLOB_TYPE_NOT_DEFINED);
        }

        final HttpURLConnection request = createURLConnection(uri, null, blobOptions, opContext);

        request.setDoOutput(true);

        request.setRequestMethod(Constants.HTTP_PUT);

        addProperties(request, properties);

        if (blobType == BlobType.PAGE_BLOB) {
            request.setFixedLengthStreamingMode(0);
            request.setRequestProperty(Constants.HeaderConstants.CONTENT_LENGTH, "0");

            request.setRequestProperty(BlobConstants.BLOB_TYPE_HEADER, BlobConstants.PAGE_BLOB);
            request.setRequestProperty(BlobConstants.SIZE, String.valueOf(pageBlobSize));

            properties.setLength(pageBlobSize);
        }
        else if (blobType == BlobType.BLOCK_BLOB){
            request.setRequestProperty(BlobConstants.BLOB_TYPE_HEADER, BlobConstants.BLOCK_BLOB);
        }
        else if (blobType == BlobType.APPEND_BLOB){
            request.setFixedLengthStreamingMode(0);
            request.setRequestProperty(BlobConstants.BLOB_TYPE_HEADER, BlobConstants.APPEND_BLOB);
            request.setRequestProperty(Constants.HeaderConstants.CONTENT_LENGTH, "0");
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
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blockId
     *            the Base64 ID for the block
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putBlock(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final String blockId)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BLOCK_QUERY_ELEMENT_NAME);
        builder.add(BLOCK_ID_QUERY_ELEMENT_NAME, blockId);

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to write a blob by specifying the list of block IDs that make up the blob. Sign
     * with length of block list data.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putBlockList(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final BlobProperties properties)
            throws IOException, URISyntaxException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BLOCK_LIST_QUERY_ELEMENT_NAME);

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        addProperties(request, properties);

        return request;
    }

    /**
     * Constructs a HttpURLConnection to upload a page. Sign with page length for update, or 0 for clear.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param pageRange
     *            A {@link PageRange} object that represents the page range.
     * @param operationType
     *            A {@link PageOperationType} object that represents the page range operation type.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putPage(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final PageRange pageRange,
            final PageOperationType operationType) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, PAGE_QUERY_ELEMENT_NAME);

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (operationType == PageOperationType.CLEAR) {
            request.setFixedLengthStreamingMode(0);
        }

        // Page write is either update or clean; required
        request.setRequestProperty(BlobConstants.PAGE_WRITE, operationType.toString());
        request.setRequestProperty(Constants.HeaderConstants.STORAGE_RANGE_HEADER, pageRange.toString());

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
            accessCondition.applySequenceConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to set the page blob's size, Sign with zero length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param newBlobSize
     *            The new blob size, if the blob is a page blob. Set this parameter to null to keep the existing blob
     *            size.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection resize(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final Long newBlobSize)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.PROPERTIES);

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        if (newBlobSize != null) {
            request.setRequestProperty(BlobConstants.SIZE, newBlobSize.toString());
        }

        return request;
    }

    /**
     * Sets the ACL for the container. Sign with length of aclBytes.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @param publicAccess
     *            The type of public access to allow for the container.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setAcl(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition,
            final BlobContainerPublicAccessType publicAccess) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = getContainerUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.ACL);

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setRequestMethod(Constants.HTTP_PUT);
        request.setDoOutput(true);

        if (publicAccess != BlobContainerPublicAccessType.OFF) {
            request.setRequestProperty(BlobConstants.BLOB_PUBLIC_ACCESS_HEADER, publicAccess.toString().toLowerCase());
        }

        if (accessCondition != null) {
            accessCondition.applyLeaseConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to set the blob's metadata, Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection setBlobMetadata(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        return setMetadata(uri, blobOptions, opContext, accessCondition, null);
    }

    /**
     * Constructs a HttpURLConnection to set the blob's properties, Sign with zero length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param properties
     *            The properties to upload.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection setBlobProperties(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final BlobProperties properties)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.PROPERTIES);

        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        if (properties != null) {
            addProperties(request, properties);
        }

        return request;
    }

    /**
     * Constructs a web request to set user-defined metadata for the container, Sign with 0 Length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setContainerMetadata(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        return setMetadata(uri, blobOptions, opContext, accessCondition, containerBuilder);
    }

    /**
     * Constructs a HttpURLConnection to set the blob's metadata, Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    private static HttpURLConnection setMetadata(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final UriQueryBuilder builder)
            throws IOException, URISyntaxException, StorageException {
        final HttpURLConnection request = BaseRequest.setMetadata(uri, blobOptions, builder, opContext);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to create a snapshot of the blob. Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param blobOptions
     *            A {@link BlobRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudBlobClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection snapshot(final URI uri, final BlobRequestOptions blobOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BlobConstants.SNAPSHOT);
        final HttpURLConnection request = createURLConnection(uri, builder, blobOptions, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Private Default Ctor
     */
    private BlobRequest() {
        // No op
    }
}
