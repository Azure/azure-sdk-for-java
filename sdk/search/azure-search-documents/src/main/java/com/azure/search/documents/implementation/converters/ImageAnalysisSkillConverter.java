// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ImageAnalysisSkill;
import com.azure.search.documents.models.ImageAnalysisSkillLanguage;
import com.azure.search.documents.models.ImageDetail;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.VisualFeature;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ImageAnalysisSkill} and
 * {@link ImageAnalysisSkill}.
 */
public final class ImageAnalysisSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ImageAnalysisSkillConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ImageAnalysisSkill} to
     * {@link ImageAnalysisSkill}.
     */
    public static ImageAnalysisSkill map(com.azure.search.documents.implementation.models.ImageAnalysisSkill obj) {
        if (obj == null) {
            return null;
        }
        ImageAnalysisSkill imageAnalysisSkill = new ImageAnalysisSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        imageAnalysisSkill.setName(_name);

        String _context = obj.getContext();
        imageAnalysisSkill.setContext(_context);

        String _description = obj.getDescription();
        imageAnalysisSkill.setDescription(_description);

        if (obj.getVisualFeatures() != null) {
            List<VisualFeature> _visualFeatures =
                obj.getVisualFeatures().stream().map(VisualFeatureConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setVisualFeatures(_visualFeatures);
        }

        if (obj.getDefaultLanguageCode() != null) {
            ImageAnalysisSkillLanguage _defaultLanguageCode =
                ImageAnalysisSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            imageAnalysisSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }

        if (obj.getDetails() != null) {
            List<ImageDetail> _details =
                obj.getDetails().stream().map(ImageDetailConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setDetails(_details);
        }
        return imageAnalysisSkill;
    }

    /**
     * Maps from {@link ImageAnalysisSkill} to
     * {@link com.azure.search.documents.implementation.models.ImageAnalysisSkill}.
     */
    public static com.azure.search.documents.implementation.models.ImageAnalysisSkill map(ImageAnalysisSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ImageAnalysisSkill imageAnalysisSkill =
            new com.azure.search.documents.implementation.models.ImageAnalysisSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        imageAnalysisSkill.setName(_name);

        String _context = obj.getContext();
        imageAnalysisSkill.setContext(_context);

        String _description = obj.getDescription();
        imageAnalysisSkill.setDescription(_description);

        if (obj.getVisualFeatures() != null) {
            List<com.azure.search.documents.implementation.models.VisualFeature> _visualFeatures =
                obj.getVisualFeatures().stream().map(VisualFeatureConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setVisualFeatures(_visualFeatures);
        }

        if (obj.getDefaultLanguageCode() != null) {
            com.azure.search.documents.implementation.models.ImageAnalysisSkillLanguage _defaultLanguageCode =
                ImageAnalysisSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            imageAnalysisSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }

        if (obj.getDetails() != null) {
            List<com.azure.search.documents.implementation.models.ImageDetail> _details =
                obj.getDetails().stream().map(ImageDetailConverter::map).collect(Collectors.toList());
            imageAnalysisSkill.setDetails(_details);
        }
        return imageAnalysisSkill;
    }
}
