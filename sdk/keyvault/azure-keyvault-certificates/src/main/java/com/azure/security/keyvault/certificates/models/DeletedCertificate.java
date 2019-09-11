// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;


import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 *  Deleted Certificate is the resource consisting of name, recovery id, deleted date, scheduled purge date and its attributes inherited from {@link CertificateBase}.
 *  It is managed by Secret Service.
 *
 *  @see CertificateAsyncClient
 */
public final class DeletedCertificate extends CertificateBase {

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
    private OffsetDateTime deletedDate;

    /**
     * The Certificate policy.
     */
    @JsonProperty("policy")
    private CertificatePolicy certificatePolicy;

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
    public DeletedCertificate recoveryId(String recoveryId) {
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
     * Get the certificate policy.
     *
     * @return the certificate policy.
     */
    public CertificatePolicy certificatePolicy() {
        return this.certificatePolicy;
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
