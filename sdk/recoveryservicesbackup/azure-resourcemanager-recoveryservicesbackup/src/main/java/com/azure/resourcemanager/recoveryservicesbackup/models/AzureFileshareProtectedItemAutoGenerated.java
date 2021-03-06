// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicesbackup.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.OffsetDateTime;
import java.util.Map;

/** Azure File Share workload-specific backup item. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "protectedItemType")
@JsonTypeName("AzureFileShareProtectedItem")
@Fluent
public final class AzureFileshareProtectedItemAutoGenerated extends ProtectedItem {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(AzureFileshareProtectedItemAutoGenerated.class);

    /*
     * Friendly name of the fileshare represented by this backup item.
     */
    @JsonProperty(value = "friendlyName")
    private String friendlyName;

    /*
     * Backup status of this backup item.
     */
    @JsonProperty(value = "protectionStatus")
    private String protectionStatus;

    /*
     * Backup state of this backup item.
     */
    @JsonProperty(value = "protectionState")
    private ProtectionState protectionState;

    /*
     * backups running status for this backup item.
     */
    @JsonProperty(value = "healthStatus")
    private HealthStatus healthStatus;

    /*
     * Last backup operation status. Possible values: Healthy, Unhealthy.
     */
    @JsonProperty(value = "lastBackupStatus")
    private String lastBackupStatus;

    /*
     * Timestamp of the last backup operation on this backup item.
     */
    @JsonProperty(value = "lastBackupTime")
    private OffsetDateTime lastBackupTime;

    /*
     * Health details of different KPIs
     */
    @JsonProperty(value = "kpisHealths")
    private Map<String, KpiResourceHealthDetails> kpisHealths;

    /*
     * Additional information with this backup item.
     */
    @JsonProperty(value = "extendedInfo")
    private AzureFileshareProtectedItemExtendedInfo extendedInfo;

    /**
     * Get the friendlyName property: Friendly name of the fileshare represented by this backup item.
     *
     * @return the friendlyName value.
     */
    public String friendlyName() {
        return this.friendlyName;
    }

    /**
     * Set the friendlyName property: Friendly name of the fileshare represented by this backup item.
     *
     * @param friendlyName the friendlyName value to set.
     * @return the AzureFileshareProtectedItemAutoGenerated object itself.
     */
    public AzureFileshareProtectedItemAutoGenerated withFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    /**
     * Get the protectionStatus property: Backup status of this backup item.
     *
     * @return the protectionStatus value.
     */
    public String protectionStatus() {
        return this.protectionStatus;
    }

    /**
     * Set the protectionStatus property: Backup status of this backup item.
     *
     * @param protectionStatus the protectionStatus value to set.
     * @return the AzureFileshareProtectedItemAutoGenerated object itself.
     */
    public AzureFileshareProtectedItemAutoGenerated withProtectionStatus(String protectionStatus) {
        this.protectionStatus = protectionStatus;
        return this;
    }

    /**
     * Get the protectionState property: Backup state of this backup item.
     *
     * @return the protectionState value.
     */
    public ProtectionState protectionState() {
        return this.protectionState;
    }

    /**
     * Set the protectionState property: Backup state of this backup item.
     *
     * @param protectionState the protectionState value to set.
     * @return the AzureFileshareProtectedItemAutoGenerated object itself.
     */
    public AzureFileshareProtectedItemAutoGenerated withProtectionState(ProtectionState protectionState) {
        this.protectionState = protectionState;
        return this;
    }

    /**
     * Get the healthStatus property: backups running status for this backup item.
     *
     * @return the healthStatus value.
     */
    public HealthStatus healthStatus() {
        return this.healthStatus;
    }

    /**
     * Set the healthStatus property: backups running status for this backup item.
     *
     * @param healthStatus the healthStatus value to set.
     * @return the AzureFileshareProtectedItemAutoGenerated object itself.
     */
    public AzureFileshareProtectedItemAutoGenerated withHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
        return this;
    }

    /**
     * Get the lastBackupStatus property: Last backup operation status. Possible values: Healthy, Unhealthy.
     *
     * @return the lastBackupStatus value.
     */
    public String lastBackupStatus() {
        return this.lastBackupStatus;
    }

    /**
     * Set the lastBackupStatus property: Last backup operation status. Possible values: Healthy, Unhealthy.
     *
     * @param lastBackupStatus the lastBackupStatus value to set.
     * @return the AzureFileshareProtectedItemAutoGenerated object itself.
     */
    public AzureFileshareProtectedItemAutoGenerated withLastBackupStatus(String lastBackupStatus) {
        this.lastBackupStatus = lastBackupStatus;
        return this;
    }

    /**
     * Get the lastBackupTime property: Timestamp of the last backup operation on this backup item.
     *
     * @return the lastBackupTime value.
     */
    public OffsetDateTime lastBackupTime() {
        return this.lastBackupTime;
    }

    /**
     * Set the lastBackupTime property: Timestamp of the last backup operation on this backup item.
     *
     * @param lastBackupTime the lastBackupTime value to set.
     * @return the AzureFileshareProtectedItemAutoGenerated object itself.
     */
    public AzureFileshareProtectedItemAutoGenerated withLastBackupTime(OffsetDateTime lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
        return this;
    }

    /**
     * Get the kpisHealths property: Health details of different KPIs.
     *
     * @return the kpisHealths value.
     */
    public Map<String, KpiResourceHealthDetails> kpisHealths() {
        return this.kpisHealths;
    }

    /**
     * Set the kpisHealths property: Health details of different KPIs.
     *
     * @param kpisHealths the kpisHealths value to set.
     * @return the AzureFileshareProtectedItemAutoGenerated object itself.
     */
    public AzureFileshareProtectedItemAutoGenerated withKpisHealths(Map<String, KpiResourceHealthDetails> kpisHealths) {
        this.kpisHealths = kpisHealths;
        return this;
    }

    /**
     * Get the extendedInfo property: Additional information with this backup item.
     *
     * @return the extendedInfo value.
     */
    public AzureFileshareProtectedItemExtendedInfo extendedInfo() {
        return this.extendedInfo;
    }

    /**
     * Set the extendedInfo property: Additional information with this backup item.
     *
     * @param extendedInfo the extendedInfo value to set.
     * @return the AzureFileshareProtectedItemAutoGenerated object itself.
     */
    public AzureFileshareProtectedItemAutoGenerated withExtendedInfo(
        AzureFileshareProtectedItemExtendedInfo extendedInfo) {
        this.extendedInfo = extendedInfo;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withBackupManagementType(
        BackupManagementType backupManagementType) {
        super.withBackupManagementType(backupManagementType);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withWorkloadType(DataSourceType workloadType) {
        super.withWorkloadType(workloadType);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withContainerName(String containerName) {
        super.withContainerName(containerName);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withSourceResourceId(String sourceResourceId) {
        super.withSourceResourceId(sourceResourceId);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withPolicyId(String policyId) {
        super.withPolicyId(policyId);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withLastRecoveryPoint(OffsetDateTime lastRecoveryPoint) {
        super.withLastRecoveryPoint(lastRecoveryPoint);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withBackupSetName(String backupSetName) {
        super.withBackupSetName(backupSetName);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withCreateMode(CreateMode createMode) {
        super.withCreateMode(createMode);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withDeferredDeleteTimeInUtc(
        OffsetDateTime deferredDeleteTimeInUtc) {
        super.withDeferredDeleteTimeInUtc(deferredDeleteTimeInUtc);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withIsScheduledForDeferredDelete(
        Boolean isScheduledForDeferredDelete) {
        super.withIsScheduledForDeferredDelete(isScheduledForDeferredDelete);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withDeferredDeleteTimeRemaining(
        String deferredDeleteTimeRemaining) {
        super.withDeferredDeleteTimeRemaining(deferredDeleteTimeRemaining);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withIsDeferredDeleteScheduleUpcoming(
        Boolean isDeferredDeleteScheduleUpcoming) {
        super.withIsDeferredDeleteScheduleUpcoming(isDeferredDeleteScheduleUpcoming);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureFileshareProtectedItemAutoGenerated withIsRehydrate(Boolean isRehydrate) {
        super.withIsRehydrate(isRehydrate);
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
        if (kpisHealths() != null) {
            kpisHealths()
                .values()
                .forEach(
                    e -> {
                        if (e != null) {
                            e.validate();
                        }
                    });
        }
        if (extendedInfo() != null) {
            extendedInfo().validate();
        }
    }
}
