package com.microsoft.azure.keyvault.requests;

import java.util.Collections;
import java.util.Map;

import com.microsoft.azure.keyvault.SecretIdentifier;
import com.microsoft.azure.keyvault.models.SecretAttributes;

/**
 * The update secret request class.
 */
public final class UpdateSecretRequest {
    private final String vaultBaseUrl;
    private final String secretName;
    private final String secretVersion;
    private final String contentType;
    private final SecretAttributes secretAttributes;
    private final Map<String, String> tags;

    /**
     * The {@link UpdateSecretRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String secretName;

        // Optional parameters
        private String secretVersion;
        private String contentType;
        private SecretAttributes attributes;
        private Map<String, String> tags;

        /**
         * The builder for constructing {@link UpdateSecretRequest} object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param secretName
         *            The name of the secret in the given vault.
         */
        public Builder(String vaultBaseUrl, String secretName) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.secretName = secretName;
        }

        /**
         * The builder for constructing {@link UpdateSecretRequest} object.
         * 
         * @param secretId
         *            The secret identifier, e.g.
         *            https://{vault-name}.vault.azure.net/secrets/{secret-name}
         *            /{ secret-version}.
         */
        public Builder(String secretId) {
            SecretIdentifier id = new SecretIdentifier(secretId);
            this.vaultBaseUrl = id.vault();
            this.secretName = id.name();
            this.secretVersion = id.version();
        }

        /**
         * Sets the secret version.
         * @param version the secret version.
         * @return the Builder object itself.
         */
        public Builder withVersion(String version) {
            this.secretVersion = version;
            return this;
        }

        /**
         * Set the content type value.
         * 
         * @param contentType
         *            Type of the secret value such as a password.
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
         * builds the {@link UpdateSecretRequest} object.
         * 
         * @return the {@link UpdateSecretRequest} object.
         */
        public UpdateSecretRequest build() {
            return new UpdateSecretRequest(this);
        }
    }

    private UpdateSecretRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        secretName = builder.secretName;
        secretVersion = builder.secretVersion == null ? "" : builder.secretVersion;
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
     * @return the secretVersion
     */
    public String secretVersion() {
        return secretVersion;
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
