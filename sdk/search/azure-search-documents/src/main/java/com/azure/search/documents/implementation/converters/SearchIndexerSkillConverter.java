// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.implementation.models.ConditionalSkill;
import com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkill;
import com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkill;
import com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkill;
import com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill;
import com.azure.search.documents.indexes.implementation.models.MergeSkill;
import com.azure.search.documents.indexes.implementation.models.OcrSkill;
import com.azure.search.documents.indexes.implementation.models.SentimentSkill;
import com.azure.search.documents.indexes.implementation.models.ShaperSkill;
import com.azure.search.documents.indexes.implementation.models.SplitSkill;
import com.azure.search.documents.indexes.implementation.models.TextTranslationSkill;
import com.azure.search.documents.indexes.implementation.models.WebApiSkill;
import com.azure.search.documents.indexes.models.SearchIndexerSkill;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerSkill} and
 * {@link SearchIndexerSkill}.
 */
public final class SearchIndexerSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerSkillConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerSkill} to
     * {@link SearchIndexerSkill}. Dedicate works to sub class converter.
     */
    public static SearchIndexerSkill map(com.azure.search.documents.indexes.implementation.models.SearchIndexerSkill obj) {
        if (obj instanceof TextTranslationSkill) {
            return TextTranslationSkillConverter.map((TextTranslationSkill) obj);
        }
        if (obj instanceof EntityRecognitionSkill) {
            return EntityRecognitionSkillConverter.map((EntityRecognitionSkill) obj);
        }
        if (obj instanceof SentimentSkill) {
            return SentimentSkillConverter.map((SentimentSkill) obj);
        }
        if (obj instanceof LanguageDetectionSkill) {
            return LanguageDetectionSkillConverter.map((LanguageDetectionSkill) obj);
        }
        if (obj instanceof ConditionalSkill) {
            return ConditionalSkillConverter.map((ConditionalSkill) obj);
        }
        if (obj instanceof ImageAnalysisSkill) {
            return ImageAnalysisSkillConverter.map((ImageAnalysisSkill) obj);
        }
        if (obj instanceof ShaperSkill) {
            return ShaperSkillConverter.map((ShaperSkill) obj);
        }
        if (obj instanceof KeyPhraseExtractionSkill) {
            return KeyPhraseExtractionSkillConverter.map((KeyPhraseExtractionSkill) obj);
        }
        if (obj instanceof MergeSkill) {
            return MergeSkillConverter.map((MergeSkill) obj);
        }
        if (obj instanceof SplitSkill) {
            return SplitSkillConverter.map((SplitSkill) obj);
        }
        if (obj instanceof WebApiSkill) {
            return WebApiSkillConverter.map((WebApiSkill) obj);
        }
        if (obj instanceof OcrSkill) {
            return OcrSkillConverter.map((OcrSkill) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link SearchIndexerSkill} to
     * {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerSkill}. Dedicate works to sub class
     * converter.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchIndexerSkill map(SearchIndexerSkill obj) {
        if (obj instanceof com.azure.search.documents.indexes.models.LanguageDetectionSkill) {
            return LanguageDetectionSkillConverter.map((com.azure.search.documents.indexes.models.LanguageDetectionSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.WebApiSkill) {
            return WebApiSkillConverter.map((com.azure.search.documents.indexes.models.WebApiSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.OcrSkill) {
            return OcrSkillConverter.map((com.azure.search.documents.indexes.models.OcrSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.TextTranslationSkill) {
            return TextTranslationSkillConverter.map((com.azure.search.documents.indexes.models.TextTranslationSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.EntityRecognitionSkill) {
            return EntityRecognitionSkillConverter.map((com.azure.search.documents.indexes.models.EntityRecognitionSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.ImageAnalysisSkill) {
            return ImageAnalysisSkillConverter.map((com.azure.search.documents.indexes.models.ImageAnalysisSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.SplitSkill) {
            return SplitSkillConverter.map((com.azure.search.documents.indexes.models.SplitSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.KeyPhraseExtractionSkill) {
            return KeyPhraseExtractionSkillConverter.map((com.azure.search.documents.indexes.models.KeyPhraseExtractionSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.SentimentSkill) {
            return SentimentSkillConverter.map((com.azure.search.documents.indexes.models.SentimentSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.ConditionalSkill) {
            return ConditionalSkillConverter.map((com.azure.search.documents.indexes.models.ConditionalSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.ShaperSkill) {
            return ShaperSkillConverter.map((com.azure.search.documents.indexes.models.ShaperSkill) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.models.MergeSkill) {
            return MergeSkillConverter.map((com.azure.search.documents.indexes.models.MergeSkill) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private SearchIndexerSkillConverter() {
    }
}
