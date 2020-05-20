// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ClassicSimilarity;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ClassicSimilarity} and
 * {@link ClassicSimilarity}.
 */
public final class ClassicSimilarityConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ClassicSimilarityConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ClassicSimilarity} to
     * {@link ClassicSimilarity}.
     */
    public static ClassicSimilarity map(com.azure.search.documents.implementation.models.ClassicSimilarity obj) {
        if (obj == null) {
            return null;
        }
        ClassicSimilarity classicSimilarity = new ClassicSimilarity();
        return classicSimilarity;
    }

    /**
     * Maps from {@link ClassicSimilarity} to
     * {@link com.azure.search.documents.implementation.models.ClassicSimilarity}.
     */
    public static com.azure.search.documents.implementation.models.ClassicSimilarity map(ClassicSimilarity obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ClassicSimilarity classicSimilarity =
            new com.azure.search.documents.implementation.models.ClassicSimilarity();
        return classicSimilarity;
    }
}
