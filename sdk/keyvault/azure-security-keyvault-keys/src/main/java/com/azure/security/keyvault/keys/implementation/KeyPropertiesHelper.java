// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.security.keyvault.keys.models.KeyProperties;

import java.time.OffsetDateTime;

public final class KeyPropertiesHelper {
    private static KeyPropertiesAccessor accessor;

    public interface KeyPropertiesAccessor {
        void setCreatedOn(KeyProperties keyProperties, OffsetDateTime createdOn);
        void setUpdatedOn(KeyProperties keyProperties, OffsetDateTime updatedOn);
        void setRecoveryLevel(KeyProperties keyProperties, String recoveryLevel);
        void setName(KeyProperties keyProperties, String name);
        void setVersion(KeyProperties keyProperties, String version);
        void setId(KeyProperties keyProperties, String id);
        void setManaged(KeyProperties keyProperties, Boolean managed);
        void setRecoverableDays(KeyProperties keyProperties, Integer recoverableDays);
        void setHsmPlatform(KeyProperties keyProperties, String hsmPlatform);
    }

    public static void setCreatedOn(KeyProperties keyProperties, OffsetDateTime createdOn) {
        accessor.setCreatedOn(keyProperties, createdOn);
    }

    public static void setUpdatedOn(KeyProperties keyProperties, OffsetDateTime updatedOn) {
        accessor.setUpdatedOn(keyProperties, updatedOn);
    }

    public static void setRecoveryLevel(KeyProperties keyProperties, String recoveryLevel) {
        accessor.setRecoveryLevel(keyProperties, recoveryLevel);
    }

    public static void setName(KeyProperties keyProperties, String name) {
        accessor.setName(keyProperties, name);
    }

    public static void setVersion(KeyProperties keyProperties, String version) {
        accessor.setVersion(keyProperties, version);
    }

    public static void setId(KeyProperties keyProperties, String id) {
        accessor.setId(keyProperties, id);
    }

    public static void setManaged(KeyProperties keyProperties, Boolean managed) {
        accessor.setManaged(keyProperties, managed);
    }

    public static void setRecoverableDays(KeyProperties keyProperties, Integer recoverableDays) {
        accessor.setRecoverableDays(keyProperties, recoverableDays);
    }

    public static void setHsmPlatform(KeyProperties keyProperties, String hsmPlatform) {
        accessor.setHsmPlatform(keyProperties, hsmPlatform);
    }

    public static void setAccessor(KeyPropertiesAccessor accessor) {
        KeyPropertiesHelper.accessor = accessor;
    }

    private KeyPropertiesHelper() { }
}
