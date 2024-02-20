// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

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

    /**
     * Get the name property: The Name property.
     *
     * @return the name value.
     */
    public BlobName getName() {
        return this.name;
    }

    /**
     * Set the name property: The Name property.
     *
     * @param name the name value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setName(BlobName name) {
        this.name = name;
        return this;
    }

    /**
     * Get the deleted property: The Deleted property.
     *
     * @return the deleted value.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    /**
     * Set the deleted property: The Deleted property.
     *
     * @param deleted the deleted value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    /**
     * Get the snapshot property: The Snapshot property.
     *
     * @return the snapshot value.
     */
    public String getSnapshot() {
        return this.snapshot;
    }

    /**
     * Set the snapshot property: The Snapshot property.
     *
     * @param snapshot the snapshot value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Get the versionId property: The VersionId property.
     *
     * @return the versionId value.
     */
    public String getVersionId() {
        return this.versionId;
    }

    /**
     * Set the versionId property: The VersionId property.
     *
     * @param versionId the versionId value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    /**
     * Get the isCurrentVersion property: The IsCurrentVersion property.
     *
     * @return the isCurrentVersion value.
     */
    public Boolean isCurrentVersion() {
        return this.isCurrentVersion;
    }

    /**
     * Set the isCurrentVersion property: The IsCurrentVersion property.
     *
     * @param isCurrentVersion the isCurrentVersion value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setIsCurrentVersion(Boolean isCurrentVersion) {
        this.isCurrentVersion = isCurrentVersion;
        return this;
    }

    /**
     * Get the properties property: Properties of a blob.
     *
     * @return the properties value.
     */
    public BlobItemPropertiesInternal getProperties() {
        return this.properties;
    }

    /**
     * Set the properties property: Properties of a blob.
     *
     * @param properties the properties value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setProperties(BlobItemPropertiesInternal properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the metadata property: Dictionary of &lt;string&gt;.
     *
     * @return the metadata value.
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the metadata property: Dictionary of &lt;string&gt;.
     *
     * @param metadata the metadata value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get the blobTags property: Blob tags.
     *
     * @return the blobTags value.
     */
    public BlobTags getBlobTags() {
        return this.blobTags;
    }

    /**
     * Set the blobTags property: Blob tags.
     *
     * @param blobTags the blobTags value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setBlobTags(BlobTags blobTags) {
        this.blobTags = blobTags;
        return this;
    }

    /**
     * Get the objectReplicationMetadata property: Dictionary of &lt;string&gt;.
     *
     * @return the objectReplicationMetadata value.
     */
    public Map<String, String> getObjectReplicationMetadata() {
        return this.objectReplicationMetadata;
    }

    /**
     * Set the objectReplicationMetadata property: Dictionary of &lt;string&gt;.
     *
     * @param objectReplicationMetadata the objectReplicationMetadata value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setObjectReplicationMetadata(Map<String, String> objectReplicationMetadata) {
        this.objectReplicationMetadata = objectReplicationMetadata;
        return this;
    }

    /**
     * Get the hasVersionsOnly property: The HasVersionsOnly property.
     *
     * @return the hasVersionsOnly value.
     */
    public Boolean isHasVersionsOnly() {
        return this.hasVersionsOnly;
    }

    /**
     * Set the hasVersionsOnly property: The HasVersionsOnly property.
     *
     * @param hasVersionsOnly the hasVersionsOnly value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setHasVersionsOnly(Boolean hasVersionsOnly) {
        this.hasVersionsOnly = hasVersionsOnly;
        return this;
    }

    /**
     * Get the isPrefix property: The IsPrefix property.
     *
     * @return the isPrefix value.
     */
    public Boolean isPrefix() {
        return this.isPrefix;
    }

    /**
     * Set the isPrefix property: The IsPrefix property.
     *
     * @param isPrefix the isPrefix value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItemInternal setIsPrefix(Boolean isPrefix) {
        this.isPrefix = isPrefix;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        xmlWriter.writeStartElement(getRootElementName(rootElementName, "Blob"));

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
            for (Map.Entry<String, String> meta : metadata.entrySet()) {
                xmlWriter.writeStringElement(meta.getKey(), meta.getValue());
            }
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeXml(blobTags);

        if (objectReplicationMetadata != null) {
            xmlWriter.writeStartElement("OrMetadata");
            for (Map.Entry<String, String> meta : objectReplicationMetadata.entrySet()) {
                xmlWriter.writeStringElement(meta.getKey(), meta.getValue());
            }
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeBooleanElement("HasVersionsOnly", hasVersionsOnly);
        xmlWriter.writeBooleanElement("IsPrefix", isPrefix);

        return xmlWriter.writeEndElement();
    }

    public static BlobItemInternal fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static BlobItemInternal fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        return xmlReader.readObject(getRootElementName(rootElementName, "Blob"), reader -> {
            BlobItemInternal deserialized = new BlobItemInternal();

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("Name".equals(elementName)) {
                    deserialized.name = BlobName.fromXml(reader);
                } else if ("Deleted".equals(elementName)) {
                    deserialized.deleted = reader.getBooleanElement();
                } else if ("Snapshot".equals(elementName)) {
                    deserialized.snapshot = reader.getStringElement();
                } else if ("VersionId".equals(elementName)) {
                    deserialized.versionId = reader.getStringElement();
                } else if ("IsCurrentVersion".equals(elementName)) {
                    deserialized.isCurrentVersion = reader.getBooleanElement();
                } else if ("Properties".equals(elementName)) {
                    deserialized.properties = BlobItemPropertiesInternal.fromXml(reader);
                } else if ("Metadata".equals(elementName)) {
                    while (reader.nextElement() != XmlToken.END_ELEMENT) {
                        if (deserialized.metadata == null) {
                            deserialized.metadata = new LinkedHashMap<>();
                        }

                        deserialized.metadata.put(reader.getElementName().getLocalPart(), reader.getStringElement());
                    }
                } else if ("Tags".equals(elementName)) {
                    deserialized.blobTags = BlobTags.fromXml(reader);
                } else if ("OrMetadata".equals(elementName)) {
                    while (reader.nextElement() != XmlToken.END_ELEMENT) {
                        if (deserialized.objectReplicationMetadata == null) {
                            deserialized.objectReplicationMetadata = new LinkedHashMap<>();
                        }

                        deserialized.objectReplicationMetadata.put(reader.getElementName().getLocalPart(),
                            reader.getStringElement());
                    }
                } else if ("HasVersionsOnly".equals(elementName)) {
                    deserialized.hasVersionsOnly = reader.getBooleanElement();
                } else if ("IsPrefix".equals(elementName)) {
                    deserialized.isPrefix = reader.getBooleanElement();
                }
            }

            return deserialized;
        });
    }
}
