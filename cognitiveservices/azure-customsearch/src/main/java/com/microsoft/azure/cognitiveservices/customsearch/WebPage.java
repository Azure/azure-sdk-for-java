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
 * Defines a webpage that is relevant to the query.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("WebPage")
public class WebPage extends CreativeWork {
    /**
     * The display URL of the webpage. The URL is meant for display purposes
     * only and is not well formed.
     */
    @JsonProperty(value = "displayUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String displayUrl;

    /**
     * A snippet of text from the webpage that describes its contents.
     */
    @JsonProperty(value = "snippet", access = JsonProperty.Access.WRITE_ONLY)
    private String snippet;

    /**
     * A list of links to related content that Bing found in the website that
     * contains this webpage. The Webpage object in this context includes only
     * the name, url, urlPingSuffix, and snippet fields.
     */
    @JsonProperty(value = "deepLinks", access = JsonProperty.Access.WRITE_ONLY)
    private List<WebPage> deepLinks;

    /**
     * The last time that Bing crawled the webpage. The date is in the form,
     * YYYY-MM-DDTHH:MM:SS. For example, 2015-04-13T05:23:39.
     */
    @JsonProperty(value = "dateLastCrawled", access = JsonProperty.Access.WRITE_ONLY)
    private String dateLastCrawled;

    /**
     * A list of search tags that the webpage owner specified on the webpage.
     * The API returns only indexed search tags. The name field of the MetaTag
     * object contains the indexed search tag. Search tags begin with search.*
     * (for example, search.assetId). The content field contains the tag's
     * value.
     */
    @JsonProperty(value = "searchTags", access = JsonProperty.Access.WRITE_ONLY)
    private List<WebMetaTag> searchTags;

    /**
     * Get the displayUrl value.
     *
     * @return the displayUrl value
     */
    public String displayUrl() {
        return this.displayUrl;
    }

    /**
     * Get the snippet value.
     *
     * @return the snippet value
     */
    public String snippet() {
        return this.snippet;
    }

    /**
     * Get the deepLinks value.
     *
     * @return the deepLinks value
     */
    public List<WebPage> deepLinks() {
        return this.deepLinks;
    }

    /**
     * Get the dateLastCrawled value.
     *
     * @return the dateLastCrawled value
     */
    public String dateLastCrawled() {
        return this.dateLastCrawled;
    }

    /**
     * Get the searchTags value.
     *
     * @return the searchTags value
     */
    public List<WebMetaTag> searchTags() {
        return this.searchTags;
    }

}
