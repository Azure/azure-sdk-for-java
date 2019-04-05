// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DeletedSecret extends SecretAttributes {

    /**
     * The url of the recovery object, used to identify and recover the deleted
     * secret.
     */
    @JsonProperty(value = "recoveryId")
    private String recoveryId;

    /**
     * The time when the secret is scheduled to be purged, in UTC.
     */
    private OffsetDateTime scheduledPurgeDate;

    /**
     * The time when the secret was deleted, in UTC.
     */
    private OffsetDateTime deletedDate;

    /**
     * Get the recoveryId value.
     *
     * @return the recoveryId value
     */
    public String recoveryId() {
        return this.recoveryId;
    }

    /**
     * Set the recoveryId value.
     *
     * @param recoveryId the recoveryId value to set
     * @return the DeletedSecret object itself.
     */
    public DeletedSecret recoveryId(String recoveryId) {
        this.recoveryId = recoveryId;
        return this;
    }

    /**
     * Get the scheduledPurgeDate value.
     *
     * @return the scheduledPurgeDate value
     */
    public OffsetDateTime scheduledPurgeDate() {
        if (this.scheduledPurgeDate == null) {
            return null;
        }

        return scheduledPurgeDate;
    }

    /**
     * Get the deletedDate value.
     *
     * @return the deletedDate value
     */
    public OffsetDateTime deletedDate() {
        if (this.deletedDate == null) {
            return null;
        }
        return this.deletedDate;
    }

    /**
     * Unpacks the scheduledPurageDate json response. Converts the {@link Long scheduledPurgeDate} epoch second value to OffsetDateTime and updates the
     * value of class variable scheduledPurgeDate.
     */
    @JsonProperty("scheduledPurgeDate")
    private void unpackScheduledPurgeDate(Long scheduledPurgeDate){
        this.scheduledPurgeDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(scheduledPurgeDate * 1000L), ZoneOffset.UTC);
    }

    /**
     * Unpacks the deletedDate json response. Converts the {@link Long deletedDate} epoch second value to OffsetDateTime and updates the
     * value of class variable deletedDate.
     */
    @JsonProperty("deletedDate")
    private void deletedDate(Long deletedDate){
        this.deletedDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(deletedDate * 1000L), ZoneOffset.UTC);
    }

}
