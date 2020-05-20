// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.LengthTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.LengthTokenFilter} and
 * {@link LengthTokenFilter}.
 */
public final class LengthTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(LengthTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.LengthTokenFilter} to
     * {@link LengthTokenFilter}.
     */
    public static LengthTokenFilter map(com.azure.search.documents.implementation.models.LengthTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        LengthTokenFilter lengthTokenFilter = new LengthTokenFilter();

        String _name = obj.getName();
        lengthTokenFilter.setName(_name);

        Integer _minLength = obj.getMinLength();
        lengthTokenFilter.setMinLength(_minLength);

        Integer _maxLength = obj.getMaxLength();
        lengthTokenFilter.setMaxLength(_maxLength);
        return lengthTokenFilter;
    }

    /**
     * Maps from {@link LengthTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.LengthTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.LengthTokenFilter map(LengthTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.LengthTokenFilter lengthTokenFilter =
            new com.azure.search.documents.implementation.models.LengthTokenFilter();

        String _name = obj.getName();
        lengthTokenFilter.setName(_name);

        Integer _minLength = obj.getMinLength();
        lengthTokenFilter.setMinLength(_minLength);

        Integer _maxLength = obj.getMaxLength();
        lengthTokenFilter.setMaxLength(_maxLength);
        return lengthTokenFilter;
    }
}
