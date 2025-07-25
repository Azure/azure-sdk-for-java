// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.compute.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Resource;
import com.azure.core.management.SystemData;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.compute.models.ApiError;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.compute.models.EncryptionSetIdentity;
import com.azure.resourcemanager.compute.models.KeyForDiskEncryptionSet;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * disk encryption set resource.
 */
@Fluent
public final class DiskEncryptionSetInner extends Resource {
    /*
     * The properties property.
     */
    private EncryptionSetProperties innerProperties;

    /*
     * The managed identity for the disk encryption set. It should be given permission on the key vault before it can be
     * used to encrypt disks.
     */
    private EncryptionSetIdentity identity;

    /*
     * Azure Resource Manager metadata containing createdBy and modifiedBy information.
     */
    private SystemData systemData;

    /*
     * The type of the resource.
     */
    private String type;

    /*
     * The name of the resource.
     */
    private String name;

    /*
     * Fully qualified resource Id for the resource.
     */
    private String id;

    /**
     * Creates an instance of DiskEncryptionSetInner class.
     */
    public DiskEncryptionSetInner() {
    }

    /**
     * Get the innerProperties property: The properties property.
     * 
     * @return the innerProperties value.
     */
    private EncryptionSetProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the identity property: The managed identity for the disk encryption set. It should be given permission on the
     * key vault before it can be used to encrypt disks.
     * 
     * @return the identity value.
     */
    public EncryptionSetIdentity identity() {
        return this.identity;
    }

    /**
     * Set the identity property: The managed identity for the disk encryption set. It should be given permission on the
     * key vault before it can be used to encrypt disks.
     * 
     * @param identity the identity value to set.
     * @return the DiskEncryptionSetInner object itself.
     */
    public DiskEncryptionSetInner withIdentity(EncryptionSetIdentity identity) {
        this.identity = identity;
        return this;
    }

    /**
     * Get the systemData property: Azure Resource Manager metadata containing createdBy and modifiedBy information.
     * 
     * @return the systemData value.
     */
    public SystemData systemData() {
        return this.systemData;
    }

    /**
     * Get the type property: The type of the resource.
     * 
     * @return the type value.
     */
    @Override
    public String type() {
        return this.type;
    }

    /**
     * Get the name property: The name of the resource.
     * 
     * @return the name value.
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * Get the id property: Fully qualified resource Id for the resource.
     * 
     * @return the id value.
     */
    @Override
    public String id() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiskEncryptionSetInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiskEncryptionSetInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Get the encryptionType property: The type of key used to encrypt the data of the disk.
     * 
     * @return the encryptionType value.
     */
    public DiskEncryptionSetType encryptionType() {
        return this.innerProperties() == null ? null : this.innerProperties().encryptionType();
    }

    /**
     * Set the encryptionType property: The type of key used to encrypt the data of the disk.
     * 
     * @param encryptionType the encryptionType value to set.
     * @return the DiskEncryptionSetInner object itself.
     */
    public DiskEncryptionSetInner withEncryptionType(DiskEncryptionSetType encryptionType) {
        if (this.innerProperties() == null) {
            this.innerProperties = new EncryptionSetProperties();
        }
        this.innerProperties().withEncryptionType(encryptionType);
        return this;
    }

    /**
     * Get the activeKey property: The key vault key which is currently used by this disk encryption set.
     * 
     * @return the activeKey value.
     */
    public KeyForDiskEncryptionSet activeKey() {
        return this.innerProperties() == null ? null : this.innerProperties().activeKey();
    }

    /**
     * Set the activeKey property: The key vault key which is currently used by this disk encryption set.
     * 
     * @param activeKey the activeKey value to set.
     * @return the DiskEncryptionSetInner object itself.
     */
    public DiskEncryptionSetInner withActiveKey(KeyForDiskEncryptionSet activeKey) {
        if (this.innerProperties() == null) {
            this.innerProperties = new EncryptionSetProperties();
        }
        this.innerProperties().withActiveKey(activeKey);
        return this;
    }

    /**
     * Get the previousKeys property: A readonly collection of key vault keys previously used by this disk encryption
     * set while a key rotation is in progress. It will be empty if there is no ongoing key rotation.
     * 
     * @return the previousKeys value.
     */
    public List<KeyForDiskEncryptionSet> previousKeys() {
        return this.innerProperties() == null ? null : this.innerProperties().previousKeys();
    }

    /**
     * Get the provisioningState property: The disk encryption set provisioning state.
     * 
     * @return the provisioningState value.
     */
    public String provisioningState() {
        return this.innerProperties() == null ? null : this.innerProperties().provisioningState();
    }

    /**
     * Get the rotationToLatestKeyVersionEnabled property: Set this flag to true to enable auto-updating of this disk
     * encryption set to the latest key version.
     * 
     * @return the rotationToLatestKeyVersionEnabled value.
     */
    public Boolean rotationToLatestKeyVersionEnabled() {
        return this.innerProperties() == null ? null : this.innerProperties().rotationToLatestKeyVersionEnabled();
    }

    /**
     * Set the rotationToLatestKeyVersionEnabled property: Set this flag to true to enable auto-updating of this disk
     * encryption set to the latest key version.
     * 
     * @param rotationToLatestKeyVersionEnabled the rotationToLatestKeyVersionEnabled value to set.
     * @return the DiskEncryptionSetInner object itself.
     */
    public DiskEncryptionSetInner withRotationToLatestKeyVersionEnabled(Boolean rotationToLatestKeyVersionEnabled) {
        if (this.innerProperties() == null) {
            this.innerProperties = new EncryptionSetProperties();
        }
        this.innerProperties().withRotationToLatestKeyVersionEnabled(rotationToLatestKeyVersionEnabled);
        return this;
    }

    /**
     * Get the lastKeyRotationTimestamp property: The time when the active key of this disk encryption set was updated.
     * 
     * @return the lastKeyRotationTimestamp value.
     */
    public OffsetDateTime lastKeyRotationTimestamp() {
        return this.innerProperties() == null ? null : this.innerProperties().lastKeyRotationTimestamp();
    }

    /**
     * Get the autoKeyRotationError property: The error that was encountered during auto-key rotation. If an error is
     * present, then auto-key rotation will not be attempted until the error on this disk encryption set is fixed.
     * 
     * @return the autoKeyRotationError value.
     */
    public ApiError autoKeyRotationError() {
        return this.innerProperties() == null ? null : this.innerProperties().autoKeyRotationError();
    }

    /**
     * Get the federatedClientId property: Multi-tenant application client id to access key vault in a different tenant.
     * Setting the value to 'None' will clear the property.
     * 
     * @return the federatedClientId value.
     */
    public String federatedClientId() {
        return this.innerProperties() == null ? null : this.innerProperties().federatedClientId();
    }

    /**
     * Set the federatedClientId property: Multi-tenant application client id to access key vault in a different tenant.
     * Setting the value to 'None' will clear the property.
     * 
     * @param federatedClientId the federatedClientId value to set.
     * @return the DiskEncryptionSetInner object itself.
     */
    public DiskEncryptionSetInner withFederatedClientId(String federatedClientId) {
        if (this.innerProperties() == null) {
            this.innerProperties = new EncryptionSetProperties();
        }
        this.innerProperties().withFederatedClientId(federatedClientId);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (innerProperties() != null) {
            innerProperties().validate();
        }
        if (identity() != null) {
            identity().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("location", location());
        jsonWriter.writeMapField("tags", tags(), (writer, element) -> writer.writeString(element));
        jsonWriter.writeJsonField("properties", this.innerProperties);
        jsonWriter.writeJsonField("identity", this.identity);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DiskEncryptionSetInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DiskEncryptionSetInner if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the DiskEncryptionSetInner.
     */
    public static DiskEncryptionSetInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DiskEncryptionSetInner deserializedDiskEncryptionSetInner = new DiskEncryptionSetInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedDiskEncryptionSetInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedDiskEncryptionSetInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedDiskEncryptionSetInner.type = reader.getString();
                } else if ("location".equals(fieldName)) {
                    deserializedDiskEncryptionSetInner.withLocation(reader.getString());
                } else if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedDiskEncryptionSetInner.withTags(tags);
                } else if ("properties".equals(fieldName)) {
                    deserializedDiskEncryptionSetInner.innerProperties = EncryptionSetProperties.fromJson(reader);
                } else if ("identity".equals(fieldName)) {
                    deserializedDiskEncryptionSetInner.identity = EncryptionSetIdentity.fromJson(reader);
                } else if ("systemData".equals(fieldName)) {
                    deserializedDiskEncryptionSetInner.systemData = SystemData.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedDiskEncryptionSetInner;
        });
    }
}
