// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates.implementation;

import com.azure.security.keyvault.certificates.implementation.models.CertificateIssuerItem;
import com.azure.security.keyvault.certificates.models.IssuerProperties;

public final class IssuerPropertiesHelper {
    private static IssuerPropertiesAccessor accessor;

    public interface IssuerPropertiesAccessor {
        IssuerProperties createIssuerProperties(CertificateIssuerItem impl);
    }

    public static IssuerProperties createIssuerProperties(CertificateIssuerItem impl) {
        if (accessor == null) {
            new IssuerProperties();
        }

        assert accessor != null;
        return accessor.createIssuerProperties(impl);
    }

    public static void setAccessor(IssuerPropertiesAccessor accessor) {
        IssuerPropertiesHelper.accessor = accessor;
    }

    private IssuerPropertiesHelper() {
    }
}
