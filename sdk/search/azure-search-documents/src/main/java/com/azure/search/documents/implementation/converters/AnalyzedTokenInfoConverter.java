// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.AnalyzedTokenInfo} and
 * {@link AnalyzedTokenInfo}.
 */
public final class AnalyzedTokenInfoConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.AnalyzedTokenInfo} to
     * {@link AnalyzedTokenInfo}.
     */
    public static AnalyzedTokenInfo map(com.azure.search.documents.indexes.implementation.models.AnalyzedTokenInfo obj) {
        if (obj == null) {
            return null;
        }
        return new AnalyzedTokenInfo(obj.getToken(), obj.getStartOffset(),
            obj.getEndOffset(), obj.getPosition());
    }

    /**
     * Maps from {@link AnalyzedTokenInfo} to
     * {@link com.azure.search.documents.indexes.implementation.models.AnalyzedTokenInfo}.
     */
    public static com.azure.search.documents.indexes.implementation.models.AnalyzedTokenInfo map(AnalyzedTokenInfo obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.AnalyzedTokenInfo analyzedTokenInfo =
            new com.azure.search.documents.indexes.implementation.models.AnalyzedTokenInfo(obj.getToken(),
                obj.getStartOffset(), obj.getEndOffset(), obj.getPosition());
        analyzedTokenInfo.validate();
        return analyzedTokenInfo;
    }

    private AnalyzedTokenInfoConverter() {
    }
}
