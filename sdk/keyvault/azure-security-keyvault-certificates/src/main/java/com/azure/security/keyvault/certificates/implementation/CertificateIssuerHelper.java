// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates.implementation;

import com.azure.security.keyvault.certificates.implementation.models.IssuerBundle;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;

public final class CertificateIssuerHelper {
    private static CertificateIssuerAccessor accessor;

    public interface CertificateIssuerAccessor {
        CertificateIssuer createCertificateIssuer(IssuerBundle issuerBundle);

        IssuerBundle getIssuerBundle(CertificateIssuer certificateIssuer);
    }

    public static CertificateIssuer createCertificateIssuer(IssuerBundle issuerBundle) {
        if (accessor == null) {
            new CertificateIssuer("");
        }

        assert accessor != null;
        return accessor.createCertificateIssuer(issuerBundle);
    }

    public static IssuerBundle getIssuerBundle(CertificateIssuer certificateIssuer) {
        if (accessor == null) {
            new CertificateIssuer("");
        }

        assert accessor != null;
        return accessor.getIssuerBundle(certificateIssuer);
    }

    public static void setAccessor(CertificateIssuerAccessor accessor) {
        CertificateIssuerHelper.accessor = accessor;
    }
}
