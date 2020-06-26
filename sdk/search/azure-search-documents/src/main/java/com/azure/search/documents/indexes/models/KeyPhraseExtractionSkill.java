// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * A skill that uses text analytics for key phrase extraction.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Skills.Text.KeyPhraseExtractionSkill")
@Fluent
public final class KeyPhraseExtractionSkill extends SearchIndexerSkill {
    /*
     * A value indicating which language code to use. Default is en. Possible
     * values include: 'da', 'nl', 'en', 'fi', 'fr', 'de', 'it', 'ja', 'ko',
     * 'no', 'pl', 'pt-PT', 'pt-BR', 'ru', 'es', 'sv'
     */
    @JsonProperty(value = "defaultLanguageCode")
    private KeyPhraseExtractionSkillLanguage defaultLanguageCode;

    /*
     * A number indicating how many key phrases to return. If absent, all
     * identified key phrases will be returned.
     */
    @JsonProperty(value = "maxKeyPhraseCount")
    private Integer maxKeyPhraseCount;

    /**
     * Constructor of {@link KeyPhraseExtractionSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     * that can be consumed as an input by another skill.
     */
    public KeyPhraseExtractionSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        super(inputs, outputs);
    }

    /**
     * Get the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'da', 'nl', 'en',
     * 'fi', 'fr', 'de', 'it', 'ja', 'ko', 'no', 'pl', 'pt-PT', 'pt-BR', 'ru',
     * 'es', 'sv'.
     *
     * @return the defaultLanguageCode value.
     */
    public KeyPhraseExtractionSkillLanguage getDefaultLanguageCode() {
        return this.defaultLanguageCode;
    }

    /**
     * Set the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'da', 'nl', 'en',
     * 'fi', 'fr', 'de', 'it', 'ja', 'ko', 'no', 'pl', 'pt-PT', 'pt-BR', 'ru',
     * 'es', 'sv'.
     *
     * @param defaultLanguageCode the defaultLanguageCode value to set.
     * @return the KeyPhraseExtractionSkill object itself.
     */
    public KeyPhraseExtractionSkill setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
        return this;
    }

    /**
     * Get the maxKeyPhraseCount property: A number indicating how many key
     * phrases to return. If absent, all identified key phrases will be
     * returned.
     *
     * @return the maxKeyPhraseCount value.
     */
    public Integer getMaxKeyPhraseCount() {
        return this.maxKeyPhraseCount;
    }

    /**
     * Set the maxKeyPhraseCount property: A number indicating how many key
     * phrases to return. If absent, all identified key phrases will be
     * returned.
     *
     * @param maxKeyPhraseCount the maxKeyPhraseCount value to set.
     * @return the KeyPhraseExtractionSkill object itself.
     */
    public KeyPhraseExtractionSkill setMaxKeyPhraseCount(Integer maxKeyPhraseCount) {
        this.maxKeyPhraseCount = maxKeyPhraseCount;
        return this;
    }
}
