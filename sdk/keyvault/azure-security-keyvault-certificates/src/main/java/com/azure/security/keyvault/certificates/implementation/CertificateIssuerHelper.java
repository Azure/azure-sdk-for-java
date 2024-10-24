// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates.implementation;

import com.azure.security.keyvault.certificates.implementation.models.IssuerBundle;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;

public final class CertificateIssuerHelper {
    private static CertificateIssuerAccessor accessor;

    public interface CertificateIssuerAccessor {
        CertificateIssuer createCertificateIssuer(IssuerBundle impl);

        IssuerBundle getImpl(CertificateIssuer certificateIssuer);
    }

    public static CertificateIssuer createCertificateIssuer(IssuerBundle impl) {
        if (accessor == null) {
            new CertificateIssuer("");
        }

        assert accessor != null;
        return accessor.createCertificateIssuer(impl);
    }

    public static IssuerBundle getIssuerBundle(CertificateIssuer certificateIssuer) {
        if (accessor == null) {
            new CertificateIssuer("");
        }

        assert accessor != null;
        return accessor.getImpl(certificateIssuer);
    }

    public static void setAccessor(CertificateIssuerAccessor accessor) {
        CertificateIssuerHelper.accessor = accessor;
    }
}
