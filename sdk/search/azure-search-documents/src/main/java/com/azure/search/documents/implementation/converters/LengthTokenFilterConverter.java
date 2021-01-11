// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.LengthTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.LengthTokenFilter} and
 * {@link LengthTokenFilter}.
 */
public final class LengthTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.LengthTokenFilter} to
     * {@link LengthTokenFilter}.
     */
    public static LengthTokenFilter map(com.azure.search.documents.indexes.implementation.models.LengthTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        LengthTokenFilter lengthTokenFilter = new LengthTokenFilter(obj.getName());

        Integer minLength = obj.getMinLength();
        lengthTokenFilter.setMinLength(minLength);

        Integer maxLength = obj.getMaxLength();
        lengthTokenFilter.setMaxLength(maxLength);
        return lengthTokenFilter;
    }

    /**
     * Maps from {@link LengthTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.LengthTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.LengthTokenFilter map(LengthTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.LengthTokenFilter lengthTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.LengthTokenFilter(obj.getName());


        Integer minLength = obj.getMinLength();
        lengthTokenFilter.setMinLength(minLength);

        Integer maxLength = obj.getMaxLength();
        lengthTokenFilter.setMaxLength(maxLength);
        return lengthTokenFilter;
    }

    private LengthTokenFilterConverter() {
    }
}
