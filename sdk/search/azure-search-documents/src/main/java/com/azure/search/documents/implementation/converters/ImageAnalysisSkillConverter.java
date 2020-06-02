// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ImageAnalysisSkill;
import com.azure.search.documents.indexes.models.ImageAnalysisSkillLanguage;
import com.azure.search.documents.indexes.models.ImageDetail;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.VisualFeature;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkill} and
 * {@link ImageAnalysisSkill}.
 */
public final class ImageAnalysisSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkill} to
     * {@link ImageAnalysisSkill}.
     */
    public static ImageAnalysisSkill map(com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkill obj) {
        if (obj == null) {
            return null;
        }
        ImageAnalysisSkill imageAnalysisSkill = new ImageAnalysisSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setInputs(inputs);
        }

        String name = obj.getName();
        imageAnalysisSkill.setName(name);

        String context = obj.getContext();
        imageAnalysisSkill.setContext(context);

        String description = obj.getDescription();
        imageAnalysisSkill.setDescription(description);

        if (obj.getVisualFeatures() != null) {
            List<VisualFeature> visualFeatures =
                obj.getVisualFeatures().stream().map(VisualFeatureConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setVisualFeatures(visualFeatures);
        }

        if (obj.getDefaultLanguageCode() != null) {
            ImageAnalysisSkillLanguage defaultLanguageCode =
                ImageAnalysisSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            imageAnalysisSkill.setDefaultLanguageCode(defaultLanguageCode);
        }

        if (obj.getDetails() != null) {
            List<ImageDetail> details =
                obj.getDetails().stream().map(ImageDetailConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setDetails(details);
        }
        return imageAnalysisSkill;
    }

    /**
     * Maps from {@link ImageAnalysisSkill} to
     * {@link com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkill map(ImageAnalysisSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkill imageAnalysisSkill =
            new com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setInputs(inputs);
        }

        String name = obj.getName();
        imageAnalysisSkill.setName(name);

        String context = obj.getContext();
        imageAnalysisSkill.setContext(context);

        String description = obj.getDescription();
        imageAnalysisSkill.setDescription(description);

        if (obj.getVisualFeatures() != null) {
            List<com.azure.search.documents.indexes.implementation.models.VisualFeature> visualFeatures =
                obj.getVisualFeatures().stream().map(VisualFeatureConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setVisualFeatures(visualFeatures);
        }

        if (obj.getDefaultLanguageCode() != null) {
            com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkillLanguage defaultLanguageCode =
                ImageAnalysisSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            imageAnalysisSkill.setDefaultLanguageCode(defaultLanguageCode);
        }

        if (obj.getDetails() != null) {
            List<com.azure.search.documents.indexes.implementation.models.ImageDetail> details =
                obj.getDetails().stream().map(ImageDetailConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setDetails(details);
        }
        return imageAnalysisSkill;
    }

    private ImageAnalysisSkillConverter() {
    }
}
