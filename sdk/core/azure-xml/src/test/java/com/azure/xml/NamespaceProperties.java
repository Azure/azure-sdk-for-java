// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import java.time.OffsetDateTime;

public class NamespaceProperties implements XmlSerializable<NamespaceProperties> {
    private String alias;
    private OffsetDateTime createdTime;
    private MessagingSku messagingSku;
    private Integer messagingUnits;
    private OffsetDateTime modifiedTime;
    private String name;
    private NamespaceType namespaceType;

    /**
     * Get the alias property: Alias for the geo-disaster recovery Service Bus namespace.
     *
     * @return the alias value.
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Set the alias property: Alias for the geo-disaster recovery Service Bus namespace.
     *
     * @param alias the alias value to set.
     * @return the NamespaceProperties object itself.
     */
    public NamespaceProperties setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * Get the createdTime property: The exact time the namespace was created.
     *
     * @return the createdTime value.
     */
    public OffsetDateTime getCreatedTime() {
        return this.createdTime;
    }

    /**
     * Set the createdTime property: The exact time the namespace was created.
     *
     * @param createdTime the createdTime value to set.
     * @return the NamespaceProperties object itself.
     */
    NamespaceProperties setCreatedTime(OffsetDateTime createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    /**
     * Get the messagingSku property: The SKU for the messaging entity.
     *
     * @return the messagingSku value.
     */
    public MessagingSku getMessagingSku() {
        return this.messagingSku;
    }

    /**
     * Set the messagingSku property: The SKU for the messaging entity.
     *
     * @param messagingSku the messagingSku value to set.
     * @return the NamespaceProperties object itself.
     */
    public NamespaceProperties setMessagingSku(MessagingSku messagingSku) {
        this.messagingSku = messagingSku;
        return this;
    }

    /**
     * Get the messagingUnits property: The number of messaging units allocated to the namespace.
     *
     * @return the messagingUnits value.
     */
    public Integer getMessagingUnits() {
        return this.messagingUnits;
    }

    /**
     * Set the messagingUnits property: The number of messaging units allocated to the namespace.
     *
     * @param messagingUnits the messagingUnits value to set.
     * @return the NamespaceProperties object itself.
     */
    public NamespaceProperties setMessagingUnits(Integer messagingUnits) {
        this.messagingUnits = messagingUnits;
        return this;
    }

    /**
     * Get the modifiedTime property: The exact time the namespace was last modified.
     *
     * @return the modifiedTime value.
     */
    public OffsetDateTime getModifiedTime() {
        return this.modifiedTime;
    }

    /**
     * Set the modifiedTime property: The exact time the namespace was last modified.
     *
     * @param modifiedTime the modifiedTime value to set.
     * @return the NamespaceProperties object itself.
     */
    public NamespaceProperties setModifiedTime(OffsetDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
        return this;
    }

    /**
     * Get the name property: Name of the namespace.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: Name of the namespace.
     *
     * @param name the name value to set.
     * @return the NamespaceProperties object itself.
     */
    public NamespaceProperties setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the namespaceType property: The type of entities the namespace can contain.
     *
     * @return the namespaceType value.
     */
    public NamespaceType getNamespaceType() {
        return this.namespaceType;
    }

    /**
     * Set the namespaceType property: The type of entities the namespace can contain.
     *
     * @param namespaceType the namespaceType value to set.
     * @return the NamespaceProperties object itself.
     */
    public NamespaceProperties setNamespaceType(NamespaceType namespaceType) {
        this.namespaceType = namespaceType;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        xmlWriter.writeStartElement("NamespaceInfo");
        xmlWriter.writeNamespace("http://schemas.microsoft.com/netservices/2010/10/servicebus/connect");
        xmlWriter.writeStringElement("Alias", alias);
        xmlWriter.writeStringElement("CreatedTime", createdTime == null ? null : createdTime.toString());
        xmlWriter.writeStringElement("MessagingSKU", messagingSku == null ? null : messagingSku.toString());
        xmlWriter.writeNumberElement("MessagingUnits", messagingUnits);
        xmlWriter.writeStringElement("ModifiedTime", modifiedTime == null ? null : modifiedTime.toString());
        xmlWriter.writeStringElement("Name", name);
        xmlWriter.writeStringElement("NamespaceType", namespaceType == null ? null : namespaceType.toString());

        return xmlWriter.writeEndElement().flush();
    }

    public static NamespaceProperties fromXml(XmlReader xmlReader) {
        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            xmlReader.nextElement();
        }

        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            throw new IllegalStateException("Illegal start of XML deserialization. "
                + "Expected 'XmlToken.START_ELEMENT' but it was: 'XmlToken." + xmlReader.currentToken() + "'.");
        }

        String expectedNamespaceUri = "http://schemas.microsoft.com/netservices/2010/10/servicebus/connect";

        QName qName = xmlReader.getElementName();
        if (!"NamespaceInfo".equals(qName.getLocalPart()) || !expectedNamespaceUri.equals(qName.getNamespaceURI())) {
            throw new IllegalStateException("Expected XML element to be 'NamespaceInfo' in namespace "
                + "'" + expectedNamespaceUri + "' but it was: "
                + "{'" + qName.getNamespaceURI() + "'}'" + qName.getLocalPart() + "'.");
        }

        String alias = null;
        OffsetDateTime createdTime = null;
        MessagingSku messagingSku = null;
        Integer messagingUnits = null;
        OffsetDateTime modifiedTime = null;
        String name = null;
        NamespaceType namespaceType = null;

        while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
            qName = xmlReader.getElementName();
            String localPart = qName.getLocalPart();
            String namespaceUri = qName.getNamespaceURI();

            if ("Alias".equals(localPart) && expectedNamespaceUri.equals(namespaceUri)) {
                alias = xmlReader.getElementStringValue();
            } else if ("CreatedTime".equals(localPart) && expectedNamespaceUri.equals(namespaceUri)) {
                createdTime = OffsetDateTime.parse(xmlReader.getElementStringValue());
            } else if ("MessagingSKU".equals(localPart) && expectedNamespaceUri.equals(namespaceUri)) {
                messagingSku = MessagingSku.fromString(xmlReader.getElementStringValue());
            } else if ("MessagingUnits".equals(localPart) && expectedNamespaceUri.equals(namespaceUri)) {
                messagingUnits = xmlReader.getElementIntValue();
            } else if ("ModifiedTime".equals(localPart) && expectedNamespaceUri.equals(namespaceUri)) {
                modifiedTime = OffsetDateTime.parse(xmlReader.getElementStringValue());
            } else if ("Name".equals(localPart) && expectedNamespaceUri.equals(namespaceUri)) {
                name = xmlReader.getElementStringValue();
            } else if ("NamespaceType".equals(localPart) && expectedNamespaceUri.equals(namespaceUri)) {
                namespaceType = NamespaceType.fromString(xmlReader.getElementStringValue());
            }
        }

        return new NamespaceProperties()
            .setAlias(alias)
            .setCreatedTime(createdTime)
            .setMessagingSku(messagingSku)
            .setMessagingUnits(messagingUnits)
            .setModifiedTime(modifiedTime)
            .setName(name)
            .setNamespaceType(namespaceType);
    }
}
