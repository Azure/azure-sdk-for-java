/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The WebWebGrouping model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = WebWebGrouping.class)
@JsonTypeName("Web/WebGrouping")
public class WebWebGrouping {
    /**
     * The webPages property.
     */
    @JsonProperty(value = "webPages", required = true)
    private List<WebPage> webPages;

    /**
     * Get the webPages value.
     *
     * @return the webPages value
     */
    public List<WebPage> webPages() {
        return this.webPages;
    }

    /**
     * Set the webPages value.
     *
     * @param webPages the webPages value to set
     * @return the WebWebGrouping object itself.
     */
    public WebWebGrouping withWebPages(List<WebPage> webPages) {
        this.webPages = webPages;
        return this;
    }

}
