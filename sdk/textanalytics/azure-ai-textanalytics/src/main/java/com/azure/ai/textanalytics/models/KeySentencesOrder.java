// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

/**
 *
 */
@Immutable
public final class KeySentencesOrder extends ExpandableStringEnum<KeySentencesOrder> {
    /**
     * offset as the KeySentencesOrder.
     */
    public static final KeySentencesOrder OFFSET = fromString("offset");

    /**
     * rankScore (PHI) as the KeySentencesOrder.
     */
    public static final KeySentencesOrder RANK = fromString("rank");

    /**
     * Creates or finds a {@link KeySentencesOrder} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link KeySentencesOrder}.
     */
    public static KeySentencesOrder fromString(String name) {
        return fromString(name, KeySentencesOrder.class);
    }
}
