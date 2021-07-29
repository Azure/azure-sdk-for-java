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
     * get kid
     * @return kid
     */
    public String getKid() {
        return kid;
    }

    /**
     * set kid
     * @param kid kid
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * get signature
     * @return key value
     */
    public String getValue() {
        return value;
    }

    /**
     * set signature
     * @param value key value
     */
    public void setValue(String value) {
        this.value = value;
    }
}
