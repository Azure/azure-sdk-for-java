/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * An error message received in an Azure Batch error response.
 */
public class ErrorMessage {
    /**
     * Gets or sets the language code of the error message.
     */
    private String lang;

    /**
     * Gets or sets the text of the message.
     */
    private String value;

    /**
     * Get the lang value.
     *
     * @return the lang value
     */
    public String getLang() {
        return this.lang;
    }

    /**
     * Set the lang value.
     *
     * @param lang the lang value to set
     */
    public void setLang(String lang) {
        this.lang = lang;
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
