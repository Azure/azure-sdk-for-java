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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. Provides methods for parsing the response stream
 * from a queue listing operation.
 */
final class ListQueuesResponse {

    /**
     * The list of queues from the response to a list queues operation.
     */
    private ArrayList<CloudQueue> queues = new ArrayList<CloudQueue>();

    /**
     * A value indicating if the response has been fully parsed.
     */
    private boolean isParsed;

    /**
     * The marker value from the response.
     */
    private String marker;

    /**
     * The max results value from the response.
     */
    private int maxResults;

    /**
     * The next marker value from the response, to be passed as the marker value
     * in the next queue listing request.
     */
    private String nextMarker;

    /**
     * The container prefix value from the response.
     */
    private String prefix;

    /**
     * The <code>InputStream</code> containing the response body from a queue
     * listing operation.
     */
    private final InputStream streamRef;

    /**
     * Class constructor specifying an input stream created from the response
     * body to a list queues operation.
     * 
     * @param stream
     *            An <code>InputStream</code> to parse for the results of a list
     *            queues operation.
     */
    public ListQueuesResponse(final InputStream stream) {
        this.streamRef = stream;
    }

    /**
     * Gets the value of the Marker element in the response to a list queues
     * operation. This value is not initialized until {@link #getQueues(CloudQueueClient)} or
     * {@link #parseResponse(CloudQueueClient)} has been called, and is only
     * present if the list queues request specified the marker.
     * 
     * @return A <code>String</code> containing the value of the Marker element
     *         in the response.
     */
    public String getMarker() {
        return this.marker;
    }

    /**
     * Gets the value of the MaxResults element in the response to a list queues
     * operation. This value is not initialized until {@link #getQueues(CloudQueueClient)} or
     * {@link #parseResponse(CloudQueueClient)} has been called, and is only
     * present if the request specified a maxresults value.
     * 
     * @return An <code>int</code> containing the value of the MaxResults
     *         element in the response.
     */
    public int getMaxResults() {
        return this.maxResults;
    }

    /**
     * Gets the value of the NextMarker element in the response to a list queues
     * operation. This value is not initialized until {@link #getQueues(CloudQueueClient)} or
     * {@link #parseResponse(CloudQueueClient)} has been called, and is only
     * present when maxresults was specified in the request, and more results
     * are available that have not yet been returned. This value may be sent as
     * the marker value in a list queues request to retrieve the next set of
     * queues.
     * 
     * @return A <code>String</code> containing the value of the NextMarker
     *         element in the response.
     */
    public String getNextMarker() {
        return this.nextMarker;
    }

    /**
     * Gets the value of the Prefix element in the response to a list queues
     * operation. This value is not initialized until {@link #getQueues(CloudQueueClient)} or
     * {@link #parseResponse(CloudQueueClient)} has been called, and is only
     * present if the request specified a prefix value. The list queues result
     * is filtered to return only queues with names that begin with the
     * specified prefix.
     * 
     * @return A <code>String</code> containing the value of the Prefix element
     *         in the response.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Gets the list of queues returned by the list queues request as an <code>ArrayList</code> of (@link CloudQueue}
     * objects.
     * 
     * @param serviceClient
     *            A {@link CloudQueueClient} object associated with the storage
     *            service.
     * 
     * @return an <code>ArrayList</code> of {@link CloudQueue} objects returned
     *         by the list queues operation.
     * 
     * @throws XMLStreamException
     * @throws StorageException
     */
    public ArrayList<CloudQueue> getQueues(final CloudQueueClient serviceClient) throws XMLStreamException,
            StorageException {
        if (!this.isParsed) {
            this.parseResponse(serviceClient);
        }

        return this.queues;
    }

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
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.MARKER_ELEMENT)) {
                    this.marker = Utility.readElementFromXMLReader(xmlr, Constants.MARKER_ELEMENT);
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.NEXT_MARKER_ELEMENT)) {
                    this.nextMarker = Utility.readElementFromXMLReader(xmlr, Constants.NEXT_MARKER_ELEMENT);
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.PREFIX_ELEMENT)) {
                    this.prefix = Utility.readElementFromXMLReader(xmlr, Constants.PREFIX_ELEMENT);
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(QueueConstants.QUEUES_ELEMENT)) {
                    try {
                        this.queues = QueueDeserializationHelper.readQueues(xmlr, serviceClient);
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

        this.isParsed = true;
    }

}
