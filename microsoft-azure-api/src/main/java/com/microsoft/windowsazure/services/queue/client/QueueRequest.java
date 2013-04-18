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

package com.microsoft.windowsazure.services.queue.client;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseRequest;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ListingContext;

/**
 * RESERVED FOR INTERNAL USE. Provides a set of methods for constructing web
 * requests for queue operations.
 */
final class QueueRequest {
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
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
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
    public static HttpURLConnection clearMessages(final URI uri, final int timeout, final OperationContext opContext)
            throws URISyntaxException, IOException, StorageException {

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, null, opContext);

        request.setRequestMethod("DELETE");

        return request;
    }

    /**
     * Constructs a web request to create a new queue. Sign the web request with
     * a length of 0.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
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
    public static HttpURLConnection create(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        return BaseRequest.create(uri, timeout, null, opContext);
    }

    /**
     * Constructs a web request to delete the queue. Sign the web request with a
     * length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
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
    public static HttpURLConnection delete(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        return BaseRequest.delete(uri, timeout, null, opContext);
    }

    /**
     * Constructs a web request to delete a message from the queue. Sign the web
     * request with a length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
     * @param popReceipt
     *            A <code>String</code> that contains the pop receipt value
     *            returned from an earlier call to {@link CloudQueueMessage#getPopReceipt} for the message to
     *            delete.
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return An <code>HttpURLConnection</code> configured for the specified
     *         operation.
     * 
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws IOException
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static HttpURLConnection deleteMessage(final URI uri, final int timeout, final String popReceipt,
            final OperationContext opContext) throws URISyntaxException, IOException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add("popreceipt", popReceipt);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod("DELETE");

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
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
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
    public static HttpURLConnection downloadAttributes(final URI uri, final int timeout,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        return BaseRequest.getMetadata(uri, timeout, null, opContext);
    }

    /**
     * Generates the message request body from a string containing the message.
     * The message must be encodable as UTF-8. To be included in a web request,
     * this message request body must be written to the output stream of the web
     * request.
     * 
     * @param message
     *            A <code>String<code> containing the message to wrap in a message request body.
     * 
     * @return An array of <code>byte</code> containing the message request body
     *         encoded as UTF-8.
     * 
     * @throws XMLStreamException
     * @throws StorageException
     *             If the message cannot be encoded as UTF-8.
     */
    public static byte[] generateMessageRequestBody(final String message) throws XMLStreamException, StorageException {
        final StringWriter outWriter = new StringWriter();
        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outWriter);

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(QueueConstants.QUEUE_MESSAGE_ELEMENT);

        xmlw.writeStartElement(QueueConstants.MESSAGE_TEXT_ELEMENT);
        xmlw.writeCharacters(message);
        xmlw.writeEndElement();

        // end QueueMessage_ELEMENT
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
     * Constructs a web request to return a listing of all queues in this
     * storage account. Sign the web request with a length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the storage account.
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
     * @param listingContext
     *            A {@link ListingContext} object that specifies parameters for
     *            the listing operation, if any. May be <code>null</code>.
     * @param detailsIncluded
     *            A {@link QueueListingDetails} object that specifies additional
     *            details to return with the listing, if any. May be <code>null</code>.
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
    public static HttpURLConnection list(final URI uri, final int timeout, final ListingContext listingContext,
            final QueueListingDetails detailsIncluded, final OperationContext opContext) throws URISyntaxException,
            IOException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add("comp", "list");

        if (listingContext != null) {
            if (!Utility.isNullOrEmpty(listingContext.getPrefix())) {
                builder.add("prefix", listingContext.getPrefix());
            }

            if (!Utility.isNullOrEmpty(listingContext.getMarker())) {
                builder.add("marker", listingContext.getMarker());
            }

            if (listingContext.getMaxResults() != null && listingContext.getMaxResults() > 0) {
                builder.add("maxresults", listingContext.getMaxResults().toString());
            }
        }

        if (detailsIncluded == QueueListingDetails.ALL || detailsIncluded == QueueListingDetails.METADATA) {
            builder.add("include", "metadata");
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod("GET");

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
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
     * @param numberOfMessages
     *            A nonzero value that specifies the number of messages to
     *            retrieve from the queue, up to a maximum of 32.
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
    public static HttpURLConnection peekMessages(final URI uri, final int timeout, final int numberOfMessages,
            final OperationContext opContext) throws URISyntaxException, IOException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add("peekonly", "true");

        if (numberOfMessages != 0) {
            builder.add("numofmessages", Integer.toString(numberOfMessages));
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod("GET");

        return request;
    }

    /**
     * Constructs a web request to add a message to the back of the queue. Write
     * the encoded message request body generated with a call to {@link #generateMessageRequestBody(String)} to the
     * output stream of the
     * request. Sign the web request with the length of the encoded message
     * request body.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
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
    public static HttpURLConnection putMessage(final URI uri, final int timeout, final int visibilityTimeoutInSeconds,
            final int timeToLiveInSeconds, final OperationContext opContext) throws IOException, URISyntaxException,
            StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        if (visibilityTimeoutInSeconds != 0) {
            builder.add("visibilitytimeout", Integer.toString(visibilityTimeoutInSeconds));
        }

        if (timeToLiveInSeconds != 0) {
            builder.add("messagettl", Integer.toString(timeToLiveInSeconds));
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setDoOutput(true);
        request.setRequestMethod("POST");

        return request;
    }

    /**
     * Constructs a web request to retrieve messages from the front of the
     * queue. Sign the web request with a length of -1L.
     * 
     * @param uri
     *            A <code>URI</code> object that specifies the absolute URI to
     *            the queue.
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
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
    public static HttpURLConnection retrieveMessages(final URI uri, final int timeout, final int numberOfMessages,
            final int visibilityTimeoutInSeconds, final OperationContext opContext) throws URISyntaxException,
            IOException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        if (numberOfMessages != 0) {
            builder.add("numofmessages", Integer.toString(numberOfMessages));
        }

        builder.add("visibilitytimeout", Integer.toString(visibilityTimeoutInSeconds));

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

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
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
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
    public static HttpURLConnection setMetadata(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        return BaseRequest.setMetadata(uri, timeout, null, opContext);
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
     * @param timeout
     *            The server response timeout interval in milliseconds. If the
     *            operation does not complete within the specified timeout
     *            interval, a timeout error is returned by the server. If the
     *            timeout value is 0, the maximum timeout of 30 seconds is used.
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
    public static HttpURLConnection updateMessage(final URI uri, final int timeout, final String popReceipt,
            final int visibilityTimeoutInSeconds, final OperationContext opContext) throws URISyntaxException,
            IOException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        builder.add("popreceipt", popReceipt);

        builder.add("visibilitytimeout", Integer.toString(visibilityTimeoutInSeconds));

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setRequestProperty(Constants.HeaderConstants.STORAGE_VERSION_HEADER, "2011-08-18");

        request.setDoOutput(true);
        request.setRequestMethod("PUT");

        return request;
    }

    /**
     * Sets the ACL for the queue. , Sign with length of aclBytes.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * @param publicAccess
     *            The type of public access to allow for the queue.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setAcl(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add("comp", "acl");

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setDoOutput(true);
        request.setRequestMethod("PUT");

        return request;
    }

    /**
     * Writes a collection of shared access policies to the specified stream in XML format.
     * 
     * @param sharedAccessPolicies
     *            A collection of shared access policies
     * @param outWriter
     *            an sink to write the output to.
     * @throws XMLStreamException
     */
    public static void writeSharedAccessIdentifiersToStream(
            final HashMap<String, SharedAccessQueuePolicy> sharedAccessPolicies, final StringWriter outWriter)
            throws XMLStreamException {
        Utility.assertNotNull("sharedAccessPolicies", sharedAccessPolicies);
        Utility.assertNotNull("outWriter", outWriter);

        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outWriter);

        if (sharedAccessPolicies.keySet().size() > Constants.MAX_SHARED_ACCESS_POLICY_IDENTIFIERS) {
            final String errorMessage = String
                    .format("Too many %d shared access policy identifiers provided. Server does not support setting more than %d on a single queue.",
                            sharedAccessPolicies.keySet().size(), Constants.MAX_SHARED_ACCESS_POLICY_IDENTIFIERS);

            throw new IllegalArgumentException(errorMessage);
        }

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(Constants.SIGNED_IDENTIFIERS_ELEMENT);

        for (final Entry<String, SharedAccessQueuePolicy> entry : sharedAccessPolicies.entrySet()) {
            final SharedAccessQueuePolicy policy = entry.getValue();
            xmlw.writeStartElement(Constants.SIGNED_IDENTIFIER_ELEMENT);

            // Set the identifier
            xmlw.writeStartElement(Constants.ID);
            xmlw.writeCharacters(entry.getKey());
            xmlw.writeEndElement();

            xmlw.writeStartElement(Constants.ACCESS_POLICY);

            // Set the Start Time
            xmlw.writeStartElement(Constants.START);
            xmlw.writeCharacters(Utility.getUTCTimeOrEmpty(policy.getSharedAccessStartTime()));
            // end Start
            xmlw.writeEndElement();

            // Set the Expiry Time
            xmlw.writeStartElement(Constants.EXPIRY);
            xmlw.writeCharacters(Utility.getUTCTimeOrEmpty(policy.getSharedAccessExpiryTime()));
            // end Expiry
            xmlw.writeEndElement();

            // Set the Permissions
            xmlw.writeStartElement(Constants.PERMISSION);
            xmlw.writeCharacters(SharedAccessQueuePolicy.permissionsToString(policy.getPermissions()));
            // end Permission
            xmlw.writeEndElement();

            // end AccessPolicy
            xmlw.writeEndElement();
            // end SignedIdentifier
            xmlw.writeEndElement();
        }

        // end SignedIdentifiers
        xmlw.writeEndElement();
        // end doc
        xmlw.writeEndDocument();
    }

    /**
     * Constructs a web request to return the ACL for this queue. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     */
    public static HttpURLConnection getAcl(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add("comp", "acl");

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod("GET");

        return request;
    }

    /**
     * Private Default Ctor.
     */
    private QueueRequest() {
        // No op
    }
}
