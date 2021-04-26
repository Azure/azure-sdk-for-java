// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.azure.security.jca.implementation.http.model;

import java.io.Serializable;

public class SecretBundle implements Serializable {

    private static final long serialVersionUID = 1L;
    private String contentType;
    private String value;

    public String getContentType() {
        return contentType;
    }

    public String getValue() {
        return value;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
