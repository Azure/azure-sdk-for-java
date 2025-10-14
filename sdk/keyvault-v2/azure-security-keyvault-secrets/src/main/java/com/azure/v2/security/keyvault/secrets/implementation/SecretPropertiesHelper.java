// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.secrets.implementation;

import com.azure.v2.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of {@link SecretProperties}.
 */
public final class SecretPropertiesHelper {
    private static SecretPropertiesAccessor accessor;

    /**
     * The interface to set the non-public properties of {@link SecretProperties}.
     */
    public interface SecretPropertiesAccessor {
        /**
         * Sets the ID in the {@link SecretProperties}.
         *
         * @param properties The {@link SecretProperties} to set the ID for.
         * @param id The ID to set.
         */
        void setId(SecretProperties properties, String id);

        /**
         * Sets the secret version in the {@link SecretProperties}.
         *
         * @param properties The {@link SecretProperties} to set the version for.
         * @param version The version to set.
         */
        void setVersion(SecretProperties properties, String version);

        /**
         * Sets the created on date of the secret in the {@link SecretProperties}.
         *
         * @param properties The {@link SecretProperties} to set the created on date for.
         * @param createdOn The created on date to set.
         */
        void setCreatedOn(SecretProperties properties, OffsetDateTime createdOn);

        /**
         * Sets the updated on date of the secret in the {@link SecretProperties}.
         *
         * @param properties The {@link SecretProperties} to set the updated on date for.
         * @param updatedOn The updated on date to set.
         */
        void setUpdatedOn(SecretProperties properties, OffsetDateTime updatedOn);

        /**
         * Sets the name of the secret in the {@link SecretProperties}.
         *
         * @param properties The {@link SecretProperties} to set the name for.
         * @param name The name to set.
         */
        void setName(SecretProperties properties, String name);

        /**
         * Sets the recovery level of the secret in the {@link SecretProperties}.
         *
         * @param properties The {@link SecretProperties} to set the recovery level for.
         * @param recoveryLevel The recovery level to set.
         */
        void setRecoveryLevel(SecretProperties properties, String recoveryLevel);

        /**
         * Sets the key ID of the secret in the {@link SecretProperties}.
         *
         * @param properties The {@link SecretProperties} to set the key ID for.
         * @param keyId The key ID to set.
         */
        void setKeyId(SecretProperties properties, String keyId);

        /**
         * Sets the managed property of the secret in the {@link SecretProperties}.
         *
         * @param properties The {@link SecretProperties} to set the managed property for.
         * @param managed The managed property to set.
         */
        void setManaged(SecretProperties properties, Boolean managed);

        /**
         * Sets the recoverable days of the secret in the {@link SecretProperties}.
         *
         * @param properties The {@link SecretProperties} to set the recoverable days for.
         * @param recoverableDays The recoverable days to set.
         */
        void setRecoverableDays(SecretProperties properties, Integer recoverableDays);
    }

    /**
     * Sets the ID of the secret in the {@link SecretProperties}.
     *
     * @param properties The {@link SecretProperties} to set the ID in.
     * @param id The ID to set.
     */
    public static void setId(SecretProperties properties, String id) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setId(properties, id);
    }

    /**
     * Sets the version of the secret in the {@link SecretProperties}.
     *
     * @param properties The {@link SecretProperties} to set the version in.
     * @param version The version to set.
     */
    public static void setVersion(SecretProperties properties, String version) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setVersion(properties, version);
    }

    /**
     * Sets the created on date of the secret in the {@link SecretProperties}.
     *
     * @param properties The {@link SecretProperties} to set the created on date in.
     * @param createdOn The created on date to set.
     */
    public static void setCreatedOn(SecretProperties properties, OffsetDateTime createdOn) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setCreatedOn(properties, createdOn);
    }

    /**
     * Sets the updated on date of the secret in the {@link SecretProperties}.
     *
     * @param properties The {@link SecretProperties} to set the updated on date in.
     * @param updatedOn The updated on date to set.
     */
    public static void setUpdatedOn(SecretProperties properties, OffsetDateTime updatedOn) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setUpdatedOn(properties, updatedOn);
    }

    /**
     * Sets the name of the secret in the {@link SecretProperties}.
     *
     * @param properties The {@link SecretProperties} to set the name in.
     * @param name The name to set.
     */
    public static void setName(SecretProperties properties, String name) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setName(properties, name);
    }

    /**
     * Sets the recovery level of the secret in the {@link SecretProperties}.
     *
     * @param properties The {@link SecretProperties} to set the recovery level in.
     * @param recoveryLevel The recovery level to set.
     */
    public static void setRecoveryLevel(SecretProperties properties, String recoveryLevel) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setRecoveryLevel(properties, recoveryLevel);
    }

    /**
     * Sets the key ID of the secret in the {@link SecretProperties}.
     *
     * @param properties The {@link SecretProperties} to set the key ID in.
     * @param keyId The key ID to set.
     */
    public static void setKeyId(SecretProperties properties, String keyId) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setKeyId(properties, keyId);
    }

    /**
     * Sets the managed property of the secret in the {@link SecretProperties}.
     *
     * @param properties The {@link SecretProperties} to set the managed property in.
     * @param managed The managed property to set.
     */
    public static void setManaged(SecretProperties properties, Boolean managed) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setManaged(properties, managed);
    }

    /**
     * Sets the recoverable days of the secret in the {@link SecretProperties}.
     *
     * @param properties The {@link SecretProperties} to set the recoverable days in.
     * @param recoverableDays The recoverable days to set.
     */
    public static void setRecoverableDays(SecretProperties properties, Integer recoverableDays) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setRecoverableDays(properties, recoverableDays);
    }

    /**
     * Sets the accessor for the {@link SecretPropertiesHelper}.
     *
     * @param newAccessor The new accessor to set.
     */
    public static void setAccessor(SecretPropertiesAccessor newAccessor) {
        accessor = newAccessor;
    }

    private SecretPropertiesHelper() {
        // Private constructor to prevent instantiation.
    }
}
