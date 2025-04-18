// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicesbackup.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Container with items backed up using MAB backup engine.
 */
@Fluent
public final class MabContainer extends ProtectionContainer {
    /*
     * Type of the container. The value of this property for: 1. Compute Azure VM is Microsoft.Compute/virtualMachines
     * 2.
     * Classic Compute Azure VM is Microsoft.ClassicCompute/virtualMachines 3. Windows machines (like MAB, DPM etc) is
     * Windows 4. Azure SQL instance is AzureSqlContainer. 5. Storage containers is StorageContainer. 6. Azure workload
     * Backup is VMAppContainer
     */
    private ProtectableContainerType containerType = ProtectableContainerType.WINDOWS;

    /*
     * Can the container be registered one more time.
     */
    private Boolean canReRegister;

    /*
     * ContainerID represents the container.
     */
    private Long containerId;

    /*
     * Number of items backed up in this container.
     */
    private Long protectedItemCount;

    /*
     * Agent version of this container.
     */
    private String agentVersion;

    /*
     * Additional information for this container
     */
    private MabContainerExtendedInfo extendedInfo;

    /*
     * Health details on this mab container.
     */
    private List<MabContainerHealthDetails> mabContainerHealthDetails;

    /*
     * Health state of mab container.
     */
    private String containerHealthState;

    /**
     * Creates an instance of MabContainer class.
     */
    public MabContainer() {
    }

    /**
     * Get the containerType property: Type of the container. The value of this property for: 1. Compute Azure VM is
     * Microsoft.Compute/virtualMachines 2.
     * Classic Compute Azure VM is Microsoft.ClassicCompute/virtualMachines 3. Windows machines (like MAB, DPM etc) is
     * Windows 4. Azure SQL instance is AzureSqlContainer. 5. Storage containers is StorageContainer. 6. Azure workload
     * Backup is VMAppContainer.
     * 
     * @return the containerType value.
     */
    @Override
    public ProtectableContainerType containerType() {
        return this.containerType;
    }

    /**
     * Get the canReRegister property: Can the container be registered one more time.
     * 
     * @return the canReRegister value.
     */
    public Boolean canReRegister() {
        return this.canReRegister;
    }

    /**
     * Set the canReRegister property: Can the container be registered one more time.
     * 
     * @param canReRegister the canReRegister value to set.
     * @return the MabContainer object itself.
     */
    public MabContainer withCanReRegister(Boolean canReRegister) {
        this.canReRegister = canReRegister;
        return this;
    }

    /**
     * Get the containerId property: ContainerID represents the container.
     * 
     * @return the containerId value.
     */
    public Long containerId() {
        return this.containerId;
    }

    /**
     * Set the containerId property: ContainerID represents the container.
     * 
     * @param containerId the containerId value to set.
     * @return the MabContainer object itself.
     */
    public MabContainer withContainerId(Long containerId) {
        this.containerId = containerId;
        return this;
    }

    /**
     * Get the protectedItemCount property: Number of items backed up in this container.
     * 
     * @return the protectedItemCount value.
     */
    public Long protectedItemCount() {
        return this.protectedItemCount;
    }

    /**
     * Set the protectedItemCount property: Number of items backed up in this container.
     * 
     * @param protectedItemCount the protectedItemCount value to set.
     * @return the MabContainer object itself.
     */
    public MabContainer withProtectedItemCount(Long protectedItemCount) {
        this.protectedItemCount = protectedItemCount;
        return this;
    }

    /**
     * Get the agentVersion property: Agent version of this container.
     * 
     * @return the agentVersion value.
     */
    public String agentVersion() {
        return this.agentVersion;
    }

    /**
     * Set the agentVersion property: Agent version of this container.
     * 
     * @param agentVersion the agentVersion value to set.
     * @return the MabContainer object itself.
     */
    public MabContainer withAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
        return this;
    }

    /**
     * Get the extendedInfo property: Additional information for this container.
     * 
     * @return the extendedInfo value.
     */
    public MabContainerExtendedInfo extendedInfo() {
        return this.extendedInfo;
    }

    /**
     * Set the extendedInfo property: Additional information for this container.
     * 
     * @param extendedInfo the extendedInfo value to set.
     * @return the MabContainer object itself.
     */
    public MabContainer withExtendedInfo(MabContainerExtendedInfo extendedInfo) {
        this.extendedInfo = extendedInfo;
        return this;
    }

    /**
     * Get the mabContainerHealthDetails property: Health details on this mab container.
     * 
     * @return the mabContainerHealthDetails value.
     */
    public List<MabContainerHealthDetails> mabContainerHealthDetails() {
        return this.mabContainerHealthDetails;
    }

    /**
     * Set the mabContainerHealthDetails property: Health details on this mab container.
     * 
     * @param mabContainerHealthDetails the mabContainerHealthDetails value to set.
     * @return the MabContainer object itself.
     */
    public MabContainer withMabContainerHealthDetails(List<MabContainerHealthDetails> mabContainerHealthDetails) {
        this.mabContainerHealthDetails = mabContainerHealthDetails;
        return this;
    }

    /**
     * Get the containerHealthState property: Health state of mab container.
     * 
     * @return the containerHealthState value.
     */
    public String containerHealthState() {
        return this.containerHealthState;
    }

    /**
     * Set the containerHealthState property: Health state of mab container.
     * 
     * @param containerHealthState the containerHealthState value to set.
     * @return the MabContainer object itself.
     */
    public MabContainer withContainerHealthState(String containerHealthState) {
        this.containerHealthState = containerHealthState;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MabContainer withFriendlyName(String friendlyName) {
        super.withFriendlyName(friendlyName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MabContainer withBackupManagementType(BackupManagementType backupManagementType) {
        super.withBackupManagementType(backupManagementType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MabContainer withRegistrationStatus(String registrationStatus) {
        super.withRegistrationStatus(registrationStatus);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MabContainer withHealthStatus(String healthStatus) {
        super.withHealthStatus(healthStatus);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MabContainer withProtectableObjectType(String protectableObjectType) {
        super.withProtectableObjectType(protectableObjectType);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (extendedInfo() != null) {
            extendedInfo().validate();
        }
        if (mabContainerHealthDetails() != null) {
            mabContainerHealthDetails().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("friendlyName", friendlyName());
        jsonWriter.writeStringField("backupManagementType",
            backupManagementType() == null ? null : backupManagementType().toString());
        jsonWriter.writeStringField("registrationStatus", registrationStatus());
        jsonWriter.writeStringField("healthStatus", healthStatus());
        jsonWriter.writeStringField("protectableObjectType", protectableObjectType());
        jsonWriter.writeStringField("containerType", this.containerType == null ? null : this.containerType.toString());
        jsonWriter.writeBooleanField("canReRegister", this.canReRegister);
        jsonWriter.writeNumberField("containerId", this.containerId);
        jsonWriter.writeNumberField("protectedItemCount", this.protectedItemCount);
        jsonWriter.writeStringField("agentVersion", this.agentVersion);
        jsonWriter.writeJsonField("extendedInfo", this.extendedInfo);
        jsonWriter.writeArrayField("mabContainerHealthDetails", this.mabContainerHealthDetails,
            (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("containerHealthState", this.containerHealthState);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MabContainer from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of MabContainer if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the MabContainer.
     */
    public static MabContainer fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            MabContainer deserializedMabContainer = new MabContainer();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("friendlyName".equals(fieldName)) {
                    deserializedMabContainer.withFriendlyName(reader.getString());
                } else if ("backupManagementType".equals(fieldName)) {
                    deserializedMabContainer
                        .withBackupManagementType(BackupManagementType.fromString(reader.getString()));
                } else if ("registrationStatus".equals(fieldName)) {
                    deserializedMabContainer.withRegistrationStatus(reader.getString());
                } else if ("healthStatus".equals(fieldName)) {
                    deserializedMabContainer.withHealthStatus(reader.getString());
                } else if ("protectableObjectType".equals(fieldName)) {
                    deserializedMabContainer.withProtectableObjectType(reader.getString());
                } else if ("containerType".equals(fieldName)) {
                    deserializedMabContainer.containerType = ProtectableContainerType.fromString(reader.getString());
                } else if ("canReRegister".equals(fieldName)) {
                    deserializedMabContainer.canReRegister = reader.getNullable(JsonReader::getBoolean);
                } else if ("containerId".equals(fieldName)) {
                    deserializedMabContainer.containerId = reader.getNullable(JsonReader::getLong);
                } else if ("protectedItemCount".equals(fieldName)) {
                    deserializedMabContainer.protectedItemCount = reader.getNullable(JsonReader::getLong);
                } else if ("agentVersion".equals(fieldName)) {
                    deserializedMabContainer.agentVersion = reader.getString();
                } else if ("extendedInfo".equals(fieldName)) {
                    deserializedMabContainer.extendedInfo = MabContainerExtendedInfo.fromJson(reader);
                } else if ("mabContainerHealthDetails".equals(fieldName)) {
                    List<MabContainerHealthDetails> mabContainerHealthDetails
                        = reader.readArray(reader1 -> MabContainerHealthDetails.fromJson(reader1));
                    deserializedMabContainer.mabContainerHealthDetails = mabContainerHealthDetails;
                } else if ("containerHealthState".equals(fieldName)) {
                    deserializedMabContainer.containerHealthState = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedMabContainer;
        });
    }
}
