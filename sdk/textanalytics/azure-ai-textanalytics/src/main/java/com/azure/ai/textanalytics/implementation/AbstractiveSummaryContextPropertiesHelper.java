// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AbstractiveSummaryContext;

/**
 * The helper class to set the non-public properties of an {@link AbstractiveSummaryContext} instance.
 */
public final class AbstractiveSummaryContextPropertiesHelper {
    private static AbstractiveSummaryContextAccessor accessor;

    private AbstractiveSummaryContextPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractiveSummaryContext}
     * instance.
     */
    public interface AbstractiveSummaryContextAccessor {
        void setOffset(AbstractiveSummaryContext abstractiveSummaryContext, int offset);

        void setLength(AbstractiveSummaryContext abstractiveSummaryContext, int length);
    }

    /**
     * The method called from {@link AbstractiveSummaryContext} to set it's accessor.
     *
     * @param abstractiveSummaryContextAccessor The accessor.
     */
    public static void setAccessor(final AbstractiveSummaryContextAccessor abstractiveSummaryContextAccessor) {
        accessor = abstractiveSummaryContextAccessor;
    }

    public static void setOffset(AbstractiveSummaryContext abstractiveSummaryContext, int offset) {
        accessor.setOffset(abstractiveSummaryContext, offset);
    }

    public static void setLength(AbstractiveSummaryContext abstractiveSummaryContext, int length) {
        accessor.setLength(abstractiveSummaryContext, length);
    }
}
