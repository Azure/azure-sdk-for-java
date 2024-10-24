// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates.implementation;

import com.azure.security.keyvault.certificates.models.CertificatePolicy;

public final class CertificatePolicyHelper {
    private static CertificatePolicyAccessor accessor;

    public interface CertificatePolicyAccessor {
        CertificatePolicy
            createPolicy(com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy impl);

        com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy
            getPolicy(CertificatePolicy policy);
    }

    public static CertificatePolicy
        createCertificatePolicy(com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy impl) {
        if (accessor == null) {
            new CertificatePolicy("", "");
        }

        assert accessor != null;
        return accessor.createPolicy(impl);
    }

    public static com.azure.security.keyvault.certificates.implementation.models.CertificatePolicy
        getImplCertificatePolicy(CertificatePolicy policy) {
        if (accessor == null) {
            new CertificatePolicy("", "");
        }

        assert accessor != null;
        return accessor.getPolicy(policy);
    }

    public static void setAccessor(CertificatePolicyAccessor accessor) {
        CertificatePolicyHelper.accessor = accessor;
    }

    private CertificatePolicyHelper() {
    }
}
