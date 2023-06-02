// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for ExtractiveSummarySentencesOrder.
 */
@Immutable
public final class ExtractiveSummarySentencesOrder extends ExpandableStringEnum<ExtractiveSummarySentencesOrder> {
    /**
     * offset as the ExtractiveSummarySentencesOrder.
     */
    public static final ExtractiveSummarySentencesOrder OFFSET = fromString("Offset");

    /**
     * rank as the ExtractiveSummarySentencesOrder.
     */
    public static final ExtractiveSummarySentencesOrder RANK = fromString("Rank");

    /**
     * Creates or finds a {@link ExtractiveSummarySentencesOrder} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link ExtractiveSummarySentencesOrder}.
     */
    public static ExtractiveSummarySentencesOrder fromString(String name) {
        return fromString(name, ExtractiveSummarySentencesOrder.class);
    }

    /** @return known ExtractiveSummarySentencesOrder values. */
    public static Collection<ExtractiveSummarySentencesOrder> values() {
        return values(ExtractiveSummarySentencesOrder.class);
    }
}
