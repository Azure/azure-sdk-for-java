// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

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
