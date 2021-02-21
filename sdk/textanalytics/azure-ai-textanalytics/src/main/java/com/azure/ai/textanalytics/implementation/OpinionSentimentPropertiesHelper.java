// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.OpinionSentiment;

/**
 * The helper class to set the non-public properties of an {@link OpinionSentiment} instance.
 */
public final class OpinionSentimentPropertiesHelper {
    private static OpinionSentimentAccessor accessor;

    private OpinionSentimentPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link OpinionSentiment} instance.
     */
    public interface OpinionSentimentAccessor {
        void setLength(OpinionSentiment opinionSentiment, int length);
    }

    /**
     * The method called from {@link OpinionSentiment} to set it's accessor.
     *
     * @param opinionSentimentAccessor The accessor.
     */
    public static void setAccessor(final OpinionSentimentAccessor opinionSentimentAccessor) {
        accessor = opinionSentimentAccessor;
    }

    public static void setLength(OpinionSentiment opinionSentiment, int length) {
        accessor.setLength(opinionSentiment, length);
    }
}
