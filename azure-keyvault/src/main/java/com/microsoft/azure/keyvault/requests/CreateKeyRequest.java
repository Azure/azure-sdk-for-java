package com.microsoft.azure.keyvault.requests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.keyvault.models.KeyAttributes;

/**
 * The create key request class.
 */
public final class CreateKeyRequest {

    private final String vaultBaseUrl;
    private final String keyName;
    private final String keyType;
    private final Integer keySize;
    private final List<String> keyOperations;
    private final KeyAttributes keyAttributes;
    private final Map<String, String> tags;

    /**
     * The {@link CreateKeyRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String keyName;
        private final String keyType;

        // Optional parameters
        private Integer keySize;
        private List<String> keyOperations;
        private KeyAttributes attributes;
        private Map<String, String> tags;

        /**
         * The builder for constructing {@link CreateKeyRequest} object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net
         * @param keyName
         *            The name of the key in the given vault
         * @param keyType
         *            The type of key to create. Possible values include: 'EC',
         *            'RSA', 'RSA-HSM', 'oct'
         */
        public Builder(String vaultBaseUrl, String keyName, String keyType) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.keyName = keyName;
            this.keyType = keyType;
        }

        /**
         * Set the key size value.
         * 
         * @param size
         *            the size of the key.
         * @return the Builder object itself.
         */
        public Builder withKeySize(Integer size) {
            this.keySize = size;
            return this;
        }

        /**
         * Set the key operations value.
         * 
         * @param keyOperations
         *            the key operation list.
         * @return the Builder object itself.
         */
        public Builder withKeyOperations(List<String> keyOperations) {
            this.keyOperations = keyOperations;
            return this;
        }

        /**
         * Set the key attributes value.
         * 
         * @param attributes
         *            the key management attributes value to set.
         * @return the Builder object itself.
         */
        public Builder withAttributes(KeyAttributes attributes) {
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
         * builds the {@link CreateKeyRequest} object.
         * 
         * @return the {@link CreateKeyRequest} object.
         */
        public CreateKeyRequest build() {
            return new CreateKeyRequest(this);
        }
    }

    private CreateKeyRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        keyName = builder.keyName;
        keyType = builder.keyType;
        keySize = builder.keySize;

        if (builder.keyOperations != null) {
            keyOperations = new ArrayList<String>(builder.keyOperations);
        } else {
            keyOperations = null;
        }

        if (builder.attributes != null) {
            keyAttributes = (KeyAttributes) new KeyAttributes().withNotBefore(builder.attributes.notBefore())
                    .withEnabled(builder.attributes.enabled()).withExpires(builder.attributes.expires());
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
     * @return the key type
     */
    public String keyType() {
        return keyType;
    }

    /**
     * @return the key size
     */
    public Integer keySize() {
        return keySize;
    }

    /**
     * @return the key operations
     */
    public List<String> keyOperations() {
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