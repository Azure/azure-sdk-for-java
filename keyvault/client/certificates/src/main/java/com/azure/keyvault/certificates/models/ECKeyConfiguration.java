package com.azure.keyvault.certificates.models;

import com.azure.keyvault.certificates.models.webkey.JsonWebKeyCurveName;
import com.azure.keyvault.certificates.models.webkey.JsonWebKeyType;
import java.util.List;

public class ECKeyConfiguration extends KeyConfiguration {

    private boolean hsm;

    public ECKeyConfiguration(){
        this.keyType = JsonWebKeyType.EC;
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
    public ECKeyConfiguration keyUsage(List<KeyUsageType> keyUsage) {
        this.keyUsage = keyUsage;
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
    public ECKeyConfiguration enhancedKeyUsage(List<String> ekus) {
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
    public ECKeyConfiguration exportable(Boolean exportable) {
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
     * Set the keyType value.
     *
     * @param keyType the keyType value to set
     * @return the KeyProperties object itself.
     */
    public ECKeyConfiguration keyType(JsonWebKeyType keyType) {
        this.keyType = keyType;
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
     * Set the reuseKey value.
     *
     * @param reuseKey the reuseKey value to set
     * @return the KeyProperties object itself.
     */
    public ECKeyConfiguration reuseKey(Boolean reuseKey) {
        this.reuseKey = reuseKey;
        return this;
    }

    /**
     * Get the curve value.
     *
     * @return the curve value
     */
    public JsonWebKeyCurveName curve() {
        return this.curve;
    }

    /**
     * Set the curve value.
     *
     * @param curve the curve value to set
     * @return the KeyProperties object itself.
     */
    public ECKeyConfiguration curve(JsonWebKeyCurveName curve) {
        this.curve = curve;
        return this;
    }

    public boolean hsm() {
        return this.hsm;
    }

    public ECKeyConfiguration hsm(boolean hsm){
        this.hsm = hsm;
        this.keyType = hsm ? JsonWebKeyType.EC_HSM : JsonWebKeyType.EC;
        return this;
    }

}
