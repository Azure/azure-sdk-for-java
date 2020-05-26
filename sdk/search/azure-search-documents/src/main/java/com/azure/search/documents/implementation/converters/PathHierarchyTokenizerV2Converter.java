// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.PathHierarchyTokenizerV2;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.PathHierarchyTokenizerV2} and
 * {@link PathHierarchyTokenizerV2}.
 */
public final class PathHierarchyTokenizerV2Converter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.PathHierarchyTokenizerV2} to
     * {@link PathHierarchyTokenizerV2}.
     */
    public static PathHierarchyTokenizerV2 map(com.azure.search.documents.implementation.models.PathHierarchyTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        PathHierarchyTokenizerV2 pathHierarchyTokenizerV2 = new PathHierarchyTokenizerV2();

        String name = obj.getName();
        pathHierarchyTokenizerV2.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        pathHierarchyTokenizerV2.setMaxTokenLength(maxTokenLength);

        String delimiter = obj.getDelimiter();
        pathHierarchyTokenizerV2.setDelimiter(delimiter);

        Boolean reverseTokenOrder = obj.isReverseTokenOrder();
        pathHierarchyTokenizerV2.setReverseTokenOrder(reverseTokenOrder);

        Integer numberOfTokensToSkip = obj.getNumberOfTokensToSkip();
        pathHierarchyTokenizerV2.setNumberOfTokensToSkip(numberOfTokensToSkip);

        String replacement = obj.getReplacement();
        pathHierarchyTokenizerV2.setReplacement(replacement);
        return pathHierarchyTokenizerV2;
    }

    /**
     * Maps from {@link PathHierarchyTokenizerV2} to
     * {@link com.azure.search.documents.implementation.models.PathHierarchyTokenizerV2}.
     */
    public static com.azure.search.documents.implementation.models.PathHierarchyTokenizerV2 map(PathHierarchyTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.PathHierarchyTokenizerV2 pathHierarchyTokenizerV2 =
            new com.azure.search.documents.implementation.models.PathHierarchyTokenizerV2();

        String name = obj.getName();
        pathHierarchyTokenizerV2.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        pathHierarchyTokenizerV2.setMaxTokenLength(maxTokenLength);

        String delimiter = obj.getDelimiter();
        pathHierarchyTokenizerV2.setDelimiter(delimiter);

        Boolean reverseTokenOrder = obj.isReverseTokenOrder();
        pathHierarchyTokenizerV2.setReverseTokenOrder(reverseTokenOrder);

        Integer numberOfTokensToSkip = obj.getNumberOfTokensToSkip();
        pathHierarchyTokenizerV2.setNumberOfTokensToSkip(numberOfTokensToSkip);

        String replacement = obj.getReplacement();
        pathHierarchyTokenizerV2.setReplacement(replacement);
        return pathHierarchyTokenizerV2;
    }

    private PathHierarchyTokenizerV2Converter() {
    }
}
