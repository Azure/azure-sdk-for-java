// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.PhoneticEncoder;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.PhoneticEncoder} and
 * {@link PhoneticEncoder}.
 */
public final class PhoneticEncoderConverter {
    private static final ClientLogger LOGGER = new ClientLogger(PhoneticEncoderConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.PhoneticEncoder} to enum
     * {@link PhoneticEncoder}.
     */
    public static PhoneticEncoder map(com.azure.search.documents.indexes.implementation.models.PhoneticEncoder obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case METAPHONE:
                return PhoneticEncoder.METAPHONE;
            case DOUBLE_METAPHONE:
                return PhoneticEncoder.DOUBLE_METAPHONE;
            case SOUNDEX:
                return PhoneticEncoder.SOUNDEX;
            case REFINED_SOUNDEX:
                return PhoneticEncoder.REFINED_SOUNDEX;
            case CAVERPHONE1:
                return PhoneticEncoder.CAVERPHONE1;
            case CAVERPHONE2:
                return PhoneticEncoder.CAVERPHONE2;
            case COLOGNE:
                return PhoneticEncoder.COLOGNE;
            case NYSIIS:
                return PhoneticEncoder.NYSIIS;
            case KOELNER_PHONETIK:
                return PhoneticEncoder.KOELNER_PHONETIK;
            case HAASE_PHONETIK:
                return PhoneticEncoder.HAASE_PHONETIK;
            case BEIDER_MORSE:
                return PhoneticEncoder.BEIDER_MORSE;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link PhoneticEncoder} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.PhoneticEncoder}.
     */
    public static com.azure.search.documents.indexes.implementation.models.PhoneticEncoder map(PhoneticEncoder obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case METAPHONE:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.METAPHONE;
            case DOUBLE_METAPHONE:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.DOUBLE_METAPHONE;
            case SOUNDEX:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.SOUNDEX;
            case REFINED_SOUNDEX:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.REFINED_SOUNDEX;
            case CAVERPHONE1:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.CAVERPHONE1;
            case CAVERPHONE2:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.CAVERPHONE2;
            case COLOGNE:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.COLOGNE;
            case NYSIIS:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.NYSIIS;
            case KOELNER_PHONETIK:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.KOELNER_PHONETIK;
            case HAASE_PHONETIK:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.HAASE_PHONETIK;
            case BEIDER_MORSE:
                return com.azure.search.documents.indexes.implementation.models.PhoneticEncoder.BEIDER_MORSE;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private PhoneticEncoderConverter() {
    }
}
