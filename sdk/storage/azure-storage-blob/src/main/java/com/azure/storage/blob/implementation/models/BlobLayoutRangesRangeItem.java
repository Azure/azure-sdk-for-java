// MICROSOFT_MIT_SMALLgit status

package com.azure.storage.blob.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;
import javax.xml.stream.XMLStreamException;

/**
 * The BlobLayoutRangesRangeItem model.
 */
@Fluent
public final class BlobLayoutRangesRangeItem implements XmlSerializable<BlobLayoutRangesRangeItem> {
    /*
     * The start byte offset of the range.
     */
    @Generated
    private long start;

    /*
     * The end byte offset of the range.
     */
    @Generated
    private long end;

    /*
     * Index into the Endpoints array indicating which endpoint serves this range.
     */
    @Generated
    private int endpointIndex;

    /**
     * Creates an instance of BlobLayoutRangesRangeItem class.
     */
    @Generated
    public BlobLayoutRangesRangeItem() {
    }

    /**
     * Get the start property: The start byte offset of the range.
     * 
     * @return the start value.
     */
    @Generated
    public long getStart() {
        return this.start;
    }

    /**
     * Set the start property: The start byte offset of the range.
     * 
     * @param start the start value to set.
     * @return the BlobLayoutRangesRangeItem object itself.
     */
    @Generated
    public BlobLayoutRangesRangeItem setStart(long start) {
        this.start = start;
        return this;
    }

    /**
     * Get the end property: The end byte offset of the range.
     * 
     * @return the end value.
     */
    @Generated
    public long getEnd() {
        return this.end;
    }

    /**
     * Set the end property: The end byte offset of the range.
     * 
     * @param end the end value to set.
     * @return the BlobLayoutRangesRangeItem object itself.
     */
    @Generated
    public BlobLayoutRangesRangeItem setEnd(long end) {
        this.end = end;
        return this;
    }

    /**
     * Get the endpointIndex property: Index into the Endpoints array indicating which endpoint serves this range.
     * 
     * @return the endpointIndex value.
     */
    @Generated
    public int getEndpointIndex() {
        return this.endpointIndex;
    }

    /**
     * Set the endpointIndex property: Index into the Endpoints array indicating which endpoint serves this range.
     * 
     * @param endpointIndex the endpointIndex value to set.
     * @return the BlobLayoutRangesRangeItem object itself.
     */
    @Generated
    public BlobLayoutRangesRangeItem setEndpointIndex(int endpointIndex) {
        this.endpointIndex = endpointIndex;
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
        rootElementName = rootElementName == null || rootElementName.isEmpty() ? "Range" : rootElementName;
        xmlWriter.writeStartElement(rootElementName);
        xmlWriter.writeLongAttribute("Start", this.start);
        xmlWriter.writeLongAttribute("End", this.end);
        xmlWriter.writeIntAttribute("EndpointIndex", this.endpointIndex);
        return xmlWriter.writeEndElement();
    }

    /**
     * Reads an instance of BlobLayoutRangesRangeItem from the XmlReader.
     * 
     * @param xmlReader The XmlReader being read.
     * @return An instance of BlobLayoutRangesRangeItem if the XmlReader was pointing to an instance of it, or null if
     * it was pointing to XML null.
     * @throws XMLStreamException If an error occurs while reading the BlobLayoutRangesRangeItem.
     */
    @Generated
    public static BlobLayoutRangesRangeItem fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    /**
     * Reads an instance of BlobLayoutRangesRangeItem from the XmlReader.
     * 
     * @param xmlReader The XmlReader being read.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can deserialize from different root element names.
     * @return An instance of BlobLayoutRangesRangeItem if the XmlReader was pointing to an instance of it, or null if
     * it was pointing to XML null.
     * @throws XMLStreamException If an error occurs while reading the BlobLayoutRangesRangeItem.
     */
    @Generated
    public static BlobLayoutRangesRangeItem fromXml(XmlReader xmlReader, String rootElementName)
        throws XMLStreamException {
        String finalRootElementName = rootElementName == null || rootElementName.isEmpty() ? "Range" : rootElementName;
        return xmlReader.readObject(finalRootElementName, reader -> {
            BlobLayoutRangesRangeItem deserializedBlobLayoutRangesRangeItem = new BlobLayoutRangesRangeItem();
            deserializedBlobLayoutRangesRangeItem.start = reader.getLongAttribute(null, "Start");
            deserializedBlobLayoutRangesRangeItem.end = reader.getLongAttribute(null, "End");
            deserializedBlobLayoutRangesRangeItem.endpointIndex = reader.getIntAttribute(null, "EndpointIndex");
            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                reader.skipElement();
            }

            return deserializedBlobLayoutRangesRangeItem;
        });
    }
}
