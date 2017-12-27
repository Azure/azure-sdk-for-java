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
 * Defines values for ImageSize.
 */
public final class ImageSize extends ExpandableStringEnum<ImageSize> {
    /** Static value All for ImageSize. */
    public static final ImageSize ALL = fromString("All");

    /** Static value Small for ImageSize. */
    public static final ImageSize SMALL = fromString("Small");

    /** Static value Medium for ImageSize. */
    public static final ImageSize MEDIUM = fromString("Medium");

    /** Static value Large for ImageSize. */
    public static final ImageSize LARGE = fromString("Large");

    /** Static value Wallpaper for ImageSize. */
    public static final ImageSize WALLPAPER = fromString("Wallpaper");

    /**
     * Creates or finds a ImageSize from its string representation.
     * @param name a name to look for
     * @return the corresponding ImageSize
     */
    @JsonCreator
    public static ImageSize fromString(String name) {
        return fromString(name, ImageSize.class);
    }

    /**
     * @return known ImageSize values
     */
    public static Collection<ImageSize> values() {
        return values(ImageSize.class);
    }
}
