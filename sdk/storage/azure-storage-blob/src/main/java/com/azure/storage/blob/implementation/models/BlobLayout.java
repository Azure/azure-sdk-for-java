// MICROSOFT_MIT_SMALLgit status

package com.azure.storage.blob.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * The BlobLayout model.
 */
@Fluent
public final class BlobLayout implements XmlSerializable<BlobLayout> {
    /*
     * The Ranges property.
     */
    @Generated
    private BlobLayoutRanges ranges;

    /*
     * The Endpoints property.
     */
    @Generated
    private BlobLayoutEndpoints endpoints;

    /*
     * The continuation marker used for this request.
     */
    @Generated
    private String marker;

    /*
     * If the number of ranges exceeds MaxResults, a NextMarker is returned for use in subsequent requests to continue
     * listing.
     */
    @Generated
    private String nextMarker;

    /*
     * The maximum number of ranges to return per request.
     */
    @Generated
    private Integer maxResults;

    /**
     * Creates an instance of BlobLayout class.
     */
    @Generated
    public BlobLayout() {
    }

    /**
     * Get the ranges property: The Ranges property.
     * 
     * @return the ranges value.
     */
    @Generated
    public BlobLayoutRanges getRanges() {
        return this.ranges;
    }

    /**
     * Set the ranges property: The Ranges property.
     * 
     * @param ranges the ranges value to set.
     * @return the BlobLayout object itself.
     */
    @Generated
    public BlobLayout setRanges(BlobLayoutRanges ranges) {
        this.ranges = ranges;
        return this;
    }

    /**
     * Get the endpoints property: The Endpoints property.
     * 
     * @return the endpoints value.
     */
    @Generated
    public BlobLayoutEndpoints getEndpoints() {
        return this.endpoints;
    }

    /**
     * Set the endpoints property: The Endpoints property.
     * 
     * @param endpoints the endpoints value to set.
     * @return the BlobLayout object itself.
     */
    @Generated
    public BlobLayout setEndpoints(BlobLayoutEndpoints endpoints) {
        this.endpoints = endpoints;
        return this;
    }

    /**
     * Get the marker property: The continuation marker used for this request.
     * 
     * @return the marker value.
     */
    @Generated
    public String getMarker() {
        return this.marker;
    }

    /**
     * Set the marker property: The continuation marker used for this request.
     * 
     * @param marker the marker value to set.
     * @return the BlobLayout object itself.
     */
    @Generated
    public BlobLayout setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    /**
     * Get the nextMarker property: If the number of ranges exceeds MaxResults, a NextMarker is returned for use in
     * subsequent requests to continue listing.
     * 
     * @return the nextMarker value.
     */
    @Generated
    public String getNextMarker() {
        return this.nextMarker;
    }

    /**
     * Set the nextMarker property: If the number of ranges exceeds MaxResults, a NextMarker is returned for use in
     * subsequent requests to continue listing.
     * 
     * @param nextMarker the nextMarker value to set.
     * @return the BlobLayout object itself.
     */
    @Generated
    public BlobLayout setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
        return this;
    }

    /**
     * Get the maxResults property: The maximum number of ranges to return per request.
     * 
     * @return the maxResults value.
     */
    @Generated
    public Integer getMaxResults() {
        return this.maxResults;
    }

    /**
     * Set the maxResults property: The maximum number of ranges to return per request.
     * 
     * @param maxResults the maxResults value to set.
     * @return the BlobLayout object itself.
     */
    @Generated
    public BlobLayout setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @Generated
    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Generated
    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        rootElementName = rootElementName == null || rootElementName.isEmpty() ? "BlobLayout" : rootElementName;
        xmlWriter.writeStartElement(rootElementName);
        xmlWriter.writeXml(this.ranges, "Ranges");
        xmlWriter.writeXml(this.endpoints, "Endpoints");
        xmlWriter.writeStringElement("Marker", this.marker);
        xmlWriter.writeStringElement("NextMarker", this.nextMarker);
        xmlWriter.writeNumberElement("MaxResults", this.maxResults);
        return xmlWriter.writeEndElement();
    }

    /**
     * Reads an instance of BlobLayout from the XmlReader.
     * 
     * @param xmlReader The XmlReader being read.
     * @return An instance of BlobLayout if the XmlReader was pointing to an instance of it, or null if it was pointing
     * to XML null.
     * @throws XMLStreamException If an error occurs while reading the BlobLayout.
     */
    @Generated
    public static BlobLayout fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    /**
     * Reads an instance of BlobLayout from the XmlReader.
     * 
     * @param xmlReader The XmlReader being read.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can deserialize from different root element names.
     * @return An instance of BlobLayout if the XmlReader was pointing to an instance of it, or null if it was pointing
     * to XML null.
     * @throws XMLStreamException If an error occurs while reading the BlobLayout.
     */
    @Generated
    public static BlobLayout fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        String finalRootElementName
            = rootElementName == null || rootElementName.isEmpty() ? "BlobLayout" : rootElementName;
        return xmlReader.readObject(finalRootElementName, reader -> {
            BlobLayout deserializedBlobLayout = new BlobLayout();
            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                QName elementName = reader.getElementName();

                if ("Ranges".equals(elementName.getLocalPart())) {
                    deserializedBlobLayout.ranges = BlobLayoutRanges.fromXml(reader, "Ranges");
                } else if ("Endpoints".equals(elementName.getLocalPart())) {
                    deserializedBlobLayout.endpoints = BlobLayoutEndpoints.fromXml(reader, "Endpoints");
                } else if ("Marker".equals(elementName.getLocalPart())) {
                    deserializedBlobLayout.marker = reader.getStringElement();
                } else if ("NextMarker".equals(elementName.getLocalPart())) {
                    deserializedBlobLayout.nextMarker = reader.getStringElement();
                } else if ("MaxResults".equals(elementName.getLocalPart())) {
                    deserializedBlobLayout.maxResults = reader.getNullableElement(Integer::parseInt);
                } else {
                    reader.skipElement();
                }
            }

            return deserializedBlobLayout;
        });
    }
}
