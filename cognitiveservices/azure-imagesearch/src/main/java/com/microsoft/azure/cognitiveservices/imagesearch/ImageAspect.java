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
 * Defines values for ImageAspect.
 */
public final class ImageAspect extends ExpandableStringEnum<ImageAspect> {
    /** Static value All for ImageAspect. */
    public static final ImageAspect ALL = fromString("All");

    /** Static value Square for ImageAspect. */
    public static final ImageAspect SQUARE = fromString("Square");

    /** Static value Wide for ImageAspect. */
    public static final ImageAspect WIDE = fromString("Wide");

    /** Static value Tall for ImageAspect. */
    public static final ImageAspect TALL = fromString("Tall");

    /**
     * Creates or finds a ImageAspect from its string representation.
     * @param name a name to look for
     * @return the corresponding ImageAspect
     */
    @JsonCreator
    public static ImageAspect fromString(String name) {
        return fromString(name, ImageAspect.class);
    }

    /**
     * @return known ImageAspect values
     */
    public static Collection<ImageAspect> values() {
        return values(ImageAspect.class);
    }
}
