// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ShingleTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ShingleTokenFilter} and
 * {@link ShingleTokenFilter}.
 */
public final class ShingleTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ShingleTokenFilter} to
     * {@link ShingleTokenFilter}.
     */
    public static ShingleTokenFilter map(com.azure.search.documents.indexes.implementation.models.ShingleTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        ShingleTokenFilter shingleTokenFilter = new ShingleTokenFilter(obj.getName());

        Integer minShingleSize = obj.getMinShingleSize();
        shingleTokenFilter.setMinShingleSize(minShingleSize);

        Boolean outputUnigrams = obj.isOutputUnigrams();
        shingleTokenFilter.setOutputUnigrams(outputUnigrams);

        String filterToken = obj.getFilterToken();
        shingleTokenFilter.setFilterToken(filterToken);

        Boolean outputUnigramsIfNoShingles = obj.isOutputUnigramsIfNoShingles();
        shingleTokenFilter.setOutputUnigramsIfNoShingles(outputUnigramsIfNoShingles);

        Integer maxShingleSize = obj.getMaxShingleSize();
        shingleTokenFilter.setMaxShingleSize(maxShingleSize);

        String tokenSeparator = obj.getTokenSeparator();
        shingleTokenFilter.setTokenSeparator(tokenSeparator);
        return shingleTokenFilter;
    }

    /**
     * Maps from {@link ShingleTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.ShingleTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ShingleTokenFilter map(ShingleTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.ShingleTokenFilter shingleTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.ShingleTokenFilter(obj.getName());

        Integer minShingleSize = obj.getMinShingleSize();
        shingleTokenFilter.setMinShingleSize(minShingleSize);

        Boolean outputUnigrams = obj.areOutputUnigrams();
        shingleTokenFilter.setOutputUnigrams(outputUnigrams);

        String filterToken = obj.getFilterToken();
        shingleTokenFilter.setFilterToken(filterToken);

        Boolean outputUnigramsIfNoShingles = obj.areOutputUnigramsIfNoShingles();
        shingleTokenFilter.setOutputUnigramsIfNoShingles(outputUnigramsIfNoShingles);

        Integer maxShingleSize = obj.getMaxShingleSize();
        shingleTokenFilter.setMaxShingleSize(maxShingleSize);

        String tokenSeparator = obj.getTokenSeparator();
        shingleTokenFilter.setTokenSeparator(tokenSeparator);

        return shingleTokenFilter;
    }

    private ShingleTokenFilterConverter() {
    }
}
