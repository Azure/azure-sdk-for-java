// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlWriter;

public class BlobName implements XmlSerializable<BlobName> {
    /*
     * Indicates if the blob name is encoded.
     */
    private Boolean encoded;

    /*
     * The name of the blob.
     */
    private String content;

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        xmlWriter.writeStartElement("BlobName");
        xmlWriter.writeBooleanAttribute("Encoded", encoded);

        xmlWriter.writeString(content);

        return xmlWriter.writeEndElement();
    }

    public static BlobName fromXml(XmlReader xmlReader) {
        return xmlReader.readObject("BlobName", reader -> {
            BlobName result = new BlobName();
            result.encoded = reader.getAttributeNullableValue(null, "Encoded", Boolean::parseBoolean);
            result.content = reader.getElementStringValue();

            return result;
        });
    }
}
