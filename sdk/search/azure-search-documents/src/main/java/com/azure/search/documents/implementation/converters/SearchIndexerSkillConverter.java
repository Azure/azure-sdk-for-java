package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.models.ConditionalSkill;
import com.azure.search.documents.implementation.models.EntityRecognitionSkill;
import com.azure.search.documents.implementation.models.ImageAnalysisSkill;
import com.azure.search.documents.implementation.models.InputFieldMappingEntry;
import com.azure.search.documents.implementation.models.KeyPhraseExtractionSkill;
import com.azure.search.documents.implementation.models.LanguageDetectionSkill;
import com.azure.search.documents.implementation.models.MergeSkill;
import com.azure.search.documents.implementation.models.OcrSkill;
import com.azure.search.documents.implementation.models.OutputFieldMappingEntry;
import com.azure.search.documents.implementation.models.SentimentSkill;
import com.azure.search.documents.implementation.models.ShaperSkill;
import com.azure.search.documents.implementation.models.SplitSkill;
import com.azure.search.documents.implementation.models.TextTranslationSkill;
import com.azure.search.documents.implementation.models.WebApiSkill;
import com.azure.search.documents.models.SearchIndexerSkill;

import java.util.List;
import java.util.stream.Collectors;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndexerSkill} and
 * {@link SearchIndexerSkill}.
 */
public final class SearchIndexerSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerSkillConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.implementation.models.SearchIndexerSkill} to
     * {@link SearchIndexerSkill}. Dedicate works to sub class converter.
     */
    public static SearchIndexerSkill map(com.azure.search.documents.implementation.models.SearchIndexerSkill obj) {
        if (obj instanceof SplitSkill) {
            return SplitSkillConverter.map((SplitSkill) obj);
        }
        if (obj instanceof SentimentSkill) {
            return SentimentSkillConverter.map((SentimentSkill) obj);
        }
        if (obj instanceof MergeSkill) {
            return MergeSkillConverter.map((MergeSkill) obj);
        }
        if (obj instanceof LanguageDetectionSkill) {
            return LanguageDetectionSkillConverter.map((LanguageDetectionSkill) obj);
        }
        if (obj instanceof ImageAnalysisSkill) {
            return ImageAnalysisSkillConverter.map((ImageAnalysisSkill) obj);
        }
        if (obj instanceof OcrSkill) {
            return OcrSkillConverter.map((OcrSkill) obj);
        }
        if (obj instanceof ShaperSkill) {
            return ShaperSkillConverter.map((ShaperSkill) obj);
        }
        if (obj instanceof EntityRecognitionSkill) {
            return EntityRecognitionSkillConverter.map((EntityRecognitionSkill) obj);
        }
        if (obj instanceof WebApiSkill) {
            return WebApiSkillConverter.map((WebApiSkill) obj);
        }
        if (obj instanceof ConditionalSkill) {
            return ConditionalSkillConverter.map((ConditionalSkill) obj);
        }
        if (obj instanceof KeyPhraseExtractionSkill) {
            return KeyPhraseExtractionSkillConverter.map((KeyPhraseExtractionSkill) obj);
        }
        if (obj instanceof TextTranslationSkill) {
            return TextTranslationSkillConverter.map((TextTranslationSkill) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps from {@link SearchIndexerSkill} to
     * {@link com.azure.search.documents.implementation.models.SearchIndexerSkill}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndexerSkill map(SearchIndexerSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchIndexerSkill searchIndexerSkill =
            new com.azure.search.documents.implementation.models.SearchIndexerSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            searchIndexerSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            searchIndexerSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        searchIndexerSkill.setName(_name);

        String _context = obj.getContext();
        searchIndexerSkill.setContext(_context);

        String _description = obj.getDescription();
        searchIndexerSkill.setDescription(_description);
        return searchIndexerSkill;
    }
}
