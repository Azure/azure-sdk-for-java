package com.microsoft.azure.keyvault.requests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.microsoft.azure.keyvault.models.JsonWebKey;
import com.microsoft.azure.keyvault.models.KeyAttributes;

/**
 * The import key request class.
 */
public final class ImportKeyRequest {

    private final String vaultBaseUrl;
    private final String keyName;
    private final JsonWebKey key;
    private final boolean isHsm;
    private final KeyAttributes keyAttributes;
    private final Map<String, String> tags;

    /**
     * The {@link ImportKeyRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String keyName;
        private final JsonWebKey key;

        // Optional parameters
        private boolean isHsm;
        private KeyAttributes attributes;
        private Map<String, String> tags;

        /**
         * The builder for constructing {@link ImportKeyRequest} object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param keyName
         *            The name of the key in the given vault.
         * @param key
         *            The Json web key.
         */
        public Builder(String vaultBaseUrl, String keyName, JsonWebKey key) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.keyName = keyName;
            this.key = key;
        }

        /**
         * Set the isHsm to true if the key is imported as a hardware key to
         * HSM, false otherwise.
         * 
         * @param isHsm
         *            True, if the key is hardware key to be stored in HSM.
         *            false otherwise
         * @return the Builder object itself.
         */
        public Builder withHsm(boolean isHsm) {
            this.isHsm = isHsm;
            return this;
        }

        /**
         * Set the key attributes value.
         * 
         * @param attributes
         *            the key management attributes value to set
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
         * builds the {@link ImportKeyRequest} object.
         * 
         * @return the {@link ImportKeyRequest} object.
         */
        public ImportKeyRequest build() {
            return new ImportKeyRequest(this);
        }
    }

    private ImportKeyRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        keyName = builder.keyName;
        isHsm = builder.isHsm;

        if (builder.key != null) {
            key = new JsonWebKey().withKty(builder.key.kty()).withN(builder.key.n()).withE(builder.key.e())
                    .withD(builder.key.d()).withP(builder.key.p()).withQ(builder.key.q()).withDp(builder.key.dp())
                    .withDq(builder.key.dq()).withQi(builder.key.qi()).withK(builder.key.k()).withT(builder.key.t());
            if (builder.key.keyOps() != null) {
                key.withKeyOps(new ArrayList<String>(builder.key.keyOps()));
            }
        } else {
            key = null;
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
     * @return the key
     */
    public JsonWebKey key() {
        return key;
    }

    /**
     * @return the isHsm
     */
    public boolean isHsm() {
        return isHsm;
    }

    /**
     * @return the key attribute
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
