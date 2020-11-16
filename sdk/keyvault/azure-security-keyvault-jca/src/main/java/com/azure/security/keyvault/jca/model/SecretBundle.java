// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.model;

import java.io.Serializable;

/**
 * The SecretBundle REST model.
 */
public class SecretBundle implements Serializable {
    
    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Stores the content type.
     */
    private String contentType;

    /**
     * Stores the value.
     */
    private String value;

    /**
     * Get the content type.
     * 
     * @return the content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get the value.
     *
     * @return the value.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Set the content type.
     * 
     * @param contentType the content type.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Set the value.
     *
     * @param value the value.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
