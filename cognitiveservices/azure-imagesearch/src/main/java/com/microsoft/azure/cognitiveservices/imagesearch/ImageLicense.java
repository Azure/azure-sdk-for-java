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
 * Defines values for ImageLicense.
 */
public final class ImageLicense extends ExpandableStringEnum<ImageLicense> {
    /** Static value All for ImageLicense. */
    public static final ImageLicense ALL = fromString("All");

    /** Static value Any for ImageLicense. */
    public static final ImageLicense ANY = fromString("Any");

    /** Static value Public for ImageLicense. */
    public static final ImageLicense PUBLIC = fromString("Public");

    /** Static value Share for ImageLicense. */
    public static final ImageLicense SHARE = fromString("Share");

    /** Static value ShareCommercially for ImageLicense. */
    public static final ImageLicense SHARE_COMMERCIALLY = fromString("ShareCommercially");

    /** Static value Modify for ImageLicense. */
    public static final ImageLicense MODIFY = fromString("Modify");

    /** Static value ModifyCommercially for ImageLicense. */
    public static final ImageLicense MODIFY_COMMERCIALLY = fromString("ModifyCommercially");

    /**
     * Creates or finds a ImageLicense from its string representation.
     * @param name a name to look for
     * @return the corresponding ImageLicense
     */
    @JsonCreator
    public static ImageLicense fromString(String name) {
        return fromString(name, ImageLicense.class);
    }

    /**
     * @return known ImageLicense values
     */
    public static Collection<ImageLicense> values() {
        return values(ImageLicense.class);
    }
}
