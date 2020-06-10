// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.implementation.models.AsciiFoldingTokenFilter;
import com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilter;
import com.azure.search.documents.indexes.implementation.models.CommonGramTokenFilter;
import com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter;
import com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilter;
import com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterV2;
import com.azure.search.documents.indexes.implementation.models.ElisionTokenFilter;
import com.azure.search.documents.indexes.implementation.models.KeepTokenFilter;
import com.azure.search.documents.indexes.implementation.models.KeywordMarkerTokenFilter;
import com.azure.search.documents.indexes.implementation.models.LengthTokenFilter;
import com.azure.search.documents.indexes.implementation.models.LimitTokenFilter;
import com.azure.search.documents.indexes.implementation.models.NGramTokenFilter;
import com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2;
import com.azure.search.documents.indexes.implementation.models.PatternCaptureTokenFilter;
import com.azure.search.documents.indexes.implementation.models.PatternReplaceTokenFilter;
import com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter;
import com.azure.search.documents.indexes.implementation.models.ShingleTokenFilter;
import com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter;
import com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter;
import com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter;
import com.azure.search.documents.indexes.implementation.models.StopwordsTokenFilter;
import com.azure.search.documents.indexes.implementation.models.SynonymTokenFilter;
import com.azure.search.documents.indexes.implementation.models.TruncateTokenFilter;
import com.azure.search.documents.indexes.implementation.models.UniqueTokenFilter;
import com.azure.search.documents.indexes.implementation.models.WordDelimiterTokenFilter;
import com.azure.search.documents.indexes.models.TokenFilter;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.TokenFilter} and {@link TokenFilter}.
 */
public final class TokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TokenFilterConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.indexes.implementation.models.TokenFilter} to
     * {@link TokenFilter}. Dedicate works to sub class converter.
     */
    public static TokenFilter map(com.azure.search.documents.indexes.implementation.models.TokenFilter obj) {
        if (obj instanceof CommonGramTokenFilter) {
            return CommonGramTokenFilterConverter.map((CommonGramTokenFilter) obj);
        }
        if (obj instanceof KeepTokenFilter) {
            return KeepTokenFilterConverter.map((KeepTokenFilter) obj);
        }
        if (obj instanceof StemmerOverrideTokenFilter) {
            return StemmerOverrideTokenFilterConverter.map((StemmerOverrideTokenFilter) obj);
        }
        if (obj instanceof SynonymTokenFilter) {
            return SynonymTokenFilterConverter.map((SynonymTokenFilter) obj);
        }
        if (obj instanceof DictionaryDecompounderTokenFilter) {
            return DictionaryDecompounderTokenFilterConverter.map((DictionaryDecompounderTokenFilter) obj);
        }
        if (obj instanceof LengthTokenFilter) {
            return LengthTokenFilterConverter.map((LengthTokenFilter) obj);
        }
        if (obj instanceof UniqueTokenFilter) {
            return UniqueTokenFilterConverter.map((UniqueTokenFilter) obj);
        }
        if (obj instanceof KeywordMarkerTokenFilter) {
            return KeywordMarkerTokenFilterConverter.map((KeywordMarkerTokenFilter) obj);
        }
        if (obj instanceof CjkBigramTokenFilter) {
            return CjkBigramTokenFilterConverter.map((CjkBigramTokenFilter) obj);
        }
        if (obj instanceof EdgeNGramTokenFilterV2) {
            return EdgeNGramTokenFilterConverter.map((EdgeNGramTokenFilterV2) obj);
        }
        if (obj instanceof PatternCaptureTokenFilter) {
            return PatternCaptureTokenFilterConverter.map((PatternCaptureTokenFilter) obj);
        }
        if (obj instanceof NGramTokenFilterV2) {
            return NGramTokenFilterConverter.map((NGramTokenFilterV2) obj);
        }
        if (obj instanceof PatternReplaceTokenFilter) {
            return PatternReplaceTokenFilterConverter.map((PatternReplaceTokenFilter) obj);
        }
        if (obj instanceof NGramTokenFilter) {
            return NGramTokenFilterConverter.map((NGramTokenFilter) obj);
        }
        if (obj instanceof ShingleTokenFilter) {
            return ShingleTokenFilterConverter.map((ShingleTokenFilter) obj);
        }
        if (obj instanceof LimitTokenFilter) {
            return LimitTokenFilterConverter.map((LimitTokenFilter) obj);
        }
        if (obj instanceof PhoneticTokenFilter) {
            return PhoneticTokenFilterConverter.map((PhoneticTokenFilter) obj);
        }
        if (obj instanceof StopwordsTokenFilter) {
            return StopwordsTokenFilterConverter.map((StopwordsTokenFilter) obj);
        }
        if (obj instanceof WordDelimiterTokenFilter) {
            return WordDelimiterTokenFilterConverter.map((WordDelimiterTokenFilter) obj);
        }
        if (obj instanceof SnowballTokenFilter) {
            return SnowballTokenFilterConverter.map((SnowballTokenFilter) obj);
        }
        if (obj instanceof AsciiFoldingTokenFilter) {
            return AsciiFoldingTokenFilterConverter.map((AsciiFoldingTokenFilter) obj);
        }
        if (obj instanceof EdgeNGramTokenFilter) {
            return EdgeNGramTokenFilterConverter.map((EdgeNGramTokenFilter) obj);
        }
        if (obj instanceof TruncateTokenFilter) {
            return TruncateTokenFilterConverter.map((TruncateTokenFilter) obj);
        }
        if (obj instanceof StemmerTokenFilter) {
            return StemmerTokenFilterConverter.map((StemmerTokenFilter) obj);
        }
        if (obj instanceof ElisionTokenFilter) {
            return ElisionTokenFilterConverter.map((ElisionTokenFilter) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link TokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.TokenFilter}. Dedicate works to sub class converter.
     */
    public static com.azure.search.documents.indexes.implementation.models.TokenFilter map(TokenFilter obj) {
        if (obj instanceof com.azure.search.documents.indexes.models.CjkBigramTokenFilter) {
            return CjkBigramTokenFilterConverter.map((com.azure.search.documents.indexes.models.CjkBigramTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.ElisionTokenFilter) {
            return ElisionTokenFilterConverter.map((com.azure.search.documents.indexes.models.ElisionTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.SynonymTokenFilter) {
            return SynonymTokenFilterConverter.map((com.azure.search.documents.indexes.models.SynonymTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.LengthTokenFilter) {
            return LengthTokenFilterConverter.map((com.azure.search.documents.indexes.models.LengthTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.PatternCaptureTokenFilter) {
            return PatternCaptureTokenFilterConverter.map((com.azure.search.documents.indexes.models.PatternCaptureTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.KeepTokenFilter) {
            return KeepTokenFilterConverter.map((com.azure.search.documents.indexes.models.KeepTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.AsciiFoldingTokenFilter) {
            return AsciiFoldingTokenFilterConverter.map((com.azure.search.documents.indexes.models.AsciiFoldingTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.KeywordMarkerTokenFilter) {
            return KeywordMarkerTokenFilterConverter.map((com.azure.search.documents.indexes.models.KeywordMarkerTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.UniqueTokenFilter) {
            return UniqueTokenFilterConverter.map((com.azure.search.documents.indexes.models.UniqueTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.LimitTokenFilter) {
            return LimitTokenFilterConverter.map((com.azure.search.documents.indexes.models.LimitTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.TruncateTokenFilter) {
            return TruncateTokenFilterConverter.map((com.azure.search.documents.indexes.models.TruncateTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.EdgeNGramTokenFilter) {
            return EdgeNGramTokenFilterConverter.map((com.azure.search.documents.indexes.models.EdgeNGramTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.DictionaryDecompounderTokenFilter) {
            return DictionaryDecompounderTokenFilterConverter.map((com.azure.search.documents.indexes.models.DictionaryDecompounderTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.PatternReplaceTokenFilter) {
            return PatternReplaceTokenFilterConverter.map((com.azure.search.documents.indexes.models.PatternReplaceTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.StemmerTokenFilter) {
            return StemmerTokenFilterConverter.map((com.azure.search.documents.indexes.models.StemmerTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.PhoneticTokenFilter) {
            return PhoneticTokenFilterConverter.map((com.azure.search.documents.indexes.models.PhoneticTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.CommonGramTokenFilter) {
            return CommonGramTokenFilterConverter.map((com.azure.search.documents.indexes.models.CommonGramTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.StopwordsTokenFilter) {
            return StopwordsTokenFilterConverter.map((com.azure.search.documents.indexes.models.StopwordsTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.WordDelimiterTokenFilter) {
            return WordDelimiterTokenFilterConverter.map((com.azure.search.documents.indexes.models.WordDelimiterTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.SnowballTokenFilter) {
            return SnowballTokenFilterConverter.map((com.azure.search.documents.indexes.models.SnowballTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.NGramTokenFilter) {
            return NGramTokenFilterConverter.map((com.azure.search.documents.indexes.models.NGramTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.ShingleTokenFilter) {
            return ShingleTokenFilterConverter.map((com.azure.search.documents.indexes.models.ShingleTokenFilter) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.StemmerOverrideTokenFilter) {
            return StemmerOverrideTokenFilterConverter.map((com.azure.search.documents.indexes.models.StemmerOverrideTokenFilter) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private TokenFilterConverter() {
    }
}
