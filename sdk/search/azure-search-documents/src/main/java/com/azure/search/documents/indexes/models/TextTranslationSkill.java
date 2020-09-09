// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * A skill to translate text from one language to another.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Skills.Text.TranslationSkill")
@Fluent
public final class TextTranslationSkill extends SearchIndexerSkill {
    /*
     * The language code to translate documents into for documents that don't
     * specify the to language explicitly. Possible values include: 'af', 'ar',
     * 'bn', 'bs', 'bg', 'yue', 'ca', 'zh-Hans', 'zh-Hant', 'hr', 'cs', 'da',
     * 'nl', 'en', 'et', 'fj', 'fil', 'fi', 'fr', 'de', 'el', 'ht', 'he', 'hi',
     * 'mww', 'hu', 'is', 'id', 'it', 'ja', 'sw', 'tlh', 'ko', 'lv', 'lt',
     * 'mg', 'ms', 'mt', 'nb', 'fa', 'pl', 'pt', 'otq', 'ro', 'ru', 'sm',
     * 'sr-Cyrl', 'sr-Latn', 'sk', 'sl', 'es', 'sv', 'ty', 'ta', 'te', 'th',
     * 'to', 'tr', 'uk', 'ur', 'vi', 'cy', 'yua'
     */
    @JsonProperty(value = "defaultToLanguageCode", required = true)
    private TextTranslationSkillLanguage defaultToLanguageCode;

    /*
     * The language code to translate documents from for documents that don't
     * specify the from language explicitly. Possible values include: 'af',
     * 'ar', 'bn', 'bs', 'bg', 'yue', 'ca', 'zh-Hans', 'zh-Hant', 'hr', 'cs',
     * 'da', 'nl', 'en', 'et', 'fj', 'fil', 'fi', 'fr', 'de', 'el', 'ht', 'he',
     * 'hi', 'mww', 'hu', 'is', 'id', 'it', 'ja', 'sw', 'tlh', 'ko', 'lv',
     * 'lt', 'mg', 'ms', 'mt', 'nb', 'fa', 'pl', 'pt', 'otq', 'ro', 'ru', 'sm',
     * 'sr-Cyrl', 'sr-Latn', 'sk', 'sl', 'es', 'sv', 'ty', 'ta', 'te', 'th',
     * 'to', 'tr', 'uk', 'ur', 'vi', 'cy', 'yua'
     */
    @JsonProperty(value = "defaultFromLanguageCode")
    private TextTranslationSkillLanguage defaultFromLanguageCode;

    /*
     * The language code to translate documents from when neither the
     * fromLanguageCode input nor the defaultFromLanguageCode parameter are
     * provided, and the automatic language detection is unsuccessful. Default
     * is en. Possible values include: 'af', 'ar', 'bn', 'bs', 'bg', 'yue',
     * 'ca', 'zh-Hans', 'zh-Hant', 'hr', 'cs', 'da', 'nl', 'en', 'et', 'fj',
     * 'fil', 'fi', 'fr', 'de', 'el', 'ht', 'he', 'hi', 'mww', 'hu', 'is',
     * 'id', 'it', 'ja', 'sw', 'tlh', 'ko', 'lv', 'lt', 'mg', 'ms', 'mt', 'nb',
     * 'fa', 'pl', 'pt', 'otq', 'ro', 'ru', 'sm', 'sr-Cyrl', 'sr-Latn', 'sk',
     * 'sl', 'es', 'sv', 'ty', 'ta', 'te', 'th', 'to', 'tr', 'uk', 'ur', 'vi',
     * 'cy', 'yua'
     */
    @JsonProperty(value = "suggestedFrom")
    private TextTranslationSkillLanguage suggestedFrom;

    /**
     * Constructor of {@link SearchIndexerSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     * that can be consumed as an input by another skill.
     * @param defaultToLanguageCode The language code to translate documents into for documents that don't
     * specify the to language explicitly. Possible values include: 'af', 'ar',
     * 'bn', 'bs', 'bg', 'yue', 'ca', 'zh-Hans', 'zh-Hant', 'hr', 'cs', 'da',
     * 'nl', 'en', 'et', 'fj', 'fil', 'fi', 'fr', 'de', 'el', 'ht', 'he', 'hi',
     * 'mww', 'hu', 'is', 'id', 'it', 'ja', 'sw', 'tlh', 'ko', 'lv', 'lt',
     * 'mg', 'ms', 'mt', 'nb', 'fa', 'pl', 'pt', 'otq', 'ro', 'ru', 'sm',
     * 'sr-Cyrl', 'sr-Latn', 'sk', 'sl', 'es', 'sv', 'ty', 'ta', 'te', 'th',
     * 'to', 'tr', 'uk', 'ur', 'vi', 'cy', 'yua'
     */
    public TextTranslationSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs,
        TextTranslationSkillLanguage defaultToLanguageCode) {
        super(inputs, outputs);
        this.defaultToLanguageCode = defaultToLanguageCode;
    }

    /**
     * Get the defaultToLanguageCode property: The language code to translate
     * documents into for documents that don't specify the to language
     * explicitly. Possible values include: 'af', 'ar', 'bn', 'bs', 'bg',
     * 'yue', 'ca', 'zh-Hans', 'zh-Hant', 'hr', 'cs', 'da', 'nl', 'en', 'et',
     * 'fj', 'fil', 'fi', 'fr', 'de', 'el', 'ht', 'he', 'hi', 'mww', 'hu',
     * 'is', 'id', 'it', 'ja', 'sw', 'tlh', 'ko', 'lv', 'lt', 'mg', 'ms', 'mt',
     * 'nb', 'fa', 'pl', 'pt', 'otq', 'ro', 'ru', 'sm', 'sr-Cyrl', 'sr-Latn',
     * 'sk', 'sl', 'es', 'sv', 'ty', 'ta', 'te', 'th', 'to', 'tr', 'uk', 'ur',
     * 'vi', 'cy', 'yua'.
     *
     * @return the defaultToLanguageCode value.
     */
    public TextTranslationSkillLanguage getDefaultToLanguageCode() {
        return this.defaultToLanguageCode;
    }

    /**
     * Get the defaultFromLanguageCode property: The language code to translate
     * documents from for documents that don't specify the from language
     * explicitly. Possible values include: 'af', 'ar', 'bn', 'bs', 'bg',
     * 'yue', 'ca', 'zh-Hans', 'zh-Hant', 'hr', 'cs', 'da', 'nl', 'en', 'et',
     * 'fj', 'fil', 'fi', 'fr', 'de', 'el', 'ht', 'he', 'hi', 'mww', 'hu',
     * 'is', 'id', 'it', 'ja', 'sw', 'tlh', 'ko', 'lv', 'lt', 'mg', 'ms', 'mt',
     * 'nb', 'fa', 'pl', 'pt', 'otq', 'ro', 'ru', 'sm', 'sr-Cyrl', 'sr-Latn',
     * 'sk', 'sl', 'es', 'sv', 'ty', 'ta', 'te', 'th', 'to', 'tr', 'uk', 'ur',
     * 'vi', 'cy', 'yua'.
     *
     * @return the defaultFromLanguageCode value.
     */
    public TextTranslationSkillLanguage getDefaultFromLanguageCode() {
        return this.defaultFromLanguageCode;
    }

    /**
     * Set the defaultFromLanguageCode property: The language code to translate
     * documents from for documents that don't specify the from language
     * explicitly. Possible values include: 'af', 'ar', 'bn', 'bs', 'bg',
     * 'yue', 'ca', 'zh-Hans', 'zh-Hant', 'hr', 'cs', 'da', 'nl', 'en', 'et',
     * 'fj', 'fil', 'fi', 'fr', 'de', 'el', 'ht', 'he', 'hi', 'mww', 'hu',
     * 'is', 'id', 'it', 'ja', 'sw', 'tlh', 'ko', 'lv', 'lt', 'mg', 'ms', 'mt',
     * 'nb', 'fa', 'pl', 'pt', 'otq', 'ro', 'ru', 'sm', 'sr-Cyrl', 'sr-Latn',
     * 'sk', 'sl', 'es', 'sv', 'ty', 'ta', 'te', 'th', 'to', 'tr', 'uk', 'ur',
     * 'vi', 'cy', 'yua'.
     *
     * @param defaultFromLanguageCode the defaultFromLanguageCode value to set.
     * @return the TextTranslationSkill object itself.
     */
    public TextTranslationSkill setDefaultFromLanguageCode(TextTranslationSkillLanguage defaultFromLanguageCode) {
        this.defaultFromLanguageCode = defaultFromLanguageCode;
        return this;
    }

    /**
     * Get the suggestedFrom property: The language code to translate documents
     * from when neither the fromLanguageCode input nor the
     * defaultFromLanguageCode parameter are provided, and the automatic
     * language detection is unsuccessful. Default is en. Possible values
     * include: 'af', 'ar', 'bn', 'bs', 'bg', 'yue', 'ca', 'zh-Hans',
     * 'zh-Hant', 'hr', 'cs', 'da', 'nl', 'en', 'et', 'fj', 'fil', 'fi', 'fr',
     * 'de', 'el', 'ht', 'he', 'hi', 'mww', 'hu', 'is', 'id', 'it', 'ja', 'sw',
     * 'tlh', 'ko', 'lv', 'lt', 'mg', 'ms', 'mt', 'nb', 'fa', 'pl', 'pt',
     * 'otq', 'ro', 'ru', 'sm', 'sr-Cyrl', 'sr-Latn', 'sk', 'sl', 'es', 'sv',
     * 'ty', 'ta', 'te', 'th', 'to', 'tr', 'uk', 'ur', 'vi', 'cy', 'yua'.
     *
     * @return the suggestedFrom value.
     */
    public TextTranslationSkillLanguage getSuggestedFrom() {
        return this.suggestedFrom;
    }

    /**
     * Set the suggestedFrom property: The language code to translate documents
     * from when neither the fromLanguageCode input nor the
     * defaultFromLanguageCode parameter are provided, and the automatic
     * language detection is unsuccessful. Default is en. Possible values
     * include: 'af', 'ar', 'bn', 'bs', 'bg', 'yue', 'ca', 'zh-Hans',
     * 'zh-Hant', 'hr', 'cs', 'da', 'nl', 'en', 'et', 'fj', 'fil', 'fi', 'fr',
     * 'de', 'el', 'ht', 'he', 'hi', 'mww', 'hu', 'is', 'id', 'it', 'ja', 'sw',
     * 'tlh', 'ko', 'lv', 'lt', 'mg', 'ms', 'mt', 'nb', 'fa', 'pl', 'pt',
     * 'otq', 'ro', 'ru', 'sm', 'sr-Cyrl', 'sr-Latn', 'sk', 'sl', 'es', 'sv',
     * 'ty', 'ta', 'te', 'th', 'to', 'tr', 'uk', 'ur', 'vi', 'cy', 'yua'.
     *
     * @param suggestedFrom the suggestedFrom value to set.
     * @return the TextTranslationSkill object itself.
     */
    public TextTranslationSkill setSuggestedFrom(TextTranslationSkillLanguage suggestedFrom) {
        this.suggestedFrom = suggestedFrom;
        return this;
    }
}
