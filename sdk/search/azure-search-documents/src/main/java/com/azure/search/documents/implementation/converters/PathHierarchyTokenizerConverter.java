// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.PathHierarchyTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.PathHierarchyTokenizerV2} and
 * {@link PathHierarchyTokenizer}.
 */
public final class PathHierarchyTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.PathHierarchyTokenizerV2} to
     * {@link PathHierarchyTokenizer}.
     */
    public static PathHierarchyTokenizer map(com.azure.search.documents.indexes.implementation.models.PathHierarchyTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        PathHierarchyTokenizer pathHierarchyTokenizer = new PathHierarchyTokenizer(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        pathHierarchyTokenizer.setMaxTokenLength(maxTokenLength);

        Character delimiter = obj.getDelimiter();
        pathHierarchyTokenizer.setDelimiter(delimiter);

        Boolean reverseTokenOrder = obj.isReverseTokenOrder();
        pathHierarchyTokenizer.setTokenOrderReversed(reverseTokenOrder);

        Integer numberOfTokensToSkip = obj.getNumberOfTokensToSkip();
        pathHierarchyTokenizer.setNumberOfTokensToSkip(numberOfTokensToSkip);

        Character replacement = obj.getReplacement();
        pathHierarchyTokenizer.setReplacement(replacement);
        return pathHierarchyTokenizer;
    }

    /**
     * Maps from {@link PathHierarchyTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.PathHierarchyTokenizerV2}.
     */
    public static com.azure.search.documents.indexes.implementation.models.PathHierarchyTokenizerV2 map(PathHierarchyTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.PathHierarchyTokenizerV2 pathHierarchyTokenizerV2 =
            new com.azure.search.documents.indexes.implementation.models.PathHierarchyTokenizerV2(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        pathHierarchyTokenizerV2.setMaxTokenLength(maxTokenLength);

        Character delimiter = obj.getDelimiter();
        pathHierarchyTokenizerV2.setDelimiter(delimiter);

        Boolean reverseTokenOrder = obj.isTokenOrderReversed();
        pathHierarchyTokenizerV2.setReverseTokenOrder(reverseTokenOrder);

        Integer numberOfTokensToSkip = obj.getNumberOfTokensToSkip();
        pathHierarchyTokenizerV2.setNumberOfTokensToSkip(numberOfTokensToSkip);

        Character replacement = obj.getReplacement();
        pathHierarchyTokenizerV2.setReplacement(replacement);

        return pathHierarchyTokenizerV2;
    }

    private PathHierarchyTokenizerConverter() {
    }
}
