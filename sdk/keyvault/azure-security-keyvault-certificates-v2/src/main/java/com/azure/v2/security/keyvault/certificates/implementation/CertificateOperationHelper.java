// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

import com.azure.v2.security.keyvault.certificates.models.CertificateOperation;

public final class CertificateOperationHelper {
    private static CertificateOperationAccessor accessor;

    public interface CertificateOperationAccessor {
        CertificateOperation createCertificateOperation(
            com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation impl);
    }

    public static CertificateOperation createCertificateOperation(
        com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation impl) {

        if (accessor == null) {
            new CertificateOperation();
        }

        assert accessor != null;
        return accessor.createCertificateOperation(impl);
    }

    public static void setAccessor(CertificateOperationAccessor accessor) {
        CertificateOperationHelper.accessor = accessor;
    }

    private CertificateOperationHelper() {
    }
}
