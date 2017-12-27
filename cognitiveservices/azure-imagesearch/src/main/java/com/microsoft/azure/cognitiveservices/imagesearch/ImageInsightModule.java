/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for ImageInsightModule.
 */
public final class ImageInsightModule extends ExpandableStringEnum<ImageInsightModule> {
    /** Static value All for ImageInsightModule. */
    public static final ImageInsightModule ALL = fromString("All");

    /** Static value BRQ for ImageInsightModule. */
    public static final ImageInsightModule BRQ = fromString("BRQ");

    /** Static value Caption for ImageInsightModule. */
    public static final ImageInsightModule CAPTION = fromString("Caption");

    /** Static value Collections for ImageInsightModule. */
    public static final ImageInsightModule COLLECTIONS = fromString("Collections");

    /** Static value Recipes for ImageInsightModule. */
    public static final ImageInsightModule RECIPES = fromString("Recipes");

    /** Static value PagesIncluding for ImageInsightModule. */
    public static final ImageInsightModule PAGES_INCLUDING = fromString("PagesIncluding");

    /** Static value RecognizedEntities for ImageInsightModule. */
    public static final ImageInsightModule RECOGNIZED_ENTITIES = fromString("RecognizedEntities");

    /** Static value RelatedSearches for ImageInsightModule. */
    public static final ImageInsightModule RELATED_SEARCHES = fromString("RelatedSearches");

    /** Static value ShoppingSources for ImageInsightModule. */
    public static final ImageInsightModule SHOPPING_SOURCES = fromString("ShoppingSources");

    /** Static value SimilarImages for ImageInsightModule. */
    public static final ImageInsightModule SIMILAR_IMAGES = fromString("SimilarImages");

    /** Static value SimilarProducts for ImageInsightModule. */
    public static final ImageInsightModule SIMILAR_PRODUCTS = fromString("SimilarProducts");

    /** Static value Tags for ImageInsightModule. */
    public static final ImageInsightModule TAGS = fromString("Tags");

    /**
     * Creates or finds a ImageInsightModule from its string representation.
     * @param name a name to look for
     * @return the corresponding ImageInsightModule
     */
    @JsonCreator
    public static ImageInsightModule fromString(String name) {
        return fromString(name, ImageInsightModule.class);
    }

    /**
     * @return known ImageInsightModule values
     */
    public static Collection<ImageInsightModule> values() {
        return values(ImageInsightModule.class);
    }
}
