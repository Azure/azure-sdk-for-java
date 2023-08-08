// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

public class BlobTag implements XmlSerializable<BlobTag> {
    /*
     * The Key property.
     */
    private String key;

    /*
     * The Value property.
     */
    private String value;

    /**
     * Get the key property: The Key property.
     *
     * @return the key value.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Set the key property: The Key property.
     *
     * @param key the key value to set.
     * @return the BlobTag object itself.
     */
    public BlobTag setKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * Get the value property: The Value property.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value property: The Value property.
     *
     * @param value the value value to set.
     * @return the BlobTag object itself.
     */
    public BlobTag setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        return xmlWriter.writeStartElement(getRootElementName(rootElementName, "Tag"))
            .writeStringElement("Key", key)
            .writeStringElement("Value", value)
            .writeEndElement();
    }

    public static BlobTag fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static BlobTag fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        return xmlReader.readObject(getRootElementName(rootElementName, "Tag"), reader -> {
            BlobTag deserialized = new BlobTag();

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("Key".equals(elementName)) {
                    deserialized.key = reader.getStringElement();
                } else if ("Value".equals(elementName)) {
                    deserialized.value = reader.getStringElement();
                }
            }

            return deserialized;
        });
    }
}
