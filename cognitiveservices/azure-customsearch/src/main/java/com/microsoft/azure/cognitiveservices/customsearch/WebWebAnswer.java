/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.customsearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a list of relevant webpage links.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("Web/WebAnswer")
public class WebWebAnswer extends SearchResultsAnswer {
    /**
     * A list of webpages that are relevant to the query.
     */
    @JsonProperty(value = "value", required = true)
    private List<WebPage> value;

    /**
     * A Boolean value that indicates whether the response excluded some
     * results from the answer. If Bing excluded some results, the value is
     * true.
     */
    @JsonProperty(value = "someResultsRemoved", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean someResultsRemoved;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<WebPage> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the WebWebAnswer object itself.
     */
    public WebWebAnswer withValue(List<WebPage> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the someResultsRemoved value.
     *
     * @return the someResultsRemoved value
     */
    public Boolean someResultsRemoved() {
        return this.someResultsRemoved;
    }

}
