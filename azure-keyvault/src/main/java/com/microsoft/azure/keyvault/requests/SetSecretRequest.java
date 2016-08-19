package com.microsoft.azure.keyvault.requests;

import java.util.Collections;
import java.util.Map;

import com.microsoft.azure.keyvault.models.SecretAttributes;

/**
 * The set secret request class.
 */
public final class SetSecretRequest {
    private final String vaultBaseUrl;
    private final String secretName;
    private final String value;
    private final String contentType;
    private final SecretAttributes secretAttributes;
    private final Map<String, String> tags;

    /**
     * The {@link SetSecretRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String secretName;
        private final String value;

        // Optional parameters
        private String contentType;
        private SecretAttributes attributes;
        private Map<String, String> tags;

        /**
         * The builder for constructing {@link SetSecretRequest} object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net
         * @param secretName
         *            The name of the secret in the given vault
         * @param value
         *            The value of the secret
         */
        public Builder(String vaultBaseUrl, String secretName, String value) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.secretName = secretName;
            this.value = value;
        }

        /**
         * Set the content type value.
         * 
         * @param contentType
         *            Type of the secret value such as a password
         * @return the Builder object itself.
         */
        public Builder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Set the attributes value.
         * 
         * @param attributes
         *            The secret management attributes.
         * @return the Builder object itself.
         */
        public Builder withAttributes(SecretAttributes attributes) {
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
         * builds the {@link SetSecretRequest} object.
         * 
         * @return the {@link SetSecretRequest} object.
         */
        public SetSecretRequest build() {
            return new SetSecretRequest(this);
        }
    }

    private SetSecretRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        secretName = builder.secretName;
        value = builder.value;
        contentType = builder.contentType;

        if (builder.attributes != null) {
            secretAttributes = (SecretAttributes) new SecretAttributes().withNotBefore(builder.attributes.notBefore())
                    .withEnabled(builder.attributes.enabled()).withExpires(builder.attributes.expires());
        } else {
            secretAttributes = null;
        }

        if (builder.tags != null) {
            tags = Collections.unmodifiableMap(builder.tags);
        } else {
            tags = null;
        }
    }

    /**
     * @return the vaultBaseUrl
     */
    public String vaultBaseUrl() {
        return vaultBaseUrl;
    }

    /**
     * @return the secretName
     */
    public String secretName() {
        return secretName;
    }

    /**
     * @return the value
     */
    public String value() {
        return value;
    }

    /**
     * @return the contentType
     */
    public String contentType() {
        return contentType;
    }

    /**
     * @return the secretAttributes
     */
    public SecretAttributes secretAttributes() {
        return secretAttributes;
    }

    /**
     * @return the tags
     */
    public Map<String, String> tags() {
        return tags;
    }

}
