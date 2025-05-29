// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys.implementation;

import com.azure.v2.security.keyvault.keys.models.KeyRotationPolicy;

/**
 * Helper class to manage the conversion between the implementation and public models of {@link KeyRotationPolicy}.
 * This class is used to allow the implementation to be hidden from the public API.
 */
public final class KeyRotationPolicyHelper {
    private static KeyRotationPolicyAccessor accessor;

    /**
     * Interface to be implemented by the client to provide the logic for creating {@link KeyRotationPolicy}
     * instances.
     */
    public interface KeyRotationPolicyAccessor {
        /**
         * Creates a new instance of {@link KeyRotationPolicy} using the provided implementation model.
         *
         * @param impl The implementation model to create the {@link KeyRotationPolicy} from.
         * @return A new instance of {@link KeyRotationPolicy}.
         */
        KeyRotationPolicy
            createPolicy(com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy impl);

        /**
         * Creates a new instance of {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy}
         * using the provided public model.
         *
         * @param policy The public model to create the {@link KeyRotationPolicy} from.
         * @return A new instance of
         * {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy}.
         */
        com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy getImpl(KeyRotationPolicy policy);
    }

    /**
     * Creates a {@link KeyRotationPolicy} instance from the given implementation model.
     *
     * @param impl The implementation model to create the key rotation policy from.
     * @return A {@link KeyRotationPolicy} instance.
     */
    public static KeyRotationPolicy
        createPolicy(com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy impl) {
        // If the class hasn't been loaded yet the accessor won't be set. Attempt to load the class before using the
        // accessor.
        if (accessor == null) {
            new KeyRotationPolicy();
        }

        assert accessor != null;
        return accessor.createPolicy(impl);
    }

    /**
     * Creates a {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy} instance from the
     * given public model.
     *
     * @param policy The public model to create the key rotation policy from.
     * @return A {@link com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy} instance.
     */
    public static com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy
        getImpl(KeyRotationPolicy policy) {

        return accessor.getImpl(policy);
    }

    /**
     * Sets the accessor for the {@link KeyRotationPolicyHelper} class.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(KeyRotationPolicyAccessor accessor) {
        KeyRotationPolicyHelper.accessor = accessor;
    }

    private KeyRotationPolicyHelper() {
        // Private constructor to prevent instantiation.
    }
}
