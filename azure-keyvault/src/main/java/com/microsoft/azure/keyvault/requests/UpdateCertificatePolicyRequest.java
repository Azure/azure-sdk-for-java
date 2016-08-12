package com.microsoft.azure.keyvault.requests;

import java.util.ArrayList;

import com.microsoft.azure.keyvault.models.CertificateAttributes;
import com.microsoft.azure.keyvault.models.CertificatePolicy;
import com.microsoft.azure.keyvault.models.IssuerReference;
import com.microsoft.azure.keyvault.models.KeyProperties;
import com.microsoft.azure.keyvault.models.LifetimeAction;
import com.microsoft.azure.keyvault.models.SecretProperties;
import com.microsoft.azure.keyvault.models.X509CertificateProperties;

/**
 * The update certificate policy request class.
 */
public final class UpdateCertificatePolicyRequest {
    private final String vaultBaseUrl;
    private final String certificateName;
    private final CertificatePolicy certificatePolicy;

    /**
     * The {@link UpdateCertificatePolicyRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String certificateName;

        // Optional parameters
        private CertificatePolicy policy;

        /**
         * The builder for constructing {@link UpdateCertificatePolicyRequest}
         * object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param certificateName
         *            The name of the certificate in the given vault.
         */
        public Builder(String vaultBaseUrl, String certificateName) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.certificateName = certificateName;
        }

        /**
         * Set the certificatePolicy value.
         * 
         * @param certificatePolicy
         *            The management policy for the certificate.
         * @return the Builder object itself.
         */
        public Builder withPolicy(CertificatePolicy certificatePolicy) {
            this.policy = certificatePolicy;
            return this;
        }

        /**
         * builds the {@link UpdateCertificatePolicyRequest} object.
         * 
         * @return the {@link UpdateCertificatePolicyRequest} object.
         */
        public UpdateCertificatePolicyRequest build() {
            return new UpdateCertificatePolicyRequest(this);
        }
    }

    private UpdateCertificatePolicyRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        certificateName = builder.certificateName;

        if (builder.policy != null) {
            certificatePolicy = new CertificatePolicy();
            if (builder.policy.attributes() != null) {
                certificatePolicy.withAttributes((CertificateAttributes) new CertificateAttributes()
                        .withEnabled(builder.policy.attributes().enabled())
                        .withExpires(builder.policy.attributes().expires())
                        .withNotBefore(builder.policy.attributes().notBefore()));
            }
            if (builder.policy.issuerReference() != null) {
                certificatePolicy
                        .withIssuerReference(new IssuerReference().withName(builder.policy.issuerReference().name()));
            }
            if (builder.policy.x509CertificateProperties() != null) {
                certificatePolicy.withX509CertificateProperties(new X509CertificateProperties()
                        .withValidityInMonths(builder.policy.x509CertificateProperties().validityInMonths())
                        .withSubjectAlternativeNames(
                                builder.policy.x509CertificateProperties().subjectAlternativeNames())
                        .withSubject(builder.policy.x509CertificateProperties().subject())
                        .withEkus(builder.policy.x509CertificateProperties().ekus())
                        .withKeyUsage(builder.policy.x509CertificateProperties().keyUsage()));
            }
            if (builder.policy.lifetimeActions() != null) {
                certificatePolicy.withLifetimeActions(new ArrayList<LifetimeAction>(builder.policy.lifetimeActions()));
            }
            if (builder.policy.keyProperties() != null) {
                certificatePolicy.withKeyProperties(
                        new KeyProperties().withExportable(builder.policy.keyProperties().exportable())
                                .withKeySize(builder.policy.keyProperties().keySize())
                                .withKeyType(builder.policy.keyProperties().keyType())
                                .withReuseKey(builder.policy.keyProperties().reuseKey()));
            }
            if (builder.policy.secretProperties() != null) {
                certificatePolicy.withSecretProperties(
                        new SecretProperties().withContentType(builder.policy.secretProperties().contentType()));
            }
        } else {
            certificatePolicy = null;
        }
    }

    /**
     * @return the vault base url
     */
    public String vaultBaseUrl() {
        return vaultBaseUrl;
    }

    /**
     * @return the certificate name
     */
    public String certificateName() {
        return certificateName;
    }

    /**
     * @return the certificate policy
     */
    public CertificatePolicy certificatePolicy() {
        return certificatePolicy;
    }
}
