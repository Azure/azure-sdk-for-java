// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation;

import com.azure.security.keyvault.keys.models.KeyAttestation;

public final class KeyAttestationHelper {
    private static KeyAttestationAccessor accessor;

    public interface KeyAttestationAccessor {
        KeyAttestation createKeyAttestation(com.azure.security.keyvault.keys.implementation.models.KeyAttestation impl);
    }

    public static KeyAttestation
        createKeyAttestation(com.azure.security.keyvault.keys.implementation.models.KeyAttestation impl) {
        // If the class hasn't been loaded yet the accessor won't be set. Attempt to load the class before using the
        // accessor.
        if (accessor == null) {
            new KeyAttestation();
        }

        assert accessor != null;
        return accessor.createKeyAttestation(impl);
    }

    public static void setAccessor(KeyAttestationAccessor accessor) {
        KeyAttestationHelper.accessor = accessor;
    }

    private KeyAttestationHelper() {
    }
}
