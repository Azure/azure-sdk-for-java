/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.table.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ResultContinuation;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseRequest;

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
    protected static void applyContinuationToQueryBuilder(final UriQueryBuilder builder,
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
    protected static HttpURLConnection batch(final URI rootUri, final int timeoutInMs, final String batchID,
            final UriQueryBuilder queryBuilder, final TableRequestOptions tableOptions, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final URI queryUri = PathUtility.appendPathToUri(rootUri, "$batch");

        final HttpURLConnection retConnection = BaseRequest.createURLConnection(queryUri, timeoutInMs, queryBuilder,
                opContext);
        // Note : accept behavior, java by default sends Accept behavior
        // as text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
        retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT, TableConstants.HeaderConstants.ACCEPT_TYPE);
        retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT_CHARSET, "UTF8");
        retConnection.setRequestProperty(TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION,
                TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION_VALUE);

        retConnection.setRequestProperty(Constants.HeaderConstants.CONTENT_TYPE,
                String.format(TableConstants.HeaderConstants.MULTIPART_MIXED_FORMAT, batchID));

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
    protected static HttpURLConnection coreCreate(final URI rootUri, final String tableName, final String eTag,
            final String identity, final int timeoutInMs, final UriQueryBuilder queryBuilder,
            final String requestMethod, final TableRequestOptions tableOptions, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {

        URI queryUri = null;

        // Do point query / delete etc.
        if (!Utility.isNullOrEmpty(identity)) {
            queryUri = PathUtility.appendPathToUri(rootUri, tableName.concat(String.format("(%s)", identity)));
        }
        else {
            queryUri = PathUtility.appendPathToUri(rootUri, tableName);
        }

        final HttpURLConnection retConnection = BaseRequest.createURLConnection(queryUri, timeoutInMs, queryBuilder,
                opContext);
        // Note : accept behavior, java by default sends Accept behavior
        // as text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
        retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT, TableConstants.HeaderConstants.ACCEPT_TYPE);
        retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT_CHARSET, "UTF-8");
        retConnection.setRequestProperty(TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION,
                TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION_VALUE);

        retConnection.setRequestProperty(Constants.HeaderConstants.CONTENT_TYPE,
                TableConstants.HeaderConstants.ATOMPUB_TYPE);

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
    protected static HttpURLConnection delete(final URI rootUri, final String tableName, final String identity,
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
    protected static HttpURLConnection insert(final URI rootUri, final String tableName, final String identity,
            final String eTag, final TableUpdateType updateType, final int timeoutInMs,
            final UriQueryBuilder queryBuilder, final TableRequestOptions tableOptions, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        HttpURLConnection retConnection = null;

        if (updateType == null) {
            retConnection = coreCreate(rootUri, tableName, eTag, null/* identity */, timeoutInMs, queryBuilder,
                    "POST", tableOptions, opContext);
        }
        else if (updateType == TableUpdateType.MERGE) {
            retConnection = coreCreate(rootUri, tableName, null/* ETAG */, identity, timeoutInMs, queryBuilder,
                    "POST", tableOptions, opContext);

            retConnection.setRequestProperty("X-HTTP-Method", "MERGE");
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
    protected static HttpURLConnection merge(final URI rootUri, final String tableName, final String identity,
            final String eTag, final int timeoutInMs, final UriQueryBuilder queryBuilder,
            final TableRequestOptions tableOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        final HttpURLConnection retConnection = coreCreate(rootUri, tableName, eTag, identity, timeoutInMs,
                queryBuilder, "POST", tableOptions, opContext);
        retConnection.setRequestProperty("X-HTTP-Method", "MERGE");
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
    protected static HttpURLConnection query(final URI rootUri, final String tableName, final String identity,
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
    protected static HttpURLConnection update(final URI rootUri, final String tableName, final String identity,
            final String eTag, final int timeoutInMs, final UriQueryBuilder queryBuilder,
            final TableRequestOptions tableOptions, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        final HttpURLConnection retConnection = coreCreate(rootUri, tableName, eTag, identity, timeoutInMs,
                queryBuilder, "PUT", tableOptions, opContext);

        retConnection.setDoOutput(true);
        return retConnection;
    }

    /**
     * Private Default Constructor.
     */
    private TableRequest() {
        // No op
    }
}
