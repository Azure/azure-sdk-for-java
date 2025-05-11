// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

import com.azure.v2.security.keyvault.certificates.models.CertificatePolicy;

/**
 * Helper class to manage the conversion between the implementation and public models of {@link CertificatePolicy}.
 * This class is used to allow the implementation to be hidden from the public API.
 */
public final class CertificatePolicyHelper {
    private static CertificatePolicyAccessor accessor;

    /**
     * Interface to be implemented by the client to provide the logic for creating {@link CertificatePolicy}
     * instances.
     */
    public interface CertificatePolicyAccessor {
        /**
         * Creates a {@link CertificatePolicy} instance from the given implementation model.
         *
         * @param impl The implementation model to create the certificate policy from.
         * @return A {@link CertificatePolicy} instance.
         */
        CertificatePolicy
            createPolicy(com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy impl);

        /**
         * Creates a {@link com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy}
         * instance from the given public model.
         *
         * @param policy The public model to create the certificate policy from.
         * @return A {@link com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy}
         * instance.
         */
        com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy
            getPolicy(CertificatePolicy policy);
    }

    /**
     * Creates a {@link CertificatePolicy} instance from the given implementation model.
     *
     * @param impl The implementation model to create the certificate policy from.
     * @return A {@link CertificatePolicy} instance.
     */
    public static CertificatePolicy createCertificatePolicy(
        com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy impl) {
        if (accessor == null) {
            new CertificatePolicy("", "");
        }

        assert accessor != null;
        return accessor.createPolicy(impl);
    }

    /**
     * Creates a {@link com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy} instance
     * from the given public model.
     *
     * @param policy The public model to create the certificate policy from.
     * @return A {@link com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy} instance.
     */
    public static com.azure.v2.security.keyvault.certificates.implementation.models.CertificatePolicy
        getImplCertificatePolicy(CertificatePolicy policy) {
        if (accessor == null) {
            new CertificatePolicy("", "");
        }

        assert accessor != null;
        return accessor.getPolicy(policy);
    }

    /**
     * Sets the accessor to be used for creating {@link CertificatePolicy} instances.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(CertificatePolicyAccessor accessor) {
        CertificatePolicyHelper.accessor = accessor;
    }

    private CertificatePolicyHelper() {
        // Private constructor to prevent instantiation.
    }
}
