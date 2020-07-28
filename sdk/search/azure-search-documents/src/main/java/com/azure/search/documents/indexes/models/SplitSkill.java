// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * A skill to split a string into chunks of text.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Skills.Text.SplitSkill")
@Fluent
public final class SplitSkill extends SearchIndexerSkill {
    /*
     * A value indicating which language code to use. Default is en. Possible
     * values include: 'da', 'de', 'en', 'es', 'fi', 'fr', 'it', 'ko', 'pt'
     */
    @JsonProperty(value = "defaultLanguageCode")
    private SplitSkillLanguage defaultLanguageCode;

    /*
     * A value indicating which split mode to perform. Possible values include:
     * 'Pages', 'Sentences'
     */
    @JsonProperty(value = "textSplitMode")
    private TextSplitMode textSplitMode;

    /*
     * The desired maximum page length. Default is 10000.
     */
    @JsonProperty(value = "maximumPageLength")
    private Integer maximumPageLength;

    /**
     * Constructor of {@link SplitSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     * that can be consumed as an input by another skill.
     */
    public SplitSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        super(inputs, outputs);
    }

    /**
     * Get the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'da', 'de', 'en',
     * 'es', 'fi', 'fr', 'it', 'ko', 'pt'.
     *
     * @return the defaultLanguageCode value.
     */
    public SplitSkillLanguage getDefaultLanguageCode() {
        return this.defaultLanguageCode;
    }

    /**
     * Set the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'da', 'de', 'en',
     * 'es', 'fi', 'fr', 'it', 'ko', 'pt'.
     *
     * @param defaultLanguageCode the defaultLanguageCode value to set.
     * @return the SplitSkill object itself.
     */
    public SplitSkill setDefaultLanguageCode(SplitSkillLanguage defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
        return this;
    }

    /**
     * Get the textSplitMode property: A value indicating which split mode to
     * perform. Possible values include: 'Pages', 'Sentences'.
     *
     * @return the textSplitMode value.
     */
    public TextSplitMode getTextSplitMode() {
        return this.textSplitMode;
    }

    /**
     * Set the textSplitMode property: A value indicating which split mode to
     * perform. Possible values include: 'Pages', 'Sentences'.
     *
     * @param textSplitMode the textSplitMode value to set.
     * @return the SplitSkill object itself.
     */
    public SplitSkill setTextSplitMode(TextSplitMode textSplitMode) {
        this.textSplitMode = textSplitMode;
        return this;
    }

    /**
     * Get the maximumPageLength property: The desired maximum page length.
     * Default is 10000.
     *
     * @return the maximumPageLength value.
     */
    public Integer getMaximumPageLength() {
        return this.maximumPageLength;
    }

    /**
     * Set the maximumPageLength property: The desired maximum page length.
     * Default is 10000.
     *
     * @param maximumPageLength the maximumPageLength value to set.
     * @return the SplitSkill object itself.
     */
    public SplitSkill setMaximumPageLength(Integer maximumPageLength) {
        this.maximumPageLength = maximumPageLength;
        return this;
    }
}
