package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.KeyPhraseExtractionSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.KeyPhraseExtractionSkill} and
 * {@link KeyPhraseExtractionSkill} mismatch.
 */
public final class KeyPhraseExtractionSkillConverter {
    public static KeyPhraseExtractionSkill convert(com.azure.search.documents.models.KeyPhraseExtractionSkill obj) {
        return DefaultConverter.convert(obj, KeyPhraseExtractionSkill.class);
    }

    public static com.azure.search.documents.models.KeyPhraseExtractionSkill convert(KeyPhraseExtractionSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.KeyPhraseExtractionSkill.class);
    }
}
