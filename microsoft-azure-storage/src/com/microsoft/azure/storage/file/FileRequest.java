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
package com.microsoft.azure.storage.file;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.BaseRequest;
import com.microsoft.azure.storage.core.ListingContext;
import com.microsoft.azure.storage.core.UriQueryBuilder;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. Provides a set of methods for constructing requests for file operations.
 */
final class FileRequest {

    private static final String RANGE_QUERY_ELEMENT_NAME = "range";

    private static final String RANGE_LIST_QUERY_ELEMENT_NAME = "rangelist";

    /**
     * Generates a web request to abort a copy operation.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            The access condition to apply to the request. Only lease conditions are supported for this operation.
     * @param copyId
     *            A <code>String</code> object that identifying the copy operation.
     * @return a <code>HttpURLConnection</code> configured for the operation.
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpURLConnection abortCopy(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final String copyId)
            throws StorageException, IOException, URISyntaxException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.COPY);
        builder.add(Constants.QueryConstants.COPY_ID, copyId);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, builder, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        request.setRequestProperty(Constants.HeaderConstants.COPY_ACTION_HEADER,
                Constants.HeaderConstants.COPY_ACTION_ABORT);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
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
    public static void addMetadata(final HttpURLConnection request, final Map<String, String> metadata,
            final OperationContext opContext) {
        BaseRequest.addMetadata(request, metadata, opContext);
    }

    /**
     * Adds the properties.
     * 
     * @param request
     *            The request
     * @param properties
     *            The file properties
     */
    private static void addProperties(final HttpURLConnection request, FileProperties properties) {
        BaseRequest.addOptionalHeader(request, FileConstants.CACHE_CONTROL_HEADER, properties.getCacheControl());
        BaseRequest.addOptionalHeader(request, FileConstants.CONTENT_DISPOSITION_HEADER,
                properties.getContentDisposition());
        BaseRequest.addOptionalHeader(request, FileConstants.CONTENT_ENCODING_HEADER, properties.getContentEncoding());
        BaseRequest.addOptionalHeader(request, FileConstants.CONTENT_LANGUAGE_HEADER, properties.getContentLanguage());
        BaseRequest.addOptionalHeader(request, FileConstants.FILE_CONTENT_MD5_HEADER, properties.getContentMD5());
        BaseRequest.addOptionalHeader(request, FileConstants.CONTENT_TYPE_HEADER, properties.getContentType());
    }

    /**
     * Creates a request to copy a file, Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param source
     *            The canonical path to the source file,
     *            in the form /<account-name>/<share-name>/<directory-path>/<file-name>.
     * @param sourceAccessConditionType
     *            A type of condition to check on the source file.
     * @param sourceAccessConditionValue
     *            The value of the condition to check on the source file
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpURLConnection copyFrom(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, String source)
            throws StorageException, IOException, URISyntaxException {

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, null, opContext);

        request.setFixedLengthStreamingMode(0);
        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        request.setRequestProperty(Constants.HeaderConstants.COPY_SOURCE_HEADER, source);

        if (sourceAccessCondition != null) {
            sourceAccessCondition.applyConditionToRequest(request);
        }

        if (destinationAccessCondition != null) {
            destinationAccessCondition.applyConditionToRequest(request);
        }

        return request;
    }    

    /**
     * Adds the properties.
     * 
     * @param request
     *            The request
     * @param properties
     *            The share properties
     */
    private static void addProperties(final HttpURLConnection request, FileShareProperties properties) {
        final Integer shareQuota = properties.getShareQuota();
        BaseRequest.addOptionalHeader(
                request, FileConstants.SHARE_QUOTA_HEADER, shareQuota == null ? null : shareQuota.toString());
    }

    /**
     * Constructs a web request to create a new share. Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param properties
     *            The properties to set for the share.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection createShare(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final FileShareProperties properties)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder shareBuilder = getShareUriQueryBuilder();
        final HttpURLConnection request = BaseRequest.create(uri, fileOptions, shareBuilder, opContext);
        addProperties(request, properties);
        return request;
    }

    /**
     * Constructs a HttpURLConnection to delete the file, Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection deleteFile(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        final HttpURLConnection request = BaseRequest.delete(uri, fileOptions, builder, opContext);
        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a web request to delete the share and all of the directories and files within it. Sign with no length
     * specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection deleteShare(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder shareBuilder = getShareUriQueryBuilder();
        HttpURLConnection request = BaseRequest.delete(uri, fileOptions, shareBuilder, opContext);
        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a web request to return the ACL for this share. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the share.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     */
    public static HttpURLConnection getAcl(final URI uri, final FileRequestOptions fileOptions,
            final AccessCondition accessCondition, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder builder = getShareUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.ACL);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, builder, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null && !Utility.isNullOrEmpty(accessCondition.getLeaseID())) {
            accessCondition.applyLeaseConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to download the file, Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
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
    public static HttpURLConnection getFile(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final Long offset,
            final Long count, boolean requestRangeContentMD5) throws IOException, URISyntaxException, StorageException {

        if (offset != null && requestRangeContentMD5) {
            Utility.assertNotNull("count", count);
            Utility.assertInBounds("count", count, 1, Constants.MAX_BLOCK_SIZE);
        }

        final UriQueryBuilder builder = new UriQueryBuilder();
        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, builder, opContext);
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
     * Constructs a HttpURLConnection to return the file's system properties, Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection getFileProperties(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws StorageException,
            IOException, URISyntaxException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        return getProperties(uri, fileOptions, opContext, accessCondition, builder);
    }

    /**
     * Constructs a HttpURLConnection to return a list of the file's file ranges. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection getFileRanges(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws StorageException,
            IOException, URISyntaxException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, RANGE_LIST_QUERY_ELEMENT_NAME);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, builder, opContext);
        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a web request to return the user-defined metadata for this share. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection getShareProperties(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, AccessCondition accessCondition) throws IOException, URISyntaxException,
            StorageException {
        final UriQueryBuilder shareBuilder = getShareUriQueryBuilder();
        return getProperties(uri, fileOptions, opContext, accessCondition, shareBuilder);
    }

    /**
     * Constructs a web request to return the stats, such as usage, for this share. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param options
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     */
    public static HttpURLConnection getShareStats(final URI uri, final FileRequestOptions options,
            final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        UriQueryBuilder shareBuilder = getShareUriQueryBuilder();
        shareBuilder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.STATS);

        final HttpURLConnection retConnection = BaseRequest.createURLConnection(uri, options, shareBuilder, opContext);
        retConnection.setRequestMethod(Constants.HTTP_GET);

        return retConnection;
    }

    /**
     * Gets the share Uri query builder.
     * 
     * A <CODE>UriQueryBuilder</CODE> for the share.
     * 
     * @throws StorageException
     */
    private static UriQueryBuilder getShareUriQueryBuilder() throws StorageException {
        final UriQueryBuilder uriBuilder = new UriQueryBuilder();
        try {
            uriBuilder.add(Constants.QueryConstants.RESOURCETYPE, "share");
        }
        catch (final IllegalArgumentException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
        return uriBuilder;
    }

    /**
     * Gets the share Uri query builder.
     * 
     * A <CODE>UriQueryBuilder</CODE> for the share.
     * 
     * @throws StorageException
     */
    private static UriQueryBuilder getDirectoryUriQueryBuilder() throws StorageException {
        final UriQueryBuilder uriBuilder = new UriQueryBuilder();
        try {
            uriBuilder.add(Constants.QueryConstants.RESOURCETYPE, "directory");
        }
        catch (final IllegalArgumentException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
        return uriBuilder;
    }

    /**
     * Constructs a web request to return the user-defined metadata. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    private static HttpURLConnection getProperties(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, AccessCondition accessCondition, final UriQueryBuilder builder)
            throws IOException, URISyntaxException, StorageException {
        HttpURLConnection request = BaseRequest.getProperties(uri, fileOptions, builder, opContext);

        if (accessCondition != null) {
            accessCondition.applyLeaseConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a request to return a listing of all shares in this storage account. Sign with no length
     * specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
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
    public static HttpURLConnection listShares(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final ListingContext listingContext,
            final ShareListingDetails detailsIncluded) throws URISyntaxException, IOException, StorageException {

        final UriQueryBuilder builder = BaseRequest.getListUriQueryBuilder(listingContext);

        if (detailsIncluded == ShareListingDetails.ALL || detailsIncluded == ShareListingDetails.METADATA) {
            builder.add(Constants.QueryConstants.INCLUDE, Constants.QueryConstants.METADATA);
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, builder, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        return request;
    }
    
    /**
     * Constructs a web request to set user-defined metadata for the share, Sign with 0 Length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setShareMetadata(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder shareBuilder = getShareUriQueryBuilder();
        return setMetadata(uri, fileOptions, opContext, accessCondition, shareBuilder);
    }

    /**
     * Constructs a web request to set user-defined metadata for the directory, Sign with 0 Length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the directory.
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * */
    public static HttpURLConnection setDirectoryMetadata(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder directoryBuilder = getDirectoryUriQueryBuilder();
        return setMetadata(uri, fileOptions, opContext, accessCondition, directoryBuilder);
    }

    /**
     * Constructs a web request to create a new directory. Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection createDirectory(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder directoryBuilder = getDirectoryUriQueryBuilder();
        return BaseRequest.create(uri, fileOptions, directoryBuilder, opContext);
    }

    /**
     * Constructs a web request to delete the directory and all of the directories and files within it. Sign with no
     * length
     * specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the directory.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection deleteDirectory(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder directoryBuilder = getDirectoryUriQueryBuilder();
        HttpURLConnection request = BaseRequest.delete(uri, fileOptions, directoryBuilder, opContext);
        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a web request to return the properties for this directory. Sign with no length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the directory.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection getDirectoryProperties(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, AccessCondition accessCondition) throws IOException, URISyntaxException,
            StorageException {
        final UriQueryBuilder directoryBuilder = getDirectoryUriQueryBuilder();
        return getProperties(uri, fileOptions, opContext, accessCondition, directoryBuilder);
    }

    /**
     * Constructs a request to return a listing of all files and directories in this storage account. Sign with no
     * length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param listingContext
     *            A set of parameters for the listing operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection listFilesAndDirectories(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final ListingContext listingContext) throws URISyntaxException,
            IOException, StorageException {

        final UriQueryBuilder builder = getDirectoryUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.LIST);

        if (listingContext != null) {
            if (!Utility.isNullOrEmpty(listingContext.getMarker())) {
                builder.add(Constants.QueryConstants.MARKER, listingContext.getMarker());
            }

            if (listingContext.getMaxResults() != null && listingContext.getMaxResults() > 0) {
                builder.add(Constants.QueryConstants.MAX_RESULTS, listingContext.getMaxResults().toString());
            }
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, builder, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        return request;
    }

    /**
     * Constructs a HttpURLConnection to upload a file.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param properties
     *            The properties to set for the file.
     * @param fileSize
     *            The size of the file.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putFile(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final FileProperties properties,
            final long fileSize) throws IOException, URISyntaxException, StorageException {
        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, null, opContext);

        request.setDoOutput(true);

        request.setRequestMethod(Constants.HTTP_PUT);

        addProperties(request, properties);

        request.setFixedLengthStreamingMode(0);
        request.setRequestProperty(Constants.HeaderConstants.CONTENT_LENGTH, "0");

        request.setRequestProperty(FileConstants.FILE_TYPE_HEADER, FileConstants.FILE);
        request.setRequestProperty(FileConstants.CONTENT_LENGTH_HEADER, String.valueOf(fileSize));

        properties.setLength(fileSize);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to upload a file range. Sign with file size for update, or 0 for clear.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param range
     *            a {link @FileRange} representing the file range
     * @param operationType
     *            a {link @FileRangeOperationType} enumeration value representing the file range operation type.
     * 
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putRange(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final FileRange range,
            FileRangeOperationType operationType) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, RANGE_QUERY_ELEMENT_NAME);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, builder, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        if (operationType == FileRangeOperationType.CLEAR) {
            request.setFixedLengthStreamingMode(0);
        }

        // Range write is either update or clear; required
        request.setRequestProperty(FileConstants.FILE_RANGE_WRITE, operationType.toString());
        request.setRequestProperty(Constants.HeaderConstants.STORAGE_RANGE_HEADER, range.toString());

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to set the share's properties, signed with zero length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param options
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
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
    public static HttpURLConnection setShareProperties(final URI uri, final FileRequestOptions options,
            final OperationContext opContext, final AccessCondition accessCondition, final FileShareProperties properties)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = getShareUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.PROPERTIES);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, options, builder, opContext);

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
     * Constructs a HttpURLConnection to set the file's size, Sign with zero length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param newFileSize
     *            The new file size. Set this parameter to null to keep the existing file size.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection resize(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final Long newFileSize)
            throws IOException, URISyntaxException, StorageException {
        final HttpURLConnection request = setFileProperties(uri, fileOptions, opContext, accessCondition, null);

        if (newFileSize != null) {
            request.setRequestProperty(FileConstants.CONTENT_LENGTH_HEADER, newFileSize.toString());
        }

        return request;
    }

    /**
     * Sets the ACL for the share. Sign with length of aclBytes.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the share.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setAcl(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = getShareUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.ACL);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, builder, opContext);

        request.setRequestMethod(Constants.HTTP_PUT);
        request.setDoOutput(true);

        if (accessCondition != null && !Utility.isNullOrEmpty(accessCondition.getLeaseID())) {
            accessCondition.applyLeaseConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to set metadata, Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    private static HttpURLConnection setMetadata(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final UriQueryBuilder builder)
            throws IOException, URISyntaxException, StorageException {
        final HttpURLConnection request = BaseRequest.setMetadata(uri, fileOptions, builder, opContext);

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to set the file's metadata, Sign with 0 length.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection setFileMetadata(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition) throws IOException,
            URISyntaxException, StorageException {
        return setMetadata(uri, fileOptions, opContext, accessCondition, null);
    }

    /**
     * Constructs a HttpURLConnection to set the file's properties, Sign with zero length specified.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that specifies the absolute URI.
     * @param fileOptions
     *            A {@link FileRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudFileClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
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
    public static HttpURLConnection setFileProperties(final URI uri, final FileRequestOptions fileOptions,
            final OperationContext opContext, final AccessCondition accessCondition, final FileProperties properties)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.PROPERTIES);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, fileOptions, builder, opContext);

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
     * Private Default Ctor
     */
    private FileRequest() {
        // No op
    }
}
