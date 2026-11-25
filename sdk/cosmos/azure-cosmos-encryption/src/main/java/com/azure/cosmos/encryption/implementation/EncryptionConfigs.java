// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Internal configuration for the encryption SDK.
 */
public final class EncryptionConfigs {
    static final String MIXED_ARRAYS_ENABLED = "COSMOS.ENCRYPTION_MIXED_ARRAYS_ENABLED";
    static final String MIXED_ARRAYS_ENABLED_VARIABLE = "COSMOS_ENCRYPTION_MIXED_ARRAYS_ENABLED";
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionConfigs.class);
    private static final AtomicBoolean LEGACY_MODE_WARNING_LOGGED = new AtomicBoolean(false);

    private EncryptionConfigs() {
    }

    /**
     * Resolves the mixed-array encryption setting, giving the JVM property precedence over the environment variable.
     *
     * @return whether corrected mixed-array traversal is enabled
     */
    public static boolean isMixedArrayEncryptionEnabled() {
        return isMixedArrayEncryptionEnabled(System.getProperty(MIXED_ARRAYS_ENABLED),
            System.getenv(MIXED_ARRAYS_ENABLED_VARIABLE));
    }

    static boolean isMixedArrayEncryptionEnabled(String systemPropertyValue, String environmentVariableValue) {
        String value = systemPropertyValue == null || systemPropertyValue.isEmpty()
            ? environmentVariableValue : systemPropertyValue;
        if (value == null || value.isEmpty() || "true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            if (LEGACY_MODE_WARNING_LOGGED.compareAndSet(false, true)) {
                LOGGER.warn("Mixed-array encryption is disabled through {} or {}. Legacy traversal can leave "
                    + "scalar array elements unencrypted and cannot correctly read mixed arrays written with "
                    + "the fix enabled. Use this temporary compatibility mode only until legacy data is migrated.",
                    MIXED_ARRAYS_ENABLED, MIXED_ARRAYS_ENABLED_VARIABLE);
            }
            return false;
        }
        throw new IllegalArgumentException("Invalid " + MIXED_ARRAYS_ENABLED + " or "
            + MIXED_ARRAYS_ENABLED_VARIABLE + " setting. Only 'true' or 'false' are supported.");
    }
}
