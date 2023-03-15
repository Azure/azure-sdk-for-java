// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.util.ExpandableStringEnum;

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
    public static final SearchAudience AZURE_GOVERNMENT = fromString("https://search.azure.us");

    /**
     * The {@link SearchAudience} for the Azure Public cloud.
     */
    public static final SearchAudience AZURE_PUBLIC_CLOUD = fromString("https://search.azure.com");

    /**
     * Creates a new instance of {@link SearchAudience} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link SearchAudience} which doesn't have a String enum
     * value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public SearchAudience() {
    }

    /**
     * Creates or finds a SearchAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SearchAudience.
     */
    public static SearchAudience fromString(String name) {
        return fromString(name, SearchAudience.class);
    }

    /**
     * Gets the known SearchAudience values.
     *
     * @return known SearchAudience values.
     */
    public static Collection<SearchAudience> values() {
        return values(SearchAudience.class);
    }
}
