// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.implementation;

import com.azure.security.keyvault.secrets.models.DeletedSecret;

import java.time.OffsetDateTime;

public final class DeletedSecretHelper {
    private static DeletedSecretAccessor accessor;

    public interface DeletedSecretAccessor {
        void setRecoveryId(DeletedSecret deletedSecret, String recoveryId);
        void setScheduledPurgeDate(DeletedSecret deletedSecret, OffsetDateTime scheduledPurgeDate);
        void setDeletedOn(DeletedSecret deletedSecret, OffsetDateTime deletedOn);
    }

    public static void setRecoveryId(DeletedSecret deletedSecret, String recoveryId) {
        accessor.setRecoveryId(deletedSecret, recoveryId);
    }

    public static void setScheduledPurgeDate(DeletedSecret deletedSecret, OffsetDateTime scheduledPurgeDate) {
        accessor.setScheduledPurgeDate(deletedSecret, scheduledPurgeDate);
    }

    public static void setDeletedOn(DeletedSecret deletedSecret, OffsetDateTime deletedOn) {
        accessor.setDeletedOn(deletedSecret, deletedOn);
    }

    public static void setAccessor(DeletedSecretAccessor newAccessor) {
        accessor = newAccessor;
    }

    private DeletedSecretHelper() {
    }
}
