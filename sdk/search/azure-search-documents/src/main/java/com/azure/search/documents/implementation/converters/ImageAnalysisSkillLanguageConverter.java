// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ImageAnalysisSkillLanguage;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkillLanguage} and
 * {@link ImageAnalysisSkillLanguage}.
 */
public final class ImageAnalysisSkillLanguageConverter {
    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkillLanguage} to enum
     * {@link ImageAnalysisSkillLanguage}.
     */
    public static ImageAnalysisSkillLanguage map(com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return ImageAnalysisSkillLanguage.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link ImageAnalysisSkillLanguage} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkillLanguage}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkillLanguage map(ImageAnalysisSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.ImageAnalysisSkillLanguage.fromString(obj.toString());
    }

    private ImageAnalysisSkillLanguageConverter() {
    }
}
