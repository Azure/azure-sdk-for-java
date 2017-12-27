/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for SafeSearch.
 */
public final class SafeSearch extends ExpandableStringEnum<SafeSearch> {
    /** Static value Off for SafeSearch. */
    public static final SafeSearch OFF = fromString("Off");

    /** Static value Moderate for SafeSearch. */
    public static final SafeSearch MODERATE = fromString("Moderate");

    /** Static value Strict for SafeSearch. */
    public static final SafeSearch STRICT = fromString("Strict");

    /**
     * Creates or finds a SafeSearch from its string representation.
     * @param name a name to look for
     * @return the corresponding SafeSearch
     */
    @JsonCreator
    public static SafeSearch fromString(String name) {
        return fromString(name, SafeSearch.class);
    }

    /**
     * @return known SafeSearch values
     */
    public static Collection<SafeSearch> values() {
        return values(SafeSearch.class);
    }
}
