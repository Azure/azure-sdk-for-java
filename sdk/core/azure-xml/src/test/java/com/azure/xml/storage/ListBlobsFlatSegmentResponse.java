// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

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

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
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

    public static ListBlobsFlatSegmentResponse fromXml(XmlReader xmlReader) {
        return xmlReader.readObject("EnumerationResults", reader -> {
            ListBlobsFlatSegmentResponse deserialized = new ListBlobsFlatSegmentResponse();

            deserialized.serviceEndpoint = reader.getAttributeStringValue(null, "ServiceEndpoint");
            deserialized.containerName = reader.getAttributeStringValue(null, "ContainerName");

            boolean segmentFound = false;

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("Prefix".equals(elementName)) {
                    deserialized.prefix = reader.getElementStringValue();
                } else if ("Marker".equals(elementName)) {
                    deserialized.marker = reader.getElementStringValue();
                } else if ("MaxResults".equals(elementName)) {
                    deserialized.maxResults = reader.getElementIntValue();
                } else if ("Blobs".equals(elementName)) {
                    deserialized.segment = BlobFlatListSegment.fromXml(reader);
                    segmentFound = true;
                } else if ("NextMarker".equals(elementName)) {
                    deserialized.nextMarker = reader.getElementStringValue();
                }
            }

            if (segmentFound) {
                return deserialized;
            }

            throw new IllegalStateException("Missing required properties");
        });
    }
}
