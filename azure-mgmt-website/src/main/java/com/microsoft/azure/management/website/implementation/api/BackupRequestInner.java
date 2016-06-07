/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Description of a backup which will be performed.
 */
@JsonFlatten
public class BackupRequestInner extends Resource {
    /**
     * Name of the backup.
     */
    @JsonProperty(value = "properties.name")
    private String backupRequestName;

    /**
     * True if the backup schedule is enabled (must be included in that case),
     * false if the backup schedule should be disabled.
     */
    @JsonProperty(value = "properties.enabled")
    private Boolean enabled;

    /**
     * SAS URL to the container.
     */
    @JsonProperty(value = "properties.storageAccountUrl")
    private String storageAccountUrl;

    /**
     * Schedule for the backup if it is executed periodically.
     */
    @JsonProperty(value = "properties.backupSchedule")
    private BackupSchedule backupSchedule;

    /**
     * Databases included in the backup.
     */
    @JsonProperty(value = "properties.databases")
    private List<DatabaseBackupSetting> databases;

    /**
     * Type of the backup. Possible values include: 'Default', 'Clone',
     * 'Relocation'.
     */
    @JsonProperty(value = "properties.type")
    private BackupRestoreOperationType backupRequestType;

    /**
     * Get the backupRequestName value.
     *
     * @return the backupRequestName value
     */
    public String backupRequestName() {
        return this.backupRequestName;
    }

    /**
     * Set the backupRequestName value.
     *
     * @param backupRequestName the backupRequestName value to set
     * @return the BackupRequestInner object itself.
     */
    public BackupRequestInner withBackupRequestName(String backupRequestName) {
        this.backupRequestName = backupRequestName;
        return this;
    }

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the BackupRequestInner object itself.
     */
    public BackupRequestInner withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the storageAccountUrl value.
     *
     * @return the storageAccountUrl value
     */
    public String storageAccountUrl() {
        return this.storageAccountUrl;
    }

    /**
     * Set the storageAccountUrl value.
     *
     * @param storageAccountUrl the storageAccountUrl value to set
     * @return the BackupRequestInner object itself.
     */
    public BackupRequestInner withStorageAccountUrl(String storageAccountUrl) {
        this.storageAccountUrl = storageAccountUrl;
        return this;
    }

    /**
     * Get the backupSchedule value.
     *
     * @return the backupSchedule value
     */
    public BackupSchedule backupSchedule() {
        return this.backupSchedule;
    }

    /**
     * Set the backupSchedule value.
     *
     * @param backupSchedule the backupSchedule value to set
     * @return the BackupRequestInner object itself.
     */
    public BackupRequestInner withBackupSchedule(BackupSchedule backupSchedule) {
        this.backupSchedule = backupSchedule;
        return this;
    }

    /**
     * Get the databases value.
     *
     * @return the databases value
     */
    public List<DatabaseBackupSetting> databases() {
        return this.databases;
    }

    /**
     * Set the databases value.
     *
     * @param databases the databases value to set
     * @return the BackupRequestInner object itself.
     */
    public BackupRequestInner withDatabases(List<DatabaseBackupSetting> databases) {
        this.databases = databases;
        return this;
    }

    /**
     * Get the backupRequestType value.
     *
     * @return the backupRequestType value
     */
    public BackupRestoreOperationType backupRequestType() {
        return this.backupRequestType;
    }

    /**
     * Set the backupRequestType value.
     *
     * @param backupRequestType the backupRequestType value to set
     * @return the BackupRequestInner object itself.
     */
    public BackupRequestInner withBackupRequestType(BackupRestoreOperationType backupRequestType) {
        this.backupRequestType = backupRequestType;
        return this;
    }

}
