// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;

public class ListBlobsFlatSegmentResponse implements XmlSerializable<ListBlobsFlatSegmentResponse> {
    /*
     * The ServiceEndpoint property.
     */
    private String serviceEndpoint;

    /*
     * The ContainerName property.
     */
    private String containerName;

    /*
     * The Prefix property.
     */
    private String prefix;

    /*
     * The Marker property.
     */
    private String marker;

    /*
     * The MaxResults property.
     */
    private int maxResults;

    /*
     * The Segment property.
     */
    private BlobFlatListSegment segment;

    /*
     * The NextMarker property.
     */
    private String nextMarker;

    /**
     * Get the serviceEndpoint property: The ServiceEndpoint property.
     *
     * @return the serviceEndpoint value.
     */
    public String getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    /**
     * Set the serviceEndpoint property: The ServiceEndpoint property.
     *
     * @param serviceEndpoint the serviceEndpoint value to set.
     * @return the ListBlobsFlatSegmentResponse object itself.
     */
    public ListBlobsFlatSegmentResponse setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
        return this;
    }

    /**
     * Get the containerName property: The ContainerName property.
     *
     * @return the containerName value.
     */
    public String getContainerName() {
        return this.containerName;
    }

    /**
     * Set the containerName property: The ContainerName property.
     *
     * @param containerName the containerName value to set.
     * @return the ListBlobsFlatSegmentResponse object itself.
     */
    public ListBlobsFlatSegmentResponse setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Get the prefix property: The Prefix property.
     *
     * @return the prefix value.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Set the prefix property: The Prefix property.
     *
     * @param prefix the prefix value to set.
     * @return the ListBlobsFlatSegmentResponse object itself.
     */
    public ListBlobsFlatSegmentResponse setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Get the marker property: The Marker property.
     *
     * @return the marker value.
     */
    public String getMarker() {
        return this.marker;
    }

    /**
     * Set the marker property: The Marker property.
     *
     * @param marker the marker value to set.
     * @return the ListBlobsFlatSegmentResponse object itself.
     */
    public ListBlobsFlatSegmentResponse setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    /**
     * Get the maxResults property: The MaxResults property.
     *
     * @return the maxResults value.
     */
    public int getMaxResults() {
        return this.maxResults;
    }

    /**
     * Set the maxResults property: The MaxResults property.
     *
     * @param maxResults the maxResults value to set.
     * @return the ListBlobsFlatSegmentResponse object itself.
     */
    public ListBlobsFlatSegmentResponse setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Get the segment property: The Segment property.
     *
     * @return the segment value.
     */
    public BlobFlatListSegment getSegment() {
        return this.segment;
    }

    /**
     * Set the segment property: The Segment property.
     *
     * @param segment the segment value to set.
     * @return the ListBlobsFlatSegmentResponse object itself.
     */
    public ListBlobsFlatSegmentResponse setSegment(BlobFlatListSegment segment) {
        this.segment = segment;
        return this;
    }

    /**
     * Get the nextMarker property: The NextMarker property.
     *
     * @return the nextMarker value.
     */
    public String getNextMarker() {
        return this.nextMarker;
    }

    /**
     * Set the nextMarker property: The NextMarker property.
     *
     * @param nextMarker the nextMarker value to set.
     * @return the ListBlobsFlatSegmentResponse object itself.
     */
    public ListBlobsFlatSegmentResponse setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement("EnumerationResults");
        xmlWriter.writeStringAttribute("ServiceEndpoint", serviceEndpoint);
        xmlWriter.writeStringAttribute("ContainerName", containerName);

        if (prefix == null) {
            xmlWriter.writeStartSelfClosingElement("Prefix");
        } else {
            xmlWriter.writeStringElement("Prefix", prefix);
        }

        if (marker == null) {
            xmlWriter.writeStartSelfClosingElement("Marker");
        } else {
            xmlWriter.writeStringElement("Marker", marker);
        }

        xmlWriter.writeIntElement("MaxResults", maxResults);
        xmlWriter.writeXml(segment);

        if (nextMarker == null) {
            xmlWriter.writeStartSelfClosingElement("NextMarker");
        } else {
            xmlWriter.writeStringElement("NextMarker", nextMarker);
        }

        return xmlWriter.writeEndElement();
    }

    public static ListBlobsFlatSegmentResponse fromXml(XmlReader xmlReader) throws XMLStreamException {
        return xmlReader.readObject("EnumerationResults", reader -> {
            ListBlobsFlatSegmentResponse deserialized = new ListBlobsFlatSegmentResponse();

            deserialized.serviceEndpoint = reader.getStringAttribute(null, "ServiceEndpoint");
            deserialized.containerName = reader.getStringAttribute(null, "ContainerName");

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("Prefix".equals(elementName)) {
                    deserialized.prefix = reader.getStringElement();
                } else if ("Marker".equals(elementName)) {
                    deserialized.marker = reader.getStringElement();
                } else if ("MaxResults".equals(elementName)) {
                    deserialized.maxResults = reader.getIntElement();
                } else if ("Blobs".equals(elementName)) {
                    deserialized.segment = BlobFlatListSegment.fromXml(reader);
                } else if ("NextMarker".equals(elementName)) {
                    deserialized.nextMarker = reader.getStringElement();
                }
            }

            return deserialized;
        });
    }
}
