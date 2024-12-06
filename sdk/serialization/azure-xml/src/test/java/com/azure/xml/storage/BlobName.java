// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

public class BlobName implements XmlSerializable<BlobName> {
    /*
     * Indicates if the blob name is encoded.
     */
    private Boolean encoded;

    /*
     * The name of the blob.
     */
    private String content;

    /**
     * Get the encoded property: Indicates if the blob name is encoded.
     *
     * @return the encoded value.
     */
    public Boolean isEncoded() {
        return this.encoded;
    }

    /**
     * Set the encoded property: Indicates if the blob name is encoded.
     *
     * @param encoded the encoded value to set.
     * @return the BlobName object itself.
     */
    public BlobName setEncoded(Boolean encoded) {
        this.encoded = encoded;
        return this;
    }

    /**
     * Get the content property: The name of the blob.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: The name of the blob.
     *
     * @param content the content value to set.
     * @return the BlobName object itself.
     */
    public BlobName setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        xmlWriter.writeStartElement(getRootElementName(rootElementName, "Name"));
        xmlWriter.writeBooleanAttribute("Encoded", encoded);

        xmlWriter.writeString(content);

        return xmlWriter.writeEndElement();
    }

    public static BlobName fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static BlobName fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        // BEGIN: com.azure.xml.XmlReader.readObject#String-ReadValueCallback
        return xmlReader.readObject(getRootElementName(rootElementName, "Name"), reader -> {
            BlobName result = new BlobName();
            result.encoded = reader.getNullableAttribute(null, "Encoded", Boolean::parseBoolean);
            result.content = reader.getStringElement();

            return result;
        });
        // END: com.azure.xml.XmlReader.readObject#String-ReadValueCallback
    }
}
