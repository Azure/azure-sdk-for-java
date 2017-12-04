/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.TermData;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.GetAllTermsPaging;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Gets all term Id response properties.
 */
public class GetAllTermsInner {
    /**
     * Term data details.
     */
    @JsonProperty(value = "data")
    private TermData data;

    /**
     * Paging details.
     */
    @JsonProperty(value = "paging")
    private GetAllTermsPaging paging;

    /**
     * Get the data value.
     *
     * @return the data value
     */
    public TermData data() {
        return this.data;
    }

    /**
     * Set the data value.
     *
     * @param data the data value to set
     * @return the GetAllTermsInner object itself.
     */
    public GetAllTermsInner withData(TermData data) {
        this.data = data;
        return this;
    }

    /**
     * Get the paging value.
     *
     * @return the paging value
     */
    public GetAllTermsPaging paging() {
        return this.paging;
    }

    /**
     * Set the paging value.
     *
     * @param paging the paging value to set
     * @return the GetAllTermsInner object itself.
     */
    public GetAllTermsInner withPaging(GetAllTermsPaging paging) {
        this.paging = paging;
        return this;
    }

}
