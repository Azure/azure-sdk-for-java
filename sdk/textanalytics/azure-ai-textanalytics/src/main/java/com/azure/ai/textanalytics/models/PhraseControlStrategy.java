// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

/** Defines values for PhraseControlStrategy. */
@Immutable
public final class PhraseControlStrategy extends ExpandableStringEnum<PhraseControlStrategy> {
    /** Static value encourage for PhraseControlStrategy. */
    public static final PhraseControlStrategy ENCOURAGE = fromString("encourage");

    /** Static value discourage for PhraseControlStrategy. */
    public static final PhraseControlStrategy DISCOURAGE = fromString("discourage");

    /** Static value disallow for PhraseControlStrategy. */
    public static final PhraseControlStrategy DISALLOW = fromString("disallow");

    /**
     * Creates or finds a PhraseControlStrategy from its string representation.
     * @param name a name to look for
     * @return the corresponding PhraseControlStrategy
     */
    public static PhraseControlStrategy fromString(String name) {
        return fromString(name, PhraseControlStrategy.class);
    }
}
