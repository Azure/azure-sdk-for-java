// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.implementation.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class CertificatePolicy implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @JsonProperty("key_props")
    private KeyProperties keyProperties;

    public KeyProperties getKeyProperties() {
        return keyProperties;
    }

    public void setKeyProperties(KeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }
}
