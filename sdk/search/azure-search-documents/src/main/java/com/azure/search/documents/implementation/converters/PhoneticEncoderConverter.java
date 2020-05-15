package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.PhoneticEncoder;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.PhoneticEncoder} and
 * {@link PhoneticEncoder} mismatch.
 */
public final class PhoneticEncoderConverter {
    public static PhoneticEncoder convert(com.azure.search.documents.models.PhoneticEncoder obj) {
        return DefaultConverter.convert(obj, PhoneticEncoder.class);
    }

    public static com.azure.search.documents.models.PhoneticEncoder convert(PhoneticEncoder obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.PhoneticEncoder.class);
    }
}
