// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ShingleTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ShingleTokenFilter} and
 * {@link ShingleTokenFilter}.
 */
public final class ShingleTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ShingleTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ShingleTokenFilter} to
     * {@link ShingleTokenFilter}.
     */
    public static ShingleTokenFilter map(com.azure.search.documents.implementation.models.ShingleTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        ShingleTokenFilter shingleTokenFilter = new ShingleTokenFilter();

        String _name = obj.getName();
        shingleTokenFilter.setName(_name);

        Integer _minShingleSize = obj.getMinShingleSize();
        shingleTokenFilter.setMinShingleSize(_minShingleSize);

        Boolean _outputUnigrams = obj.isOutputUnigrams();
        shingleTokenFilter.setOutputUnigrams(_outputUnigrams);

        String _filterToken = obj.getFilterToken();
        shingleTokenFilter.setFilterToken(_filterToken);

        Boolean _outputUnigramsIfNoShingles = obj.isOutputUnigramsIfNoShingles();
        shingleTokenFilter.setOutputUnigramsIfNoShingles(_outputUnigramsIfNoShingles);

        Integer _maxShingleSize = obj.getMaxShingleSize();
        shingleTokenFilter.setMaxShingleSize(_maxShingleSize);

        String _tokenSeparator = obj.getTokenSeparator();
        shingleTokenFilter.setTokenSeparator(_tokenSeparator);
        return shingleTokenFilter;
    }

    /**
     * Maps from {@link ShingleTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.ShingleTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.ShingleTokenFilter map(ShingleTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ShingleTokenFilter shingleTokenFilter =
            new com.azure.search.documents.implementation.models.ShingleTokenFilter();

        String _name = obj.getName();
        shingleTokenFilter.setName(_name);

        Integer _minShingleSize = obj.getMinShingleSize();
        shingleTokenFilter.setMinShingleSize(_minShingleSize);

        Boolean _outputUnigrams = obj.isOutputUnigrams();
        shingleTokenFilter.setOutputUnigrams(_outputUnigrams);

        String _filterToken = obj.getFilterToken();
        shingleTokenFilter.setFilterToken(_filterToken);

        Boolean _outputUnigramsIfNoShingles = obj.isOutputUnigramsIfNoShingles();
        shingleTokenFilter.setOutputUnigramsIfNoShingles(_outputUnigramsIfNoShingles);

        Integer _maxShingleSize = obj.getMaxShingleSize();
        shingleTokenFilter.setMaxShingleSize(_maxShingleSize);

        String _tokenSeparator = obj.getTokenSeparator();
        shingleTokenFilter.setTokenSeparator(_tokenSeparator);
        return shingleTokenFilter;
    }
}
