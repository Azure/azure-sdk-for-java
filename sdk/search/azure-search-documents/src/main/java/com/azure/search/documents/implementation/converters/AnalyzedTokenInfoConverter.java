// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
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
        AnalyzedTokenInfo analyzedTokenInfo = new AnalyzedTokenInfo();

        int endOffset = obj.getEndOffset();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "endOffset", endOffset);

        int startOffset = obj.getStartOffset();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "startOffset", startOffset);

        int position = obj.getPosition();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "position", position);

        String token = obj.getToken();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "token", token);
        return analyzedTokenInfo;
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
            new com.azure.search.documents.indexes.implementation.models.AnalyzedTokenInfo();

        int endOffset = obj.getEndOffset();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "endOffset", endOffset);

        int startOffset = obj.getStartOffset();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "startOffset", startOffset);

        int position = obj.getPosition();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "position", position);

        String token = obj.getToken();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "token", token);
        return analyzedTokenInfo;
    }

    private AnalyzedTokenInfoConverter() {
    }
}
