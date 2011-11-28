package com.microsoft.windowsazure.services.blob.client;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to parse a get block list response stream.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
final class GetBlockListResponse {
    /**
     * Holds the ArrayList of CloudBlobs from the response.
     */
    private final ArrayList<BlockEntry> blocks = new ArrayList<BlockEntry>();

    /**
     * Stores the value indicating if the response has been fully parsed.
     */
    private boolean isParsed;

    /**
     * Stores the InputStream to read from.
     */
    private final InputStream streamRef;

    /**
     * Constructs the response from the inputstream.
     * 
     * @param stream
     *            the inputstream for the response the server returned.
     */
    public GetBlockListResponse(final InputStream stream) {
        this.streamRef = stream;
    }

    /**
     * Returns an ArrayList of BlockEntrys for the given block blob.
     * 
     * @return an ArrayList of BlockEntrys for the given block blob.
     * @throws XMLStreamException
     * @throws StorageException
     */
    public ArrayList<BlockEntry> getBlocks() throws XMLStreamException, StorageException {
        if (!this.isParsed) {
            this.parseResponse();
        }

        return this.blocks;
    }

    /**
     * Parses the XML stream.
     * 
     * @throws XMLStreamException
     * @throws StorageException
     */
    public void parseResponse() throws XMLStreamException, StorageException {
        final XMLStreamReader xmlr = Utility.createXMLStreamReaderFromStream(this.streamRef);

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // 1. get BlockList Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.BLOCK_LIST_ELEMENT);

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (name.equals(BlobConstants.COMMITTED_BLOCKS_ELEMENT)) {
                    // Move to block element
                    eventType = xmlr.next();
                    if (eventType == XMLStreamConstants.START_ELEMENT
                            && BlobConstants.BLOCK_ELEMENT.equals(xmlr.getName().toString())) {
                        this.blocks.addAll(BlobDeserializationHelper.readBlobBlocks(xmlr, BlockSearchMode.COMMITTED));
                        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.COMMITTED_BLOCKS_ELEMENT);
                    }
                }
                else if (name.equals(BlobConstants.UNCOMMITTED_BLOCKS_ELEMENT)) {
                    // Move to block element
                    eventType = xmlr.next();
                    if (eventType == XMLStreamConstants.START_ELEMENT
                            && BlobConstants.BLOCK_ELEMENT.equals(xmlr.getName().toString())) {
                        this.blocks.addAll(BlobDeserializationHelper.readBlobBlocks(xmlr, BlockSearchMode.UNCOMMITTED));
                        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.UNCOMMITTED_BLOCKS_ELEMENT);
                    }
                }
                else if (name.equals(BlobConstants.BLOCK_LIST_ELEMENT) && eventType == XMLStreamConstants.END_ELEMENT) {
                    break;
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            "The response recieved is invalid or improperly formatted.",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
        }

        this.isParsed = true;
    }
}
