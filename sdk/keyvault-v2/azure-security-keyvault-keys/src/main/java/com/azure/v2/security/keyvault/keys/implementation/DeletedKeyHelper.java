// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys.implementation;

import com.azure.v2.security.keyvault.keys.models.DeletedKey;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;

import java.time.OffsetDateTime;

/**
 * The helper class to create instances of {@link DeletedKey} and set its properties. This class is used to allow the
 * implementation to be hidden from the public API.
 */
public final class DeletedKeyHelper {
    private static DeletedKeyAccessor accessor;

    /**
     * The interface to be implemented by the client library to create instances of {@link DeletedKey} and set its
     * properties.
     */
    public interface DeletedKeyAccessor {
        /**
         * Creates a new instance of {@link DeletedKey} using the provided {@link JsonWebKey}.
         *
         * @param jsonWebKey The {@link JsonWebKey} to create the {@link DeletedKey} from.
         * @return A new instance of {@link DeletedKey}.
         */
        DeletedKey createDeletedKey(JsonWebKey jsonWebKey);

        /**
         * Sets the recovery ID of the {@link DeletedKey}.
         *
         * @param deletedKey The {@link DeletedKey} to set the recovery ID for.
         * @param recoveryId The recovery ID to set.
         */
        void setRecoveryId(DeletedKey deletedKey, String recoveryId);

        /**
         * Sets the scheduled purge date of the {@link DeletedKey}.
         *
         * @param deletedKey The {@link DeletedKey} to set the scheduled purge date for.
         * @param scheduledPurgeDate The scheduled purge date to set.
         */
        void setScheduledPurgeDate(DeletedKey deletedKey, OffsetDateTime scheduledPurgeDate);

        /**
         * Sets the deleted on date of the {@link DeletedKey}.
         *
         * @param deletedKey The {@link DeletedKey} to set the deleted on date for.
         * @param deletedOn The deleted on date to set.
         */
        void setDeletedOn(DeletedKey deletedKey, OffsetDateTime deletedOn);
    }

    /**
     * Creates a new instance of {@link DeletedKey} using the provided {@link JsonWebKey}.
     *
     * @param jsonWebKey The {@link JsonWebKey} to create the {@link DeletedKey} from.
     * @return A new instance of {@link DeletedKey}.
     */
    public static DeletedKey createDeletedKey(JsonWebKey jsonWebKey) {
        if (accessor == null) {
            new DeletedKey();
        }

        assert accessor != null;
        return accessor.createDeletedKey(jsonWebKey);
    }

    /**
     * Sets the recovery ID of the {@link DeletedKey}.
     *
     * @param deletedKey The {@link DeletedKey} to set the recovery ID for.
     * @param recoveryId The recovery ID to set.
     */
    public static void setRecoveryId(DeletedKey deletedKey, String recoveryId) {
        accessor.setRecoveryId(deletedKey, recoveryId);
    }

    /**
     * Sets the scheduled purge date of the {@link DeletedKey}.
     *
     * @param deletedKey The {@link DeletedKey} to set the scheduled purge date for.
     * @param scheduledPurgeDate The scheduled purge date to set.
     */
    public static void setScheduledPurgeDate(DeletedKey deletedKey, OffsetDateTime scheduledPurgeDate) {
        accessor.setScheduledPurgeDate(deletedKey, scheduledPurgeDate);
    }

    /**
     * Sets the deleted on date of the {@link DeletedKey}.
     *
     * @param deletedKey The {@link DeletedKey} to set the deleted on date for.
     * @param deletedOn The deleted on date to set.
     */
    public static void setDeletedOn(DeletedKey deletedKey, OffsetDateTime deletedOn) {
        accessor.setDeletedOn(deletedKey, deletedOn);
    }

    /**
     * Sets the accessor for the {@link DeletedKeyHelper}.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(DeletedKeyAccessor accessor) {
        DeletedKeyHelper.accessor = accessor;
    }

    private DeletedKeyHelper() {
        // Private constructor to prevent instantiation
    }
}
