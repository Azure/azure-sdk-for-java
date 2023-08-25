// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.security.keyvault.keys.models.KeyRotationPolicy;

public final class KeyRotationPolicyHelper {
    private static KeyRotationPolicyAccessor accessor;

    public interface KeyRotationPolicyAccessor {
        void setImpl(KeyRotationPolicy policy,
            com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy impl);
        com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy getImpl(KeyRotationPolicy policy);
    }

    public static void setImpl(KeyRotationPolicy policy,
        com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy impl) {
        accessor.setImpl(policy, impl);
    }

    public static com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy getImpl(
        KeyRotationPolicy policy) {
        return accessor.getImpl(policy);
    }

    public static void setAccessor(KeyRotationPolicyAccessor accessor) {
        KeyRotationPolicyHelper.accessor = accessor;
    }

    private KeyRotationPolicyHelper() { }
}
