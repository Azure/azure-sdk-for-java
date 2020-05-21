// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ClassicTokenizer;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ClassicTokenizer} and
 * {@link ClassicTokenizer}.
 */
public final class ClassicTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ClassicTokenizerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ClassicTokenizer} to {@link ClassicTokenizer}.
     */
    public static ClassicTokenizer map(com.azure.search.documents.implementation.models.ClassicTokenizer obj) {
        if (obj == null) {
            return null;
        }
        ClassicTokenizer classicTokenizer = new ClassicTokenizer();

        String _name = obj.getName();
        classicTokenizer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        classicTokenizer.setMaxTokenLength(_maxTokenLength);
        return classicTokenizer;
    }

    /**
     * Maps from {@link ClassicTokenizer} to {@link com.azure.search.documents.implementation.models.ClassicTokenizer}.
     */
    public static com.azure.search.documents.implementation.models.ClassicTokenizer map(ClassicTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ClassicTokenizer classicTokenizer =
            new com.azure.search.documents.implementation.models.ClassicTokenizer();

        String _name = obj.getName();
        classicTokenizer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        classicTokenizer.setMaxTokenLength(_maxTokenLength);
        return classicTokenizer;
    }
}
