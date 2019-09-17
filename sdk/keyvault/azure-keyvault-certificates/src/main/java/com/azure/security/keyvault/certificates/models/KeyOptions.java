// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.models.webkey.KeyCurveName;
import com.azure.security.keyvault.certificates.models.webkey.KeyType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the key options configuration in certificate policy.
 */
public class KeyOptions {

    /**
     * Indicates if the private key can be exported.
     */
    @JsonProperty(value = "exportable")
    Boolean exportable;

    /**
     * The type of key pair to be used for the certificate. Possible values
     * include: 'EC', 'EC-HSM', 'RSA', 'RSA-HSM', 'oct'.
     */
    @JsonProperty(value = "kty")
    KeyType keyType;

    /**
     * The key size in bits. For example: 2048, 3072, or 4096 for RSA.
     */
    @JsonProperty(value = "key_size")
    Integer keySize;

    /**
     * Indicates if the same key pair will be used on certificate renewal.
     */
    @JsonProperty(value = "reuse_key")
    Boolean reuseKey;

    /**
     * Elliptic curve name. For valid values, see KeyCurveName. Possible
     * values include: 'P-256', 'P-384', 'P-521', 'P-256K'.
     */
    @JsonProperty(value = "crv")
    KeyCurveName curve;

    /**
     * List of key usages.
     */
    @JsonProperty(value = "key_usage")
    List<KeyUsageType> keyUsage;

    /**
     * The enhanced key usage.
     */
    @JsonProperty(value = "ekus")
    List<String> enhancedKeyUsage;

    public KeyOptions(KeyType keyType) {
        this.keyType = keyType;
    }

    KeyOptions() { }

    /**
     * Get the keyUsage value.
     *
     * @return the keyUsage value
     */
    public List<KeyUsageType> keyUsage() {
        return this.keyUsage;
    }

    /**
     * Set the key usage.
     *
     * @param keyUsage the keyUsage value to set
     * @return the X509CertificateProperties object itself.
     */
    public KeyOptions keyUsage(KeyUsageType... keyUsage) {
        this.keyUsage = Arrays.asList(keyUsage);
        return this;
    }

    /**
     * Get the enhanced key usage value.
     *
     * @return the enhanced key usage value
     */
    public List<String> ekus() {
        return this.enhancedKeyUsage;
    }

    /**
     * Set the enhanced key usage value.
     *
     * @param ekus the ekus value to set
     * @return the X509CertificateProperties object itself.
     */
    public KeyOptions enhancedKeyUsage(List<String> ekus) {
        this.enhancedKeyUsage = ekus;
        return this;
    }

    /**
     * Get the exportable value.
     *
     * @return the exportable value
     */
    public Boolean exportable() {
        return this.exportable;
    }

    /**
     * Set the exportable value.
     *
     * @param exportable the exportable value to set
     * @return the KeyProperties object itself.
     */
    public KeyOptions exportable(Boolean exportable) {
        this.exportable = exportable;
        return this;
    }

    /**
     * Get the keyType value.
     *
     * @return the keyType value
     */
    public KeyType keyType() {
        return this.keyType;
    }

    /**
     * Get the keySize value.
     *
     * @return the keySize value
     */
    public Integer keySize() {
        return this.keySize;
    }

    /**
     * Get the reuseKey value.
     *
     * @return the reuseKey value
     */
    public Boolean reuseKey() {
        return this.reuseKey;
    }

    /**
     * Set the reuseKey value.
     *
     * @param reuseKey the reuseKey value to set
     * @return the KeyProperties object itself.
     */
    public KeyOptions reuseKey(Boolean reuseKey) {
        this.reuseKey = reuseKey;
        return this;
    }

    /**
     * Get the curve value.
     *
     * @return the curve value
     */
    public KeyCurveName curve() {
        return this.curve;
    }

    /*
     * Set the key usage.
     */
    KeyOptions keyUsage(List<KeyUsageType> keyUsage) {
        this.keyUsage = keyUsage;
        return this;
    }

    /*
     * Set the key size.
     */
    KeyOptions keySize(Integer keySize) {
        this.keySize = keySize;
        return this;
    }

    /*
     * Set the key curve.
     */
    KeyOptions curve(KeyCurveName curve) {
        this.curve = curve;
        return this;
    }

    /*
     * Set the key type.
     */
    KeyOptions keyType(KeyType keyType) {
        this.keyType = keyType;
        return this;
    }
}
