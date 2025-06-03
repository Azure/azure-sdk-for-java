// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

import com.azure.v2.security.keyvault.certificates.models.CertificateOperation;

/**
 * Helper class to manage the conversion between the implementation and public models of {@link CertificateOperation}.
 * This class is used to allow the implementation to be hidden from the public API.
 */
public final class CertificateOperationHelper {
    private static CertificateOperationAccessor accessor;

    /**
     * Interface to be implemented by the client to provide the logic for creating {@link CertificateOperation}
     * instances.
     */
    public interface CertificateOperationAccessor {
        /**
         * Creates a {@link CertificateOperation} instance from the given implementation model.
         *
         * @param impl The implementation model to create the certificate operation from.
         * @return A {@link CertificateOperation} instance.
         */
        CertificateOperation createCertificateOperation(
            com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation impl);
    }

    /**
     * Creates a {@link CertificateOperation} instance from the given implementation model.
     *
     * @param impl The implementation model to create the certificate operation from.
     * @return A {@link CertificateOperation} instance.
     */
    public static CertificateOperation createCertificateOperation(
        com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation impl) {

        if (accessor == null) {
            new CertificateOperation();
        }

        assert accessor != null;
        return accessor.createCertificateOperation(impl);
    }

    /**
     * Sets the accessor to be used for creating {@link CertificateOperation} instances.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(CertificateOperationAccessor accessor) {
        CertificateOperationHelper.accessor = accessor;
    }

    private CertificateOperationHelper() {
        // Private constructor to prevent instantiation.
    }
}
