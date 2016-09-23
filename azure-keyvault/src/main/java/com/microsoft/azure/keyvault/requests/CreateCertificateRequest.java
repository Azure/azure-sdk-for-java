package com.microsoft.azure.keyvault.requests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.microsoft.azure.keyvault.models.Attributes;
import com.microsoft.azure.keyvault.models.CertificateAttributes;
import com.microsoft.azure.keyvault.models.CertificatePolicy;
import com.microsoft.azure.keyvault.models.IssuerParameters;
import com.microsoft.azure.keyvault.models.KeyProperties;
import com.microsoft.azure.keyvault.models.LifetimeAction;
import com.microsoft.azure.keyvault.models.SecretProperties;
import com.microsoft.azure.keyvault.models.X509CertificateProperties;

/**
 * The create certificate request class.
 */
public final class CreateCertificateRequest {
    private final String vaultBaseUrl;
    private final String certificateName;
    private final CertificatePolicy certificatePolicy;
    private final CertificateAttributes certificateAttributes;
    private final Map<String, String> tags;

    /**
     * the {@link CreateCertificateRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String certificateName;

        // Optional parameters
        private CertificatePolicy policy;
        private CertificateAttributes attributes;
        private Map<String, String> tags;

        /**
         * The builder for constructing {@link CreateCertificateRequest} object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param certificateName
         *            The name of the certificate in the given vault
         */
        public Builder(String vaultBaseUrl, String certificateName) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.certificateName = certificateName;
        }

        /**
         * Set the certificatePolicy value. Mandatory if sending the create
         * request for the first time.
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
         * Set the attributes value.
         * 
         * @param attributes
         *            The attributes of the certificate.
         * @return the Builder object itself.
         */
        public Builder withAttributes(Attributes attributes) {
            this.attributes = (CertificateAttributes) attributes;
            return this;
        }

        /**
         * Set the tags value.
         * 
         * @param tags
         *            Application-specific metadata in the form of key-value
         *            pairs.
         * @return the Builder object itself.
         */
        public Builder withTags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        /**
         * builds the {@link CreateCertificateRequest} object.
         * 
         * @return the {@link CreateCertificateRequest} object.
         */
        public CreateCertificateRequest build() {
            return new CreateCertificateRequest(this);
        }
    }

    private CreateCertificateRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        certificateName = builder.certificateName;

        if (builder.attributes != null) {
            certificateAttributes = (CertificateAttributes) new CertificateAttributes()
                    .withNotBefore(builder.attributes.notBefore()).withEnabled(builder.attributes.enabled())
                    .withExpires(builder.attributes.expires());
        } else {
            certificateAttributes = null;
        }

        if (builder.policy != null) {
            certificatePolicy = new CertificatePolicy();
            if (builder.policy.attributes() != null) {
                certificatePolicy.withAttributes((CertificateAttributes) new CertificateAttributes()
                        .withEnabled(builder.policy.attributes().enabled())
                        .withExpires(builder.policy.attributes().expires())
                        .withNotBefore(builder.policy.attributes().notBefore()));
            }
            if (builder.policy.issuerParameters() != null) {
                certificatePolicy
                        .withIssuerParameters(new IssuerParameters().withName(builder.policy.issuerParameters().name()));
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

        if (builder.tags != null) {
            tags = Collections.unmodifiableMap(builder.tags);
        } else {
            tags = null;
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

    /**
     * @return the certificate attributes
     */
    public CertificateAttributes certificateAttributes() {
        return certificateAttributes;
    }

    /**
     * @return the tags
     */
    public Map<String, String> tags() {
        return tags;
    }
}
