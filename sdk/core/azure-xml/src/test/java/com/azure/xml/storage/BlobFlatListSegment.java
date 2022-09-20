// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

public class BlobFlatListSegment implements XmlSerializable<BlobFlatListSegment> {
    /*
     * The BlobItems property.
     */
    private List<BlobItemInternal> blobItems = new ArrayList<>();

    /**
     * Get the blobItems property: The BlobItems property.
     *
     * @return the blobItems value.
     */
    public List<BlobItemInternal> getBlobItems() {
        return this.blobItems;
    }

    /**
     * Set the blobItems property: The BlobItems property.
     *
     * @param blobItems the blobItems value to set.
     * @return the BlobFlatListSegment object itself.
     */
    public BlobFlatListSegment setBlobItems(List<BlobItemInternal> blobItems) {
        this.blobItems = blobItems;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement("Blobs");

        if (blobItems != null) {
            for (BlobItemInternal blobItem : blobItems) {
                xmlWriter.writeXml(blobItem);
            }
        }

        return xmlWriter.writeEndElement();
    }

    public static BlobFlatListSegment fromXml(XmlReader xmlReader) throws XMLStreamException {
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
