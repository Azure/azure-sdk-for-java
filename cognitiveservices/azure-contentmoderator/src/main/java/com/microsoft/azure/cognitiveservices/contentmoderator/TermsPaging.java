/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Paging details.
 */
public class TermsPaging {
    /**
     * Total details.
     */
    @JsonProperty(value = "Total")
    private Integer total;

    /**
     * Limit details.
     */
    @JsonProperty(value = "Limit")
    private Integer limit;

    /**
     * Offset details.
     */
    @JsonProperty(value = "Offset")
    private Integer offset;

    /**
     * Returned text details.
     */
    @JsonProperty(value = "Returned")
    private Integer returned;

    /**
     * Get the total value.
     *
     * @return the total value
     */
    public Integer total() {
        return this.total;
    }

    /**
     * Set the total value.
     *
     * @param total the total value to set
     * @return the TermsPaging object itself.
     */
    public TermsPaging withTotal(Integer total) {
        this.total = total;
        return this;
    }

    /**
     * Get the limit value.
     *
     * @return the limit value
     */
    public Integer limit() {
        return this.limit;
    }

    /**
     * Set the limit value.
     *
     * @param limit the limit value to set
     * @return the TermsPaging object itself.
     */
    public TermsPaging withLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Get the offset value.
     *
     * @return the offset value
     */
    public Integer offset() {
        return this.offset;
    }

    /**
     * Set the offset value.
     *
     * @param offset the offset value to set
     * @return the TermsPaging object itself.
     */
    public TermsPaging withOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Get the returned value.
     *
     * @return the returned value
     */
    public Integer returned() {
        return this.returned;
    }

    /**
     * Set the returned value.
     *
     * @param returned the returned value to set
     * @return the TermsPaging object itself.
     */
    public TermsPaging withReturned(Integer returned) {
        this.returned = returned;
        return this;
    }

}
