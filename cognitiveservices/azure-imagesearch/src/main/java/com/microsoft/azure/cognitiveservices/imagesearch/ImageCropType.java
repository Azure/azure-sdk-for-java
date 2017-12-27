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
 * Defines values for ImageCropType.
 */
public final class ImageCropType extends ExpandableStringEnum<ImageCropType> {
    /** Static value Rectangular for ImageCropType. */
    public static final ImageCropType RECTANGULAR = fromString("Rectangular");

    /**
     * Creates or finds a ImageCropType from its string representation.
     * @param name a name to look for
     * @return the corresponding ImageCropType
     */
    @JsonCreator
    public static ImageCropType fromString(String name) {
        return fromString(name, ImageCropType.class);
    }

    /**
     * @return known ImageCropType values
     */
    public static Collection<ImageCropType> values() {
        return values(ImageCropType.class);
    }
}
