// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.OcrSkill;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.OcrSkill} and {@link OcrSkill}.
 */
public final class OcrSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.OcrSkill} to {@link OcrSkill}.
     */
    public static OcrSkill map(com.azure.search.documents.indexes.implementation.models.OcrSkill obj) {
        if (obj == null) {
            return null;
        }

        OcrSkill ocrSkill = new OcrSkill(obj.getInputs(), obj.getOutputs());

        String name = obj.getName();
        ocrSkill.setName(name);

        String context = obj.getContext();
        ocrSkill.setContext(context);

        String description = obj.getDescription();
        ocrSkill.setDescription(description);


        if (obj.getDefaultLanguageCode() != null) {
            ocrSkill.setDefaultLanguageCode(obj.getDefaultLanguageCode());
        }

        Boolean shouldDetectOrientation = obj.isShouldDetectOrientation();
        ocrSkill.setShouldDetectOrientation(shouldDetectOrientation);
        return ocrSkill;
    }

    /**
     * Maps from {@link OcrSkill} to {@link com.azure.search.documents.indexes.implementation.models.OcrSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.OcrSkill map(OcrSkill obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.OcrSkill ocrSkill =
            new com.azure.search.documents.indexes.implementation.models.OcrSkill(obj.getInputs(), obj.getOutputs());

        String name = obj.getName();
        ocrSkill.setName(name);

        String context = obj.getContext();
        ocrSkill.setContext(context);

        String description = obj.getDescription();
        ocrSkill.setDescription(description);

        if (obj.getDefaultLanguageCode() != null) {
            ocrSkill.setDefaultLanguageCode(obj.getDefaultLanguageCode());
        }

        Boolean shouldDetectOrientation = obj.setShouldDetectOrientation();
        ocrSkill.setShouldDetectOrientation(shouldDetectOrientation);

        return ocrSkill;
    }

    private OcrSkillConverter() {
    }
}
