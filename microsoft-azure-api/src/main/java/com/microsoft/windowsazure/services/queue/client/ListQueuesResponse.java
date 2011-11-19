package com.microsoft.windowsazure.services.queue.client;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class for parsing a list queues response stream.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
final class ListQueuesResponse {

    /**
     * Holds the list of queues.
     */
    private ArrayList<CloudQueue> queues = new ArrayList<CloudQueue>();

    /**
     * Stores the value indicating if the response has been fully parsed.
     */
    private boolean isParsed;

    /**
     * Stores the marker.
     */
    private String marker;

    /**
     * Stores the max results.
     */
    private int maxResults;

    /**
     * Stores the next marker.
     */
    private String nextMarker;

    /**
     * Stores the container prefix.
     */
    private String prefix;

    /**
     * Stores the InputStream to read from.
     */
    private final InputStream streamRef;

    public ListQueuesResponse(final InputStream stream) {
        this.streamRef = stream;
    }

    public String getMarker() {
        return this.marker;
    }

    public int getMaxResults() {
        return this.maxResults;
    }

    public String getNextMarker() {
        return this.nextMarker;
    }

    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Returns an ArrayList of CloudQueue
     * 
     * @param serviceClient
     *            a reference to the client object associated with this object.
     * @return an ArrayList of CloudQueue
     * @throws XMLStreamException
     * @throws StorageException
     */
    public ArrayList<CloudQueue> getQueues(final CloudQueueClient serviceClient)
            throws XMLStreamException, StorageException {
        if (!this.isParsed) {
            this.parseResponse(serviceClient);
        }

        return this.queues;
    }

    /**
     * Parses the XML stream.
     * 
     * @param serviceClient
     *            a reference to the client object associated with this object.
     * @throws XMLStreamException
     * @throws StorageException
     */
    public void parseResponse(final CloudQueueClient serviceClient) throws XMLStreamException, StorageException {
        final XMLStreamReader xmlr = Utility.createXMLStreamReaderFromStream(this.streamRef);
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
                    tempParseString = Utility.readElementFromXMLReader(xmlr, Constants.MAX_RESULTS_ELEMENT);
                    this.maxResults = Integer.parseInt(tempParseString);
                } else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.MARKER_ELEMENT)) {
                    this.marker = Utility.readElementFromXMLReader(xmlr, Constants.MARKER_ELEMENT);
                } else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.NEXT_MARKER_ELEMENT)) {
                    this.nextMarker = Utility.readElementFromXMLReader(xmlr, Constants.NEXT_MARKER_ELEMENT);
                } else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.PREFIX_ELEMENT)) {
                    this.prefix = Utility.readElementFromXMLReader(xmlr, Constants.PREFIX_ELEMENT);
                } else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(QueueConstants.QUEUES_ELEMENT)) {
                    try {
                        this.queues = QueueDeserializationHelper.readQueues(xmlr, serviceClient);
                    } catch (final URISyntaxException e) {
                        throw new XMLStreamException(e);
                    } catch (final ParseException e) {
                        throw new XMLStreamException(e);
                    }

                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, QueueConstants.QUEUES_ELEMENT);
                    // eventType = xmlr.next();
                } else if (eventType == XMLStreamConstants.END_ELEMENT && "EnumerationResults".equals(name)) {
                    break;
                }
            }
        }

        this.isParsed = true;
    }

}
