// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DeletedSecret extends SecretInfo {

    /**
     * The url of the recovery object, used to identify and recover the deleted
     * secret.
     */
    @JsonProperty(value = "recoveryId")
    private String recoveryId;

    /**
     * The time when the secret is scheduled to be purged, in UTC.
     */
    @JsonProperty(value = "scheduledPurgeDate", access = JsonProperty.Access.WRITE_ONLY)
    private Long scheduledPurgeDate;

    /**
     * The time when the secret was deleted, in UTC.
     */
    @JsonProperty(value = "deletedDate", access = JsonProperty.Access.WRITE_ONLY)
    private Long deletedDate;

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
     * @return the DeletedSecretItem object itself.
     */
    public DeletedSecret withRecoveryId(String recoveryId) {
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

        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.scheduledPurgeDate * 1000L), ZoneOffset.UTC);
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
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.deletedDate * 1000L), ZoneOffset.UTC);
    }

}
