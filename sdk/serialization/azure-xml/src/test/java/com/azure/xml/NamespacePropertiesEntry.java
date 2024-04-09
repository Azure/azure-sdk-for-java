// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.time.OffsetDateTime;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

public class NamespacePropertiesEntry implements XmlSerializable<NamespacePropertiesEntry> {
    private String id;
    private String title;
    private OffsetDateTime updated;
    private ResponseAuthor author;
    private ResponseLink link;
    private NamespacePropertiesEntryContent content;

    /**
     * Get the id property: The URL of the GET request.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: The URL of the GET request.
     *
     * @param id the id value to set.
     * @return the NamespacePropertiesEntry object itself.
     */
    public NamespacePropertiesEntry setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the title property: The name of the namespace.
     *
     * @return the title value.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title property: The name of the namespace.
     *
     * @param title the title value to set.
     * @return the NamespacePropertiesEntry object itself.
     */
    public NamespacePropertiesEntry setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the updated property: The timestamp for when this namespace was last updated.
     *
     * @return the updated value.
     */
    public OffsetDateTime getUpdated() {
        return this.updated;
    }

    /**
     * Set the updated property: The timestamp for when this namespace was last updated.
     *
     * @param updated the updated value to set.
     * @return the NamespacePropertiesEntry object itself.
     */
    public NamespacePropertiesEntry setUpdated(OffsetDateTime updated) {
        this.updated = updated;
        return this;
    }

    /**
     * Get the author property: The author that created this resource.
     *
     * @return the author value.
     */
    public ResponseAuthor getAuthor() {
        return this.author;
    }

    /**
     * Set the author property: The author that created this resource.
     *
     * @param author the author value to set.
     * @return the NamespacePropertiesEntry object itself.
     */
    public NamespacePropertiesEntry setAuthor(ResponseAuthor author) {
        this.author = author;
        return this;
    }

    /**
     * Get the link property: The URL for the HTTP request.
     *
     * @return the link value.
     */
    public ResponseLink getLink() {
        return this.link;
    }

    /**
     * Set the link property: The URL for the HTTP request.
     *
     * @param link the link value to set.
     * @return the NamespacePropertiesEntry object itself.
     */
    public NamespacePropertiesEntry setLink(ResponseLink link) {
        this.link = link;
        return this;
    }

    /**
     * Get the content property: Information about the namespace.
     *
     * @return the content value.
     */
    public NamespacePropertiesEntryContent getContent() {
        return this.content;
    }

    /**
     * Set the content property: Information about the namespace.
     *
     * @param content the content value to set.
     * @return the NamespacePropertiesEntry object itself.
     */
    public NamespacePropertiesEntry setContent(NamespacePropertiesEntryContent content) {
        this.content = content;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        xmlWriter.writeStartElement(getRootElementName(rootElementName, "entry"));
        xmlWriter.writeNamespace("http://www.w3.org/2005/Atom");
        xmlWriter.writeStringElement("id", id);
        xmlWriter.writeStringElement("title", title);
        xmlWriter.writeStringElement("updated", updated == null ? null : updated.toString());
        xmlWriter.writeXml(author);
        xmlWriter.writeXml(link);
        xmlWriter.writeXml(content);

        return xmlWriter.writeEndElement().flush();
    }

    public static NamespacePropertiesEntry fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static NamespacePropertiesEntry fromXml(XmlReader xmlReader, String rootElementName)
        throws XMLStreamException {
        return xmlReader.readObject("http://www.w3.org/2005/Atom", getRootElementName(rootElementName, "entry"),
            reader -> {
                String id = null;
                String title = null;
                OffsetDateTime updated = null;
                ResponseAuthor author = null;
                ResponseLink link = null;
                NamespacePropertiesEntryContent content = null;

                while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
                    QName qName = xmlReader.getElementName();
                    String localPart = qName.getLocalPart();
                    String namespaceUri = qName.getNamespaceURI();

                    if ("id".equals(localPart) && "http://www.w3.org/2005/Atom".equals(namespaceUri)) {
                        id = xmlReader.getStringElement();
                    } else if ("title".equals(localPart)) {
                        title = xmlReader.getStringElement();
                    } else if ("updated".equals(localPart) && "http://www.w3.org/2005/Atom".equals(namespaceUri)) {
                        updated = OffsetDateTime.parse(xmlReader.getStringElement());
                    } else if ("author".equals(localPart) && "http://www.w3.org/2005/Atom".equals(namespaceUri)) {
                        author = ResponseAuthor.fromXml(xmlReader);
                    } else if ("link".equals(localPart) && "http://www.w3.org/2005/Atom".equals(namespaceUri)) {
                        link = ResponseLink.fromXml(xmlReader);
                    } else if ("content".equals(localPart) && "http://www.w3.org/2005/Atom".equals(namespaceUri)) {
                        content = NamespacePropertiesEntryContent.fromXml(xmlReader);
                    }
                }

                return new NamespacePropertiesEntry().setId(id)
                    .setTitle(title)
                    .setUpdated(updated)
                    .setAuthor(author)
                    .setLink(link)
                    .setContent(content);
            });
    }
}
