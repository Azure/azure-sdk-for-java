// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.FacetResult;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.FacetResult} and {@link FacetResult}.
 */
public final class FacetResultConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.FacetResult} to {@link FacetResult}.
     */
    public static FacetResult map(com.azure.search.documents.implementation.models.FacetResult obj) {
        if (obj == null) {
            return null;
        }
        FacetResult facetResult = new FacetResult();

        Long count = obj.getCount();
        PrivateFieldAccessHelper.set(facetResult, "count", count);

        if (obj.getAdditionalProperties() != null) {
            Map<String, Object> additionalProperties =
                obj.getAdditionalProperties().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            PrivateFieldAccessHelper.set(facetResult, "additionalProperties", additionalProperties);
        }
        return facetResult;
    }

    /**
     * Maps from {@link FacetResult} to {@link com.azure.search.documents.implementation.models.FacetResult}.
     */
    public static com.azure.search.documents.implementation.models.FacetResult map(FacetResult obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.FacetResult facetResult =
            new com.azure.search.documents.implementation.models.FacetResult();

        Long count = obj.getCount();
        PrivateFieldAccessHelper.set(facetResult, "count", count);

        if (obj.getAdditionalProperties() != null) {
            Map<String, Object> additionalProperties =
                obj.getAdditionalProperties().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            PrivateFieldAccessHelper.set(facetResult, "additionalProperties", additionalProperties);
        }
        return facetResult;
    }

    private FacetResultConverter() {
    }
}
