// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.implementation;

import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

public class SecretPropertiesHelper {
    private static SecretPropertiesAccessor accessor;

    public interface SecretPropertiesAccessor {
        void setId(SecretProperties properties, String id);

        void setVersion(SecretProperties properties, String version);

        void setCreatedOn(SecretProperties properties, OffsetDateTime createdOn);

        void setUpdatedOn(SecretProperties properties, OffsetDateTime updatedOn);

        void setName(SecretProperties properties, String name);

        void setRecoveryLevel(SecretProperties properties, String recoveryLevel);

        void setKeyId(SecretProperties properties, String keyId);

        void setManaged(SecretProperties properties, Boolean managed);

        void setRecoverableDays(SecretProperties properties, Integer recoverableDays);
    }

    public static void setId(SecretProperties properties, String id) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setId(properties, id);
    }

    public static void setVersion(SecretProperties properties, String version) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setVersion(properties, version);
    }

    public static void setCreatedOn(SecretProperties properties, OffsetDateTime createdOn) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setCreatedOn(properties, createdOn);
    }

    public static void setUpdatedOn(SecretProperties properties, OffsetDateTime updatedOn) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setUpdatedOn(properties, updatedOn);
    }

    public static void setName(SecretProperties properties, String name) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setName(properties, name);
    }

    public static void setRecoveryLevel(SecretProperties properties, String recoveryLevel) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setRecoveryLevel(properties, recoveryLevel);
    }

    public static void setKeyId(SecretProperties properties, String keyId) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setKeyId(properties, keyId);
    }

    public static void setManaged(SecretProperties properties, Boolean managed) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setManaged(properties, managed);
    }

    public static void setRecoverableDays(SecretProperties properties, Integer recoverableDays) {
        if (accessor == null) {
            new SecretProperties();
        }

        assert accessor != null;
        accessor.setRecoverableDays(properties, recoverableDays);
    }

    public static void setAccessor(SecretPropertiesAccessor newAccessor) {
        accessor = newAccessor;
    }
}
