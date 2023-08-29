// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.Base64Url;
import com.azure.security.keyvault.certificates.implementation.CertificatePolicyHelper;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCertificateWithPolicyHelper;
import com.azure.security.keyvault.certificates.implementation.models.CertificateBundle;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

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
    @JsonProperty("policy")
    CertificatePolicy policy;

    /**
     * Create the certificate
     * @param name the name of the certificate.
     */
    KeyVaultCertificateWithPolicy(String name) {
        super(name);
    }

    KeyVaultCertificateWithPolicy() {
        super();
    }

    KeyVaultCertificateWithPolicy(CertificateBundle bundle) {
        super();
        unpackId(bundle.getId());

        this.policy = CertificatePolicyHelper.createCertificatePolicy(bundle.getPolicy());
        this.cer = bundle.getCer();
        this.keyId = bundle.getKid();
        this.secretId = bundle.getSid();

        this.properties.enabled = bundle.getAttributes().isEnabled();
        this.properties.notBefore = bundle.getAttributes().getNotBefore();
        this.properties.expiresOn = bundle.getAttributes().getExpires();
        this.properties.createdOn = bundle.getAttributes().getCreated();
        this.properties.updatedOn = bundle.getAttributes().getUpdated();
        this.properties.recoveryLevel = Objects.toString(bundle.getAttributes().getRecoveryLevel(), null);
        this.properties.id = bundle.getPolicy().getId();
        this.properties.tags = bundle.getTags();
        this.properties.x509Thumbprint = bundle.getX509Thumbprint() == null
            ? null : new Base64Url(bundle.getX509Thumbprint());
        this.properties.recoverableDays = bundle.getAttributes().getRecoverableDays();
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
