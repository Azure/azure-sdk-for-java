// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import java.util.ArrayList;
import java.util.List;

public class BlobFlatListSegment implements XmlSerializable<BlobFlatListSegment> {
    /*
     * The BlobItems property.
     */
    private List<BlobItemInternal> blobItems = new ArrayList<>();

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        xmlWriter.writeStartElement("Blobs");

        if (blobItems != null) {
            blobItems.forEach(xmlWriter::writeXml);
        }

        return xmlWriter.writeEndElement();
    }

    public static BlobFlatListSegment fromXml(XmlReader xmlReader) {
        return xmlReader.readObject("Blobs", reader -> {
            BlobFlatListSegment deserialized = new BlobFlatListSegment();

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("Blob".equals(elementName)) {
                    deserialized.blobItems.add(BlobItemInternal.fromXml(reader));
                }
            }

            return deserialized;
        });
    }
}
