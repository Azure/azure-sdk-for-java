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

public class BlobTags implements XmlSerializable<BlobTags> {
    private static final class TagSetWrapper implements XmlSerializable<TagSetWrapper> {
        private final List<BlobTag> items;

        private TagSetWrapper(List<BlobTag> items) {
            this.items = items;
        }

        @Override
        public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
            if (items == null || items.isEmpty()) {
                return xmlWriter.writeStartSelfClosingElement("TagSet");
            }

            xmlWriter.writeStartElement("TagSet");

            for (BlobTag tag : items) {
                xmlWriter.writeXml(tag);
            }

            return xmlWriter.writeEndElement();
        }

        public static TagSetWrapper fromXml(XmlReader xmlReader) throws XMLStreamException {
            return xmlReader.readObject("TagSet", reader -> {
                List<BlobTag> items = null;

                while (reader.nextElement() != XmlToken.END_ELEMENT) {
                    String elementName = reader.getElementName().getLocalPart();

                    if ("Tag".equals(elementName)) {
                        if (items == null) {
                            items = new ArrayList<>();
                        }

                        items.add(BlobTag.fromXml(xmlReader));
                    }
                }

                return new TagSetWrapper(items);
            });
        }
    }

    /*
     * The BlobTagSet property.
     */
    private TagSetWrapper blobTagSet;

    /**
     * Get the blobTagSet property: The BlobTagSet property.
     *
     * @return the blobTagSet value.
     */
    public List<BlobTag> getBlobTagSet() {
        if (this.blobTagSet == null) {
            this.blobTagSet = new TagSetWrapper(new ArrayList<BlobTag>());
        }
        return this.blobTagSet.items;
    }

    /**
     * Set the blobTagSet property: The BlobTagSet property.
     *
     * @param blobTagSet the blobTagSet value to set.
     * @return the BlobTags object itself.
     */
    public BlobTags setBlobTagSet(List<BlobTag> blobTagSet) {
        this.blobTagSet = new TagSetWrapper(blobTagSet);
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement("Tags");

        xmlWriter.writeXml(blobTagSet);

        return xmlWriter.writeEndElement();
    }

    public static BlobTags fromXml(XmlReader xmlReader) throws XMLStreamException {
        return xmlReader.readObject("Tags", reader -> {
            BlobTags result = new BlobTags();

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("TagSet".equals(elementName)) {
                    result.blobTagSet = TagSetWrapper.fromXml(xmlReader);
                }
            }

            return result;
        });
    }
}
