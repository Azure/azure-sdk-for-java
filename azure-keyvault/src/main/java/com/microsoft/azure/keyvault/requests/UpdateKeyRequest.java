package com.microsoft.azure.keyvault.requests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.models.Attributes;
import com.microsoft.azure.keyvault.models.KeyAttributes;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;

/**
 * The key update request class.
 */
public final class UpdateKeyRequest {

    private final String vaultBaseUrl;
    private final String keyName;
    private final String keyVersion;
    private final List<JsonWebKeyOperation> keyOperations;
    private final KeyAttributes keyAttributes;
    private final Map<String, String> tags;

    /**
     * The {@link UpdateKeyRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String keyName;

        // Optional parameters
        private String keyVersion;
        private List<JsonWebKeyOperation> keyOperations;
        private KeyAttributes attributes;
        private Map<String, String> tags;

        /**
         * The builder for constructing {@link UpdateKeyRequest} object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param keyName
         *            The name of the key in the given vault.
         */
        public Builder(String vaultBaseUrl, String keyName) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.keyName = keyName;
        }

        /**
         * The builder for constructing {@link UpdateKeyRequest} object.
         * 
         * @param keyIdentifier
         *            The key identifier, e.g.
         *            https://{vault-name}.vault.azure.net/keys/{key-name}/{key-
         *            version}.
         */
        public Builder(String keyIdentifier) {
            KeyIdentifier id = new KeyIdentifier(keyIdentifier);
            this.vaultBaseUrl = id.vault();
            this.keyName = id.name();
            this.keyVersion = id.version();
        }

        /**
         * Set the key version value.
         * 
         * @param keyVersion
         *            the key version.
         * @return the Builder object itself.
         */
        public Builder withVersion(String keyVersion) {
            this.keyVersion = keyVersion;
            return this;
        }

        /**
         * Set the key operations value.
         * 
         * @param keyOperations
         *            the key operation list
         * @return the Builder object itself.
         */
        public Builder withKeyOperations(List<JsonWebKeyOperation> keyOperations) {
            this.keyOperations = keyOperations;
            return this;
        }

        /**
         * Set the key attributes value.
         * 
         * @param attributes
         *            the key management attributes value to set
         * @return the Builder object itself.
         */
        public Builder withAttributes(Attributes attributes) {
            this.attributes = (KeyAttributes) attributes;
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
         * builds the {@link UpdateKeyRequest} object.
         * 
         * @return the {@link UpdateKeyRequest} object.
         */
        public UpdateKeyRequest build() {
            return new UpdateKeyRequest(this);
        }
    }

    private UpdateKeyRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        keyName = builder.keyName;
        keyVersion = builder.keyVersion == null ? "" : builder.keyVersion;

        if (builder.keyOperations != null) {
            keyOperations = new ArrayList<JsonWebKeyOperation>(builder.keyOperations);
        } else {
            keyOperations = null;
        }

        if (builder.attributes != null) {
            keyAttributes = (KeyAttributes) new KeyAttributes().withEnabled(builder.attributes.enabled())
                    .withExpires(builder.attributes.expires()).withNotBefore(builder.attributes.notBefore());
        } else {
            keyAttributes = null;
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
     * @return the key name
     */
    public String keyName() {
        return keyName;
    }

    /**
     * @return the key version
     */
    public String keyVersion() {
        return keyVersion;
    }

    /**
     * @return the key operations
     */
    public List<JsonWebKeyOperation> keyOperations() {
        return keyOperations;
    }

    /**
     * @return the key attributes
     */
    public KeyAttributes keyAttributes() {
        return keyAttributes;
    }

    /**
     * @return the tags
     */
    public Map<String, String> tags() {
        return tags;
    }
}
