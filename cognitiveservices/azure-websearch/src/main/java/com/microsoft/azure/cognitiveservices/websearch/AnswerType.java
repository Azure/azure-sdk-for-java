/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for AnswerType.
 */
public final class AnswerType extends ExpandableStringEnum<AnswerType> {
    /** Static value WebPages for AnswerType. */
    public static final AnswerType WEB_PAGES = fromString("WebPages");

    /** Static value Images for AnswerType. */
    public static final AnswerType IMAGES = fromString("Images");

    /** Static value SpellSuggestions for AnswerType. */
    public static final AnswerType SPELL_SUGGESTIONS = fromString("SpellSuggestions");

    /** Static value News for AnswerType. */
    public static final AnswerType NEWS = fromString("News");

    /** Static value RelatedSearches for AnswerType. */
    public static final AnswerType RELATED_SEARCHES = fromString("RelatedSearches");

    /** Static value Videos for AnswerType. */
    public static final AnswerType VIDEOS = fromString("Videos");

    /** Static value Computation for AnswerType. */
    public static final AnswerType COMPUTATION = fromString("Computation");

    /** Static value TimeZone for AnswerType. */
    public static final AnswerType TIME_ZONE = fromString("TimeZone");

    /**
     * Creates or finds a AnswerType from its string representation.
     * @param name a name to look for
     * @return the corresponding AnswerType
     */
    @JsonCreator
    public static AnswerType fromString(String name) {
        return fromString(name, AnswerType.class);
    }

    /**
     * @return known AnswerType values
     */
    public static Collection<AnswerType> values() {
        return values(AnswerType.class);
    }
}
