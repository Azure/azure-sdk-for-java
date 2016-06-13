/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes Storage Resource Usage.
 */
public class Usage {
    /**
     * Gets the unit of measurement. Possible values include: 'Count',
     * 'Bytes', 'Seconds', 'Percent', 'CountsPerSecond', 'BytesPerSecond'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UsageUnit unit;

    /**
     * Gets the current count of the allocated resources in the subscription.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer currentValue;

    /**
     * Gets the maximum count of the resources that can be allocated in the
     * subscription.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer limit;

    /**
     * Gets the name of the type of usage.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UsageName name;

    /**
     * Get the unit value.
     *
     * @return the unit value
     */
    public UsageUnit unit() {
        return this.unit;
    }

    /**
     * Get the currentValue value.
     *
     * @return the currentValue value
     */
    public Integer currentValue() {
        return this.currentValue;
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
     * Get the name value.
     *
     * @return the name value
     */
    public UsageName name() {
        return this.name;
    }

}
