// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.implementation.CertificatePolicyHelper;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCertificateWithPolicyHelper;
import com.azure.security.keyvault.certificates.implementation.models.CertificateBundle;

/**
 * Represents a certificate with all of its properties including {@link CertificatePolicy}.
 */
public class KeyVaultCertificateWithPolicy extends KeyVaultCertificate {
    static {
        KeyVaultCertificateWithPolicyHelper.setAccessor(KeyVaultCertificateWithPolicy::new);
    }

    /**
     * The Certificate policy.
     */
    private CertificatePolicy policy;

    KeyVaultCertificateWithPolicy() {
        super();
    }

    KeyVaultCertificateWithPolicy(CertificateBundle bundle) {
        super(bundle.getCer(), bundle.getKid(), bundle.getSid(), new CertificateProperties(bundle));

        this.policy = CertificatePolicyHelper.createCertificatePolicy(bundle.getPolicy());
    }

    KeyVaultCertificateWithPolicy(byte[] cer, String kid, String sid, CertificateProperties properties) {
        super(cer, kid, sid, properties);
    }

    /**
     * Set the certificate properties
     * @param properties the certificate properties
     * @throws NullPointerException if {@code certificateProperties} is null
     * @return the updated certificateWithPolicy object itself.
     */
    public KeyVaultCertificateWithPolicy setProperties(CertificateProperties properties) {
        super.setProperties(properties);
        return this;
    }

    /**
     * Get the certificate policy of the certificate
     * @return the cer content.
     */
    public CertificatePolicy getPolicy() {
        return this.policy;
    }

    /**
     * Set the certificate policy of the certificate
     *
     * @param certificatePolicy the policy to set.
     * @return the certificateWithPolicy object itself.
     */
    public KeyVaultCertificateWithPolicy setPolicy(CertificatePolicy certificatePolicy) {
        this.policy = certificatePolicy;
        return this;
    }
}
