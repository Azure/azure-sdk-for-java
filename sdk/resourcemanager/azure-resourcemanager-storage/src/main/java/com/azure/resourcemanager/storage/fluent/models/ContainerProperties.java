// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.storage.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.storage.models.ImmutabilityPolicyProperties;
import com.azure.resourcemanager.storage.models.ImmutableStorageWithVersioning;
import com.azure.resourcemanager.storage.models.LeaseDuration;
import com.azure.resourcemanager.storage.models.LeaseState;
import com.azure.resourcemanager.storage.models.LeaseStatus;
import com.azure.resourcemanager.storage.models.LegalHoldProperties;
import com.azure.resourcemanager.storage.models.PublicAccess;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The properties of a container.
 */
@Fluent
public final class ContainerProperties implements JsonSerializable<ContainerProperties> {
    /*
     * The version of the deleted blob container.
     */
    private String version;

    /*
     * Indicates whether the blob container was deleted.
     */
    private Boolean deleted;

    /*
     * Blob container deletion time.
     */
    private OffsetDateTime deletedTime;

    /*
     * Remaining retention days for soft deleted blob container.
     */
    private Integer remainingRetentionDays;

    /*
     * Default the container to use specified encryption scope for all writes.
     */
    private String defaultEncryptionScope;

    /*
     * Block override of encryption scope from the container default.
     */
    private Boolean denyEncryptionScopeOverride;

    /*
     * Specifies whether data in the container may be accessed publicly and the level of access.
     */
    private PublicAccess publicAccess;

    /*
     * Returns the date and time the container was last modified.
     */
    private OffsetDateTime lastModifiedTime;

    /*
     * The lease status of the container.
     */
    private LeaseStatus leaseStatus;

    /*
     * Lease state of the container.
     */
    private LeaseState leaseState;

    /*
     * Specifies whether the lease on a container is of infinite or fixed duration, only when the container is leased.
     */
    private LeaseDuration leaseDuration;

    /*
     * A name-value pair to associate with the container as metadata.
     */
    private Map<String, String> metadata;

    /*
     * The ImmutabilityPolicy property of the container.
     */
    private ImmutabilityPolicyProperties immutabilityPolicy;

    /*
     * The LegalHold property of the container.
     */
    private LegalHoldProperties legalHold;

    /*
     * The hasLegalHold public property is set to true by SRP if there are at least one existing tag. The hasLegalHold
     * public property is set to false by SRP if all existing legal hold tags are cleared out. There can be a maximum of
     * 1000 blob containers with hasLegalHold=true for a given account.
     */
    private Boolean hasLegalHold;

    /*
     * The hasImmutabilityPolicy public property is set to true by SRP if ImmutabilityPolicy has been created for this
     * container. The hasImmutabilityPolicy public property is set to false by SRP if ImmutabilityPolicy has not been
     * created for this container.
     */
    private Boolean hasImmutabilityPolicy;

    /*
     * The object level immutability property of the container. The property is immutable and can only be set to true at
     * the container creation time. Existing containers must undergo a migration process.
     */
    private ImmutableStorageWithVersioning immutableStorageWithVersioning;

    /*
     * Enable NFSv3 root squash on blob container.
     */
    private Boolean enableNfsV3RootSquash;

    /*
     * Enable NFSv3 all squash on blob container.
     */
    private Boolean enableNfsV3AllSquash;

    /**
     * Creates an instance of ContainerProperties class.
     */
    public ContainerProperties() {
    }

    /**
     * Get the version property: The version of the deleted blob container.
     * 
     * @return the version value.
     */
    public String version() {
        return this.version;
    }

    /**
     * Get the deleted property: Indicates whether the blob container was deleted.
     * 
     * @return the deleted value.
     */
    public Boolean deleted() {
        return this.deleted;
    }

    /**
     * Get the deletedTime property: Blob container deletion time.
     * 
     * @return the deletedTime value.
     */
    public OffsetDateTime deletedTime() {
        return this.deletedTime;
    }

    /**
     * Get the remainingRetentionDays property: Remaining retention days for soft deleted blob container.
     * 
     * @return the remainingRetentionDays value.
     */
    public Integer remainingRetentionDays() {
        return this.remainingRetentionDays;
    }

    /**
     * Get the defaultEncryptionScope property: Default the container to use specified encryption scope for all writes.
     * 
     * @return the defaultEncryptionScope value.
     */
    public String defaultEncryptionScope() {
        return this.defaultEncryptionScope;
    }

    /**
     * Set the defaultEncryptionScope property: Default the container to use specified encryption scope for all writes.
     * 
     * @param defaultEncryptionScope the defaultEncryptionScope value to set.
     * @return the ContainerProperties object itself.
     */
    public ContainerProperties withDefaultEncryptionScope(String defaultEncryptionScope) {
        this.defaultEncryptionScope = defaultEncryptionScope;
        return this;
    }

    /**
     * Get the denyEncryptionScopeOverride property: Block override of encryption scope from the container default.
     * 
     * @return the denyEncryptionScopeOverride value.
     */
    public Boolean denyEncryptionScopeOverride() {
        return this.denyEncryptionScopeOverride;
    }

    /**
     * Set the denyEncryptionScopeOverride property: Block override of encryption scope from the container default.
     * 
     * @param denyEncryptionScopeOverride the denyEncryptionScopeOverride value to set.
     * @return the ContainerProperties object itself.
     */
    public ContainerProperties withDenyEncryptionScopeOverride(Boolean denyEncryptionScopeOverride) {
        this.denyEncryptionScopeOverride = denyEncryptionScopeOverride;
        return this;
    }

    /**
     * Get the publicAccess property: Specifies whether data in the container may be accessed publicly and the level of
     * access.
     * 
     * @return the publicAccess value.
     */
    public PublicAccess publicAccess() {
        return this.publicAccess;
    }

    /**
     * Set the publicAccess property: Specifies whether data in the container may be accessed publicly and the level of
     * access.
     * 
     * @param publicAccess the publicAccess value to set.
     * @return the ContainerProperties object itself.
     */
    public ContainerProperties withPublicAccess(PublicAccess publicAccess) {
        this.publicAccess = publicAccess;
        return this;
    }

    /**
     * Get the lastModifiedTime property: Returns the date and time the container was last modified.
     * 
     * @return the lastModifiedTime value.
     */
    public OffsetDateTime lastModifiedTime() {
        return this.lastModifiedTime;
    }

    /**
     * Get the leaseStatus property: The lease status of the container.
     * 
     * @return the leaseStatus value.
     */
    public LeaseStatus leaseStatus() {
        return this.leaseStatus;
    }

    /**
     * Get the leaseState property: Lease state of the container.
     * 
     * @return the leaseState value.
     */
    public LeaseState leaseState() {
        return this.leaseState;
    }

    /**
     * Get the leaseDuration property: Specifies whether the lease on a container is of infinite or fixed duration, only
     * when the container is leased.
     * 
     * @return the leaseDuration value.
     */
    public LeaseDuration leaseDuration() {
        return this.leaseDuration;
    }

    /**
     * Get the metadata property: A name-value pair to associate with the container as metadata.
     * 
     * @return the metadata value.
     */
    public Map<String, String> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata property: A name-value pair to associate with the container as metadata.
     * 
     * @param metadata the metadata value to set.
     * @return the ContainerProperties object itself.
     */
    public ContainerProperties withMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get the immutabilityPolicy property: The ImmutabilityPolicy property of the container.
     * 
     * @return the immutabilityPolicy value.
     */
    public ImmutabilityPolicyProperties immutabilityPolicy() {
        return this.immutabilityPolicy;
    }

    /**
     * Get the legalHold property: The LegalHold property of the container.
     * 
     * @return the legalHold value.
     */
    public LegalHoldProperties legalHold() {
        return this.legalHold;
    }

    /**
     * Get the hasLegalHold property: The hasLegalHold public property is set to true by SRP if there are at least one
     * existing tag. The hasLegalHold public property is set to false by SRP if all existing legal hold tags are cleared
     * out. There can be a maximum of 1000 blob containers with hasLegalHold=true for a given account.
     * 
     * @return the hasLegalHold value.
     */
    public Boolean hasLegalHold() {
        return this.hasLegalHold;
    }

    /**
     * Get the hasImmutabilityPolicy property: The hasImmutabilityPolicy public property is set to true by SRP if
     * ImmutabilityPolicy has been created for this container. The hasImmutabilityPolicy public property is set to false
     * by SRP if ImmutabilityPolicy has not been created for this container.
     * 
     * @return the hasImmutabilityPolicy value.
     */
    public Boolean hasImmutabilityPolicy() {
        return this.hasImmutabilityPolicy;
    }

    /**
     * Get the immutableStorageWithVersioning property: The object level immutability property of the container. The
     * property is immutable and can only be set to true at the container creation time. Existing containers must
     * undergo a migration process.
     * 
     * @return the immutableStorageWithVersioning value.
     */
    public ImmutableStorageWithVersioning immutableStorageWithVersioning() {
        return this.immutableStorageWithVersioning;
    }

    /**
     * Set the immutableStorageWithVersioning property: The object level immutability property of the container. The
     * property is immutable and can only be set to true at the container creation time. Existing containers must
     * undergo a migration process.
     * 
     * @param immutableStorageWithVersioning the immutableStorageWithVersioning value to set.
     * @return the ContainerProperties object itself.
     */
    public ContainerProperties
        withImmutableStorageWithVersioning(ImmutableStorageWithVersioning immutableStorageWithVersioning) {
        this.immutableStorageWithVersioning = immutableStorageWithVersioning;
        return this;
    }

    /**
     * Get the enableNfsV3RootSquash property: Enable NFSv3 root squash on blob container.
     * 
     * @return the enableNfsV3RootSquash value.
     */
    public Boolean enableNfsV3RootSquash() {
        return this.enableNfsV3RootSquash;
    }

    /**
     * Set the enableNfsV3RootSquash property: Enable NFSv3 root squash on blob container.
     * 
     * @param enableNfsV3RootSquash the enableNfsV3RootSquash value to set.
     * @return the ContainerProperties object itself.
     */
    public ContainerProperties withEnableNfsV3RootSquash(Boolean enableNfsV3RootSquash) {
        this.enableNfsV3RootSquash = enableNfsV3RootSquash;
        return this;
    }

    /**
     * Get the enableNfsV3AllSquash property: Enable NFSv3 all squash on blob container.
     * 
     * @return the enableNfsV3AllSquash value.
     */
    public Boolean enableNfsV3AllSquash() {
        return this.enableNfsV3AllSquash;
    }

    /**
     * Set the enableNfsV3AllSquash property: Enable NFSv3 all squash on blob container.
     * 
     * @param enableNfsV3AllSquash the enableNfsV3AllSquash value to set.
     * @return the ContainerProperties object itself.
     */
    public ContainerProperties withEnableNfsV3AllSquash(Boolean enableNfsV3AllSquash) {
        this.enableNfsV3AllSquash = enableNfsV3AllSquash;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (immutabilityPolicy() != null) {
            immutabilityPolicy().validate();
        }
        if (legalHold() != null) {
            legalHold().validate();
        }
        if (immutableStorageWithVersioning() != null) {
            immutableStorageWithVersioning().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("defaultEncryptionScope", this.defaultEncryptionScope);
        jsonWriter.writeBooleanField("denyEncryptionScopeOverride", this.denyEncryptionScopeOverride);
        jsonWriter.writeStringField("publicAccess", this.publicAccess == null ? null : this.publicAccess.toString());
        jsonWriter.writeMapField("metadata", this.metadata, (writer, element) -> writer.writeString(element));
        jsonWriter.writeJsonField("immutableStorageWithVersioning", this.immutableStorageWithVersioning);
        jsonWriter.writeBooleanField("enableNfsV3RootSquash", this.enableNfsV3RootSquash);
        jsonWriter.writeBooleanField("enableNfsV3AllSquash", this.enableNfsV3AllSquash);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ContainerProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ContainerProperties if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ContainerProperties.
     */
    public static ContainerProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ContainerProperties deserializedContainerProperties = new ContainerProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("version".equals(fieldName)) {
                    deserializedContainerProperties.version = reader.getString();
                } else if ("deleted".equals(fieldName)) {
                    deserializedContainerProperties.deleted = reader.getNullable(JsonReader::getBoolean);
                } else if ("deletedTime".equals(fieldName)) {
                    deserializedContainerProperties.deletedTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("remainingRetentionDays".equals(fieldName)) {
                    deserializedContainerProperties.remainingRetentionDays = reader.getNullable(JsonReader::getInt);
                } else if ("defaultEncryptionScope".equals(fieldName)) {
                    deserializedContainerProperties.defaultEncryptionScope = reader.getString();
                } else if ("denyEncryptionScopeOverride".equals(fieldName)) {
                    deserializedContainerProperties.denyEncryptionScopeOverride
                        = reader.getNullable(JsonReader::getBoolean);
                } else if ("publicAccess".equals(fieldName)) {
                    deserializedContainerProperties.publicAccess = PublicAccess.fromString(reader.getString());
                } else if ("lastModifiedTime".equals(fieldName)) {
                    deserializedContainerProperties.lastModifiedTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("leaseStatus".equals(fieldName)) {
                    deserializedContainerProperties.leaseStatus = LeaseStatus.fromString(reader.getString());
                } else if ("leaseState".equals(fieldName)) {
                    deserializedContainerProperties.leaseState = LeaseState.fromString(reader.getString());
                } else if ("leaseDuration".equals(fieldName)) {
                    deserializedContainerProperties.leaseDuration = LeaseDuration.fromString(reader.getString());
                } else if ("metadata".equals(fieldName)) {
                    Map<String, String> metadata = reader.readMap(reader1 -> reader1.getString());
                    deserializedContainerProperties.metadata = metadata;
                } else if ("immutabilityPolicy".equals(fieldName)) {
                    deserializedContainerProperties.immutabilityPolicy = ImmutabilityPolicyProperties.fromJson(reader);
                } else if ("legalHold".equals(fieldName)) {
                    deserializedContainerProperties.legalHold = LegalHoldProperties.fromJson(reader);
                } else if ("hasLegalHold".equals(fieldName)) {
                    deserializedContainerProperties.hasLegalHold = reader.getNullable(JsonReader::getBoolean);
                } else if ("hasImmutabilityPolicy".equals(fieldName)) {
                    deserializedContainerProperties.hasImmutabilityPolicy = reader.getNullable(JsonReader::getBoolean);
                } else if ("immutableStorageWithVersioning".equals(fieldName)) {
                    deserializedContainerProperties.immutableStorageWithVersioning
                        = ImmutableStorageWithVersioning.fromJson(reader);
                } else if ("enableNfsV3RootSquash".equals(fieldName)) {
                    deserializedContainerProperties.enableNfsV3RootSquash = reader.getNullable(JsonReader::getBoolean);
                } else if ("enableNfsV3AllSquash".equals(fieldName)) {
                    deserializedContainerProperties.enableNfsV3AllSquash = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedContainerProperties;
        });
    }
}
