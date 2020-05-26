// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.ImageDetail;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ImageDetail} and {@link ImageDetail}.
 */
public final class ImageDetailConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ImageDetailConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.ImageDetail} to enum {@link ImageDetail}.
     */
    public static ImageDetail map(com.azure.search.documents.indexes.implementation.models.ImageDetail obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case CELEBRITIES:
                return ImageDetail.CELEBRITIES;
            case LANDMARKS:
                return ImageDetail.LANDMARKS;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link ImageDetail} to enum {@link com.azure.search.documents.indexes.implementation.models.ImageDetail}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ImageDetail map(ImageDetail obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case CELEBRITIES:
                return com.azure.search.documents.indexes.implementation.models.ImageDetail.CELEBRITIES;
            case LANDMARKS:
                return com.azure.search.documents.indexes.implementation.models.ImageDetail.LANDMARKS;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private ImageDetailConverter() {
    }
}
