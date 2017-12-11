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
 * Defines values for ImageColor.
 */
public final class ImageColor extends ExpandableStringEnum<ImageColor> {
    /** Static value ColorOnly for ImageColor. */
    public static final ImageColor COLOR_ONLY = fromString("ColorOnly");

    /** Static value Monochrome for ImageColor. */
    public static final ImageColor MONOCHROME = fromString("Monochrome");

    /** Static value Black for ImageColor. */
    public static final ImageColor BLACK = fromString("Black");

    /** Static value Blue for ImageColor. */
    public static final ImageColor BLUE = fromString("Blue");

    /** Static value Brown for ImageColor. */
    public static final ImageColor BROWN = fromString("Brown");

    /** Static value Gray for ImageColor. */
    public static final ImageColor GRAY = fromString("Gray");

    /** Static value Green for ImageColor. */
    public static final ImageColor GREEN = fromString("Green");

    /** Static value Orange for ImageColor. */
    public static final ImageColor ORANGE = fromString("Orange");

    /** Static value Pink for ImageColor. */
    public static final ImageColor PINK = fromString("Pink");

    /** Static value Purple for ImageColor. */
    public static final ImageColor PURPLE = fromString("Purple");

    /** Static value Red for ImageColor. */
    public static final ImageColor RED = fromString("Red");

    /** Static value Teal for ImageColor. */
    public static final ImageColor TEAL = fromString("Teal");

    /** Static value White for ImageColor. */
    public static final ImageColor WHITE = fromString("White");

    /** Static value Yellow for ImageColor. */
    public static final ImageColor YELLOW = fromString("Yellow");

    /**
     * Creates or finds a ImageColor from its string representation.
     * @param name a name to look for
     * @return the corresponding ImageColor
     */
    @JsonCreator
    public static ImageColor fromString(String name) {
        return fromString(name, ImageColor.class);
    }

    /**
     * @return known ImageColor values
     */
    public static Collection<ImageColor> values() {
        return values(ImageColor.class);
    }
}
