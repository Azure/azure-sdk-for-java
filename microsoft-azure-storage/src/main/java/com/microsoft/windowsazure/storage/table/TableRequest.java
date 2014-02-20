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

package com.microsoft.windowsazure.storage.table;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.ResultContinuation;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.core.BaseRequest;
import com.microsoft.windowsazure.storage.core.PathUtility;
import com.microsoft.windowsazure.storage.core.UriQueryBuilder;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Reserved for internal use. A class used to generate requests for Table objects.
 */
final class TableRequest {
    /**
     * Reserved for internal use. Adds continuation token values to the specified query builder, if set.
     * 
     * @param builder
     *            The {@link UriQueryBuilder} object to apply the continuation token properties to.
     * @param continuationToken
     *            The {@link ResultContinuation} object containing the continuation token values to apply to the query
     *            builder. Specify <code>null</code> if no continuation token values are set.
     * 
     * @throws StorageException
     *             if an error occurs in accessing the query builder or continuation token.
     */
    private static void applyContinuationToQueryBuilder(final UriQueryBuilder builder,
            final ResultContinuation continuationToken) throws StorageException {
        if (continuationToken != null) {
            if (continuationToken.getNextPartitionKey() != null) {
                builder.add(TableConstants.TABLE_SERVICE_NEXT_PARTITION_KEY, continuationToken.getNextPartitionKey());
            }

            if (continuationToken.getNextRowKey() != null) {
                builder.add(TableConstants.TABLE_SERVICE_NEXT_ROW_KEY, continuationToken.getNextRowKey());
            }

            if (continuationToken.getNextTableName() != null) {
                builder.add(TableConstants.TABLE_SERVICE_NEXT_TABLE_NAME, continuationToken.getNextTableName());
            }
        }
    }

    /**
     * Reserved for internal use. Constructs an <code>HttpURLConnection</code> to perform a table batch operation.
     * 
     * @param rootUri
     *            A <code>java.net.URI</code> containing an absolute URI to the resource.
     * @param timeoutInMs
     *            The server timeout interval in milliseconds.
     * @param batchID
     *            The <code>String</code> containing the batch identifier.
     * @param queryBuilder
     *            The {@link UriQueryBuilder} for the operation.
     * @param tableOptions
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. This parameter is unused.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         An <code>HttpURLConnection</code> to use to perform the operation.
     * 
     * @throws IOException
     *             if there is an error opening the connection.
     * @throws URISyntaxException
     *             if the resource URI is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    public static HttpURLConnection batch(final URI rootUri, final int timeoutInMs, final String batchID,
            final UriQueryBuilder queryBuilder, final TableRequestOptions tableOptions, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final URI queryUri = PathUtility.appendPathToSingleUri(rootUri, "$batch");

        final HttpURLConnection retConnection = BaseRequest.createURLConnection(queryUri, timeoutInMs, queryBuilder,
                opContext);

        setAcceptHeaderForHttpWebRequest(retConnection, tableOptions.getTablePayloadFormat());
        retConnection.setRequestProperty(Constants.HeaderConstants.CONTENT_TYPE,
                String.format(TableConstants.HeaderConstants.MULTIPART_MIXED_FORMAT, batchID));

        retConnection.setRequestProperty(TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION,
                TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION_VALUE);

        retConnection.setRequestMethod("POST");
        retConnection.setDoOutput(true);
        return retConnection;
    }

    /**
     * Reserved for internal use. Constructs the core <code>HttpURLConnection</code> to perform an operation.
     * 
     * @param rootUri
     *            A <code>java.net.URI</code> containing an absolute URI to the resource.
     * @param tableName
     *            The name of the table.
     * @param identity
     *            The identity of the entity, to pass in the Service Managment REST operation URI as
     *            <code><em>tableName</em>(<em>identity</em>)</code>. If <code>null</code>, only the <em>tableName</em>
     *            value will be passed.
     * @param timeoutInMs
     *            The server timeout interval in milliseconds.
     * @param queryBuilder
     *            The <code>UriQueryBuilder</code> for the request.
     * @param requestMethod
     *            The HTTP request method to set.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. This parameter is unused.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         An <code>HttpURLConnection</code> to use to perform the operation.
     * 
     * @throws IOException
     *             if there is an error opening the connection.
     * @throws URISyntaxException
     *             if the resource URI is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    private static HttpURLConnection coreCreate(final URI rootUri, final String tableName, final String eTag,
            final String identity, final int timeoutInMs, final UriQueryBuilder queryBuilder,
            final String requestMethod, final TableRequestOptions tableOptions, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {

        URI queryUri = null;

        // Do point query / delete etc.
        if (!Utility.isNullOrEmpty(identity)) {
            queryUri = PathUtility.appendPathToSingleUri(rootUri, tableName.concat(String.format("(%s)", identity)));
        }
        else {
            queryUri = PathUtility.appendPathToSingleUri(rootUri, tableName);
        }

        final HttpURLConnection retConnection = BaseRequest.createURLConnection(queryUri, timeoutInMs, queryBuilder,
                opContext);

        setAcceptHeaderForHttpWebRequest(retConnection, tableOptions.getTablePayloadFormat());
        setContentTypeForHttpWebRequest(retConnection, tableOptions.getTablePayloadFormat());

        retConnection.setRequestProperty(TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION,
                TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION_VALUE);

        if (!Utility.isNullOrEmpty(eTag)) {
            retConnection.setRequestProperty(Constants.HeaderConstants.IF_MATCH, eTag);
        }

        retConnection.setRequestMethod(requestMethod);
        return retConnection;
    }

    /**
     * Reserved for internal use. Constructs an <code>HttpURLConnection</code> to perform a delete operation.
     * 
     * @param rootUri
     *            A <code>java.net.URI</code> containing an absolute URI to the resource.
     * @param tableName
     *            The name of the table.
     * @param identity
     *            The identity of the entity. The resulting request will be formatted as /tableName(identity) if not
     *            null or empty.
     * @param eTag
     *            The etag of the entity.
     * @param timeoutInMs
     *            The server timeout interval in milliseconds.
     * @param queryBuilder
     *            The {@link UriQueryBuilder} for the operation.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         An <code>HttpURLConnection</code> to use to perform the operation.
     * 
     * @throws IOException
     *             if there is an error opening the connection.
     * @throws URISyntaxException
     *             if the resource URI is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    public static HttpURLConnection delete(final URI rootUri, final String tableName, final String identity,
            final String eTag, final int timeoutInMs, final UriQueryBuilder queryBuilder,
            final TableRequestOptions tableOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {

        return coreCreate(rootUri, tableName, eTag, identity, timeoutInMs, queryBuilder, "DELETE", tableOptions,
                opContext);
    }

    /**
     * Reserved for internal use. Constructs an <code>HttpURLConnection</code> to perform an insert operation.
     * 
     * @param rootUri
     *            A <code>java.net.URI</code> containing an absolute URI to the resource.
     * @param tableName
     *            The name of the table.
     * @param identity
     *            The identity of the entity. The resulting request will be formatted as /tableName(identity) if not
     *            null or empty.
     * @param eTag
     *            The etag of the entity, can be null for straight inserts.
     * @param updateType
     *            The {@link TableUpdateType} type of update to be performed. Specify <code>null</code> for straight
     *            inserts.
     * @param timeoutInMs
     *            The server timeout interval in milliseconds.
     * @param queryBuilder
     *            The {@link UriQueryBuilder} for the operation.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         An <code>HttpURLConnection</code> to use to perform the operation.
     * 
     * @throws IOException
     *             if there is an error opening the connection.
     * @throws URISyntaxException
     *             if the resource URI is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    public static HttpURLConnection insert(final URI rootUri, final String tableName, final String identity,
            final String eTag, final boolean echoContent, final TableUpdateType updateType, final int timeoutInMs,
            final UriQueryBuilder queryBuilder, final TableRequestOptions tableOptions, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        HttpURLConnection retConnection = null;

        if (updateType == null) {
            retConnection = coreCreate(rootUri, tableName, eTag, null/* identity */, timeoutInMs, queryBuilder,
                    "POST", tableOptions, opContext);
            retConnection.setRequestProperty(TableConstants.HeaderConstants.PREFER,
                    echoContent ? TableConstants.HeaderConstants.RETURN_CONTENT
                            : TableConstants.HeaderConstants.RETURN_NO_CONTENT);
        }
        else if (updateType == TableUpdateType.MERGE) {
            retConnection = coreCreate(rootUri, tableName, null/* ETAG */, identity, timeoutInMs, queryBuilder,
                    "POST", tableOptions, opContext);

            retConnection.setRequestProperty(TableConstants.HeaderConstants.X_HTTP_METHOD, "MERGE");
        }
        else if (updateType == TableUpdateType.REPLACE) {
            retConnection = coreCreate(rootUri, tableName, null/* ETAG */, identity, timeoutInMs, queryBuilder, "PUT",
                    tableOptions, opContext);
        }

        retConnection.setDoOutput(true);

        return retConnection;
    }

    /**
     * Reserved for internal use. Constructs an HttpURLConnection to perform a merge operation.
     * 
     * @param rootUri
     *            A <code>java.net.URI</code> containing an absolute URI to the resource.
     * @param tableName
     *            The name of the table.
     * @param identity
     *            The identity of the entity. The resulting request will be formatted as /tableName(identity) if not
     *            null or empty.
     * @param eTag
     *            The etag of the entity.
     * @param timeoutInMs
     *            The server timeout interval in milliseconds.
     * @param queryBuilder
     *            The {@link UriQueryBuilder} for the operation.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         An <code>HttpURLConnection</code> to use to perform the operation.
     * 
     * @throws IOException
     *             if there is an error opening the connection.
     * @throws URISyntaxException
     *             if the resource URI is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    public static HttpURLConnection merge(final URI rootUri, final String tableName, final String identity,
            final String eTag, final int timeoutInMs, final UriQueryBuilder queryBuilder,
            final TableRequestOptions tableOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        final HttpURLConnection retConnection = coreCreate(rootUri, tableName, eTag, identity, timeoutInMs,
                queryBuilder, "POST", tableOptions, opContext);
        retConnection.setRequestProperty(TableConstants.HeaderConstants.X_HTTP_METHOD, "MERGE");
        retConnection.setDoOutput(true);
        return retConnection;
    }

    /**
     * Reserved for internal use. Constructs an HttpURLConnection to perform a single entity query operation.
     * 
     * @param rootUri
     *            A <code>java.net.URI</code> containing an absolute URI to the resource.
     * @param tableName
     *            The name of the table.
     * @param identity
     *            The identity of the entity. The resulting request will be formatted as /tableName(identity) if not
     *            null or empty.
     * @param timeoutInMs
     *            The server timeout interval in milliseconds.
     * @param queryBuilder
     *            The {@link UriQueryBuilder} for the operation.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         An <code>HttpURLConnection</code> to use to perform the operation.
     * 
     * @throws IOException
     *             if there is an error opening the connection.
     * @throws URISyntaxException
     *             if the resource URI is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    public static HttpURLConnection query(final URI rootUri, final String tableName, final String identity,
            final int timeoutInMs, UriQueryBuilder queryBuilder, final ResultContinuation continuationToken,
            final TableRequestOptions tableOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        if (queryBuilder == null) {
            queryBuilder = new UriQueryBuilder();
        }

        applyContinuationToQueryBuilder(queryBuilder, continuationToken);
        final HttpURLConnection retConnection = coreCreate(rootUri, tableName, null, identity, timeoutInMs,
                queryBuilder, "GET", tableOptions, opContext);

        return retConnection;
    }

    /**
     * Reserved for internal use. Constructs an HttpURLConnection to perform an update operation.
     * 
     * @param rootUri
     *            A <code>java.net.URI</code> containing an absolute URI to the resource.
     * @param tableName
     *            The name of the table.
     * @param identity
     *            A <code>String</code> representing the identity of the entity. The resulting request will be formatted
     *            using <em>/tableName(identity)</em> if identity is not >code>null</code> or empty.
     * @param eTag
     *            The etag of the entity.
     * @param timeoutInMs
     *            The server timeout interval in milliseconds.
     * @param queryBuilder
     *            The {@link UriQueryBuilder} for the operation.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return
     *         An <code>HttpURLConnection</code> to use to perform the operation.
     * 
     * @throws IOException
     *             if there is an error opening the connection.
     * @throws URISyntaxException
     *             if the resource URI is invalid.
     * @throws StorageException
     *             if a storage service error occurred during the operation.
     */
    public static HttpURLConnection update(final URI rootUri, final String tableName, final String identity,
            final String eTag, final int timeoutInMs, final UriQueryBuilder queryBuilder,
            final TableRequestOptions tableOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        final HttpURLConnection retConnection = coreCreate(rootUri, tableName, eTag, identity, timeoutInMs,
                queryBuilder, "PUT", tableOptions, opContext);

        retConnection.setDoOutput(true);
        return retConnection;
    }

    /**
     * Sets the ACL for the table. , Sign with length of aclBytes.
     * 
     * @param rootUri
     *            A <code>java.net.URI</code> containing an absolute URI to the resource.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setAcl(final URI rootUri, final TableRequestOptions options,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {

        UriQueryBuilder queryBuilder = new UriQueryBuilder();
        queryBuilder.add(Constants.QueryConstants.COMPONENT, "acl");

        final HttpURLConnection retConnection = BaseRequest.createURLConnection(rootUri,
                options.getTimeoutIntervalInMs(), queryBuilder, opContext);
        retConnection.setRequestMethod("PUT");
        retConnection.setDoOutput(true);

        return retConnection;
    }

    /**
     * Constructs a web request to return the ACL for this table. Sign with no length specified.
     * 
     * @param rootUri
     *            A <code>java.net.URI</code> containing an absolute URI to the resource.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudTableClient}.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * 
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     */
    public static HttpURLConnection getAcl(final URI rootUri, final TableRequestOptions options,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {

        UriQueryBuilder queryBuilder = new UriQueryBuilder();
        queryBuilder.add(Constants.QueryConstants.COMPONENT, "acl");

        final HttpURLConnection retConnection = BaseRequest.createURLConnection(rootUri,
                options.getTimeoutIntervalInMs(), queryBuilder, opContext);
        retConnection.setRequestMethod("GET");

        return retConnection;
    }

    private static void setAcceptHeaderForHttpWebRequest(HttpURLConnection retConnection,
            TablePayloadFormat payloadFormat) {
        if (payloadFormat == TablePayloadFormat.AtomPub) {
            retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT,
                    TableConstants.HeaderConstants.ATOM_ACCEPT_TYPE);
        }
        else if (payloadFormat == TablePayloadFormat.JsonFullMetadata) {
            retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT,
                    TableConstants.HeaderConstants.JSON_FULL_METADATA_ACCEPT_TYPE);
        }
        else if (payloadFormat == TablePayloadFormat.Json) {
            retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT,
                    TableConstants.HeaderConstants.JSON_ACCEPT_TYPE);
        }
        else if (payloadFormat == TablePayloadFormat.JsonNoMetadata) {
            retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT,
                    TableConstants.HeaderConstants.JSON_NO_METADATA_ACCEPT_TYPE);
        }
    }

    private static void setContentTypeForHttpWebRequest(HttpURLConnection retConnection,
            TablePayloadFormat payloadFormat) {
        if (payloadFormat == TablePayloadFormat.AtomPub) {
            retConnection.setRequestProperty(Constants.HeaderConstants.CONTENT_TYPE,
                    TableConstants.HeaderConstants.ATOM_CONTENT_TYPE);
        }
        else {
            retConnection.setRequestProperty(Constants.HeaderConstants.CONTENT_TYPE,
                    TableConstants.HeaderConstants.JSON_CONTENT_TYPE);
        }
    }

    /**
     * Private Default Constructor.
     */
    private TableRequest() {
        // No op
    }
}
