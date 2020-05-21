// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.models.ClassicTokenizer;
import com.azure.search.documents.implementation.models.EdgeNGramTokenizer;
import com.azure.search.documents.implementation.models.KeywordTokenizer;
import com.azure.search.documents.implementation.models.KeywordTokenizerV2;
import com.azure.search.documents.implementation.models.LuceneStandardTokenizer;
import com.azure.search.documents.implementation.models.LuceneStandardTokenizerV2;
import com.azure.search.documents.implementation.models.MicrosoftLanguageStemmingTokenizer;
import com.azure.search.documents.implementation.models.MicrosoftLanguageTokenizer;
import com.azure.search.documents.implementation.models.NGramTokenizer;
import com.azure.search.documents.implementation.models.PathHierarchyTokenizerV2;
import com.azure.search.documents.implementation.models.PatternTokenizer;
import com.azure.search.documents.implementation.models.UaxUrlEmailTokenizer;
import com.azure.search.documents.models.LexicalTokenizer;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.LexicalTokenizer} and
 * {@link LexicalTokenizer}.
 */
public final class LexicalTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(LexicalTokenizerConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.implementation.models.LexicalTokenizer} to
     * {@link LexicalTokenizer}. Dedicate works to sub class converter.
     */
    public static LexicalTokenizer map(com.azure.search.documents.implementation.models.LexicalTokenizer obj) {
        if (obj instanceof PatternTokenizer) {
            return PatternTokenizerConverter.map((PatternTokenizer) obj);
        }
        if (obj instanceof NGramTokenizer) {
            return NGramTokenizerConverter.map((NGramTokenizer) obj);
        }
        if (obj instanceof LuceneStandardTokenizer) {
            return LuceneStandardTokenizerConverter.map((LuceneStandardTokenizer) obj);
        }
        if (obj instanceof PathHierarchyTokenizerV2) {
            return PathHierarchyTokenizerV2Converter.map((PathHierarchyTokenizerV2) obj);
        }
        if (obj instanceof ClassicTokenizer) {
            return ClassicTokenizerConverter.map((ClassicTokenizer) obj);
        }
        if (obj instanceof KeywordTokenizer) {
            return KeywordTokenizerConverter.map((KeywordTokenizer) obj);
        }
        if (obj instanceof LuceneStandardTokenizerV2) {
            return LuceneStandardTokenizerV2Converter.map((LuceneStandardTokenizerV2) obj);
        }
        if (obj instanceof UaxUrlEmailTokenizer) {
            return UaxUrlEmailTokenizerConverter.map((UaxUrlEmailTokenizer) obj);
        }
        if (obj instanceof KeywordTokenizerV2) {
            return KeywordTokenizerV2Converter.map((KeywordTokenizerV2) obj);
        }
        if (obj instanceof MicrosoftLanguageTokenizer) {
            return MicrosoftLanguageTokenizerConverter.map((MicrosoftLanguageTokenizer) obj);
        }
        if (obj instanceof EdgeNGramTokenizer) {
            return EdgeNGramTokenizerConverter.map((EdgeNGramTokenizer) obj);
        }
        if (obj instanceof MicrosoftLanguageStemmingTokenizer) {
            return MicrosoftLanguageStemmingTokenizerConverter.map((MicrosoftLanguageStemmingTokenizer) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link LexicalTokenizer} to
     * {@link com.azure.search.documents.implementation.models.LexicalTokenizer}. Dedicate works to sub class converter.
     */
    public static com.azure.search.documents.implementation.models.LexicalTokenizer map(LexicalTokenizer obj) {
        if (obj instanceof com.azure.search.documents.models.PatternTokenizer) {
            return PatternTokenizerConverter.map((com.azure.search.documents.models.PatternTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.EdgeNGramTokenizer) {
            return EdgeNGramTokenizerConverter.map((com.azure.search.documents.models.EdgeNGramTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.MicrosoftLanguageStemmingTokenizer) {
            return MicrosoftLanguageStemmingTokenizerConverter.map((com.azure.search.documents.models.MicrosoftLanguageStemmingTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.KeywordTokenizerV2) {
            return KeywordTokenizerV2Converter.map((com.azure.search.documents.models.KeywordTokenizerV2) obj);
        }
        if (obj instanceof com.azure.search.documents.models.KeywordTokenizer) {
            return KeywordTokenizerConverter.map((com.azure.search.documents.models.KeywordTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.MicrosoftLanguageTokenizer) {
            return MicrosoftLanguageTokenizerConverter.map((com.azure.search.documents.models.MicrosoftLanguageTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.LuceneStandardTokenizerV2) {
            return LuceneStandardTokenizerV2Converter.map((com.azure.search.documents.models.LuceneStandardTokenizerV2) obj);
        }
        if (obj instanceof com.azure.search.documents.models.LuceneStandardTokenizer) {
            return LuceneStandardTokenizerConverter.map((com.azure.search.documents.models.LuceneStandardTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.UaxUrlEmailTokenizer) {
            return UaxUrlEmailTokenizerConverter.map((com.azure.search.documents.models.UaxUrlEmailTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.PathHierarchyTokenizerV2) {
            return PathHierarchyTokenizerV2Converter.map((com.azure.search.documents.models.PathHierarchyTokenizerV2) obj);
        }
        if (obj instanceof com.azure.search.documents.models.ClassicTokenizer) {
            return ClassicTokenizerConverter.map((com.azure.search.documents.models.ClassicTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.models.NGramTokenizer) {
            return NGramTokenizerConverter.map((com.azure.search.documents.models.NGramTokenizer) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private LexicalTokenizerConverter() {
    }
}
