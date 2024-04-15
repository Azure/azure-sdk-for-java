// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A listed Azure Storage handle item. */
@Fluent
public final class HandleItem implements XmlSerializable<HandleItem> {
    /*
     * XSMB service handle ID
     */
    private String handleId;

    /*
     * File or directory name including full path starting from share root
     */
    private String path;

    /*
     * FileId uniquely identifies the file or directory.
     */
    private String fileId;

    /*
     * ParentId uniquely identifies the parent directory of the object.
     */
    private String parentId;

    /*
     * SMB session ID in context of which the file handle was opened
     */
    private String sessionId;

    /*
     * Client IP that opened the handle
     */
    private String clientIp;

    /*
     * Time when the session that previously opened the handle has last been reconnected. (UTC)
     */
    private DateTimeRfc1123 openTime;

    /*
     * Time handle was last connected to (UTC)
     */
    private DateTimeRfc1123 lastReconnectTime;

    /*
     * Name of the client machine where the share is being mounted
     */
    private String clientName;

    /*
     * The AccessRightList property.
     */
    private List<ShareFileHandleAccessRights> accessRights;

    /** Creates an instance of HandleItem class. */
    public HandleItem() {}

    /**
     * Get the handleId property: XSMB service handle ID.
     *
     * @return the handleId value.
     */
    public String getHandleId() {
        return this.handleId;
    }

    /**
     * Set the handleId property: XSMB service handle ID.
     *
     * @param handleId the handleId value to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setHandleId(String handleId) {
        this.handleId = handleId;
        return this;
    }

    /**
     * Get the path property: File or directory name including full path starting from share root.
     *
     * @return the path value.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Set the path property: File or directory name including full path starting from share root.
     *
     * @param path the path value to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Get the fileId property: FileId uniquely identifies the file or directory.
     *
     * @return the fileId value.
     */
    public String getFileId() {
        return this.fileId;
    }

    /**
     * Set the fileId property: FileId uniquely identifies the file or directory.
     *
     * @param fileId the fileId value to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    /**
     * Get the parentId property: ParentId uniquely identifies the parent directory of the object.
     *
     * @return the parentId value.
     */
    public String getParentId() {
        return this.parentId;
    }

    /**
     * Set the parentId property: ParentId uniquely identifies the parent directory of the object.
     *
     * @param parentId the parentId value to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * Get the sessionId property: SMB session ID in context of which the file handle was opened.
     *
     * @return the sessionId value.
     */
    public String getSessionId() {
        return this.sessionId;
    }

    /**
     * Set the sessionId property: SMB session ID in context of which the file handle was opened.
     *
     * @param sessionId the sessionId value to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Get the clientIp property: Client IP that opened the handle.
     *
     * @return the clientIp value.
     */
    public String getClientIp() {
        return this.clientIp;
    }

    /**
     * Set the clientIp property: Client IP that opened the handle.
     *
     * @param clientIp the clientIp value to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setClientIp(String clientIp) {
        this.clientIp = clientIp;
        return this;
    }

    /**
     * Get the openTime property: Time when the session that previously opened the handle has last been reconnected.
     * (UTC).
     *
     * @return the openTime value.
     */
    public OffsetDateTime getOpenTime() {
        if (this.openTime == null) {
            return null;
        }
        return this.openTime.getDateTime();
    }

    /**
     * Set the openTime property: Time when the session that previously opened the handle has last been reconnected.
     * (UTC).
     *
     * @param openTime the openTime value to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setOpenTime(OffsetDateTime openTime) {
        if (openTime == null) {
            this.openTime = null;
        } else {
            this.openTime = new DateTimeRfc1123(openTime);
        }
        return this;
    }

    /**
     * Get the lastReconnectTime property: Time handle was last connected to (UTC).
     *
     * @return the lastReconnectTime value.
     */
    public OffsetDateTime getLastReconnectTime() {
        if (this.lastReconnectTime == null) {
            return null;
        }
        return this.lastReconnectTime.getDateTime();
    }

    /**
     * Set the lastReconnectTime property: Time handle was last connected to (UTC).
     *
     * @param lastReconnectTime the lastReconnectTime value to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setLastReconnectTime(OffsetDateTime lastReconnectTime) {
        if (lastReconnectTime == null) {
            this.lastReconnectTime = null;
        } else {
            this.lastReconnectTime = new DateTimeRfc1123(lastReconnectTime);
        }
        return this;
    }

    /**
     * Get the accessRights property: The {@link ShareFileHandleAccessRights} property.
     *
     * @return the accessRights list value.
     */
    public List<ShareFileHandleAccessRights> getAccessRights() {
        if (this.accessRights == null) {
            this.accessRights = new ArrayList<>();
        }
        return this.accessRights;
    }

    /**
     * Set the accessRights property: The {@link ShareFileHandleAccessRights} property.
     *
     * @param accessRights the accessRights list to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setAccessRights(List<ShareFileHandleAccessRights> accessRights) {
        this.accessRights = accessRights;
        return this;
    }

    /**
     * Get the clientName property: Name of the client machine where the share is being mounted.
     *
     * @return the clientName value.
     */
    public String getClientName() {
        return this.clientName;
    }

    /**
     * Set the clientName property: Name of the client machine where the share is being mounted.
     *
     * @param clientName the clientName value to set.
     * @return the HandleItem object itself.
     */
    public HandleItem setClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        rootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? "Handle" : rootElementName;
        xmlWriter.writeStartElement(rootElementName);
        xmlWriter.writeStringElement("HandleId", this.handleId);
        xmlWriter.writeStringElement("Path", this.path);
        xmlWriter.writeStringElement("FileId", this.fileId);
        xmlWriter.writeStringElement("ParentId", this.parentId);
        xmlWriter.writeStringElement("SessionId", this.sessionId);
        xmlWriter.writeStringElement("ClientIp", this.clientIp);
        xmlWriter.writeStringElement("ClientName", this.clientName);
        xmlWriter.writeStringElement("OpenTime", Objects.toString(this.openTime, null));
        xmlWriter.writeStringElement("LastReconnectTime", Objects.toString(this.lastReconnectTime, null));
        if (this.accessRights != null) {
            xmlWriter.writeStartElement("AccessRightList");
            for (ShareFileHandleAccessRights element : this.accessRights) {
                xmlWriter.writeStringElement("AccessRight", element == null ? null : element.toString());
            }
            xmlWriter.writeEndElement();
        }
        return xmlWriter.writeEndElement();
    }

    /**
     * Reads an instance of HandleItem from the XmlReader.
     *
     * @param xmlReader The XmlReader being read.
     * @return An instance of HandleItem if the XmlReader was pointing to an instance of it, or null if it was pointing
     * to XML null.
     * @throws IllegalStateException If the deserialized XML object was missing any required properties.
     * @throws XMLStreamException If an error occurs while reading the HandleItem.
     */
    public static HandleItem fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    /**
     * Reads an instance of HandleItem from the XmlReader.
     *
     * @param xmlReader The XmlReader being read.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can deserialize from different root element names.
     * @return An instance of HandleItem if the XmlReader was pointing to an instance of it, or null if it was pointing
     * to XML null.
     * @throws IllegalStateException If the deserialized XML object was missing any required properties.
     * @throws XMLStreamException If an error occurs while reading the HandleItem.
     */
    public static HandleItem fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? "Handle" : rootElementName;
        return xmlReader.readObject(finalRootElementName, reader -> {
            HandleItem deserializedHandleItem = new HandleItem();
            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                QName elementName = reader.getElementName();

                if ("HandleId".equals(elementName.getLocalPart())) {
                    deserializedHandleItem.handleId = reader.getStringElement();
                } else if ("Path".equals(elementName.getLocalPart())) {
                    deserializedHandleItem.path = reader.getStringElement();
                } else if ("FileId".equals(elementName.getLocalPart())) {
                    deserializedHandleItem.fileId = reader.getStringElement();
                } else if ("ParentId".equals(elementName.getLocalPart())) {
                    deserializedHandleItem.parentId = reader.getStringElement();
                } else if ("SessionId".equals(elementName.getLocalPart())) {
                    deserializedHandleItem.sessionId = reader.getStringElement();
                } else if ("ClientIp".equals(elementName.getLocalPart())) {
                    deserializedHandleItem.clientIp = reader.getStringElement();
                } else if ("ClientName".equals(elementName.getLocalPart())) {
                    deserializedHandleItem.clientName = reader.getStringElement();
                } else if ("OpenTime".equals(elementName.getLocalPart())) {
                    deserializedHandleItem.openTime = reader.getNullableElement(DateTimeRfc1123::new);
                } else if ("LastReconnectTime".equals(elementName.getLocalPart())) {
                    deserializedHandleItem.lastReconnectTime = reader.getNullableElement(DateTimeRfc1123::new);
                } else if ("AccessRightList".equals(elementName.getLocalPart())) {
                    while (reader.nextElement() != XmlToken.END_ELEMENT) {
                        elementName = reader.getElementName();
                        if ("AccessRight".equals(elementName.getLocalPart())) {
                            if (deserializedHandleItem.accessRights == null) {
                                deserializedHandleItem.accessRights = new ArrayList<>();
                            }
                            deserializedHandleItem.accessRights
                                .add(ShareFileHandleAccessRights.fromString(reader.getStringElement()));
                        } else {
                            reader.skipElement();
                        }
                    }
                } else {
                    reader.skipElement();
                }
            }

            return deserializedHandleItem;
        });
    }
}
