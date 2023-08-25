// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.security.keyvault.keys.models.DeletedKey;

import java.time.OffsetDateTime;

public final class DeletedKeyHelper {
    private static DeletedKeyAccessor accessor;

    public interface DeletedKeyAccessor {
        void setRecoveryId(DeletedKey deletedKey, String recoveryId);
        void setScheduledPurgeDate(DeletedKey deletedKey, OffsetDateTime scheduledPurgeDate);
        void setDeletedOn(DeletedKey deletedKey, OffsetDateTime deletedOn);
    }

    public static void setRecoveryId(DeletedKey deletedKey, String recoveryId) {
        accessor.setRecoveryId(deletedKey, recoveryId);
    }

    public static void setScheduledPurgeDate(DeletedKey deletedKey, OffsetDateTime scheduledPurgeDate) {
        accessor.setScheduledPurgeDate(deletedKey, scheduledPurgeDate);
    }

    public static void setDeletedOn(DeletedKey deletedKey, OffsetDateTime deletedOn) {
        accessor.setDeletedOn(deletedKey, deletedOn);
    }

    public static void setAccessor(DeletedKeyAccessor accessor) {
        DeletedKeyHelper.accessor = accessor;
    }

    private DeletedKeyHelper() {
    }
}
