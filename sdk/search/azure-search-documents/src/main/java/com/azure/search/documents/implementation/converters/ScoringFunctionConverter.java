// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.implementation.models.DistanceScoringFunction;
import com.azure.search.documents.indexes.implementation.models.FreshnessScoringFunction;
import com.azure.search.documents.indexes.implementation.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.implementation.models.TagScoringFunction;
import com.azure.search.documents.indexes.models.ScoringFunction;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ScoringFunction} and
 * {@link ScoringFunction}.
 */
public final class ScoringFunctionConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ScoringFunctionConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.indexes.implementation.models.ScoringFunction} to
     * {@link ScoringFunction}. Dedicate works to sub class converter.
     */
    public static ScoringFunction map(com.azure.search.documents.indexes.implementation.models.ScoringFunction obj) {
        if (obj instanceof DistanceScoringFunction) {
            return DistanceScoringFunctionConverter.map((DistanceScoringFunction) obj);
        }
        if (obj instanceof MagnitudeScoringFunction) {
            return MagnitudeScoringFunctionConverter.map((MagnitudeScoringFunction) obj);
        }
        if (obj instanceof TagScoringFunction) {
            return TagScoringFunctionConverter.map((TagScoringFunction) obj);
        }
        if (obj instanceof FreshnessScoringFunction) {
            return FreshnessScoringFunctionConverter.map((FreshnessScoringFunction) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link ScoringFunction} to
     * {@link com.azure.search.documents.indexes.implementation.models.ScoringFunction}. Dedicate works to sub class converter.
     */
    public static com.azure.search.documents.indexes.implementation.models.ScoringFunction map(ScoringFunction obj) {
        if (obj instanceof com.azure.search.documents.indexes.models.MagnitudeScoringFunction) {
            return MagnitudeScoringFunctionConverter.map((com.azure.search.documents.indexes.models.MagnitudeScoringFunction) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.TagScoringFunction) {
            return TagScoringFunctionConverter.map((com.azure.search.documents.indexes.models.TagScoringFunction) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.FreshnessScoringFunction) {
            return FreshnessScoringFunctionConverter.map((com.azure.search.documents.indexes.models.FreshnessScoringFunction) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.DistanceScoringFunction) {
            return DistanceScoringFunctionConverter.map((com.azure.search.documents.indexes.models.DistanceScoringFunction) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private ScoringFunctionConverter() {
    }
}
