// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.AnalyzedTokenInfo;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.AnalyzedTokenInfo} and
 * {@link AnalyzedTokenInfo} mismatch.
 */
public final class AnalyzedTokenInfoConverter {
    public static AnalyzedTokenInfo convert(com.azure.search.documents.models.AnalyzedTokenInfo obj) {
        return DefaultConverter.convert(obj, AnalyzedTokenInfo.class);
    }

    public static com.azure.search.documents.models.AnalyzedTokenInfo convert(AnalyzedTokenInfo obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.AnalyzedTokenInfo.class);
    }
}
