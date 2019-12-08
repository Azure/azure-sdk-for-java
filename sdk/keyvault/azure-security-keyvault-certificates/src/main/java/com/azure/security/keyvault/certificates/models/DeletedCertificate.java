// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;


import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Deleted Certificate is the resource consisting of name, recovery id, deleted date, scheduled purge date and its
 * attributes inherited from {@link KeyVaultCertificate}.
 * It is managed by Certificate Service.
 *
 * @see CertificateAsyncClient
 * @see CertificateClient
 */
public final class DeletedCertificate extends KeyVaultCertificate {

    /**
     * The url of the recovery object, used to identify and recover the deleted
     * certificate.
     */
    @JsonProperty(value = "recoveryId")
    private String recoveryId;

    /**
     * The time when the certificate is scheduled to be purged, in UTC.
     */
    private OffsetDateTime scheduledPurgeDate;

    /**
     * The time when the certificate was deleted, in UTC.
     */
    private OffsetDateTime deletedOn;

    /**
     * Get the recoveryId identifier.
     *
     * @return the recoveryId identifier.
     */
    public String getRecoveryId() {
        return this.recoveryId;
    }

    /**
     * Get the scheduled purge UTC time.
     *
     * @return the scheduledPurgeDate UTC time.
     */
    public OffsetDateTime getScheduledPurgeDate() {
        return scheduledPurgeDate;
    }

    /**
     * Get the deleted UTC time.
     *
     * @return the deletedDate UTC time.
     */
    public OffsetDateTime getDeletedOn() {
        return this.deletedOn;
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
        this.deletedOn = OffsetDateTime.ofInstant(Instant.ofEpochMilli(deletedDate * 1000L), ZoneOffset.UTC);
    }
}
