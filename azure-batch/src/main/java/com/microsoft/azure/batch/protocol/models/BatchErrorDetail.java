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
     * An identifier specifying the meaning of the Value property.
     */
    private String key;

    /**
     * The additional information included with the error response.
     */
    private String value;

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public String key() {
        return this.key;
    }

    /**
     * Set the key value.
     *
     * @param key the key value to set
     * @return the BatchErrorDetail object itself.
     */
    public BatchErrorDetail withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the BatchErrorDetail object itself.
     */
    public BatchErrorDetail withValue(String value) {
        this.value = value;
        return this;
    }

}
