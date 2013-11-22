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
package com.microsoft.windowsazure.storage.blob;

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
 * RESERVED FOR INTERNAL USE. A class used to parse a list blobs response stream.
 */
final class ListBlobsResponse {

    /**
     * Holds the ArrayList of CloudBlobs from the response.
     */
    private ArrayList<ListBlobItem> blobs = new ArrayList<ListBlobItem>();

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

    /**
     * Stores the blob delimiter.
     */
    private String delimiter;

    public ListBlobsResponse(final InputStream stream) {
        this.streamRef = stream;
    }

    public ArrayList<ListBlobItem> getBlobs(final CloudBlobClient serviceClient, final CloudBlobContainer container)
            throws XMLStreamException, StorageException {
        if (!this.isParsed) {
            this.parseResponse(serviceClient, container);
        }

        return this.blobs;
    }

    public String getDelimiter() {
        return this.delimiter;
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

    public void parseResponse(final CloudBlobClient serviceClient, final CloudBlobContainer container)
            throws XMLStreamException, StorageException {
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
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.DELIMITER_ELEMENT)) {
                    this.delimiter = Utility.readElementFromXMLReader(xmlr, Constants.DELIMITER_ELEMENT);
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(BlobConstants.BLOBS_ELEMENT)) {
                    try {
                        this.blobs = BlobDeserializationHelper.readBlobItems(xmlr, serviceClient, container);
                    }
                    catch (final URISyntaxException e) {
                        throw new XMLStreamException(e);
                    }
                    catch (final ParseException e) {
                        throw new XMLStreamException(e);
                    }

                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.BLOBS_ELEMENT);
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT && "EnumerationResults".equals(name)) {
                    break;
                }
            }
        }

        this.isParsed = true;
    }
}
