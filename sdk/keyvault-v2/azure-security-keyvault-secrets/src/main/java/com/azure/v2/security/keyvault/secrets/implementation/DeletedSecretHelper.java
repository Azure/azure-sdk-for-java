// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.secrets.implementation;

import com.azure.v2.security.keyvault.secrets.models.DeletedSecret;

import java.time.OffsetDateTime;

/**
 * The helper class to create instances of {@link DeletedSecret} and set its properties. This class is used to allow the
 * implementation to be hidden from the public API.
 */
public final class DeletedSecretHelper {
    private static DeletedSecretAccessor accessor;

    /**
     * The interface to be implemented by the client library to create instances of {@link DeletedSecret} and set its
     * properties.
     */
    public interface DeletedSecretAccessor {
        /**
         * Sets the ID of the {@link DeletedSecret}.
         *
         * @param properties The {@link DeletedSecret} to set the ID for.
         * @param id The ID to set.
         */
        void setId(DeletedSecret properties, String id);

        /**
         * Sets the version of the {@link DeletedSecret}.
         *
         * @param properties The {@link DeletedSecret} to set the version for.
         * @param version The version to set.
         */
        void setVersion(DeletedSecret properties, String version);

        /**
         * Sets the created on date of the {@link DeletedSecret}.
         *
         * @param properties The {@link DeletedSecret} to set the created on date for.
         * @param createdOn The created on date to set.
         */
        void setCreatedOn(DeletedSecret properties, OffsetDateTime createdOn);

        /**
         * Sets the updated on date of the {@link DeletedSecret}.
         *
         * @param properties The {@link DeletedSecret} to set the updated on date for.
         * @param updatedOn The updated on date to set.
         */
        void setUpdatedOn(DeletedSecret properties, OffsetDateTime updatedOn);

        /**
         * Sets the name of the {@link DeletedSecret}.
         *
         * @param properties The {@link DeletedSecret} to set the name for.
         * @param name The name to set.
         */
        void setName(DeletedSecret properties, String name);

        /**
         * Sets the recovery level of the {@link DeletedSecret}.
         *
         * @param properties The {@link DeletedSecret} to set the recovery level for.
         * @param recoveryLevel The recovery level to set.
         */
        void setRecoveryLevel(DeletedSecret properties, String recoveryLevel);

        /**
         * Sets the key ID of the {@link DeletedSecret}.
         *
         * @param properties The {@link DeletedSecret} to set the key ID for.
         * @param keyId The key ID to set.
         */
        void setKeyId(DeletedSecret properties, String keyId);

        /**
         * Sets the managed property of the {@link DeletedSecret}.
         *
         * @param properties The {@link DeletedSecret} to set the managed property for.
         * @param managed The managed property to set.
         */
        void setManaged(DeletedSecret properties, Boolean managed);

        /**
         * Sets the recoverable days of the {@link DeletedSecret}.
         *
         * @param properties The {@link DeletedSecret} to set the recoverable days for.
         * @param recoverableDays The recoverable days to set.
         */
        void setRecoverableDays(DeletedSecret properties, Integer recoverableDays);

        /**
         * Sets the recovery ID of the {@link DeletedSecret}.
         *
         * @param deletedSecret The {@link DeletedSecret} to set the recovery ID for.
         * @param recoveryId The recovery ID to set.
         */
        void setRecoveryId(DeletedSecret deletedSecret, String recoveryId);

        /**
         * Sets the scheduled purge date of the {@link DeletedSecret}.
         *
         * @param deletedSecret The {@link DeletedSecret} to set the scheduled purge date for.
         * @param scheduledPurgeDate The scheduled purge date to set.
         */
        void setScheduledPurgeDate(DeletedSecret deletedSecret, OffsetDateTime scheduledPurgeDate);

        /**
         * Sets the deleted on date of the {@link DeletedSecret}.
         *
         * @param deletedSecret The {@link DeletedSecret} to set the deleted on date for.
         * @param deletedOn The deleted on date to set.
         */
        void setDeletedOn(DeletedSecret deletedSecret, OffsetDateTime deletedOn);
    }

    /**
     * Sets the ID of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the ID for.
     * @param id The ID to set.
     */
    public static void setId(DeletedSecret deletedSecret, String id) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setId(deletedSecret, id);
    }

    /**
     * Sets the version of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the version for.
     * @param version The version to set.
     */
    public static void setVersion(DeletedSecret deletedSecret, String version) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setVersion(deletedSecret, version);
    }

    /**
     * Sets the created on date of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the created on date for.
     * @param createdOn The created on date to set.
     */
    public static void setCreatedOn(DeletedSecret deletedSecret, OffsetDateTime createdOn) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setCreatedOn(deletedSecret, createdOn);
    }

    /**
     * Sets the updated on date of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the updated on date for.
     * @param updatedOn The updated on date to set.
     */
    public static void setUpdatedOn(DeletedSecret deletedSecret, OffsetDateTime updatedOn) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setUpdatedOn(deletedSecret, updatedOn);
    }

    /**
     * Sets the name of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the name for.
     * @param name The name to set.
     */
    public static void setName(DeletedSecret deletedSecret, String name) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setName(deletedSecret, name);
    }

    /**
     * Sets the recovery level of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the recovery level for.
     * @param recoveryLevel The recovery level to set.
     */
    public static void setRecoveryLevel(DeletedSecret deletedSecret, String recoveryLevel) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setRecoveryLevel(deletedSecret, recoveryLevel);
    }

    /**
     * Sets the key ID of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the key ID for.
     * @param keyId The key ID to set.
     */
    public static void setKeyId(DeletedSecret deletedSecret, String keyId) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setKeyId(deletedSecret, keyId);
    }

    /**
     * Sets the managed property of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the managed property for.
     * @param managed The managed property to set.
     */
    public static void setManaged(DeletedSecret deletedSecret, Boolean managed) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setManaged(deletedSecret, managed);
    }

    /**
     * Sets the recoverable days of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the recoverable days for.
     * @param recoverableDays The recoverable days to set.
     */
    public static void setRecoverableDays(DeletedSecret deletedSecret, Integer recoverableDays) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setRecoverableDays(deletedSecret, recoverableDays);
    }

    /**
     * Sets the recovery ID of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the recovery ID for.
     * @param recoveryId The recovery ID to set.
     */
    public static void setRecoveryId(DeletedSecret deletedSecret, String recoveryId) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setRecoveryId(deletedSecret, recoveryId);
    }

    /**
     * Sets the scheduled purge date of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the scheduled purge date for.
     * @param scheduledPurgeDate The scheduled purge date to set.
     */
    public static void setScheduledPurgeDate(DeletedSecret deletedSecret, OffsetDateTime scheduledPurgeDate) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setScheduledPurgeDate(deletedSecret, scheduledPurgeDate);
    }

    /**
     * Sets the deleted on date of the {@link DeletedSecret}.
     *
     * @param deletedSecret The {@link DeletedSecret} to set the deleted on date for.
     * @param deletedOn The deleted on date to set.
     */
    public static void setDeletedOn(DeletedSecret deletedSecret, OffsetDateTime deletedOn) {
        if (accessor == null) {
            new DeletedSecret();
        }

        assert accessor != null;
        accessor.setDeletedOn(deletedSecret, deletedOn);
    }

    /**
     * Sets the accessor for the {@link DeletedSecretHelper}.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(DeletedSecretAccessor accessor) {
        DeletedSecretHelper.accessor = accessor;
    }

    private DeletedSecretHelper() {
        // Private constructor to prevent instantiation.
    }
}
