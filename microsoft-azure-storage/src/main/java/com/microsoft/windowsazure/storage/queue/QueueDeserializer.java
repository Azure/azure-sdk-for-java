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
package com.microsoft.windowsazure.storage.queue;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageUri;
import com.microsoft.windowsazure.storage.core.DeserializationHelper;
import com.microsoft.windowsazure.storage.core.ListResponse;
import com.microsoft.windowsazure.storage.core.PathUtility;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to parse a message list response stream.
 */
final class QueueDeserializer {
    /**
     * Parses the input stream containing the response body of the list queues
     * request result and populates the class data.
     * 
     * @param serviceClient
     *            A {@link CloudQueueClient} object associated with the storage
     *            service.
     * 
     * @throws XMLStreamException
     *             If the input stream cannot be read or parsed as a list queues
     *             response.
     * @throws StorageException
     */
    public static ListResponse<CloudQueue> getQueues(final InputStream stream, final CloudQueueClient serviceClient)
            throws XMLStreamException, StorageException {
        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(stream);

        ListResponse<CloudQueue> queues = new ListResponse<CloudQueue>();

        String tempParseString = null;

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // 1. get enumerationResults Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, "EnumerationResults");

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.MAX_RESULTS_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.MAX_RESULTS_ELEMENT);
                    queues.setMaxResults(Integer.parseInt(tempParseString));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.MARKER_ELEMENT)) {
                    queues.setMarker(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.MARKER_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.NEXT_MARKER_ELEMENT)) {
                    queues.setNextMarker(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.NEXT_MARKER_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.PREFIX_ELEMENT)) {
                    queues.setPrefix(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.PREFIX_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(QueueConstants.QUEUES_ELEMENT)) {
                    try {
                        queues.setResults(readQueues(xmlr, serviceClient));
                    }
                    catch (final URISyntaxException e) {
                        throw new XMLStreamException(e);
                    }
                    catch (final ParseException e) {
                        throw new XMLStreamException(e);
                    }

                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, QueueConstants.QUEUES_ELEMENT);
                    // eventType = xmlr.next();
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT && "EnumerationResults".equals(name)) {
                    break;
                }
            }
        }

        return queues;
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
    private static CloudQueue readQueue(final XMLStreamReader xmlr, final CloudQueueClient serviceClient)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, QueueConstants.QUEUE_ELEMENT);

        String queueName = null;
        HashMap<String, String> queueMetadata = null;

        int eventType = xmlr.getEventType();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(Constants.NAME_ELEMENT)) {
                    queueName = DeserializationHelper.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT);
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

        StorageUri queueUri = serviceClient.getStorageUri();

        queueUri = PathUtility.appendPathToUri(queueUri, queueName);

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
    private static ArrayList<CloudQueue> readQueues(final XMLStreamReader xmlr, final CloudQueueClient serviceClient)
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
    protected static ArrayList<CloudQueueMessage> readMessages(final InputStream stream,
            final boolean shouldEncodeMessage) throws XMLStreamException, ParseException, URISyntaxException,
            StorageException {
        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(stream);

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, QueueConstants.QUEUE_MESSAGES_LIST_ELEMENT);

        final ArrayList<CloudQueueMessage> messages = new ArrayList<CloudQueueMessage>();

        eventType = xmlr.next();
        while (eventType == XMLStreamConstants.START_ELEMENT && xmlr.hasName()
                && QueueConstants.QUEUE_MESSAGE_ELEMENT.equals(xmlr.getName().toString())) {
            messages.add(readMessage(xmlr, shouldEncodeMessage));
            eventType = xmlr.next();
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, QueueConstants.QUEUE_MESSAGES_LIST_ELEMENT);
        return messages;
    }

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
    private static CloudQueueMessage readMessage(final XMLStreamReader xmlr, final boolean shouldEncodeMessage)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, QueueConstants.QUEUE_MESSAGE_ELEMENT);

        final CloudQueueMessage message = new CloudQueueMessage();
        message.setMessageType(shouldEncodeMessage ? QueueMessageType.BASE_64_ENCODED : QueueMessageType.RAW_STRING);

        int eventType = xmlr.getEventType();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(QueueConstants.MESSAGE_ID_ELEMENT)) {
                    message.setMessageId(DeserializationHelper.readElementFromXMLReader(xmlr,
                            QueueConstants.MESSAGE_ID_ELEMENT));
                }
                else if (name.equals(QueueConstants.INSERTION_TIME_ELEMENT)) {
                    message.setInsertionTime(Utility.parseRFC1123DateFromStringInGMT(DeserializationHelper
                            .readElementFromXMLReader(xmlr, QueueConstants.INSERTION_TIME_ELEMENT)));
                }
                else if (name.equals(QueueConstants.EXPIRATION_TIME_ELEMENT)) {
                    message.setExpirationTime(Utility.parseRFC1123DateFromStringInGMT(DeserializationHelper
                            .readElementFromXMLReader(xmlr, QueueConstants.EXPIRATION_TIME_ELEMENT)));
                }
                else if (name.equals(QueueConstants.POP_RECEIPT_ELEMENT)) {
                    message.setPopReceipt(DeserializationHelper.readElementFromXMLReader(xmlr,
                            QueueConstants.POP_RECEIPT_ELEMENT));
                }
                else if (name.equals(QueueConstants.TIME_NEXT_VISIBLE_ELEMENT)) {
                    message.setNextVisibleTime(Utility.parseRFC1123DateFromStringInGMT(DeserializationHelper
                            .readElementFromXMLReader(xmlr, QueueConstants.TIME_NEXT_VISIBLE_ELEMENT)));
                }
                else if (name.equals(QueueConstants.DEQUEUE_COUNT_ELEMENT)) {
                    message.setDequeueCount(Integer.parseInt(DeserializationHelper.readElementFromXMLReader(xmlr,
                            QueueConstants.DEQUEUE_COUNT_ELEMENT)));
                }
                else if (name.equals(QueueConstants.MESSAGE_TEXT_ELEMENT)) {
                    message.messageContent = DeserializationHelper.readElementFromXMLReader(xmlr,
                            QueueConstants.MESSAGE_TEXT_ELEMENT);
                }

            }
            else if (eventType == XMLStreamConstants.END_ELEMENT && name.equals(QueueConstants.QUEUE_MESSAGE_ELEMENT)) {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, QueueConstants.QUEUE_MESSAGE_ELEMENT);
        return message;
    }
}
