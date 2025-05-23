// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicessiterecovery.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * HyperV replica Azure input to update replication protected item.
 */
@Fluent
public final class HyperVReplicaAzureUpdateReplicationProtectedItemInput
    extends UpdateReplicationProtectedItemProviderInput {
    /*
     * The class type.
     */
    private String instanceType = "HyperVReplicaAzure";

    /*
     * The recovery Azure resource group Id for classic deployment.
     */
    private String recoveryAzureV1ResourceGroupId;

    /*
     * The recovery Azure resource group Id for resource manager deployment.
     */
    private String recoveryAzureV2ResourceGroupId;

    /*
     * A value indicating whether managed disks should be used during failover.
     */
    private String useManagedDisks;

    /*
     * The dictionary of disk resource Id to disk encryption set ARM Id.
     */
    private Map<String, String> diskIdToDiskEncryptionMap;

    /*
     * The target proximity placement group Id.
     */
    private String targetProximityPlacementGroupId;

    /*
     * The target availability zone.
     */
    private String targetAvailabilityZone;

    /*
     * The target VM tags.
     */
    private Map<String, String> targetVmTags;

    /*
     * The tags for the target managed disks.
     */
    private Map<String, String> targetManagedDiskTags;

    /*
     * The tags for the target NICs.
     */
    private Map<String, String> targetNicTags;

    /*
     * The SQL Server license type.
     */
    private SqlServerLicenseType sqlServerLicenseType;

    /*
     * The license type for Linux VM's.
     */
    private LinuxLicenseType linuxLicenseType;

    /*
     * The OS name selected by user.
     */
    private String userSelectedOSName;

    /*
     * The list of disk update properties.
     */
    private List<UpdateDiskInput> vmDisks;

    /**
     * Creates an instance of HyperVReplicaAzureUpdateReplicationProtectedItemInput class.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput() {
    }

    /**
     * Get the instanceType property: The class type.
     * 
     * @return the instanceType value.
     */
    @Override
    public String instanceType() {
        return this.instanceType;
    }

    /**
     * Get the recoveryAzureV1ResourceGroupId property: The recovery Azure resource group Id for classic deployment.
     * 
     * @return the recoveryAzureV1ResourceGroupId value.
     */
    public String recoveryAzureV1ResourceGroupId() {
        return this.recoveryAzureV1ResourceGroupId;
    }

    /**
     * Set the recoveryAzureV1ResourceGroupId property: The recovery Azure resource group Id for classic deployment.
     * 
     * @param recoveryAzureV1ResourceGroupId the recoveryAzureV1ResourceGroupId value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput
        withRecoveryAzureV1ResourceGroupId(String recoveryAzureV1ResourceGroupId) {
        this.recoveryAzureV1ResourceGroupId = recoveryAzureV1ResourceGroupId;
        return this;
    }

    /**
     * Get the recoveryAzureV2ResourceGroupId property: The recovery Azure resource group Id for resource manager
     * deployment.
     * 
     * @return the recoveryAzureV2ResourceGroupId value.
     */
    public String recoveryAzureV2ResourceGroupId() {
        return this.recoveryAzureV2ResourceGroupId;
    }

    /**
     * Set the recoveryAzureV2ResourceGroupId property: The recovery Azure resource group Id for resource manager
     * deployment.
     * 
     * @param recoveryAzureV2ResourceGroupId the recoveryAzureV2ResourceGroupId value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput
        withRecoveryAzureV2ResourceGroupId(String recoveryAzureV2ResourceGroupId) {
        this.recoveryAzureV2ResourceGroupId = recoveryAzureV2ResourceGroupId;
        return this;
    }

    /**
     * Get the useManagedDisks property: A value indicating whether managed disks should be used during failover.
     * 
     * @return the useManagedDisks value.
     */
    public String useManagedDisks() {
        return this.useManagedDisks;
    }

    /**
     * Set the useManagedDisks property: A value indicating whether managed disks should be used during failover.
     * 
     * @param useManagedDisks the useManagedDisks value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput withUseManagedDisks(String useManagedDisks) {
        this.useManagedDisks = useManagedDisks;
        return this;
    }

    /**
     * Get the diskIdToDiskEncryptionMap property: The dictionary of disk resource Id to disk encryption set ARM Id.
     * 
     * @return the diskIdToDiskEncryptionMap value.
     */
    public Map<String, String> diskIdToDiskEncryptionMap() {
        return this.diskIdToDiskEncryptionMap;
    }

    /**
     * Set the diskIdToDiskEncryptionMap property: The dictionary of disk resource Id to disk encryption set ARM Id.
     * 
     * @param diskIdToDiskEncryptionMap the diskIdToDiskEncryptionMap value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput
        withDiskIdToDiskEncryptionMap(Map<String, String> diskIdToDiskEncryptionMap) {
        this.diskIdToDiskEncryptionMap = diskIdToDiskEncryptionMap;
        return this;
    }

    /**
     * Get the targetProximityPlacementGroupId property: The target proximity placement group Id.
     * 
     * @return the targetProximityPlacementGroupId value.
     */
    public String targetProximityPlacementGroupId() {
        return this.targetProximityPlacementGroupId;
    }

    /**
     * Set the targetProximityPlacementGroupId property: The target proximity placement group Id.
     * 
     * @param targetProximityPlacementGroupId the targetProximityPlacementGroupId value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput
        withTargetProximityPlacementGroupId(String targetProximityPlacementGroupId) {
        this.targetProximityPlacementGroupId = targetProximityPlacementGroupId;
        return this;
    }

    /**
     * Get the targetAvailabilityZone property: The target availability zone.
     * 
     * @return the targetAvailabilityZone value.
     */
    public String targetAvailabilityZone() {
        return this.targetAvailabilityZone;
    }

    /**
     * Set the targetAvailabilityZone property: The target availability zone.
     * 
     * @param targetAvailabilityZone the targetAvailabilityZone value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput
        withTargetAvailabilityZone(String targetAvailabilityZone) {
        this.targetAvailabilityZone = targetAvailabilityZone;
        return this;
    }

    /**
     * Get the targetVmTags property: The target VM tags.
     * 
     * @return the targetVmTags value.
     */
    public Map<String, String> targetVmTags() {
        return this.targetVmTags;
    }

    /**
     * Set the targetVmTags property: The target VM tags.
     * 
     * @param targetVmTags the targetVmTags value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput withTargetVmTags(Map<String, String> targetVmTags) {
        this.targetVmTags = targetVmTags;
        return this;
    }

    /**
     * Get the targetManagedDiskTags property: The tags for the target managed disks.
     * 
     * @return the targetManagedDiskTags value.
     */
    public Map<String, String> targetManagedDiskTags() {
        return this.targetManagedDiskTags;
    }

    /**
     * Set the targetManagedDiskTags property: The tags for the target managed disks.
     * 
     * @param targetManagedDiskTags the targetManagedDiskTags value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput
        withTargetManagedDiskTags(Map<String, String> targetManagedDiskTags) {
        this.targetManagedDiskTags = targetManagedDiskTags;
        return this;
    }

    /**
     * Get the targetNicTags property: The tags for the target NICs.
     * 
     * @return the targetNicTags value.
     */
    public Map<String, String> targetNicTags() {
        return this.targetNicTags;
    }

    /**
     * Set the targetNicTags property: The tags for the target NICs.
     * 
     * @param targetNicTags the targetNicTags value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput withTargetNicTags(Map<String, String> targetNicTags) {
        this.targetNicTags = targetNicTags;
        return this;
    }

    /**
     * Get the sqlServerLicenseType property: The SQL Server license type.
     * 
     * @return the sqlServerLicenseType value.
     */
    public SqlServerLicenseType sqlServerLicenseType() {
        return this.sqlServerLicenseType;
    }

    /**
     * Set the sqlServerLicenseType property: The SQL Server license type.
     * 
     * @param sqlServerLicenseType the sqlServerLicenseType value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput
        withSqlServerLicenseType(SqlServerLicenseType sqlServerLicenseType) {
        this.sqlServerLicenseType = sqlServerLicenseType;
        return this;
    }

    /**
     * Get the linuxLicenseType property: The license type for Linux VM's.
     * 
     * @return the linuxLicenseType value.
     */
    public LinuxLicenseType linuxLicenseType() {
        return this.linuxLicenseType;
    }

    /**
     * Set the linuxLicenseType property: The license type for Linux VM's.
     * 
     * @param linuxLicenseType the linuxLicenseType value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput
        withLinuxLicenseType(LinuxLicenseType linuxLicenseType) {
        this.linuxLicenseType = linuxLicenseType;
        return this;
    }

    /**
     * Get the userSelectedOSName property: The OS name selected by user.
     * 
     * @return the userSelectedOSName value.
     */
    public String userSelectedOSName() {
        return this.userSelectedOSName;
    }

    /**
     * Set the userSelectedOSName property: The OS name selected by user.
     * 
     * @param userSelectedOSName the userSelectedOSName value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput withUserSelectedOSName(String userSelectedOSName) {
        this.userSelectedOSName = userSelectedOSName;
        return this;
    }

    /**
     * Get the vmDisks property: The list of disk update properties.
     * 
     * @return the vmDisks value.
     */
    public List<UpdateDiskInput> vmDisks() {
        return this.vmDisks;
    }

    /**
     * Set the vmDisks property: The list of disk update properties.
     * 
     * @param vmDisks the vmDisks value to set.
     * @return the HyperVReplicaAzureUpdateReplicationProtectedItemInput object itself.
     */
    public HyperVReplicaAzureUpdateReplicationProtectedItemInput withVmDisks(List<UpdateDiskInput> vmDisks) {
        this.vmDisks = vmDisks;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (vmDisks() != null) {
            vmDisks().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("instanceType", this.instanceType);
        jsonWriter.writeStringField("recoveryAzureV1ResourceGroupId", this.recoveryAzureV1ResourceGroupId);
        jsonWriter.writeStringField("recoveryAzureV2ResourceGroupId", this.recoveryAzureV2ResourceGroupId);
        jsonWriter.writeStringField("useManagedDisks", this.useManagedDisks);
        jsonWriter.writeMapField("diskIdToDiskEncryptionMap", this.diskIdToDiskEncryptionMap,
            (writer, element) -> writer.writeString(element));
        jsonWriter.writeStringField("targetProximityPlacementGroupId", this.targetProximityPlacementGroupId);
        jsonWriter.writeStringField("targetAvailabilityZone", this.targetAvailabilityZone);
        jsonWriter.writeMapField("targetVmTags", this.targetVmTags, (writer, element) -> writer.writeString(element));
        jsonWriter.writeMapField("targetManagedDiskTags", this.targetManagedDiskTags,
            (writer, element) -> writer.writeString(element));
        jsonWriter.writeMapField("targetNicTags", this.targetNicTags, (writer, element) -> writer.writeString(element));
        jsonWriter.writeStringField("sqlServerLicenseType",
            this.sqlServerLicenseType == null ? null : this.sqlServerLicenseType.toString());
        jsonWriter.writeStringField("linuxLicenseType",
            this.linuxLicenseType == null ? null : this.linuxLicenseType.toString());
        jsonWriter.writeStringField("userSelectedOSName", this.userSelectedOSName);
        jsonWriter.writeArrayField("vmDisks", this.vmDisks, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of HyperVReplicaAzureUpdateReplicationProtectedItemInput from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of HyperVReplicaAzureUpdateReplicationProtectedItemInput if the JsonReader was pointing to an
     * instance of it, or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the HyperVReplicaAzureUpdateReplicationProtectedItemInput.
     */
    public static HyperVReplicaAzureUpdateReplicationProtectedItemInput fromJson(JsonReader jsonReader)
        throws IOException {
        return jsonReader.readObject(reader -> {
            HyperVReplicaAzureUpdateReplicationProtectedItemInput deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput
                = new HyperVReplicaAzureUpdateReplicationProtectedItemInput();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("instanceType".equals(fieldName)) {
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.instanceType = reader.getString();
                } else if ("recoveryAzureV1ResourceGroupId".equals(fieldName)) {
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.recoveryAzureV1ResourceGroupId
                        = reader.getString();
                } else if ("recoveryAzureV2ResourceGroupId".equals(fieldName)) {
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.recoveryAzureV2ResourceGroupId
                        = reader.getString();
                } else if ("useManagedDisks".equals(fieldName)) {
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.useManagedDisks
                        = reader.getString();
                } else if ("diskIdToDiskEncryptionMap".equals(fieldName)) {
                    Map<String, String> diskIdToDiskEncryptionMap = reader.readMap(reader1 -> reader1.getString());
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.diskIdToDiskEncryptionMap
                        = diskIdToDiskEncryptionMap;
                } else if ("targetProximityPlacementGroupId".equals(fieldName)) {
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.targetProximityPlacementGroupId
                        = reader.getString();
                } else if ("targetAvailabilityZone".equals(fieldName)) {
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.targetAvailabilityZone
                        = reader.getString();
                } else if ("targetVmTags".equals(fieldName)) {
                    Map<String, String> targetVmTags = reader.readMap(reader1 -> reader1.getString());
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.targetVmTags = targetVmTags;
                } else if ("targetManagedDiskTags".equals(fieldName)) {
                    Map<String, String> targetManagedDiskTags = reader.readMap(reader1 -> reader1.getString());
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.targetManagedDiskTags
                        = targetManagedDiskTags;
                } else if ("targetNicTags".equals(fieldName)) {
                    Map<String, String> targetNicTags = reader.readMap(reader1 -> reader1.getString());
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.targetNicTags = targetNicTags;
                } else if ("sqlServerLicenseType".equals(fieldName)) {
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.sqlServerLicenseType
                        = SqlServerLicenseType.fromString(reader.getString());
                } else if ("linuxLicenseType".equals(fieldName)) {
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.linuxLicenseType
                        = LinuxLicenseType.fromString(reader.getString());
                } else if ("userSelectedOSName".equals(fieldName)) {
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.userSelectedOSName
                        = reader.getString();
                } else if ("vmDisks".equals(fieldName)) {
                    List<UpdateDiskInput> vmDisks = reader.readArray(reader1 -> UpdateDiskInput.fromJson(reader1));
                    deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput.vmDisks = vmDisks;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedHyperVReplicaAzureUpdateReplicationProtectedItemInput;
        });
    }
}
