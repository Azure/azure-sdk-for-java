// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.implementation.Base64Url;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a certificate with all of its properties.
 */
public class Certificate {

    /**
     * CER contents of x509 certificate.
     */
    @JsonProperty(value = "cer")
    private byte[] cer;

    /**
     * The key id.
     */
    @JsonProperty(value = "kid", access = JsonProperty.Access.WRITE_ONLY)
    private String keyId;

    /**
     * The secret id.
     */
    @JsonProperty(value = "sid", access = JsonProperty.Access.WRITE_ONLY)
    private String secretId;

    /**
     * The Certificate policy.
     */
    @JsonProperty("policy")
    private CertificatePolicy certificatePolicy;

    /**
     * The certificate properties
     */
    private CertificateProperties properties;

    /**
     * Create the certificate
     * @param name the name of the certificate.
     */
    public Certificate(String name) {
        properties = new CertificateProperties(name);
    }

    Certificate() {
        properties = new CertificateProperties();
    }

    /**
     * Get the certificate properties.
     * @return the certificate properties.
     */
    public CertificateProperties getProperties() {
        return properties;
    }

    /**
     * Set the certificate properties
     * @param properties the certificate properties
     * @throws NullPointerException if {@code certificateProperties} is null
     * @return the updated certificate object itself.
     */
    public Certificate setProperties(CertificateProperties properties) {
        Objects.requireNonNull(properties, "The certificate properties cannot be null");
        properties.name = this.properties.name;
        this.properties = properties;
        return this;
    }

    /**
     * Get the certificate identifier
     * @return the certificate identifier
     */
    public String getId() {
        return properties.getId();
    }

    /**
     * Get the certificate name
     * @return the certificate name
     */
    public String getName() {
        return properties.getName();
    }

    /**
     * Get the key id of the certificate
     * @return the key Id.
     */
    public String getKeyId() {
        return this.keyId;
    }

    /**
     * Get the secret id of the certificate
     * @return the key Id.
     */
    public String getSecretId() {
        return this.secretId;
    }

    /**
     * Get the cer content of the certificate
     * @return the cer content.
     */
    public byte[] getCer() {
        return ImplUtils.clone(cer);
    }

    /**
     * Get the certificate policy of the certificate
     * @return the cer content.
     */
    public CertificatePolicy getCertificatePolicy() {
        return this.certificatePolicy;
    }

    /**
     * Set the certificate policy of the certificate
     *
     * @param certificatePolicy the policy to set.
     * @return the certificate object itself.
     */
    public Certificate setCertificatePolicy(CertificatePolicy certificatePolicy) {
        this.certificatePolicy = certificatePolicy;
        return this;
    }

    @JsonProperty("attributes")
    @SuppressWarnings("unchecked")
    private void unpackBaseAttributes(Map<String, Object> attributes) {
        properties.unpackBaseAttributes(attributes);
    }

    @JsonProperty(value = "id")
    private void unpackId(String id) {
        properties.unpackId(id);
    }

    @JsonProperty(value = "tags")
    private void unpackTags(Map<String, String> tags) {
        properties.tags = tags;
    }

    @JsonProperty(value = "x5t")
    private void unpackX5t(Base64Url base64Url) {
        properties.x509Thumbprint = base64Url;
    }
}
