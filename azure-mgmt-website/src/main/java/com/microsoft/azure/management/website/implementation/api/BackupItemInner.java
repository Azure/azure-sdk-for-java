/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Backup description.
 */
@JsonFlatten
public class BackupItemInner extends Resource {
    /**
     * Id of the backup.
     */
    @JsonProperty(value = "properties.id")
    private Integer backupItemId;

    /**
     * SAS URL for the storage account container which contains this backup.
     */
    @JsonProperty(value = "properties.storageAccountUrl")
    private String storageAccountUrl;

    /**
     * Name of the blob which contains data for this backup.
     */
    @JsonProperty(value = "properties.blobName")
    private String blobName;

    /**
     * Name of this backup.
     */
    @JsonProperty(value = "properties.name")
    private String backupItemName;

    /**
     * Backup status. Possible values include: 'InProgress', 'Failed',
     * 'Succeeded', 'TimedOut', 'Created', 'Skipped', 'PartiallySucceeded',
     * 'DeleteInProgress', 'DeleteFailed', 'Deleted'.
     */
    @JsonProperty(value = "properties.status")
    private BackupItemStatus status;

    /**
     * Size of the backup in bytes.
     */
    @JsonProperty(value = "properties.sizeInBytes")
    private Long sizeInBytes;

    /**
     * Timestamp of the backup creation.
     */
    @JsonProperty(value = "properties.created")
    private DateTime created;

    /**
     * Details regarding this backup. Might contain an error message.
     */
    @JsonProperty(value = "properties.log")
    private String log;

    /**
     * List of databases included in the backup.
     */
    @JsonProperty(value = "properties.databases")
    private List<DatabaseBackupSetting> databases;

    /**
     * True if this backup has been created due to a schedule being triggered.
     */
    @JsonProperty(value = "properties.scheduled")
    private Boolean scheduled;

    /**
     * Timestamp of a last restore operation which used this backup.
     */
    @JsonProperty(value = "properties.lastRestoreTimeStamp")
    private DateTime lastRestoreTimeStamp;

    /**
     * Timestamp when this backup finished.
     */
    @JsonProperty(value = "properties.finishedTimeStamp")
    private DateTime finishedTimeStamp;

    /**
     * Unique correlation identifier. Please use this along with the timestamp
     * while communicating with Azure support.
     */
    @JsonProperty(value = "properties.correlationId")
    private String correlationId;

    /**
     * Size of the original web app which has been backed up.
     */
    @JsonProperty(value = "properties.websiteSizeInBytes")
    private Long websiteSizeInBytes;

    /**
     * Get the backupItemId value.
     *
     * @return the backupItemId value
     */
    public Integer backupItemId() {
        return this.backupItemId;
    }

    /**
     * Set the backupItemId value.
     *
     * @param backupItemId the backupItemId value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withBackupItemId(Integer backupItemId) {
        this.backupItemId = backupItemId;
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
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withStorageAccountUrl(String storageAccountUrl) {
        this.storageAccountUrl = storageAccountUrl;
        return this;
    }

    /**
     * Get the blobName value.
     *
     * @return the blobName value
     */
    public String blobName() {
        return this.blobName;
    }

    /**
     * Set the blobName value.
     *
     * @param blobName the blobName value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withBlobName(String blobName) {
        this.blobName = blobName;
        return this;
    }

    /**
     * Get the backupItemName value.
     *
     * @return the backupItemName value
     */
    public String backupItemName() {
        return this.backupItemName;
    }

    /**
     * Set the backupItemName value.
     *
     * @param backupItemName the backupItemName value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withBackupItemName(String backupItemName) {
        this.backupItemName = backupItemName;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public BackupItemStatus status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withStatus(BackupItemStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get the sizeInBytes value.
     *
     * @return the sizeInBytes value
     */
    public Long sizeInBytes() {
        return this.sizeInBytes;
    }

    /**
     * Set the sizeInBytes value.
     *
     * @param sizeInBytes the sizeInBytes value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
        return this;
    }

    /**
     * Get the created value.
     *
     * @return the created value
     */
    public DateTime created() {
        return this.created;
    }

    /**
     * Set the created value.
     *
     * @param created the created value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withCreated(DateTime created) {
        this.created = created;
        return this;
    }

    /**
     * Get the log value.
     *
     * @return the log value
     */
    public String log() {
        return this.log;
    }

    /**
     * Set the log value.
     *
     * @param log the log value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withLog(String log) {
        this.log = log;
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
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withDatabases(List<DatabaseBackupSetting> databases) {
        this.databases = databases;
        return this;
    }

    /**
     * Get the scheduled value.
     *
     * @return the scheduled value
     */
    public Boolean scheduled() {
        return this.scheduled;
    }

    /**
     * Set the scheduled value.
     *
     * @param scheduled the scheduled value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withScheduled(Boolean scheduled) {
        this.scheduled = scheduled;
        return this;
    }

    /**
     * Get the lastRestoreTimeStamp value.
     *
     * @return the lastRestoreTimeStamp value
     */
    public DateTime lastRestoreTimeStamp() {
        return this.lastRestoreTimeStamp;
    }

    /**
     * Set the lastRestoreTimeStamp value.
     *
     * @param lastRestoreTimeStamp the lastRestoreTimeStamp value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withLastRestoreTimeStamp(DateTime lastRestoreTimeStamp) {
        this.lastRestoreTimeStamp = lastRestoreTimeStamp;
        return this;
    }

    /**
     * Get the finishedTimeStamp value.
     *
     * @return the finishedTimeStamp value
     */
    public DateTime finishedTimeStamp() {
        return this.finishedTimeStamp;
    }

    /**
     * Set the finishedTimeStamp value.
     *
     * @param finishedTimeStamp the finishedTimeStamp value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withFinishedTimeStamp(DateTime finishedTimeStamp) {
        this.finishedTimeStamp = finishedTimeStamp;
        return this;
    }

    /**
     * Get the correlationId value.
     *
     * @return the correlationId value
     */
    public String correlationId() {
        return this.correlationId;
    }

    /**
     * Set the correlationId value.
     *
     * @param correlationId the correlationId value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Get the websiteSizeInBytes value.
     *
     * @return the websiteSizeInBytes value
     */
    public Long websiteSizeInBytes() {
        return this.websiteSizeInBytes;
    }

    /**
     * Set the websiteSizeInBytes value.
     *
     * @param websiteSizeInBytes the websiteSizeInBytes value to set
     * @return the BackupItemInner object itself.
     */
    public BackupItemInner withWebsiteSizeInBytes(Long websiteSizeInBytes) {
        this.websiteSizeInBytes = websiteSizeInBytes;
        return this;
    }

}
