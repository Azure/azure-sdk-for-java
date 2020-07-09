// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.CertificateKeyType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Properties of the key pair backing a certificate.
 */
class KeyProperties {

    KeyProperties(CertificatePolicy certificatePolicy) {
        this.curve = certificatePolicy.getKeyCurveName();
        this.exportable = certificatePolicy.isExportable();
        this.keySize = certificatePolicy.getKeySize();
        this.keyType = certificatePolicy.getKeyType();
        this.reuseKey = certificatePolicy.isKeyReusable();
    }


    /**
     * Indicates if the private key can be exported.
     */
    @JsonProperty(value = "exportable")
    private Boolean exportable;

    /**
     * The type of key pair to be used for the certificate. Possible values
     * include: 'EC', 'EC-HSM', 'RSA', 'RSA-HSM', 'oct'.
     */
    @JsonProperty(value = "kty")
    private CertificateKeyType keyType;

    /**
     * The key size in bits. For example: 2048, 3072, or 4096 for RSA.
     */
    @JsonProperty(value = "key_size")
    private Integer keySize;

    /**
     * Indicates if the same key pair will be used on certificate renewal.
     */
    @JsonProperty(value = "reuse_key")
    private Boolean reuseKey;

    /**
     * Elliptic curve name. For valid values, see JsonWebKeyCurveName. Possible
     * values include: 'P-256', 'P-384', 'P-521', 'P-256K'.
     */
    @JsonProperty(value = "crv")
    private CertificateKeyCurveName curve;

    /**
     * Get the exportable value.
     *
     * @return the exportable value
     */
    Boolean exportable() {
        return this.exportable;
    }

    /**
     * Set the exportable value.
     *
     * @param exportable the exportable value to set
     * @return the KeyProperties object itself.
     */
    KeyProperties exportable(Boolean exportable) {
        this.exportable = exportable;
        return this;
    }

    /**
     * Get the keyType value.
     *
     * @return the keyType value
     */
    CertificateKeyType keyType() {
        return this.keyType;
    }

    /**
     * Set the keyType value.
     *
     * @param keyType the keyType value to set
     * @return the KeyProperties object itself.
     */
    KeyProperties keyType(CertificateKeyType keyType) {
        this.keyType = keyType;
        return this;
    }

    /**
     * Get the keySize value.
     *
     * @return the keySize value
     */
    Integer keySize() {
        return this.keySize;
    }

    /**
     * Set the keySize value.
     *
     * @param keySize the keySize value to set
     * @return the KeyProperties object itself.
     */
    KeyProperties keySize(Integer keySize) {
        this.keySize = keySize;
        return this;
    }

    /**
     * Get the reuseKey value.
     *
     * @return the reuseKey value
     */
    Boolean reuseKey() {
        return this.reuseKey;
    }

    /**
     * Set the reuseKey value.
     *
     * @param reuseKey the reuseKey value to set
     * @return the KeyProperties object itself.
     */
    KeyProperties reuseKey(Boolean reuseKey) {
        this.reuseKey = reuseKey;
        return this;
    }

    /**
     * Get the curve value.
     *
     * @return the curve value
     */
    CertificateKeyCurveName curve() {
        return this.curve;
    }

    /**
     * Set the curve value.
     *
     * @param curve the curve value to set
     * @return the KeyProperties object itself.
     */
    KeyProperties curve(CertificateKeyCurveName curve) {
        this.curve = curve;
        return this;
    }
}
