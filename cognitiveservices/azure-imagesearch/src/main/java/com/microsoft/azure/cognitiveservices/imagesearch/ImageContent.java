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
 * Defines values for ImageContent.
 */
public final class ImageContent extends ExpandableStringEnum<ImageContent> {
    /** Static value Face for ImageContent. */
    public static final ImageContent FACE = fromString("Face");

    /** Static value Portrait for ImageContent. */
    public static final ImageContent PORTRAIT = fromString("Portrait");

    /**
     * Creates or finds a ImageContent from its string representation.
     * @param name a name to look for
     * @return the corresponding ImageContent
     */
    @JsonCreator
    public static ImageContent fromString(String name) {
        return fromString(name, ImageContent.class);
    }

    /**
     * @return known ImageContent values
     */
    public static Collection<ImageContent> values() {
        return values(ImageContent.class);
    }
}
