// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.ClassicTokenizer;
import com.azure.search.documents.indexes.models.EdgeNGramTokenizer;
import com.azure.search.documents.indexes.models.KeywordTokenizer;
import com.azure.search.documents.indexes.models.LuceneStandardTokenizer;
import com.azure.search.documents.indexes.models.MicrosoftLanguageStemmingTokenizer;
import com.azure.search.documents.indexes.models.MicrosoftLanguageTokenizer;
import com.azure.search.documents.indexes.models.NGramTokenizer;
import com.azure.search.documents.indexes.models.PathHierarchyTokenizer;
import com.azure.search.documents.indexes.models.PatternTokenizer;
import com.azure.search.documents.indexes.models.UaxUrlEmailTokenizer;
import com.azure.search.documents.indexes.models.LexicalTokenizer;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.LexicalTokenizer} and
 * {@link LexicalTokenizer}.
 */
public final class LexicalTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(LexicalTokenizerConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.indexes.implementation.models.LexicalTokenizer} to
     * {@link LexicalTokenizer}. Dedicate works to sub class converter.
     */
    public static LexicalTokenizer map(com.azure.search.documents.indexes.implementation.models.LexicalTokenizer obj) {
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.PatternTokenizer) {
            return PatternTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.PatternTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.NGramTokenizer) {
            return NGramTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.NGramTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizer) {
            return LuceneStandardTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.PathHierarchyTokenizerV2) {
            return PathHierarchyTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.PathHierarchyTokenizerV2) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.ClassicTokenizer) {
            return ClassicTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.ClassicTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.KeywordTokenizer) {
            return KeywordTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.KeywordTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2) {
            return LuceneStandardTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.UaxUrlEmailTokenizer) {
            return UaxUrlEmailTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.UaxUrlEmailTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2) {
            return KeywordTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer) {
            return MicrosoftLanguageTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer) {
            return EdgeNGramTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenizer) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer) {
            return MicrosoftLanguageStemmingTokenizerConverter.map((com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link LexicalTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.LexicalTokenizer}. Dedicate works to sub class converter.
     */
    public static com.azure.search.documents.indexes.implementation.models.LexicalTokenizer map(LexicalTokenizer obj) {
        if (obj instanceof PatternTokenizer) {
            return PatternTokenizerConverter.map((PatternTokenizer) obj);
        }
        if (obj instanceof EdgeNGramTokenizer) {
            return EdgeNGramTokenizerConverter.map((EdgeNGramTokenizer) obj);
        }
        if (obj instanceof MicrosoftLanguageStemmingTokenizer) {
            return MicrosoftLanguageStemmingTokenizerConverter.map((MicrosoftLanguageStemmingTokenizer) obj);
        }
        if (obj instanceof KeywordTokenizer) {
            return KeywordTokenizerConverter.map((KeywordTokenizer) obj);
        }
        if (obj instanceof MicrosoftLanguageTokenizer) {
            return MicrosoftLanguageTokenizerConverter.map((MicrosoftLanguageTokenizer) obj);
        }
        if (obj instanceof LuceneStandardTokenizer) {
            return LuceneStandardTokenizerConverter.map((LuceneStandardTokenizer) obj);
        }
        if (obj instanceof UaxUrlEmailTokenizer) {
            return UaxUrlEmailTokenizerConverter.map((UaxUrlEmailTokenizer) obj);
        }
        if (obj instanceof PathHierarchyTokenizer) {
            return PathHierarchyTokenizerConverter.map((PathHierarchyTokenizer) obj);
        }
        if (obj instanceof ClassicTokenizer) {
            return ClassicTokenizerConverter.map((ClassicTokenizer) obj);
        }
        if (obj instanceof NGramTokenizer) {
            return NGramTokenizerConverter.map((NGramTokenizer) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private LexicalTokenizerConverter() {
    }
}
