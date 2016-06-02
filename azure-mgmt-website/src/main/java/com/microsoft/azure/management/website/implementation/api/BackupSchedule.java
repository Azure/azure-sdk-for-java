/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Description of a backup schedule. Describes how often should be the backup
 * performed and what should be the retention policy.
 */
public class BackupSchedule {
    /**
     * How often should be the backup executed (e.g. for weekly backup, this
     * should be set to 7 and FrequencyUnit should be set to Day).
     */
    private Integer frequencyInterval;

    /**
     * How often should be the backup executed (e.g. for weekly backup, this
     * should be set to Day and FrequencyInterval should be set to 7).
     * Possible values include: 'Day', 'Hour'.
     */
    @JsonProperty(required = true)
    private FrequencyUnit frequencyUnit;

    /**
     * True if the retention policy should always keep at least one backup in
     * the storage account, regardless how old it is; false otherwise.
     */
    private Boolean keepAtLeastOneBackup;

    /**
     * After how many days backups should be deleted.
     */
    private Integer retentionPeriodInDays;

    /**
     * When the schedule should start working.
     */
    private DateTime startTime;

    /**
     * The last time when this schedule was triggered.
     */
    private DateTime lastExecutionTime;

    /**
     * Get the frequencyInterval value.
     *
     * @return the frequencyInterval value
     */
    public Integer frequencyInterval() {
        return this.frequencyInterval;
    }

    /**
     * Set the frequencyInterval value.
     *
     * @param frequencyInterval the frequencyInterval value to set
     * @return the BackupSchedule object itself.
     */
    public BackupSchedule withFrequencyInterval(Integer frequencyInterval) {
        this.frequencyInterval = frequencyInterval;
        return this;
    }

    /**
     * Get the frequencyUnit value.
     *
     * @return the frequencyUnit value
     */
    public FrequencyUnit frequencyUnit() {
        return this.frequencyUnit;
    }

    /**
     * Set the frequencyUnit value.
     *
     * @param frequencyUnit the frequencyUnit value to set
     * @return the BackupSchedule object itself.
     */
    public BackupSchedule withFrequencyUnit(FrequencyUnit frequencyUnit) {
        this.frequencyUnit = frequencyUnit;
        return this;
    }

    /**
     * Get the keepAtLeastOneBackup value.
     *
     * @return the keepAtLeastOneBackup value
     */
    public Boolean keepAtLeastOneBackup() {
        return this.keepAtLeastOneBackup;
    }

    /**
     * Set the keepAtLeastOneBackup value.
     *
     * @param keepAtLeastOneBackup the keepAtLeastOneBackup value to set
     * @return the BackupSchedule object itself.
     */
    public BackupSchedule withKeepAtLeastOneBackup(Boolean keepAtLeastOneBackup) {
        this.keepAtLeastOneBackup = keepAtLeastOneBackup;
        return this;
    }

    /**
     * Get the retentionPeriodInDays value.
     *
     * @return the retentionPeriodInDays value
     */
    public Integer retentionPeriodInDays() {
        return this.retentionPeriodInDays;
    }

    /**
     * Set the retentionPeriodInDays value.
     *
     * @param retentionPeriodInDays the retentionPeriodInDays value to set
     * @return the BackupSchedule object itself.
     */
    public BackupSchedule withRetentionPeriodInDays(Integer retentionPeriodInDays) {
        this.retentionPeriodInDays = retentionPeriodInDays;
        return this;
    }

    /**
     * Get the startTime value.
     *
     * @return the startTime value
     */
    public DateTime startTime() {
        return this.startTime;
    }

    /**
     * Set the startTime value.
     *
     * @param startTime the startTime value to set
     * @return the BackupSchedule object itself.
     */
    public BackupSchedule withStartTime(DateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Get the lastExecutionTime value.
     *
     * @return the lastExecutionTime value
     */
    public DateTime lastExecutionTime() {
        return this.lastExecutionTime;
    }

    /**
     * Set the lastExecutionTime value.
     *
     * @param lastExecutionTime the lastExecutionTime value to set
     * @return the BackupSchedule object itself.
     */
    public BackupSchedule withLastExecutionTime(DateTime lastExecutionTime) {
        this.lastExecutionTime = lastExecutionTime;
        return this;
    }

}
