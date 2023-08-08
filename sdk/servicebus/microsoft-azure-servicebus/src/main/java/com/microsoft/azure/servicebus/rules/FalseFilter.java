// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.rules;

/**
 * Represents a special kind of filter that doesn't match any message.
 *
 * @since 1.0
 */
public class FalseFilter extends SqlFilter {
    private static final String FALSE_FILTER_EXPRESSION = "1=0";

    /**
     * A false filter object that is pre-created. Clients can use this object instead of recreating a new instance every time.
     */
    public static final FalseFilter DEFAULT = new FalseFilter();

    /**
     * Creates a false filter.
     */
    public FalseFilter() {
        super(FALSE_FILTER_EXPRESSION);
    }
}
