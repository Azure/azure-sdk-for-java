package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ImageDetail;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ImageDetail} and
 * {@link ImageDetail} mismatch.
 */
public final class ImageDetailConverter {
    public static ImageDetail convert(com.azure.search.documents.models.ImageDetail obj) {
        return DefaultConverter.convert(obj, ImageDetail.class);
    }

    public static com.azure.search.documents.models.ImageDetail convert(ImageDetail obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ImageDetail.class);
    }
}
