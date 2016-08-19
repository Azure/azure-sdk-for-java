package com.microsoft.azure.keyvault.requests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.keyvault.models.CertificateAttributes;

/**
 * The merge certificate request class.
 */
public final class MergeCertificateRequest {
    private final String vaultBaseUrl;
    private final String certificateName;
    private final List<byte[]> x509Certificates;
    private final CertificateAttributes certificateAttributes;
    private final Map<String, String> tags;

    /**
     * The {@link MergeCertificateRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String certificateName;
        private final List<byte[]> x509Certificates;

        // Optional parameters
        private CertificateAttributes attributes;
        private Map<String, String> tags;

        /**
         * The builder for constructing {@link MergeCertificateRequest} object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param certificateName
         *            The name of the certificate in the given vault.
         * @param x509Certificates
         *            The certificate or the certificate chain to merge.
         */
        public Builder(String vaultBaseUrl, String certificateName, List<byte[]> x509Certificates) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.certificateName = certificateName;
            this.x509Certificates = x509Certificates;
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
         * builds the {@link MergeCertificateRequest} object.
         * 
         * @return the {@link MergeCertificateRequest} object.
         */
        public MergeCertificateRequest build() {
            return new MergeCertificateRequest(this);
        }
    }

    private MergeCertificateRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        certificateName = builder.certificateName;
        x509Certificates = new ArrayList<byte[]>(builder.x509Certificates);

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
     * @return the x509 certificates
     */
    public List<byte[]> x509Certificates() {
        return x509Certificates;
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
