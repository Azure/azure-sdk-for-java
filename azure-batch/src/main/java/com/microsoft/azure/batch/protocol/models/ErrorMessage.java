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
     * The language code of the error message.
     */
    private String lang;

    /**
     * The text of the message.
     */
    private String value;

    /**
     * Get the lang value.
     *
     * @return the lang value
     */
    public String lang() {
        return this.lang;
    }

    /**
     * Set the lang value.
     *
     * @param lang the lang value to set
     * @return the ErrorMessage object itself.
     */
    public ErrorMessage withLang(String lang) {
        this.lang = lang;
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
     * @return the ErrorMessage object itself.
     */
    public ErrorMessage withValue(String value) {
        this.value = value;
        return this;
    }

}
