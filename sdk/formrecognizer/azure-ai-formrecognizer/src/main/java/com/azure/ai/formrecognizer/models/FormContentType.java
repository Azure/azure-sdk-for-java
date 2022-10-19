// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Define values for content type supported for local files.
 */
public final class FormContentType extends ExpandableStringEnum<FormContentType> {

    /**
     * Static value Line for FormContentType.
     */
    public static final FormContentType APPLICATION_PDF = fromString("application/pdf");

    /**
     * Static value Line for FormContentType.
     */
    public static final FormContentType IMAGE_JPEG = fromString("image/jpeg");

    /**
     * Static value Line for FormContentType.
     */
    public static final FormContentType IMAGE_PNG = fromString("image/png");

    /**
     * Static value Line for FormContentType.
     */
    public static final FormContentType IMAGE_TIFF = fromString("image/tiff");

    /**
     * Static value Line for FormContentType.
     */
    public static final FormContentType IMAGE_BMP = fromString("image/bmp");

    /**
     * Creates or finds a ElementType from its string representation.
     *
     * @param value a value to look for.
     *
     * @return the corresponding ElementType.
     */
    public static FormContentType fromString(String value) {
        return fromString(value, FormContentType.class);
    }

}
