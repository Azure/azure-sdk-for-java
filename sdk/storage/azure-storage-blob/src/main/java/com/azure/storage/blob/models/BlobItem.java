// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.implementation.accesshelpers.BlobItemConstructorProxy;
import com.azure.storage.blob.implementation.accesshelpers.BlobItemPropertiesConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobItemInternal;
import com.azure.storage.blob.implementation.models.BlobName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Map;

import static com.azure.storage.blob.implementation.util.ModelHelper.tagMapFromBlobTags;
import static com.azure.storage.blob.implementation.util.ModelHelper.toBlobNameString;
import static com.azure.storage.blob.implementation.util.ModelHelper.toBlobTags;
import static com.azure.storage.blob.implementation.util.ModelHelper.toObjectReplicationMetadata;
import static com.azure.storage.blob.implementation.util.ModelHelper.transformObjectReplicationMetadata;

/**
 * An Azure Storage blob.
 */
@JacksonXmlRootElement(localName = "Blob")
@Fluent
public final class BlobItem {
    @JsonUnwrapped
    private final BlobItemInternal blobItemInternal;

    static {
        BlobItemConstructorProxy.setAccessor(BlobItem::new);
    }

    private BlobItem(BlobItemInternal blobItemInternal) {
        this.blobItemInternal = blobItemInternal;
    }

    /**
     * Constructs a new instance of {@link BlobItem}.
     */
    public BlobItem() {
        // Added to maintain backwards compatibility as the private constructor removes the implicit no args
        // constructor.
        this.blobItemInternal = new BlobItemInternal();
    }

    @JsonIgnore
    private String convertedName;

    @JsonIgnore
    private BlobItemProperties convertedProperties;

    @JsonIgnore
    private Map<String, String> convertedTags;

    @JsonIgnore
    private List<ObjectReplicationPolicy> convertedObjectReplicationSourcePolicies;

    /**
     * Get the name property: The name property.
     *
     * @return the name value.
     */
    public String getName() {
        if (convertedName == null) {
            convertedName = toBlobNameString(blobItemInternal.getName());
        }

        return convertedName;
    }

    /**
     * Set the name property: The name property.
     *
     * @param name the name value to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setName(String name) {
        convertedName = name;
        blobItemInternal.setName(new BlobName().setContent(name));
        return this;
    }

    /**
     * Get the deleted property: The deleted property.
     *
     * @return the deleted value.
     */
    public boolean isDeleted() {
        return blobItemInternal.isDeleted();
    }

    /**
     * Set the deleted property: The deleted property.
     *
     * @param deleted the deleted value to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setDeleted(boolean deleted) {
        blobItemInternal.setDeleted(deleted);
        return this;
    }

    /**
     * Get the snapshot property: The snapshot property.
     *
     * @return the snapshot value.
     */
    public String getSnapshot() {
        return blobItemInternal.getSnapshot();
    }

    /**
     * Set the snapshot property: The snapshot property.
     *
     * @param snapshot the snapshot value to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setSnapshot(String snapshot) {
        blobItemInternal.setSnapshot(snapshot);
        return this;
    }

    /**
     * Get the properties property: The properties property.
     *
     * @return the properties value.
     */
    public BlobItemProperties getProperties() {
        if (convertedProperties == null) {
            convertedProperties = BlobItemPropertiesConstructorProxy.create(blobItemInternal.getProperties());
        }

        return convertedProperties;
    }

    /**
     * Set the properties property: The properties property.
     *
     * @param properties the properties value to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setProperties(BlobItemProperties properties) {
        this.convertedProperties = properties;
        blobItemInternal.setProperties(BlobItemPropertiesConstructorProxy.getInternalProperties(properties));

        return this;
    }

    /**
     * Get the metadata property: The metadata property.
     *
     * @return the metadata value.
     */
    public Map<String, String> getMetadata() {
        return blobItemInternal.getMetadata();
    }

    /**
     * Set the metadata property: The metadata property.
     *
     * @param metadata the metadata value to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setMetadata(Map<String, String> metadata) {
        blobItemInternal.setMetadata(metadata);
        return this;
    }

    /**
     * Get the tags property: The tags property.
     *
     * @return the metadata value.
     */
    public Map<String, String> getTags() {
        if (convertedTags == null) {
            convertedTags = tagMapFromBlobTags(blobItemInternal.getBlobTags());
        }

        return convertedTags;
    }

    /**
     * Set the tags property: The tags property.
     *
     * @param tags the tags value to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setTags(Map<String, String> tags) {
        convertedTags = tags;
        blobItemInternal.setBlobTags(toBlobTags(tags));

        return this;
    }

    /**
     * Get the versionId property: The versionId property.
     *
     * @return the versionId value.
     */
    public String getVersionId() {
        return blobItemInternal.getVersionId();
    }

    /**
     * Set the versionId property: The versionId property.
     *
     * @param versionId the versionId value to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setVersionId(String versionId) {
        blobItemInternal.setVersionId(versionId);
        return this;
    }

    /**
     * Get the isCurrentVersion property: The isCurrentVersion property.
     *
     * @return the isCurrentVersion value.
     */
    public Boolean isCurrentVersion() {
        return blobItemInternal.isCurrentVersion();
    }

    /**
     *  Set the isCurrentVersion property: The isCurrentVersion property.
     *
     * @param isCurrentVersion the isCurrentVersion value to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setCurrentVersion(Boolean isCurrentVersion) {
        blobItemInternal.setIsCurrentVersion(isCurrentVersion);
        return this;
    }

    /**
     * Get the objectReplicationSourcePolicies  property: The
     * objectReplicationSourcePolicies  property.
     *
     * @return the objectReplicationSourcePolicies  value.
     */
    public List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies() {
        if (convertedObjectReplicationSourcePolicies == null) {
            convertedObjectReplicationSourcePolicies =
                transformObjectReplicationMetadata(blobItemInternal.getObjectReplicationMetadata());
        }

        return convertedObjectReplicationSourcePolicies;
    }

    /**
     * Set the objectReplicationSourcePolicies  property: The
     * objectReplicationSourcePolicies  property.
     *
     * @param objectReplicationSourcePolicies the objectReplicationSourcePolicies  value
     * to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setObjectReplicationSourcePolicies(List<ObjectReplicationPolicy> objectReplicationSourcePolicies) {
        convertedObjectReplicationSourcePolicies = objectReplicationSourcePolicies;
        blobItemInternal.setObjectReplicationMetadata(toObjectReplicationMetadata(objectReplicationSourcePolicies));

        return this;
    }

    /**
     * Get the hasVersionsOnly property: The HasVersionsOnly property.
     *
     * @return the hasVersionsOnly value.
     */
    public Boolean hasVersionsOnly() {
        return blobItemInternal.isHasVersionsOnly();
    }

    /**
     * Set the hasVersionsOnly property: The HasVersionsOnly property.
     *
     * @param hasVersionsOnly the hasVersionsOnly value to set.
     * @return the BlobItemInternal object itself.
     */
    public BlobItem setHasVersionsOnly(Boolean hasVersionsOnly) {
        blobItemInternal.setHasVersionsOnly(hasVersionsOnly);
        return this;
    }

    /**
     * Get the isPrefix property: If blobs are named to mimic a directory hierarchy (i.e. path elements separated by a
     * delimiter), this property may be used to determine if the {@code BlobItem} is a virtual directory.
     *
     * @return the isPrefix value.
     */
    public Boolean isPrefix() {
        return blobItemInternal.isPrefix() != null && blobItemInternal.isPrefix();
    }

    /**
     * Set the isPrefix property: The isPrefix property.
     *
     * @param isPrefix the isPrefix value to set.
     * @return the BlobItem object itself.
     */
    public BlobItem setIsPrefix(Boolean isPrefix) {
        blobItemInternal.setIsPrefix(isPrefix);
        return this;
    }
}
