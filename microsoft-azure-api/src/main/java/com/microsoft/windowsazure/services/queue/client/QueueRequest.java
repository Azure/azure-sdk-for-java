package com.microsoft.windowsazure.services.queue.client;

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

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseRequest;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ListingContext;

/**
 * RESERVED FOR INTERNAL USE. A class used to generate requests for queue objects.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
final class QueueRequest {
    /**
     * Adds user-defined metadata to the request as one or more name-value pairs.
     * 
     * @param request
     *            The web request.
     * @param metadata
     *            The user-defined metadata.
     * */
    public static void addMetadata(
            final HttpURLConnection request, final HashMap<String, String> metadata, final OperationContext opContext) {
        BaseRequest.addMetadata(request, metadata, opContext);
    }

    /**
     * Constructs a request to clear messages.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection clearMessages(final URI uri, final int timeout, final OperationContext opContext)
            throws URISyntaxException, IOException, StorageException {

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, null, opContext);

        request.setRequestMethod("DELETE");

        return request;
    }

    /**
     * Constructs a web request to create a new queue. Sign with 0 length.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection create(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        return BaseRequest.create(uri, timeout, null, opContext);
    }

    /**
     * Constructs a web request to delete the queue. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection delete(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        return BaseRequest.delete(uri, timeout, null, opContext);
    }

    /**
     * Constructs a request to delete a message.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection deleteMessage(
            final URI uri, final int timeout, final String popReceipt, final OperationContext opContext)
            throws URISyntaxException, IOException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();
        builder.add("popreceipt", popReceipt);

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod("DELETE");

        return request;
    }

    /**
     * Constructs a web request to download user-defined metadata and ApproximateMessageCount for the queue.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * 
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection downloadAttributes(
            final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        return BaseRequest.getMetadata(uri, timeout, null, opContext);
    }

    /**
     * Constructs a web request to delete the queue. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection exist(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        return BaseRequest.delete(uri, timeout, null, opContext);
    }

    /**
     * Parse message request body.
     * 
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
        } catch (final UnsupportedEncodingException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }

    /**
     * Constructs a request to return a listing of all queues in this storage account. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI for the account.
     * @param timeout
     *            The absolute URI for the account.
     * @param listingContext
     *            A set of parameters for the listing operation.
     * @param detailsIncluded
     *            Additional details to return with the listing.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection list(
            final URI uri, final int timeout, final ListingContext listingContext,
            final QueueListingDetails detailsIncluded, final OperationContext opContext)
            throws URISyntaxException, IOException, StorageException {

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
     * Constructs a request to peek messages.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection peekMessages(
            final URI uri, final int timeout, final int numberOfMessages, final OperationContext opContext)
            throws URISyntaxException, IOException, StorageException {

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
     * Constructs a web request to create a new message. Sign with 0 length.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection putMessage(
            final URI uri, final int timeout, final int visibilityTimeoutInSeconds, final int timeToLiveInSeconds,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {

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
     * Constructs a request to retrieve messages.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection retrieveMessages(
            final URI uri, final int timeout, final int numberOfMessages, final int visibilityTimeoutInSeconds,
            final OperationContext opContext) throws URISyntaxException, IOException, StorageException {

        final UriQueryBuilder builder = new UriQueryBuilder();

        if (numberOfMessages != 0) {
            builder.add("numofmessages", Integer.toString(numberOfMessages));
        }

        if (visibilityTimeoutInSeconds != 0) {
            builder.add("visibilitytimeout", Integer.toString(visibilityTimeoutInSeconds));
        }

        final HttpURLConnection request = BaseRequest.createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod("GET");

        return request;
    }

    /**
     * Constructs a web request to set user-defined metadata for the queue, Sign with 0 Length.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * 
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setMetadata(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        return BaseRequest.setMetadata(uri, timeout, null, opContext);
    }

    /**
     * Constructs a request to update message.
     * 
     * @param uri
     *            The absolute URI to the queue.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection updateMessage(
            final URI uri, final int timeout, final String popReceipt, final int visibilityTimeoutInSeconds,
            final OperationContext opContext) throws URISyntaxException, IOException, StorageException {

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
     * Private Default Ctor.
     */
    private QueueRequest() {
        // No op
    }
}
