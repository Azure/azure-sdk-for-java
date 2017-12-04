/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Paging details.
 */
public class GetAllTermsPaging {
    /**
     * Total details.
     */
    @JsonProperty(value = "Total")
    private Double total;

    /**
     * Limit details.
     */
    @JsonProperty(value = "Limit")
    private Double limit;

    /**
     * Offset details.
     */
    @JsonProperty(value = "Offset")
    private Double offset;

    /**
     * Returned text details.
     */
    @JsonProperty(value = "Returned")
    private Double returned;

    /**
     * Get the total value.
     *
     * @return the total value
     */
    public Double total() {
        return this.total;
    }

    /**
     * Set the total value.
     *
     * @param total the total value to set
     * @return the GetAllTermsPaging object itself.
     */
    public GetAllTermsPaging withTotal(Double total) {
        this.total = total;
        return this;
    }

    /**
     * Get the limit value.
     *
     * @return the limit value
     */
    public Double limit() {
        return this.limit;
    }

    /**
     * Set the limit value.
     *
     * @param limit the limit value to set
     * @return the GetAllTermsPaging object itself.
     */
    public GetAllTermsPaging withLimit(Double limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Get the offset value.
     *
     * @return the offset value
     */
    public Double offset() {
        return this.offset;
    }

    /**
     * Set the offset value.
     *
     * @param offset the offset value to set
     * @return the GetAllTermsPaging object itself.
     */
    public GetAllTermsPaging withOffset(Double offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Get the returned value.
     *
     * @return the returned value
     */
    public Double returned() {
        return this.returned;
    }

    /**
     * Set the returned value.
     *
     * @param returned the returned value to set
     * @return the GetAllTermsPaging object itself.
     */
    public GetAllTermsPaging withReturned(Double returned) {
        this.returned = returned;
        return this;
    }

}
