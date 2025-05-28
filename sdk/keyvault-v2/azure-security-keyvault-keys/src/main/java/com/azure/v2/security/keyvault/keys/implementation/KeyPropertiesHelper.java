// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys.implementation;

import com.azure.v2.security.keyvault.keys.models.KeyProperties;

import java.time.OffsetDateTime;

/**
 * The helper class to set the properties of {@link KeyProperties} instances. This class is used to allow the
 * implementation to be hidden from the public API.
 */
public final class KeyPropertiesHelper {
    private static KeyPropertiesAccessor accessor;

    /**
     * The interface to be implemented by the client library to set the properties of {@link KeyProperties} instances.
     */
    public interface KeyPropertiesAccessor {
        /**
         * Sets the properties of the {@link KeyProperties} instance.
         *
         * @param keyProperties The {@link KeyProperties} instance to set the properties for.
         * @param createdOn The created on date to set.
         */
        void setCreatedOn(KeyProperties keyProperties, OffsetDateTime createdOn);

        /**
         * Sets the properties of the {@link KeyProperties} instance.
         *
         * @param keyProperties The {@link KeyProperties} instance to set the properties for.
         * @param updatedOn The updated on date to set.
         */
        void setUpdatedOn(KeyProperties keyProperties, OffsetDateTime updatedOn);

        /**
         * Sets the properties of the {@link KeyProperties} instance.
         *
         * @param keyProperties The {@link KeyProperties} instance to set the properties for.
         * @param recoveryLevel The recovery level to set.
         */
        void setRecoveryLevel(KeyProperties keyProperties, String recoveryLevel);

        /**
         * Sets the properties of the {@link KeyProperties} instance.
         *
         * @param keyProperties The {@link KeyProperties} instance to set the properties for.
         * @param name The name to set.
         */
        void setName(KeyProperties keyProperties, String name);

        /**
         * Sets the properties of the {@link KeyProperties} instance.
         *
         * @param keyProperties The {@link KeyProperties} instance to set the properties for.
         * @param version The version to set.
         */
        void setVersion(KeyProperties keyProperties, String version);

        /**
         * Sets the properties of the {@link KeyProperties} instance.
         *
         * @param keyProperties The {@link KeyProperties} instance to set the properties for.
         * @param id The ID to set.
         */
        void setId(KeyProperties keyProperties, String id);

        /**
         * Sets the properties of the {@link KeyProperties} instance.
         *
         * @param keyProperties The {@link KeyProperties} instance to set the properties for.
         * @param managed The managed status to set.
         */
        void setManaged(KeyProperties keyProperties, Boolean managed);

        /**
         * Sets the properties of the {@link KeyProperties} instance.
         *
         * @param keyProperties The {@link KeyProperties} instance to set the properties for.
         * @param recoverableDays The recoverable days to set.
         */
        void setRecoverableDays(KeyProperties keyProperties, Integer recoverableDays);

        /**
         * Sets the properties of the {@link KeyProperties} instance.
         *
         * @param keyProperties The {@link KeyProperties} instance to set the properties for.
         * @param hsmPlatform The HSM platform to set.
         */
        void setHsmPlatform(KeyProperties keyProperties, String hsmPlatform);
    }

    /**
     * Sets the properties of the {@link KeyProperties} instance.
     *
     * @param keyProperties The {@link KeyProperties} instance to set the properties for.
     * @param createdOn The created on date to set.
     */
    public static void setCreatedOn(KeyProperties keyProperties, OffsetDateTime createdOn) {
        accessor.setCreatedOn(keyProperties, createdOn);
    }

    /**
     * Sets the properties of the {@link KeyProperties} instance.
     *
     * @param keyProperties The {@link KeyProperties} instance to set the properties for.
     * @param updatedOn The updated on date to set.
     */
    public static void setUpdatedOn(KeyProperties keyProperties, OffsetDateTime updatedOn) {
        accessor.setUpdatedOn(keyProperties, updatedOn);
    }

    /**
     * Sets the properties of the {@link KeyProperties} instance.
     *
     * @param keyProperties The {@link KeyProperties} instance to set the properties for.
     * @param recoveryLevel The recovery level to set.
     */
    public static void setRecoveryLevel(KeyProperties keyProperties, String recoveryLevel) {
        accessor.setRecoveryLevel(keyProperties, recoveryLevel);
    }

    /**
     * Sets the properties of the {@link KeyProperties} instance.
     *
     * @param keyProperties The {@link KeyProperties} instance to set the properties for.
     * @param name The name to set.
     */
    public static void setName(KeyProperties keyProperties, String name) {
        accessor.setName(keyProperties, name);
    }

    /**
     * Sets the properties of the {@link KeyProperties} instance.
     *
     * @param keyProperties The {@link KeyProperties} instance to set the properties for.
     * @param version The version to set.
     */
    public static void setVersion(KeyProperties keyProperties, String version) {
        accessor.setVersion(keyProperties, version);
    }

    /**
     * Sets the properties of the {@link KeyProperties} instance.
     *
     * @param keyProperties The {@link KeyProperties} instance to set the properties for.
     * @param id The ID to set.
     */
    public static void setId(KeyProperties keyProperties, String id) {
        accessor.setId(keyProperties, id);
    }

    /**
     * Sets the properties of the {@link KeyProperties} instance.
     *
     * @param keyProperties The {@link KeyProperties} instance to set the properties for.
     * @param managed The managed status to set.
     */
    public static void setManaged(KeyProperties keyProperties, Boolean managed) {
        accessor.setManaged(keyProperties, managed);
    }

    /**
     * Sets the properties of the {@link KeyProperties} instance.
     *
     * @param keyProperties The {@link KeyProperties} instance to set the properties for.
     * @param recoverableDays The recoverable days to set.
     */
    public static void setRecoverableDays(KeyProperties keyProperties, Integer recoverableDays) {
        accessor.setRecoverableDays(keyProperties, recoverableDays);
    }

    /**
     * Sets the properties of the {@link KeyProperties} instance.
     *
     * @param keyProperties The {@link KeyProperties} instance to set the properties for.
     * @param hsmPlatform The HSM platform to set.
     */
    public static void setHsmPlatform(KeyProperties keyProperties, String hsmPlatform) {
        accessor.setHsmPlatform(keyProperties, hsmPlatform);
    }

    /**
     * Sets the accessor for the {@link KeyProperties} instance.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(KeyPropertiesAccessor accessor) {
        KeyPropertiesHelper.accessor = accessor;
    }

    private KeyPropertiesHelper() {
        // Private constructor to prevent instantiation.
    }
}
