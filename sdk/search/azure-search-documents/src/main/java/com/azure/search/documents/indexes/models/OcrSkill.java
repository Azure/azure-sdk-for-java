// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * A skill that extracts text from image files.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Skills.Vision.OcrSkill")
@Fluent
public final class OcrSkill extends SearchIndexerSkill {
    /*
     * A value indicating which language code to use. Default is en. Possible
     * values include: 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi',
     * 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es',
     * 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     */
    @JsonProperty(value = "defaultLanguageCode")
    private OcrSkillLanguage defaultLanguageCode;

    /*
     * A value indicating to turn orientation detection on or not. Default is
     * false.
     */
    @JsonProperty(value = "detectOrientation")
    private Boolean shouldDetectOrientation;

    /**
     * Constructor of {@link OcrSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     */
    public OcrSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        super(inputs, outputs);
    }

    /**
     * Get the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'zh-Hans',
     * 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it',
     * 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro',
     * 'sr-Cyrl', 'sr-Latn', 'sk'.
     *
     * @return the defaultLanguageCode value.
     */
    public OcrSkillLanguage getDefaultLanguageCode() {
        return this.defaultLanguageCode;
    }

    /**
     * Set the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'zh-Hans',
     * 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it',
     * 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro',
     * 'sr-Cyrl', 'sr-Latn', 'sk'.
     *
     * @param defaultLanguageCode the defaultLanguageCode value to set.
     * @return the OcrSkill object itself.
     */
    public OcrSkill setDefaultLanguageCode(OcrSkillLanguage defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
        return this;
    }

    /**
     * Get the shouldDetectOrientation property: A value indicating to turn
     * orientation detection on or not. Default is false.
     *
     * @return the shouldDetectOrientation value.
     * @deprecated Use {@link #isShouldDetectOrientation()} instead.
     */
    @Deprecated
    public Boolean setShouldDetectOrientation() {
        return this.shouldDetectOrientation;
    }

    /**
     * Get the shouldDetectOrientation property: A value indicating to turn
     * orientation detection on or not. Default is false.
     *
     * @return the shouldDetectOrientation value.
     */
    public Boolean isShouldDetectOrientation() {
        return this.shouldDetectOrientation;
    }

    /**
     * Set the shouldDetectOrientation property: A value indicating to turn
     * orientation detection on or not. Default is false.
     *
     * @param shouldDetectOrientation the shouldDetectOrientation value to set.
     * @return the OcrSkill object itself.
     */
    public OcrSkill setShouldDetectOrientation(Boolean shouldDetectOrientation) {
        this.shouldDetectOrientation = shouldDetectOrientation;
        return this;
    }
}
