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

package com.microsoft.azure.storage.queue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.BaseRequest;
import com.microsoft.azure.storage.core.ListingContext;
import com.microsoft.azure.storage.core.UriQueryBuilder;

/**
 * RESERVED FOR INTERNAL USE. Provides a set of methods for constructing web
 * requests for queue operations.
 */
final class QueueRequest {

    private static final String METADATA = "metadata";

    private static final String POP_RECEIPT = "popreceipt";

    private static final String PEEK_ONLY = "peekonly";

    private static final String NUMBER_OF_MESSAGES = "numofmessages";

    private static final String VISIBILITY_TIMEOUT = "visibilitytimeout";

    private static final String MESSAGE_TTL = "messagettl";

    /**
     * Adds user-defined metadata to the web request as one or more name-value
     * pairs.
     * 
     * @param request
     *            The <code>HttpURLConnection</code> web request to add the
     *            metadata to.
     * @param metadata
     *            A <code>HashMap</code> containing the user-defined metadata to
     *            add.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     */
    public static void addMetadata(final HttpURLConnection request, final HashMap<String, String> metadata,
            final OperationContext opContext) {
        BaseRequest.addMetadata(request, metadata, opContext);
    }

    /**
     * Constructs a web request to clear all the messages in the queue. Sign the
     * web request with a length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection clearMessages(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext) throws URISyntaxException, IOException, StorageException {

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, queueOptions, null, opContext);

        request.setRequestMethod(Constants.HTTP_DELETE);

        return request;
    }

    /**
     * Constructs a web request to create a new queue. Sign the web request with
     * a length of 0.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection create(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        return BaseRequest.create(uri, queueOptions, null, opContext);
    }

    /**
     * Constructs a web request to delete the queue. Sign the web request with a
     * length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection delete(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        return BaseRequest.delete(uri, queueOptions, null, opContext);
    }

    /**
     * Constructs a web request to delete a message from the queue. Sign the web
     * request with a length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * @param popReceipt
     *            A <code>String</code> that contains the pop receipt value
     *            returned from an earlier call to {@link CloudQueueMessage#getPopReceipt} for the message to
     *            delete.
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws IOException
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection deleteMessage(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext, final String popReceipt) throws URISyntaxException, IOException,
            StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(POP_RECEIPT, popReceipt);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, queueOptions, builder, opContext);

        request.setRequestMethod(Constants.HTTP_DELETE);

        return request;
    }

    /**
     * Constructs a web request to download user-defined metadata and the
     * approximate message count for the queue. Sign the web request with a
     * length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection downloadAttributes(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        UriQueryBuilder builder = new UriQueryBuilder();

        builder.add(Constants.QueryConstants.COMPONENT, METADATA);
        final HttpURLConnection retConnection = BaseRequest.createURLConnection(uri, queueOptions, builder, opContext);

        retConnection.setRequestMethod(Constants.HTTP_HEAD);

        return retConnection;
    }

    /**
     * Constructs a web request to return a listing of all queues in this
     * storage account. Sign the web request with a length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the storage account.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * @param listingContext
     *            A {@link ListingContext} object that specifies parameters for
     *            the listing operation, if any. May be <code>null</code>.
     * @param detailsIncluded
     *            A {@link QueueListingDetails} object that specifies additional
     *            details to return with the listing, if any. May be <code>null</code>.
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection list(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext, final ListingContext listingContext,
            final QueueListingDetails detailsIncluded) throws URISyntaxException, IOException, StorageException {
        final UriQueryBuilder builder = BaseRequest.getListUriQueryBuilder(listingContext);

        if (detailsIncluded == QueueListingDetails.ALL || detailsIncluded == QueueListingDetails.METADATA) {
            builder.add(Constants.QueryConstants.INCLUDE, Constants.QueryConstants.METADATA);
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, queueOptions, builder, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        return request;
    }

    /**
     * Constructs a web request to retrieve a specified number of messages from
     * the front of the queue without changing their visibility. Sign the web
     * request with a length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * @param numberOfMessages
     *            A nonzero value that specifies the number of messages to
     *            retrieve from the queue, up to a maximum of 32.
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection peekMessages(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext, final int numberOfMessages) throws URISyntaxException, IOException,
            StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(PEEK_ONLY, "true");

        if (numberOfMessages != 0) {
            builder.add(NUMBER_OF_MESSAGES, Integer.toString(numberOfMessages));
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, queueOptions, builder, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        return request;
    }

    /**
     * Constructs a web request to add a message to the back of the queue. Write the encoded message request body
     * generated with a call to QueueMessageSerializer#generateMessageRequestBody(String)} to the output
     * stream of the request. Sign the web request with the length of the encoded message request body.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * @param visibilityTimeoutInSeconds
     *            Specifies the length of time for the message to be invisible
     *            in seconds, starting when it is added to the queue. A value of
     *            0 will make the message visible immediately. The value must be
     *            greater than or equal to 0, and cannot be larger than 7 days.
     *            The visibility timeout of a message cannot be set to a value
     *            greater than the time-to-live time.
     * @param timeToLiveInSeconds
     *            Specifies the time-to-live interval for the message, in
     *            seconds. The maximum time-to-live allowed is 7 days. If this
     *            parameter is 0, the default time-to-live of 7 days is used.
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection putMessage(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext, final int visibilityTimeoutInSeconds, final int timeToLiveInSeconds)
            throws IOException, URISyntaxException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        if (visibilityTimeoutInSeconds != 0) {
            builder.add(VISIBILITY_TIMEOUT, Integer.toString(visibilityTimeoutInSeconds));
        }

        if (timeToLiveInSeconds != 0) {
            builder.add(MESSAGE_TTL, Integer.toString(timeToLiveInSeconds));
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, queueOptions, builder, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_POST);

        return request;
    }

    /**
     * Constructs a web request to retrieve messages from the front of the
     * queue. Sign the web request with a length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * @param numberOfMessages
     *            A nonzero value that specifies the number of messages to
     *            retrieve from the queue, up to a maximum of 32.
     * @param visibilityTimeoutInSeconds
     *            Specifies the visibility timeout value in seconds, relative to
     *            server time, to make the retrieved messages invisible until
     *            the visibility timeout expires. The value must be larger than
     *            or equal to 0, and cannot be larger than 7 days. The
     *            visibility timeout of a message can be set to a value later
     *            than the expiry time, which will prevent the message from
     *            being retrieved again whether it is processed or not.
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection retrieveMessages(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext, final int numberOfMessages, final int visibilityTimeoutInSeconds)
            throws URISyntaxException, IOException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        if (numberOfMessages != 0) {
            builder.add(NUMBER_OF_MESSAGES, Integer.toString(numberOfMessages));
        }

        builder.add(VISIBILITY_TIMEOUT, Integer.toString(visibilityTimeoutInSeconds));

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, queueOptions, builder, opContext);

        request.setRequestMethod("GET");

        return request;
    }

    /**
     * Constructs a web request to set user-defined metadata for the queue. Each
     * call to this operation replaces all existing metadata attached to the
     * queue. Use the {@link #addMetadata} method to specify the metadata to set
     * on the queue. To remove all metadata from the queue, call this web
     * request with no metadata added. Sign the web request with a length of 0.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection setMetadata(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        return BaseRequest.setMetadata(uri, queueOptions, null, opContext);
    }

    /**
     * Constructs a web request to update the visibility timeout of a message in
     * the queue. Optionally updates the message content if a message request
     * body is written to the output stream of the web request. The web request
     * should be signed with the length of the encoded message request body if
     * one is included, or a length of 0 if no message request body is included.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * @param popReceipt
     *            A <code>String</code> that contains the pop receipt value
     *            returned from an earlier call to {@link CloudQueueMessage#getPopReceipt} for the message to
     *            update.
     * @param visibilityTimeoutInSeconds
     *            Specifies the new visibility timeout value in seconds,
     *            relative to server time, to make the retrieved messages
     *            invisible until the visibility timeout expires. The value must
     *            be larger than or equal to 0, and cannot be larger than 7
     *            days. The visibility timeout of a message can be set to a
     *            value later than the expiry time, which will prevent the
     *            message from being retrieved again whether it is processed or
     *            not.
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws IOException
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection updateMessage(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext, final String popReceipt, final int visibilityTimeoutInSeconds)
            throws URISyntaxException, IOException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        builder.add(POP_RECEIPT, popReceipt);

        builder.add(VISIBILITY_TIMEOUT, Integer.toString(visibilityTimeoutInSeconds));

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, queueOptions, builder, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        return request;
    }

    /**
     * Sets the ACL for the queue. Sign with length of aclBytes.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setAcl(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.ACL);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, queueOptions, builder, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);

        return request;
    }

    /**
     * Constructs a web request to return the ACL for this queue. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param queueOptions
     *            A {@link QueueRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. Specify <code>null</code> to use the request options specified on the
     *            {@link CloudQueueClient}.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     */
    public static HttpURLConnection getAcl(final URI uri, final QueueRequestOptions queueOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.ACL);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, queueOptions, builder, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        return request;
    }

    /**
     * Private Default Ctor.
     */
    private QueueRequest() {
        // No op
    }
}
