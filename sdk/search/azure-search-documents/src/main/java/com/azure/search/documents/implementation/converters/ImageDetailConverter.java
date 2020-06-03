// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ImageDetail;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ImageDetail} and {@link ImageDetail}.
 */
public final class ImageDetailConverter {

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.ImageDetail} to enum {@link ImageDetail}.
     */
    public static ImageDetail map(com.azure.search.documents.indexes.implementation.models.ImageDetail obj) {
        if (obj == null) {
            return null;
        }
        return ImageDetail.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link ImageDetail} to enum {@link com.azure.search.documents.indexes.implementation.models.ImageDetail}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ImageDetail map(ImageDetail obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.ImageDetail.fromString(obj.toString());
    }

    private ImageDetailConverter() {
    }
}
