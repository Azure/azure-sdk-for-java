// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates.implementation;

import com.azure.security.keyvault.certificates.implementation.models.CertificateItem;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateItem;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;

public final class CertificatePropertiesHelper {
    private static CertificatePropertiesAccessor accessor;

    public interface CertificatePropertiesAccessor {
        CertificateProperties createCertificateProperties(CertificateItem item);

        CertificateProperties createCertificateProperties(DeletedCertificateItem item);
    }

    public static CertificateProperties createCertificateProperties(CertificateItem item) {
        if (accessor == null) {
            // CertificateProperties doesn't have a public constructor but DeletedCertificate does and creates an
            // instance of CertificateProperties. This will result in CertificateProperties being loaded by the class
            // loader.
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createCertificateProperties(item);
    }

    public static CertificateProperties createCertificateProperties(DeletedCertificateItem item) {
        if (accessor == null) {
            // CertificateProperties doesn't have a public constructor but DeletedCertificate does and creates an
            // instance of CertificateProperties. This will result in CertificateProperties being loaded by the class
            // loader.
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createCertificateProperties(item);
    }

    public static void setAccessor(CertificatePropertiesAccessor accessor) {
        CertificatePropertiesHelper.accessor = accessor;
    }

    private CertificatePropertiesHelper() {
    }
}
