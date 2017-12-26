/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderator.TermsData;
import com.microsoft.azure.cognitiveservices.contentmoderator.TermsPaging;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Terms properties.
 */
public class TermsInner {
    /**
     * Term data details.
     */
    @JsonProperty(value = "Data")
    private TermsData data;

    /**
     * Paging details.
     */
    @JsonProperty(value = "Paging")
    private TermsPaging paging;

    /**
     * Get the data value.
     *
     * @return the data value
     */
    public TermsData data() {
        return this.data;
    }

    /**
     * Set the data value.
     *
     * @param data the data value to set
     * @return the TermsInner object itself.
     */
    public TermsInner withData(TermsData data) {
        this.data = data;
        return this;
    }

    /**
     * Get the paging value.
     *
     * @return the paging value
     */
    public TermsPaging paging() {
        return this.paging;
    }

    /**
     * Set the paging value.
     *
     * @param paging the paging value to set
     * @return the TermsInner object itself.
     */
    public TermsInner withPaging(TermsPaging paging) {
        this.paging = paging;
        return this;
    }

}
