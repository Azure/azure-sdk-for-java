// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.security.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;

public class EcKeyCreateOptions extends KeyCreateOptions {

    /**
     * The Ec key curve.
     */
    private KeyCurveName curve;

    /**
     * The hsm indicator for the key.
     */
    private boolean hsm;

    /**
     * Creates a EcKeyCreateOptions with {@code name} as name of the Ec key.
     * @param name The name of the Ec key.
     */
    public EcKeyCreateOptions(String name) {
        super.name = name;
        this.keyType = KeyType.EC;
    }

    /**
     * Get the curve.
     *
     * @return the curve.
     */
    public KeyCurveName getCurve() {
        return this.curve;
    }

    /**
     * Set the curve.
     *
     * @param curve The curve to set
     * @return the EcKeyCreateOptions object itself.
     */
    public EcKeyCreateOptions setCurve(KeyCurveName curve) {
        this.curve = curve;
        return this;
    }

    /**
     * Set the key operations value.
     *
     * @param keyOperations The key operations value to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public EcKeyCreateOptions setKeyOperations(KeyOperation... keyOperations) {
        this.keyOperations = Arrays.asList(keyOperations);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime getNotBefore} UTC time.
     *
     * @param notBefore The getNotBefore UTC time to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public EcKeyCreateOptions setNotBefore(OffsetDateTime notBefore) {
        super.setNotBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime getExpires} UTC time.
     *
     * @param expires The expiry time to set for the key.
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public EcKeyCreateOptions setExpires(OffsetDateTime expires) {
        super.setExpires(expires);
        return this;
    }

    /**
     * Set the getTags to be associated with the key.
     *
     * @param tags The getTags to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public EcKeyCreateOptions setTags(Map<String, String> tags) {
        super.setTags(tags);
        return this;
    }

    /**
     * Set the setEnabled value.
     *
     * @param enabled The setEnabled value to set
     * @return the EcKeyCreateOptions object itself.
     */
    public KeyCreateOptions setEnabled(Boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    /**
     * Set whether the key being getCreated is of hsm type or not.
     * @param hsm The hsm value to set.
     * @return the EcKeyCreateOptions object itself.
     */
    public EcKeyCreateOptions setHsm(Boolean hsm) {
        this.hsm = hsm;
        this.keyType = hsm ? KeyType.EC_HSM : KeyType.EC;
        return this;
    }

    /**
     * Get the hsm value of the key being getCreated.
     * @return the hsm value.
     */
    public Boolean isHsm() {
        return this.hsm;
    }
}
