// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.storage.blob.models.ClearRange;
import com.azure.storage.blob.models.PageRange;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * the list of pages.
 */
@JacksonXmlRootElement(localName = "PageList")
public final class PageList {
    /*
     * The pageRange property.
     */
    @JsonProperty("PageRange")
    private List<PageRange> pageRange = new ArrayList<>();

    /*
     * The clearRange property.
     */
    @JsonProperty("ClearRange")
    private List<ClearRange> clearRange = new ArrayList<>();

    /**
     * Get the pageRange property: The pageRange property.
     *
     * @return the pageRange value.
     */
    public List<PageRange> pageRange() {
        return this.pageRange;
    }

    /**
     * Set the pageRange property: The pageRange property.
     *
     * @param pageRange the pageRange value to set.
     * @return the PageList object itself.
     */
    public PageList pageRange(List<PageRange> pageRange) {
        this.pageRange = pageRange;
        return this;
    }

    /**
     * Get the clearRange property: The clearRange property.
     *
     * @return the clearRange value.
     */
    public List<ClearRange> clearRange() {
        return this.clearRange;
    }

    /**
     * Set the clearRange property: The clearRange property.
     *
     * @param clearRange the clearRange value to set.
     * @return the PageList object itself.
     */
    public PageList clearRange(List<ClearRange> clearRange) {
        this.clearRange = clearRange;
        return this;
    }
}
