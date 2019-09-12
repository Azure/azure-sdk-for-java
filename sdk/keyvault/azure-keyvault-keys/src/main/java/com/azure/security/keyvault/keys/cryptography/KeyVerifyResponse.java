// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.fasterxml.jackson.annotation.JsonProperty;

class KeyVerifyResponse {

    /**
     * True if the signature is verified, otherwise false.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean value;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public Boolean getValue() {
        return this.value;
    }
}
