// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.security.keyvault.keys.models.KeyRotationPolicy;

public final class KeyRotationPolicyHelper {
    private static KeyRotationPolicyAccessor accessor;

    public interface KeyRotationPolicyAccessor {
        KeyRotationPolicy createPolicy(com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy impl);

        com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy getImpl(KeyRotationPolicy policy);
    }

    public static KeyRotationPolicy
        createPolicy(com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy impl) {
        // If the class hasn't been loaded yet the accessor won't be set. Attempt to load the class before using the
        // accessor.
        if (accessor == null) {
            new KeyRotationPolicy();
        }

        assert accessor != null;
        return accessor.createPolicy(impl);
    }

    public static com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy
        getImpl(KeyRotationPolicy policy) {
        return accessor.getImpl(policy);
    }

    public static void setAccessor(KeyRotationPolicyAccessor accessor) {
        KeyRotationPolicyHelper.accessor = accessor;
    }

    private KeyRotationPolicyHelper() {
    }
}
