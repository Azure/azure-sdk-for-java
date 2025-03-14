// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicesbackup.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Backup resource vault config details.
 */
@Fluent
public final class BackupResourceVaultConfig implements JsonSerializable<BackupResourceVaultConfig> {
    /*
     * Storage type.
     */
    private StorageType storageModelType;

    /*
     * Storage type.
     */
    private StorageType storageType;

    /*
     * Locked or Unlocked. Once a machine is registered against a resource, the storageTypeState is always Locked.
     */
    private StorageTypeState storageTypeState;

    /*
     * Enabled or Disabled.
     */
    private EnhancedSecurityState enhancedSecurityState;

    /*
     * Soft Delete feature state
     */
    private SoftDeleteFeatureState softDeleteFeatureState;

    /*
     * Soft delete retention period in days
     */
    private Integer softDeleteRetentionPeriodInDays;

    /*
     * ResourceGuard Operation Requests
     */
    private List<String> resourceGuardOperationRequests;

    /*
     * This flag is no longer in use. Please use 'softDeleteFeatureState' to set the soft delete state for the vault
     */
    private Boolean isSoftDeleteFeatureStateEditable;

    /**
     * Creates an instance of BackupResourceVaultConfig class.
     */
    public BackupResourceVaultConfig() {
    }

    /**
     * Get the storageModelType property: Storage type.
     * 
     * @return the storageModelType value.
     */
    public StorageType storageModelType() {
        return this.storageModelType;
    }

    /**
     * Set the storageModelType property: Storage type.
     * 
     * @param storageModelType the storageModelType value to set.
     * @return the BackupResourceVaultConfig object itself.
     */
    public BackupResourceVaultConfig withStorageModelType(StorageType storageModelType) {
        this.storageModelType = storageModelType;
        return this;
    }

    /**
     * Get the storageType property: Storage type.
     * 
     * @return the storageType value.
     */
    public StorageType storageType() {
        return this.storageType;
    }

    /**
     * Set the storageType property: Storage type.
     * 
     * @param storageType the storageType value to set.
     * @return the BackupResourceVaultConfig object itself.
     */
    public BackupResourceVaultConfig withStorageType(StorageType storageType) {
        this.storageType = storageType;
        return this;
    }

    /**
     * Get the storageTypeState property: Locked or Unlocked. Once a machine is registered against a resource, the
     * storageTypeState is always Locked.
     * 
     * @return the storageTypeState value.
     */
    public StorageTypeState storageTypeState() {
        return this.storageTypeState;
    }

    /**
     * Set the storageTypeState property: Locked or Unlocked. Once a machine is registered against a resource, the
     * storageTypeState is always Locked.
     * 
     * @param storageTypeState the storageTypeState value to set.
     * @return the BackupResourceVaultConfig object itself.
     */
    public BackupResourceVaultConfig withStorageTypeState(StorageTypeState storageTypeState) {
        this.storageTypeState = storageTypeState;
        return this;
    }

    /**
     * Get the enhancedSecurityState property: Enabled or Disabled.
     * 
     * @return the enhancedSecurityState value.
     */
    public EnhancedSecurityState enhancedSecurityState() {
        return this.enhancedSecurityState;
    }

    /**
     * Set the enhancedSecurityState property: Enabled or Disabled.
     * 
     * @param enhancedSecurityState the enhancedSecurityState value to set.
     * @return the BackupResourceVaultConfig object itself.
     */
    public BackupResourceVaultConfig withEnhancedSecurityState(EnhancedSecurityState enhancedSecurityState) {
        this.enhancedSecurityState = enhancedSecurityState;
        return this;
    }

    /**
     * Get the softDeleteFeatureState property: Soft Delete feature state.
     * 
     * @return the softDeleteFeatureState value.
     */
    public SoftDeleteFeatureState softDeleteFeatureState() {
        return this.softDeleteFeatureState;
    }

    /**
     * Set the softDeleteFeatureState property: Soft Delete feature state.
     * 
     * @param softDeleteFeatureState the softDeleteFeatureState value to set.
     * @return the BackupResourceVaultConfig object itself.
     */
    public BackupResourceVaultConfig withSoftDeleteFeatureState(SoftDeleteFeatureState softDeleteFeatureState) {
        this.softDeleteFeatureState = softDeleteFeatureState;
        return this;
    }

    /**
     * Get the softDeleteRetentionPeriodInDays property: Soft delete retention period in days.
     * 
     * @return the softDeleteRetentionPeriodInDays value.
     */
    public Integer softDeleteRetentionPeriodInDays() {
        return this.softDeleteRetentionPeriodInDays;
    }

    /**
     * Set the softDeleteRetentionPeriodInDays property: Soft delete retention period in days.
     * 
     * @param softDeleteRetentionPeriodInDays the softDeleteRetentionPeriodInDays value to set.
     * @return the BackupResourceVaultConfig object itself.
     */
    public BackupResourceVaultConfig withSoftDeleteRetentionPeriodInDays(Integer softDeleteRetentionPeriodInDays) {
        this.softDeleteRetentionPeriodInDays = softDeleteRetentionPeriodInDays;
        return this;
    }

    /**
     * Get the resourceGuardOperationRequests property: ResourceGuard Operation Requests.
     * 
     * @return the resourceGuardOperationRequests value.
     */
    public List<String> resourceGuardOperationRequests() {
        return this.resourceGuardOperationRequests;
    }

    /**
     * Set the resourceGuardOperationRequests property: ResourceGuard Operation Requests.
     * 
     * @param resourceGuardOperationRequests the resourceGuardOperationRequests value to set.
     * @return the BackupResourceVaultConfig object itself.
     */
    public BackupResourceVaultConfig withResourceGuardOperationRequests(List<String> resourceGuardOperationRequests) {
        this.resourceGuardOperationRequests = resourceGuardOperationRequests;
        return this;
    }

    /**
     * Get the isSoftDeleteFeatureStateEditable property: This flag is no longer in use. Please use
     * 'softDeleteFeatureState' to set the soft delete state for the vault.
     * 
     * @return the isSoftDeleteFeatureStateEditable value.
     */
    public Boolean isSoftDeleteFeatureStateEditable() {
        return this.isSoftDeleteFeatureStateEditable;
    }

    /**
     * Set the isSoftDeleteFeatureStateEditable property: This flag is no longer in use. Please use
     * 'softDeleteFeatureState' to set the soft delete state for the vault.
     * 
     * @param isSoftDeleteFeatureStateEditable the isSoftDeleteFeatureStateEditable value to set.
     * @return the BackupResourceVaultConfig object itself.
     */
    public BackupResourceVaultConfig withIsSoftDeleteFeatureStateEditable(Boolean isSoftDeleteFeatureStateEditable) {
        this.isSoftDeleteFeatureStateEditable = isSoftDeleteFeatureStateEditable;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("storageModelType",
            this.storageModelType == null ? null : this.storageModelType.toString());
        jsonWriter.writeStringField("storageType", this.storageType == null ? null : this.storageType.toString());
        jsonWriter.writeStringField("storageTypeState",
            this.storageTypeState == null ? null : this.storageTypeState.toString());
        jsonWriter.writeStringField("enhancedSecurityState",
            this.enhancedSecurityState == null ? null : this.enhancedSecurityState.toString());
        jsonWriter.writeStringField("softDeleteFeatureState",
            this.softDeleteFeatureState == null ? null : this.softDeleteFeatureState.toString());
        jsonWriter.writeNumberField("softDeleteRetentionPeriodInDays", this.softDeleteRetentionPeriodInDays);
        jsonWriter.writeArrayField("resourceGuardOperationRequests", this.resourceGuardOperationRequests,
            (writer, element) -> writer.writeString(element));
        jsonWriter.writeBooleanField("isSoftDeleteFeatureStateEditable", this.isSoftDeleteFeatureStateEditable);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of BackupResourceVaultConfig from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of BackupResourceVaultConfig if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the BackupResourceVaultConfig.
     */
    public static BackupResourceVaultConfig fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BackupResourceVaultConfig deserializedBackupResourceVaultConfig = new BackupResourceVaultConfig();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("storageModelType".equals(fieldName)) {
                    deserializedBackupResourceVaultConfig.storageModelType = StorageType.fromString(reader.getString());
                } else if ("storageType".equals(fieldName)) {
                    deserializedBackupResourceVaultConfig.storageType = StorageType.fromString(reader.getString());
                } else if ("storageTypeState".equals(fieldName)) {
                    deserializedBackupResourceVaultConfig.storageTypeState
                        = StorageTypeState.fromString(reader.getString());
                } else if ("enhancedSecurityState".equals(fieldName)) {
                    deserializedBackupResourceVaultConfig.enhancedSecurityState
                        = EnhancedSecurityState.fromString(reader.getString());
                } else if ("softDeleteFeatureState".equals(fieldName)) {
                    deserializedBackupResourceVaultConfig.softDeleteFeatureState
                        = SoftDeleteFeatureState.fromString(reader.getString());
                } else if ("softDeleteRetentionPeriodInDays".equals(fieldName)) {
                    deserializedBackupResourceVaultConfig.softDeleteRetentionPeriodInDays
                        = reader.getNullable(JsonReader::getInt);
                } else if ("resourceGuardOperationRequests".equals(fieldName)) {
                    List<String> resourceGuardOperationRequests = reader.readArray(reader1 -> reader1.getString());
                    deserializedBackupResourceVaultConfig.resourceGuardOperationRequests
                        = resourceGuardOperationRequests;
                } else if ("isSoftDeleteFeatureStateEditable".equals(fieldName)) {
                    deserializedBackupResourceVaultConfig.isSoftDeleteFeatureStateEditable
                        = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedBackupResourceVaultConfig;
        });
    }
}
