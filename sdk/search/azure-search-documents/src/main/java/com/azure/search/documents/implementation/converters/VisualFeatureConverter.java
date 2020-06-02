// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.VisualFeature;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.VisualFeature} and {@link VisualFeature}.
 */
public final class VisualFeatureConverter {
    private static final ClientLogger LOGGER = new ClientLogger(VisualFeatureConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.VisualFeature} to enum
     * {@link VisualFeature}.
     */
    public static VisualFeature map(com.azure.search.documents.indexes.implementation.models.VisualFeature obj) {
        if (obj == null) {
            return null;
        }
        return VisualFeature.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link VisualFeature} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.VisualFeature}.
     */
    public static com.azure.search.documents.indexes.implementation.models.VisualFeature map(VisualFeature obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.VisualFeature.fromString(obj.toString());
    }
}
