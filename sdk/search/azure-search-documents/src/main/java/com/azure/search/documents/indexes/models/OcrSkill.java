// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

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
     */
    public Boolean shouldDetectOrientation() {
        return this.shouldDetectOrientation;
    }

    /**
     * Set the shouldDetectOrientation property: A value indicating to turn
     * orientation detection on or not. Default is false.
     *
     * @param shouldDetectOrientation the shouldDetectOrientation value to set.
     * @return the OcrSkill object itself.
     */
    public OcrSkill shouldDetectOrientation(Boolean shouldDetectOrientation) {
        this.shouldDetectOrientation = shouldDetectOrientation;
        return this;
    }
}
