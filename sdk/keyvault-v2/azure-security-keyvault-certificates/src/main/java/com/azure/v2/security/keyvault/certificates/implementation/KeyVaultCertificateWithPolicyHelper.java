// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateBundle;
import com.azure.v2.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;

/**
 * Helper class to manage the conversion between the implementation and public models of
 * {@link KeyVaultCertificateWithPolicy}. This class is used to allow the implementation to be hidden from the public
 * API.
 */
public final class KeyVaultCertificateWithPolicyHelper {
    private static KeyVaultCertificateWithPolicyAccessor accessor;

    /**
     * Interface to be implemented by the client to provide the logic for creating {@link KeyVaultCertificateWithPolicy}
     * instances.
     */
    public interface KeyVaultCertificateWithPolicyAccessor {
        /**
         * Creates a {@link KeyVaultCertificateWithPolicy} instance from the given implementation model.
         *
         * @param bundle The implementation model to create the certificate with policy from.
         * @return A {@link KeyVaultCertificateWithPolicy} instance.
         */
        KeyVaultCertificateWithPolicy createCertificateWithPolicy(CertificateBundle bundle);
    }

    /**
     * Creates a {@link KeyVaultCertificateWithPolicy} instance from the given implementation model.
     *
     * @param bundle The implementation model to create the certificate with policy from.
     * @return A {@link KeyVaultCertificateWithPolicy} instance.
     */
    public static KeyVaultCertificateWithPolicy createCertificateWithPolicy(CertificateBundle bundle) {
        if (accessor == null) {
            // KeyVaultCertificateWithPolicy doesn't have a public constructor but DeletedCertificate does and is a
            // subtype of it. This will result in KeyVaultCertificateWithPolicy being loaded by the class loader.
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createCertificateWithPolicy(bundle);
    }

    /**
     * Sets the accessor to be used for creating {@link KeyVaultCertificateWithPolicy} instances.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(KeyVaultCertificateWithPolicyAccessor accessor) {
        KeyVaultCertificateWithPolicyHelper.accessor = accessor;
    }

    private KeyVaultCertificateWithPolicyHelper() {
        // Private constructor to prevent instantiation.
    }
}
