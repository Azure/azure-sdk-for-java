package com.microsoft.azure.keyvault.requests;

import java.util.Collections;
import java.util.Map;

import com.microsoft.azure.keyvault.models.CertificateAttributes;

/**
 * The update certificate request class.
 */
public final class UpdateCertificateRequest {
    private final String vaultBaseUrl;
    private final String certificateName;
    private final String certificateVersion;
    private final CertificateAttributes certificateAttributes;
    private final Map<String, String> tags;

    /**
     * The {@link UpdateCertificateRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String certificateName;

        // Optional parameters
        private String certificateVersion;
        private CertificateAttributes attributes;
        private Map<String, String> tags;

        /**
         * The builder for constructing {@link UpdateCertificateRequest} object.
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
         * Set the certificate version value.
         * 
         * @param version
         *            The version of the certificate.
         * @return the Builder object itself.
         */
        public Builder withCertificateVersion(String version) {
            this.certificateVersion = version;
            return this;
        }

        /**
         * Set the attributes value.
         * 
         * @param attributes
         *            The attributes of the certificate.
         * @return the Builder object itself.
         */
        public Builder withAttributes(CertificateAttributes attributes) {
            this.attributes = attributes;
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
         * builds the {@link UpdateCertificateRequest} object.
         * 
         * @return the {@link UpdateCertificateRequest} object.
         */
        public UpdateCertificateRequest build() {
            return new UpdateCertificateRequest(this);
        }
    }

    private UpdateCertificateRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        certificateName = builder.certificateName;
        certificateVersion = builder.certificateVersion == null ? "" : builder.certificateVersion;

        if (builder.attributes != null) {
            certificateAttributes = (CertificateAttributes) new CertificateAttributes()
                    .withNotBefore(builder.attributes.notBefore()).withEnabled(builder.attributes.enabled())
                    .withExpires(builder.attributes.expires());
        } else {
            certificateAttributes = null;
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
     * @return the certificate version
     */
    public String certificateVersion() {
        return certificateVersion;
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
