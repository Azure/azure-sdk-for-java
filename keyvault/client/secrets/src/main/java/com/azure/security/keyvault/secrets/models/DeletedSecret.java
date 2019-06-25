// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Deleted Secret is the resource consisting of name, recovery id, deleted date, scheduled purge date and its attributes inherited from {@link SecretBase}.
 * It is managed by Secret Service.
 *
 *  @see SecretClient
 *  @see SecretAsyncClient
 */
public final class DeletedSecret extends SecretBase {

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
     * Get the recoveryId identifier.
     *
     * @return the recoveryId identifier.
     */
    public String recoveryId() {
        return this.recoveryId;
    }

    /**
     * Set the recoveryId identifier.
     *
     * @param recoveryId The recoveryId identifier to set
     * @return the DeletedSecret object itself.
     */
    public DeletedSecret recoveryId(String recoveryId) {
        this.recoveryId = recoveryId;
        return this;
    }

    /**
     * Get the scheduled purge UTC time.
     *
     * @return the scheduledPurgeDate UTC time.
     */
    public OffsetDateTime scheduledPurgeDate() {
        return scheduledPurgeDate;
    }

    /**
     * Get the deleted UTC time.
     *
     * @return the deletedDate UTC time.
     */
    public OffsetDateTime deletedDate() {
        return this.deletedDate;
    }

    /**
     * Unpacks the scheduledPurageDate json response. Converts the {@link Long scheduledPurgeDate} epoch second value to OffsetDateTime and updates the
     * value of class variable scheduledPurgeDate.
     */
    @JsonProperty("scheduledPurgeDate")
    private void unpackScheduledPurgeDate(Long scheduledPurgeDate) {
        this.scheduledPurgeDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(scheduledPurgeDate * 1000L), ZoneOffset.UTC);
    }

    /**
     * Unpacks the deletedDate json response. Converts the {@link Long deletedDate} epoch second value to OffsetDateTime and updates the
     * value of class variable deletedDate.
     */
    @JsonProperty("deletedDate")
    private void deletedDate(Long deletedDate) {
        this.deletedDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(deletedDate * 1000L), ZoneOffset.UTC);
    }

}
