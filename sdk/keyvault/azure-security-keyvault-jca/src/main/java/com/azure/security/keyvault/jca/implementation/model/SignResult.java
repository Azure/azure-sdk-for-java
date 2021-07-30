// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

/**
 * Result of sign certificate
 */
public class SignResult {

    private String kid;

    private String value;

    /**
     * get keyId
     * @return keyId
     */
    public String getKid() {
        return kid;
    }

    /**
     * set keyId
     * @param kid keyId
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * get key value
     * @return key value
     */
    public String getValue() {
        return value;
    }

    /**
     * set key value
     * @param value key value
     */
    public void setValue(String value) {
        this.value = value;
    }
}
