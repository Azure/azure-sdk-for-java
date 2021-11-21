// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for SummarySentencesOrder.
 */
@Immutable
public final class SummarySentencesOrder extends ExpandableStringEnum<SummarySentencesOrder> {
    /**
     * offset as the SummarySentencesOrder.
     */
    public static final SummarySentencesOrder OFFSET = fromString("Offset");

    /**
     * rank as the SummarySentencesOrder.
     */
    public static final SummarySentencesOrder RANK = fromString("Rank");

    /**
     * Creates or finds a {@link SummarySentencesOrder} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link SummarySentencesOrder}.
     */
    public static SummarySentencesOrder fromString(String name) {
        return fromString(name, SummarySentencesOrder.class);
    }
}
