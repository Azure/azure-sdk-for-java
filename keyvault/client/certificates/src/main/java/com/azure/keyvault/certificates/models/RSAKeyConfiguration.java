// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.certificates.models;

import com.azure.keyvault.certificates.models.webkey.JsonWebKeyType;
import java.util.Arrays;
import java.util.List;

public class RSAKeyConfiguration extends KeyConfiguration {

    private boolean hsm;

    public RSAKeyConfiguration(){
        this.keyType = JsonWebKeyType.RSA;
    }

    /**
     * Get the keyUsage value.
     *
     * @return the keyUsage value
     */
    public List<KeyUsageType> keyUsage() {
          return this.keyUsage;
      }

    /**
     * Set the keyUsage value.
     *
     * @param keyUsage the keyUsage value to set
     * @return the X509CertificateProperties object itself.
     */
     public RSAKeyConfiguration keyUsage(KeyUsageType ... keyUsage) {
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
     public RSAKeyConfiguration enhancedKeyUsage(List<String> ekus) {
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
    public RSAKeyConfiguration exportable(Boolean exportable) {
        this.exportable = exportable;
        return this;
    }

    /**
     * Get the keyType value.
     *
     * @return the keyType value
     */
    public JsonWebKeyType keyType() {
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
     * Set the keySize value.
     *
     * @param keySize the keySize value to set
     * @return the KeyProperties object itself.
     */
    public RSAKeyConfiguration keySize(Integer keySize) {
        this.keySize = keySize;
        return this;
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
     * Set the keyUsage value.
     *
     * @param keyUsage the keyUsage value to set
     * @return the X509CertificateProperties object itself.
     */
    public RSAKeyConfiguration keyUsage(List<KeyUsageType> keyUsage) {
        this.keyUsage = keyUsage;
        return this;
    }

    /**
     * Set the reuseKey value.
     *
     * @param reuseKey the reuseKey value to set
     * @return the KeyProperties object itself.
     */
    public RSAKeyConfiguration reuseKey(Boolean reuseKey) {
        this.reuseKey = reuseKey;
        return this;
    }

    public boolean hsm() {
        return this.hsm;
    }

    public RSAKeyConfiguration hsm(boolean hsm){
        this.hsm = hsm;
        this.keyType = hsm ? JsonWebKeyType.RSA_HSM : JsonWebKeyType.RSA;
        return this;
    }
}
