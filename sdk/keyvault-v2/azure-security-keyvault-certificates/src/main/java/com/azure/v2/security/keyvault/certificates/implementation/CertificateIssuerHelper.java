// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

import com.azure.v2.security.keyvault.certificates.implementation.models.IssuerBundle;
import com.azure.v2.security.keyvault.certificates.models.CertificateIssuer;

/**
 * Helper class to manage the conversion between the implementation and public models of {@link CertificateIssuer}.
 * This class is used to allow the implementation to be hidden from the public API.
 */
public final class CertificateIssuerHelper {
    private static CertificateIssuerAccessor accessor;

    /**
     * Interface to be implemented by the client to provide the logic for creating {@link CertificateIssuer} and
     * {@link IssuerBundle} instances.
     */
    public interface CertificateIssuerAccessor {
        /**
         * Creates a {@link CertificateIssuer} instance from the given {@link IssuerBundle}.
         *
         * @param issuerBundle The issuer bundle to create the certificate issuer from.
         * @return A {@link CertificateIssuer} instance.
         */
        CertificateIssuer createCertificateIssuer(IssuerBundle issuerBundle);

        /**
         * Creates an {@link IssuerBundle} instance from the given {@link CertificateIssuer}.
         *
         * @param certificateIssuer The certificate issuer to create the issuer bundle from.
         * @return An {@link IssuerBundle} instance.
         */
        IssuerBundle getIssuerBundle(CertificateIssuer certificateIssuer);
    }

    /**
     * Creates a {@link CertificateIssuer} instance from the given {@link IssuerBundle}.
     *
     * @param issuerBundle The issuer bundle to create the certificate issuer from.
     * @return A {@link CertificateIssuer} instance.
     */
    public static CertificateIssuer createCertificateIssuer(IssuerBundle issuerBundle) {
        if (accessor == null) {
            new CertificateIssuer("");
        }

        assert accessor != null;
        return accessor.createCertificateIssuer(issuerBundle);
    }

    /**
     * Creates an {@link IssuerBundle} instance from the given {@link CertificateIssuer}.
     *
     * @param certificateIssuer The certificate issuer to create the issuer bundle from.
     * @return An {@link IssuerBundle} instance.
     */
    public static IssuerBundle getIssuerBundle(CertificateIssuer certificateIssuer) {
        if (accessor == null) {
            new CertificateIssuer("");
        }

        assert accessor != null;
        return accessor.getIssuerBundle(certificateIssuer);
    }

    /**
     * Sets the accessor to be used for creating {@link CertificateIssuer} and {@link IssuerBundle} instances.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(CertificateIssuerAccessor accessor) {
        CertificateIssuerHelper.accessor = accessor;
    }

    private CertificateIssuerHelper() {
        // Private constructor to prevent instantiation.
    }
}
