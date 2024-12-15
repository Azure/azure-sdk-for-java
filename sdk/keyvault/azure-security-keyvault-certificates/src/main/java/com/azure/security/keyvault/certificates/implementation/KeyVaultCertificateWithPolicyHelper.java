// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates.implementation;

import com.azure.security.keyvault.certificates.implementation.models.CertificateBundle;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;

public final class KeyVaultCertificateWithPolicyHelper {
    private static KeyVaultCertificateWithPolicyAccessor accessor;

    public interface KeyVaultCertificateWithPolicyAccessor {
        KeyVaultCertificateWithPolicy createCertificateWithPolicy(CertificateBundle bundle);
    }

    public static KeyVaultCertificateWithPolicy createCertificateWithPolicy(CertificateBundle bundle) {
        if (accessor == null) {
            // KeyVaultCertificateWithPolicy doesn't have a public constructor but DeletedCertificate does and is a
            // subtype of it. This will result in KeyVaultCertificateWithPolicy being loaded by the class loader.
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createCertificateWithPolicy(bundle);
    }

    public static void setAccessor(KeyVaultCertificateWithPolicyAccessor accessor) {
        KeyVaultCertificateWithPolicyHelper.accessor = accessor;
    }

    private KeyVaultCertificateWithPolicyHelper() {
    }
}
