// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OcrSkill;
import com.azure.search.documents.models.OcrSkillLanguage;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.TextExtractionAlgorithm;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.OcrSkill} and {@link OcrSkill}.
 */
public final class OcrSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(OcrSkillConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.OcrSkill} to {@link OcrSkill}.
     */
    public static OcrSkill map(com.azure.search.documents.implementation.models.OcrSkill obj) {
        if (obj == null) {
            return null;
        }
        OcrSkill ocrSkill = new OcrSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            ocrSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            ocrSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        ocrSkill.setName(_name);

        String _context = obj.getContext();
        ocrSkill.setContext(_context);

        String _description = obj.getDescription();
        ocrSkill.setDescription(_description);

        if (obj.getTextExtractionAlgorithm() != null) {
            TextExtractionAlgorithm _textExtractionAlgorithm =
                TextExtractionAlgorithmConverter.map(obj.getTextExtractionAlgorithm());
            ocrSkill.setTextExtractionAlgorithm(_textExtractionAlgorithm);
        }

        if (obj.getDefaultLanguageCode() != null) {
            OcrSkillLanguage _defaultLanguageCode = OcrSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            ocrSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }

        Boolean _shouldDetectOrientation = obj.isShouldDetectOrientation();
        ocrSkill.setShouldDetectOrientation(_shouldDetectOrientation);
        return ocrSkill;
    }

    /**
     * Maps from {@link OcrSkill} to {@link com.azure.search.documents.implementation.models.OcrSkill}.
     */
    public static com.azure.search.documents.implementation.models.OcrSkill map(OcrSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.OcrSkill ocrSkill =
            new com.azure.search.documents.implementation.models.OcrSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            ocrSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            ocrSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        ocrSkill.setName(_name);

        String _context = obj.getContext();
        ocrSkill.setContext(_context);

        String _description = obj.getDescription();
        ocrSkill.setDescription(_description);

        if (obj.getTextExtractionAlgorithm() != null) {
            com.azure.search.documents.implementation.models.TextExtractionAlgorithm _textExtractionAlgorithm =
                TextExtractionAlgorithmConverter.map(obj.getTextExtractionAlgorithm());
            ocrSkill.setTextExtractionAlgorithm(_textExtractionAlgorithm);
        }

        if (obj.getDefaultLanguageCode() != null) {
            com.azure.search.documents.implementation.models.OcrSkillLanguage _defaultLanguageCode =
                OcrSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            ocrSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }

        Boolean _shouldDetectOrientation = obj.shouldDetectOrientation();
        ocrSkill.setShouldDetectOrientation(_shouldDetectOrientation);
        return ocrSkill;
    }
}
