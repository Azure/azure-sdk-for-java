// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.implementation;

import com.azure.security.keyvault.secrets.models.DeletedSecret;

import java.time.OffsetDateTime;

public final class DeletedSecretHelper {
    private static DeletedSecretAccessor accessor;

    public interface DeletedSecretAccessor {
        void setId(DeletedSecret properties, String id);

        void setVersion(DeletedSecret properties, String version);

        void setCreatedOn(DeletedSecret properties, OffsetDateTime createdOn);

        void setUpdatedOn(DeletedSecret properties, OffsetDateTime updatedOn);

        void setName(DeletedSecret properties, String name);

        void setRecoveryLevel(DeletedSecret properties, String recoveryLevel);

        void setKeyId(DeletedSecret properties, String keyId);

        void setManaged(DeletedSecret properties, Boolean managed);

        void setRecoverableDays(DeletedSecret properties, Integer recoverableDays);

        void setRecoveryId(DeletedSecret deletedSecret, String recoveryId);

        void setScheduledPurgeDate(DeletedSecret deletedSecret, OffsetDateTime scheduledPurgeDate);

        void setDeletedOn(DeletedSecret deletedSecret, OffsetDateTime deletedOn);
    }

    public static void setId(DeletedSecret deletedSecret, String id) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setId(deletedSecret, id);
    }

    public static void setVersion(DeletedSecret deletedSecret, String version) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setVersion(deletedSecret, version);
    }

    public static void setCreatedOn(DeletedSecret deletedSecret, OffsetDateTime createdOn) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setCreatedOn(deletedSecret, createdOn);
    }

    public static void setUpdatedOn(DeletedSecret deletedSecret, OffsetDateTime updatedOn) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setUpdatedOn(deletedSecret, updatedOn);
    }

    public static void setName(DeletedSecret deletedSecret, String name) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setName(deletedSecret, name);
    }

    public static void setRecoveryLevel(DeletedSecret deletedSecret, String recoveryLevel) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setRecoveryLevel(deletedSecret, recoveryLevel);
    }

    public static void setKeyId(DeletedSecret deletedSecret, String keyId) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setKeyId(deletedSecret, keyId);
    }

    public static void setManaged(DeletedSecret deletedSecret, Boolean managed) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setManaged(deletedSecret, managed);
    }

    public static void setRecoverableDays(DeletedSecret deletedSecret, Integer recoverableDays) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setRecoverableDays(deletedSecret, recoverableDays);
    }

    public static void setRecoveryId(DeletedSecret deletedSecret, String recoveryId) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setRecoveryId(deletedSecret, recoveryId);
    }

    public static void setScheduledPurgeDate(DeletedSecret deletedSecret, OffsetDateTime scheduledPurgeDate) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;

        accessor.setScheduledPurgeDate(deletedSecret, scheduledPurgeDate);
    }

    public static void setDeletedOn(DeletedSecret deletedSecret, OffsetDateTime deletedOn) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;

        accessor.setDeletedOn(deletedSecret, deletedOn);
    }

    public static void setAccessor(DeletedSecretAccessor newAccessor) {
        accessor = newAccessor;
    }

    private DeletedSecretHelper() {
    }
}
