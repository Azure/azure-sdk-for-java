// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.implementation.DeletedKeyHelper;

import java.time.OffsetDateTime;

/**
 * Deleted Key is the resource consisting of name, recovery id, deleted date, scheduled purge date and its attributes
 * inherited from {@link KeyVaultKey}.
 * It is managed by Key Service.
 *
 * @see KeyClient
 * @see KeyAsyncClient
 */
public final class DeletedKey extends KeyVaultKey {
    static {
        DeletedKeyHelper.setAccessor(new DeletedKeyHelper.DeletedKeyAccessor() {
            @Override
            public void setRecoveryId(DeletedKey deletedKey, String recoveryId) {
                deletedKey.recoveryId = recoveryId;
            }

            @Override
            public void setScheduledPurgeDate(DeletedKey deletedKey, OffsetDateTime scheduledPurgeDate) {
                deletedKey.scheduledPurgeDate = scheduledPurgeDate;
            }

            @Override
            public void setDeletedOn(DeletedKey deletedKey, OffsetDateTime deletedOn) {
                deletedKey.deletedOn = deletedOn;
            }
        });
    }

    /**
     * The url of the recovery object, used to identify and recover the deleted
     * key.
     */
    private String recoveryId;

    /**
     * The time when the key is scheduled to be purged, in UTC.
     */
    private OffsetDateTime scheduledPurgeDate;

    /**
     * The time when the key was deleted, in UTC.
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
     * Get the key value.
     *
     * @return the key value
     */
    public JsonWebKey getKey() {
        return super.getKey();
    }

}
