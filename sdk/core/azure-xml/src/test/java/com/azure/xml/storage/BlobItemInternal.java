// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlobItemInternal implements XmlSerializable<BlobItemInternal> {
    /*
     * The Name property.
     */
    private BlobName name;

    /*
     * The Deleted property.
     */
    private boolean deleted;

    /*
     * The Snapshot property.
     */
    private String snapshot;

    /*
     * The VersionId property.
     */
    private String versionId;

    /*
     * The IsCurrentVersion property.
     */
    private Boolean isCurrentVersion;

    /*
     * Properties of a blob
     */
    private BlobItemPropertiesInternal properties;

    /*
     * Dictionary of <string>
     */
    private Map<String, String> metadata;

    /*
     * Blob tags
     */
    private BlobTags blobTags;

    /*
     * Dictionary of <string>
     */
    private Map<String, String> objectReplicationMetadata;

    /*
     * The HasVersionsOnly property.
     */
    private Boolean hasVersionsOnly;

    /*
     * The IsPrefix property.
     */
    private Boolean isPrefix;

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        xmlWriter.writeStartElement("Blob");

        xmlWriter.writeXml(name);
        xmlWriter.writeBooleanElement("Deleted", deleted);

        if (snapshot == null) {
            xmlWriter.writeStartSelfClosingElement("Snapshot");
        } else {
            xmlWriter.writeStringElement("Snapshot", snapshot);
        }

        if (versionId == null) {
            xmlWriter.writeStartSelfClosingElement("VersionId");
        } else {
            xmlWriter.writeStringElement("VersionId", snapshot);
        }

        xmlWriter.writeBooleanElement("IsCurrentVersion", isCurrentVersion);
        xmlWriter.writeXml(properties);

        if (metadata != null) {
            xmlWriter.writeStartElement("Metadata");
            metadata.forEach(xmlWriter::writeStringElement);
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeXml(blobTags);

        if (objectReplicationMetadata != null) {
            xmlWriter.writeStartElement("OrMetadata");
            objectReplicationMetadata.forEach(xmlWriter::writeStringElement);
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeBooleanElement("HasVersionsOnly", hasVersionsOnly);
        xmlWriter.writeBooleanElement("IsPrefix", isPrefix);

        return xmlWriter.writeEndElement();
    }

    public static BlobItemInternal fromXml(XmlReader xmlReader) {
        return xmlReader.readObject("Blob", reader -> {
            BlobItemInternal deserialized = new BlobItemInternal();

            boolean nameFound = false;
            boolean deletedFound = false;
            boolean snapshotFound = false;
            boolean propertiesFound = false;

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("Name".equals(elementName)) {
                    deserialized.name = BlobName.fromXml(reader);
                    nameFound = true;
                } else if ("Deleted".equals(elementName)) {
                    deserialized.deleted = reader.getElementBooleanValue();
                    deletedFound = true;
                } else if ("Snapshot".equals(elementName)) {
                    deserialized.snapshot = reader.getElementStringValue();
                    snapshotFound = true;
                } else if ("VersionId".equals(elementName)) {
                    deserialized.versionId = reader.getElementStringValue();
                } else if ("IsCurrentVersion".equals(elementName)) {
                    deserialized.isCurrentVersion = reader.getElementBooleanValue();
                } else if ("Properties".equals(elementName)) {
                    deserialized.properties = BlobItemPropertiesInternal.fromXml(reader);
                    propertiesFound = true;
                } else if ("Metadata".equals(elementName)) {
                    while (reader.nextElement() != XmlToken.END_ELEMENT) {
                        if (deserialized.metadata == null) {
                            deserialized.metadata = new LinkedHashMap<>();
                        }

                        deserialized.metadata.put(reader.getElementName().getLocalPart(),
                            reader.getElementStringValue());
                    }
                } else if ("Tags".equals(elementName)) {
                    deserialized.blobTags = BlobTags.fromXml(reader);
                } else if ("OrMetadata".equals(elementName)) {
                    while (reader.nextElement() != XmlToken.END_ELEMENT) {
                        if (deserialized.objectReplicationMetadata == null) {
                            deserialized.objectReplicationMetadata = new LinkedHashMap<>();
                        }

                        deserialized.objectReplicationMetadata.put(reader.getElementName().getLocalPart(),
                            reader.getElementStringValue());
                    }
                } else if ("HasVersionsOnly".equals(elementName)) {
                    deserialized.hasVersionsOnly = reader.getElementBooleanValue();
                } else if ("IsPrefix".equals(elementName)) {
                    deserialized.isPrefix = reader.getElementBooleanValue();
                }
            }

            if (nameFound && deletedFound && snapshotFound && propertiesFound) {
                return deserialized;
            }

            throw new IllegalStateException("Missing required properties.");
        });
    }
}
