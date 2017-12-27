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
 * Defines values for ImageType.
 */
public final class ImageType extends ExpandableStringEnum<ImageType> {
    /** Static value AnimatedGif for ImageType. */
    public static final ImageType ANIMATED_GIF = fromString("AnimatedGif");

    /** Static value Clipart for ImageType. */
    public static final ImageType CLIPART = fromString("Clipart");

    /** Static value Line for ImageType. */
    public static final ImageType LINE = fromString("Line");

    /** Static value Photo for ImageType. */
    public static final ImageType PHOTO = fromString("Photo");

    /** Static value Shopping for ImageType. */
    public static final ImageType SHOPPING = fromString("Shopping");

    /** Static value Transparent for ImageType. */
    public static final ImageType TRANSPARENT = fromString("Transparent");

    /**
     * Creates or finds a ImageType from its string representation.
     * @param name a name to look for
     * @return the corresponding ImageType
     */
    @JsonCreator
    public static ImageType fromString(String name) {
        return fromString(name, ImageType.class);
    }

    /**
     * @return known ImageType values
     */
    public static Collection<ImageType> values() {
        return values(ImageType.class);
    }
}
