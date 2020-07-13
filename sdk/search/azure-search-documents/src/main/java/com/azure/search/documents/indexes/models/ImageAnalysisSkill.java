// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Arrays;
import java.util.List;

/**
 * A skill that analyzes image files. It extracts a rich set of visual features
 * based on the image content.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Skills.Vision.ImageAnalysisSkill")
@Fluent
public final class ImageAnalysisSkill extends SearchIndexerSkill {
    /*
     * A value indicating which language code to use. Default is en. Possible
     * values include: 'en', 'es', 'ja', 'pt', 'zh'
     */
    @JsonProperty(value = "defaultLanguageCode")
    private ImageAnalysisSkillLanguage defaultLanguageCode;

    /*
     * A list of visual features.
     */
    @JsonProperty(value = "visualFeatures")
    private List<VisualFeature> visualFeatures;

    /*
     * A string indicating which domain-specific details to return.
     */
    @JsonProperty(value = "details")
    private List<ImageDetail> details;

    /**
     * Constructor of {@link SearchIndexerSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     * that can be consumed as an input by another skill.
     */
    public ImageAnalysisSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        super(inputs, outputs);
    }

    /**
     * Get the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'en', 'es', 'ja',
     * 'pt', 'zh'.
     *
     * @return the defaultLanguageCode value.
     */
    public ImageAnalysisSkillLanguage getDefaultLanguageCode() {
        return this.defaultLanguageCode;
    }

    /**
     * Set the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'en', 'es', 'ja',
     * 'pt', 'zh'.
     *
     * @param defaultLanguageCode the defaultLanguageCode value to set.
     * @return the ImageAnalysisSkill object itself.
     */
    public ImageAnalysisSkill setDefaultLanguageCode(ImageAnalysisSkillLanguage defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
        return this;
    }

    /**
     * Get the visualFeatures property: A list of visual features.
     *
     * @return the visualFeatures value.
     */
    public List<VisualFeature> getVisualFeatures() {
        return this.visualFeatures;
    }

    /**
     * Set the visualFeatures property: A list of visual features.
     *
     * @param visualFeatures the visualFeatures value to set.
     * @return the ImageAnalysisSkill object itself.
     */
    public ImageAnalysisSkill setVisualFeatures(VisualFeature... visualFeatures) {
        this.visualFeatures = (visualFeatures == null) ? null : Arrays.asList(visualFeatures);
        return this;
    }

    /**
     * Set the visualFeatures property: A list of visual features.
     *
     * @param visualFeatures the visualFeatures value to set.
     * @return the ImageAnalysisSkill object itself.
     */
    @JsonSetter
    public ImageAnalysisSkill setVisualFeatures(List<VisualFeature> visualFeatures) {
        this.visualFeatures = visualFeatures;
        return this;
    }

    /**
     * Get the details property: A string indicating which domain-specific
     * details to return.
     *
     * @return the details value.
     */
    public List<ImageDetail> getDetails() {
        return this.details;
    }

    /**
     * Set the details property: A string indicating which domain-specific
     * details to return.
     *
     * @param details the details value to set.
     * @return the ImageAnalysisSkill object itself.
     */
    public ImageAnalysisSkill setDetails(ImageDetail... details) {
        this.details = (details == null) ? null : Arrays.asList(details);
        return this;
    }

    /**
     * Set the details property: A string indicating which domain-specific
     * details to return.
     *
     * @param details the details value to set.
     * @return the ImageAnalysisSkill object itself.
     */
    @JsonSetter
    public ImageAnalysisSkill setDetails(List<ImageDetail> details) {
        this.details = details;
        return this;
    }
}
