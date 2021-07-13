// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

/**
 *
 */
@Immutable
public final class SummarizedSentencesOrder extends ExpandableStringEnum<SummarizedSentencesOrder> {
    /**
     * offset as the SummarizedSentencesOrder.
     */
    public static final SummarizedSentencesOrder OFFSET = fromString("offset");

    /**
     * rankScore (PHI) as the SummarizedSentencesOrder.
     */
    public static final SummarizedSentencesOrder RANK_SCORE = fromString("rankScore");

    /**
     * Creates or finds a {@link SummarizedSentencesOrder} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link SummarizedSentencesOrder}.
     */
    public static SummarizedSentencesOrder fromString(String name) {
        return fromString(name, SummarizedSentencesOrder.class);
    }
}
