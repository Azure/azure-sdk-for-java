// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ExtractiveSummarySentence;

/**
 * The helper class to set the non-public properties of an {@link ExtractiveSummarySentence} instance.
 */
public final class ExtractiveSummarySentencePropertiesHelper {
    private static ExtractiveSummarySentenceAccessor accessor;

    private ExtractiveSummarySentencePropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ExtractiveSummarySentence} instance.
     */
    public interface ExtractiveSummarySentenceAccessor {
        void setText(ExtractiveSummarySentence extractiveSummarySentence, String text);

        void setRankScore(ExtractiveSummarySentence extractiveSummarySentence, double rankScore);

        void setOffset(ExtractiveSummarySentence extractiveSummarySentence, int offset);

        void setLength(ExtractiveSummarySentence extractiveSummarySentence, int length);
    }

    /**
     * The method called from {@link ExtractiveSummarySentence} to set it's accessor.
     *
     * @param extractiveSummarySentenceAccessor The accessor.
     */
    public static void setAccessor(final ExtractiveSummarySentenceAccessor extractiveSummarySentenceAccessor) {
        accessor = extractiveSummarySentenceAccessor;
    }

    public static void setText(ExtractiveSummarySentence extractiveSummarySentence, String text) {
        accessor.setText(extractiveSummarySentence, text);
    }

    public static void setRankScore(ExtractiveSummarySentence extractiveSummarySentence, double rankScore) {
        accessor.setRankScore(extractiveSummarySentence, rankScore);
    }

    public static void setOffset(ExtractiveSummarySentence extractiveSummarySentence, int offset) {
        accessor.setOffset(extractiveSummarySentence, offset);
    }

    public static void setLength(ExtractiveSummarySentence extractiveSummarySentence, int length) {
        accessor.setLength(extractiveSummarySentence, length);
    }
}
