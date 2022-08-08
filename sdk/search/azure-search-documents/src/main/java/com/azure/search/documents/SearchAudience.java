// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Cloud audiences available for Search.
 */
public final class SearchAudience extends ExpandableStringEnum<SearchAudience> {
    /**
     * The {@link SearchAudience} for Azure China cloud.
     */
    public static final SearchAudience AZURE_CHINA = fromString("https://search.azure.cn");

    /**
     * The {@link SearchAudience} for Azure US Government cloud.
     */
    public static final SearchAudience AZURE_US_GOVERNMENT = fromString("https://search.azure.us");

    /**
     * The {@link SearchAudience} for the Azure Public cloud.
     */
    public static final SearchAudience AZURE_PUBLIC = fromString("https://search.azure.com");

    /**
     * Creates or finds a SearchAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SearchAudience.
     */
    @JsonCreator
    public static SearchAudience fromString(String name) {
        return fromString(name, SearchAudience.class);
    }

    /** @return known SearchAudience values. */
    public static Collection<SearchAudience> values() {
        return values(SearchAudience.class);
    }
}
