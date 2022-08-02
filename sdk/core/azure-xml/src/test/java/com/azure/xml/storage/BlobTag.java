// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

public class BlobTag implements XmlSerializable<BlobTag> {
    /*
     * The Key property.
     */
    private String key;

    /*
     * The Value property.
     */
    private String value;

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        xmlWriter.writeStartElement("Tag");

        xmlWriter.writeStringElement("Key", key);
        xmlWriter.writeStringElement("Value", value);

        return xmlWriter.writeEndElement();
    }

    public static BlobTag fromXml(XmlReader xmlReader) {
        return xmlReader.readObject("Tag", reader -> {
            BlobTag deserialized = new BlobTag();

            boolean keyFound = false;
            boolean valueFound = false;

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("Key".equals(elementName)) {
                    deserialized.key = reader.getElementStringValue();
                    keyFound = true;
                } else if ("Value".equals(elementName)) {
                    deserialized.value = reader.getElementStringValue();
                    valueFound = true;
                }
            }

            if (keyFound && valueFound) {
                return deserialized;
            }

            throw new IllegalStateException("Missing required properties.");
        });
    }
}
