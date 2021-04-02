// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.DocumentExtractionSkill;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.DocumentExtractionSkill} and
 * {@link DocumentExtractionSkill}.
 */
public final class DocumentExtractionSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.DocumentExtractionSkill} to
     * {@link DocumentExtractionSkill}.
     */
    public static DocumentExtractionSkill map(
        com.azure.search.documents.indexes.implementation.models.DocumentExtractionSkill obj) {
        if (obj == null) {
            return null;
        }

        DocumentExtractionSkill documentExtractionSkill = new DocumentExtractionSkill(obj.getInputs(),
            obj.getOutputs());

        documentExtractionSkill.setParsingMode(obj.getParsingMode());
        documentExtractionSkill.setDataToExtract(obj.getDataToExtract());
        documentExtractionSkill.setConfiguration(obj.getConfiguration());

        return documentExtractionSkill;
    }

    /**
     * Maps from {@link DocumentExtractionSkill} to
     * {@link com.azure.search.documents.indexes.implementation.models.DocumentExtractionSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.DocumentExtractionSkill map(
        DocumentExtractionSkill obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.DocumentExtractionSkill documentExtractionSkill =
            new com.azure.search.documents.indexes.implementation.models.DocumentExtractionSkill(obj.getInputs(),
                obj.getOutputs());

        documentExtractionSkill.setParsingMode(obj.getParsingMode());
        documentExtractionSkill.setDataToExtract(obj.getDataToExtract());
        documentExtractionSkill.setConfiguration(obj.getConfiguration());

        return documentExtractionSkill;
    }
}
