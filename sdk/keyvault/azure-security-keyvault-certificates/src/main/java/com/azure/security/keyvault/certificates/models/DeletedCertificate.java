// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.implementation.CertificatePropertiesHelper;
import com.azure.security.keyvault.certificates.implementation.DeletedCertificateHelper;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateBundle;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateItem;

import java.time.OffsetDateTime;

/**
 * Deleted Certificate is the resource consisting of name, recovery id, deleted date, scheduled purge date and its
 * attributes inherited from {@link KeyVaultCertificate}.
 * It is managed by Certificate Service.
 *
 * @see CertificateAsyncClient
 * @see CertificateClient
 */
public final class DeletedCertificate extends KeyVaultCertificateWithPolicy {
    static {
        DeletedCertificateHelper.setAccessor(new DeletedCertificateHelper.DeletedCertificateAccessor() {
            @Override
            public DeletedCertificate createDeletedCertificate(DeletedCertificateItem item) {
                return new DeletedCertificate(item);
            }

            @Override
            public DeletedCertificate createDeletedCertificate(DeletedCertificateBundle bundle) {
                return new DeletedCertificate(bundle);
            }
        });
    }

    /**
     * Creates an instance of {@link DeletedCertificate}.
     */
    public DeletedCertificate() {
        super();

        this.recoveryId = null;
        this.deletedOn = null;
        this.scheduledPurgeDate = null;
    }

    private DeletedCertificate(DeletedCertificateItem item) {
        super();

        this.recoveryId = item.getRecoveryId();
        this.deletedOn = item.getDeletedDate();
        this.scheduledPurgeDate = item.getScheduledPurgeDate();

        this.setProperties(CertificatePropertiesHelper.createCertificateProperties(item));
    }

    private DeletedCertificate(DeletedCertificateBundle bundle) {
        super(bundle);

        this.recoveryId = bundle.getRecoveryId();
        this.deletedOn = bundle.getDeletedDate();
        this.scheduledPurgeDate = bundle.getScheduledPurgeDate();
    }

    /**
     * The url of the recovery object, used to identify and recover the deleted
     * certificate.
     */
    private final String recoveryId;

    /**
     * The time when the certificate is scheduled to be purged, in UTC.
     */
    private final OffsetDateTime scheduledPurgeDate;

    /**
     * The time when the certificate was deleted, in UTC.
     */
    private final OffsetDateTime deletedOn;

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
}
