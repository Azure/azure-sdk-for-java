/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * An item of additional information included in an Azure Batch error response.
 */
public class BatchErrorDetail {
    /**
     * Gets or sets an identifier specifying the meaning of the Value property.
     */
    private String key;

    /**
     * Gets or sets the additional information included with the error
     * response.
     */
    private String value;

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Set the key value.
     *
     * @param key the key value to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
