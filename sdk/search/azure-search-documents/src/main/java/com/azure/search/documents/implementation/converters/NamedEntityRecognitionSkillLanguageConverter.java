package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.NamedEntityRecognitionSkillLanguage;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.NamedEntityRecognitionSkillLanguage} and
 * {@link NamedEntityRecognitionSkillLanguage} mismatch.
 */
public final class NamedEntityRecognitionSkillLanguageConverter {
    public static NamedEntityRecognitionSkillLanguage convert(com.azure.search.documents.models.NamedEntityRecognitionSkillLanguage obj) {
        return DefaultConverter.convert(obj, NamedEntityRecognitionSkillLanguage.class);
    }

    public static com.azure.search.documents.models.NamedEntityRecognitionSkillLanguage convert(NamedEntityRecognitionSkillLanguage obj) {
        return DefaultConverter.convert(obj,
            com.azure.search.documents.models.NamedEntityRecognitionSkillLanguage.class);
    }
}
