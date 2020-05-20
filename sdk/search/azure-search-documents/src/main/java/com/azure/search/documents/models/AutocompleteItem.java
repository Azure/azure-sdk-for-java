// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The result of Autocomplete requests.
 */
@Fluent
public final class AutocompleteItem {
    /*
     * The completed term.
     */
    @JsonProperty(value = "text", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private String text;

    /*
     * The query along with the completed term.
     */
    @JsonProperty(value = "queryPlusText", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private String queryPlusText;

    /**
     * Get the text property: The completed term.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the queryPlusText property: The query along with the completed term.
     *
     * @return the queryPlusText value.
     */
    public String getQueryPlusText() {
        return this.queryPlusText;
    }
}
