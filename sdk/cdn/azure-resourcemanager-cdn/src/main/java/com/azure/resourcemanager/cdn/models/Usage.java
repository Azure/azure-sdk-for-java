// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

/**
 * Represents a single usage record returned by Azure Front Door endpoint APIs.
 */
public interface Usage {
    /**
     * Gets the full resource identifier for the usage entry.
     *
     * @return the resource identifier
     */
    String id();

    /**
     * Gets the measurement unit for the usage.
     *
     * @return the unit for the usage value
     */
    UsageUnit unit();

    /**
     * Gets the current value that has been consumed.
     *
     * @return the current usage value
     */
    long currentValue();

    /**
     * Gets the total limit for the usage metric.
     *
     * @return the usage limit
     */
    long limit();

    /**
     * Gets the friendly name for this usage value.
     *
     * @return the usage name metadata
     */
    UsageName name();
}
