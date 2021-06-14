// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The TableServiceErrorOdataErrorMessage model. */
@Fluent
public final class TableServiceErrorOdataErrorMessage {
    /*
     * Language code of the error message.
     */
    @JsonProperty(value = "lang")
    private String lang;

    /*
     * The error message.
     */
    @JsonProperty(value = "value")
    private String value;

    /**
     * Get the lang property: Language code of the error message.
     *
     * @return the lang value.
     */
    public String getLang() {
        return this.lang;
    }

    /**
     * Set the lang property: Language code of the error message.
     *
     * @param lang the lang value to set.
     * @return the TableServiceErrorOdataErrorMessage object itself.
     */
    public TableServiceErrorOdataErrorMessage setLang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Get the value property: The error message.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value property: The error message.
     *
     * @param value the value value to set.
     * @return the TableServiceErrorOdataErrorMessage object itself.
     */
    public TableServiceErrorOdataErrorMessage setValue(String value) {
        this.value = value;
        return this;
    }
}
