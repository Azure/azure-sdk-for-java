/*
 * QueueDeserializationHelper.java
 * 
 * Copyright (c) 2011 Microsoft. All rights reserved.
 */
package com.microsoft.windowsazure.services.queue.client;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.DeserializationHelper;

/**
 * RESERVED FOR INTERNAL USE. Class to provide object deserialization for
 * queues.
 */
final class QueueDeserializationHelper {
    /**
     * Populates the message from an XMLStreamReader.
     * 
     * @param xmlr
     *            The <code>XMLStreamReader</code> to read from.
     * @param shouldEncodeMessage
     *            A flag indicating whether messages should be base-64 encoded.
     * 
     * @return A {@link CloudQueueMessage} from the stream.
     * 
     * @throws XMLStreamException
     *             if there is an error parsing the stream
     * @throws ParseException
     *             if there is an error in parsing a date
     * @throws URISyntaxException
     *             if the uri is invalid
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    protected static CloudQueueMessage readMessage(final XMLStreamReader xmlr, final boolean shouldEncodeMessage)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, QueueConstants.QUEUE_MESSAGE_ELEMENT);

        final CloudQueueMessage message = new CloudQueueMessage();
        message.messageType = shouldEncodeMessage ? QueueMessageType.BASE_64_ENCODED : QueueMessageType.RAW_STRING;

        int eventType = xmlr.getEventType();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(QueueConstants.MESSAGE_ID_ELEMENT)) {
                    message.id = Utility.readElementFromXMLReader(xmlr, QueueConstants.MESSAGE_ID_ELEMENT);
                }
                else if (name.equals(QueueConstants.INSERTION_TIME_ELEMENT)) {
                    message.insertionTime = Utility.parseRFC1123DateFromStringInGMT(Utility.readElementFromXMLReader(
                            xmlr, QueueConstants.INSERTION_TIME_ELEMENT));
                }
                else if (name.equals(QueueConstants.EXPIRATION_TIME_ELEMENT)) {
                    message.expirationTime = Utility.parseRFC1123DateFromStringInGMT(Utility.readElementFromXMLReader(
                            xmlr, QueueConstants.EXPIRATION_TIME_ELEMENT));
                }
                else if (name.equals(QueueConstants.POP_RECEIPT_ELEMENT)) {
                    message.popReceipt = Utility.readElementFromXMLReader(xmlr, QueueConstants.POP_RECEIPT_ELEMENT);
                }
                else if (name.equals(QueueConstants.TIME_NEXT_VISIBLE_ELEMENT)) {
                    message.nextVisibleTime = Utility.parseRFC1123DateFromStringInGMT(Utility.readElementFromXMLReader(
                            xmlr, QueueConstants.TIME_NEXT_VISIBLE_ELEMENT));
                }
                else if (name.equals(QueueConstants.DEQUEUE_COUNT_ELEMENT)) {
                    message.dequeueCount = Integer.parseInt(Utility.readElementFromXMLReader(xmlr,
                            QueueConstants.DEQUEUE_COUNT_ELEMENT));
                }
                else if (name.equals(QueueConstants.MESSAGE_TEXT_ELEMENT)) {
                    message.messageContent = Utility
                            .readElementFromXMLReader(xmlr, QueueConstants.MESSAGE_TEXT_ELEMENT);
                }

            }
            else if (eventType == XMLStreamConstants.END_ELEMENT && name.equals(QueueConstants.QUEUE_MESSAGE_ELEMENT)) {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, QueueConstants.QUEUE_MESSAGE_ELEMENT);
        return message;
    }

    /**
     * Populates CloudQueueMessage objects from the XMLStreamReader; the reader
     * must be at the Start element of QueuesElement.
     * 
     * @param stream
     *            The <code>InputStream</code> object to deserialize from.
     * @param shouldEncodeMessage
     *            A flag indicating whether messages should be base-64 encoded.
     * 
     * @return An <code>ArrayList</code> of {@link CloudQueueMessage} from the
     *         stream.
     * 
     * @throws XMLStreamException
     *             If there is a parsing exception.
     * @throws ParseException
     *             If a date value is not correctly encoded.
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static ArrayList<CloudQueueMessage> readMessages(final InputStream stream, final boolean shouldEncodeMessage)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {
        final XMLStreamReader xmlr = Utility.createXMLStreamReaderFromStream(stream);

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, QueueConstants.QUEUE_MESSAGES_LIST_ELEMENT);

        final ArrayList<CloudQueueMessage> messages = new ArrayList<CloudQueueMessage>();

        eventType = xmlr.next();
        while (eventType == XMLStreamConstants.START_ELEMENT && xmlr.hasName()
                && QueueConstants.QUEUE_MESSAGE_ELEMENT.equals(xmlr.getName().toString())) {
            messages.add(QueueDeserializationHelper.readMessage(xmlr, shouldEncodeMessage));
            eventType = xmlr.next();
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, QueueConstants.QUEUE_MESSAGES_LIST_ELEMENT);
        return messages;
    }

    /**
     * Reserved for internal use.
     * 
     * Populates the queue from an XMLStreamReader
     * 
     * @param xmlr
     *            The <code>XMLStreamReader</code> to read from.
     * @param serviceClient
     *            The {@link CloudQueueClient} to create the return value with.
     * 
     * @return A {@link CloudQueue} populated with the deserialized data.
     * 
     * @throws XMLStreamException
     *             If there is a parsing exception.
     * @throws ParseException
     *             If a date value is not correctly encoded.
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    protected static CloudQueue readQueue(final XMLStreamReader xmlr, final CloudQueueClient serviceClient)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, QueueConstants.QUEUE_ELEMENT);

        String queueName = null;
        URI queueUri = null;
        HashMap<String, String> queueMetadata = null;

        int eventType = xmlr.getEventType();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(Constants.URL_ELEMENT)) {
                    queueUri = new URI(Utility.readElementFromXMLReader(xmlr, Constants.URL_ELEMENT));
                }
                else if (name.equals(Constants.NAME_ELEMENT)) {
                    queueName = Utility.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT);
                }
                else if (name.equals(Constants.METADATA_ELEMENT)) {
                    // parse metadata
                    queueMetadata = DeserializationHelper.parseMetadateFromXML(xmlr);
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.METADATA_ELEMENT);
                }
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT && name.equals(QueueConstants.QUEUE_ELEMENT)) {
                break;
            }
        }

        final CloudQueue queue = new CloudQueue(queueUri, serviceClient);
        queue.setMetadata(queueMetadata);
        queue.setName(queueName);

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, QueueConstants.QUEUE_ELEMENT);
        return queue;
    }

    /**
     * Populates CloudQueue objects from the XMLStreamReader; the reader must be
     * at the Start element of QueuesElement.
     * 
     * @param xmlr
     *            The <code>XMLStreamReader</code> to read from.
     * @param serviceClient
     *            The {@link CloudQueueClient} to create the return value with.
     * 
     * @return An <code>ArrayList</code> of{@link CloudQueue} populated with the
     *         deserialized data.
     * 
     * @throws XMLStreamException
     *             If there is a parsing exception.
     * @throws ParseException
     *             If a date value is not correctly encoded.
     * @throws URISyntaxException
     *             If the URI is not valid.
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static ArrayList<CloudQueue> readQueues(final XMLStreamReader xmlr, final CloudQueueClient serviceClient)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, QueueConstants.QUEUES_ELEMENT);

        final ArrayList<CloudQueue> queues = new ArrayList<CloudQueue>();

        eventType = xmlr.next();
        while (eventType == XMLStreamConstants.START_ELEMENT && xmlr.hasName()
                && QueueConstants.QUEUE_ELEMENT.equals(xmlr.getName().toString())) {
            queues.add(readQueue(xmlr, serviceClient));
            eventType = xmlr.next();
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, QueueConstants.QUEUES_ELEMENT);
        return queues;
    }

    /**
     * Private Default Ctor.
     */
    private QueueDeserializationHelper() {
        // No op
    }

}
