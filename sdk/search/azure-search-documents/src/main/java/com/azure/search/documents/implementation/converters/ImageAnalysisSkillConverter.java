package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ImageAnalysisSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ImageAnalysisSkill} and
 * {@link ImageAnalysisSkill} mismatch.
 */
public final class ImageAnalysisSkillConverter {
    public static ImageAnalysisSkill convert(com.azure.search.documents.models.ImageAnalysisSkill obj) {
        return DefaultConverter.convert(obj, ImageAnalysisSkill.class);
    }

    public static com.azure.search.documents.models.ImageAnalysisSkill convert(ImageAnalysisSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ImageAnalysisSkill.class);
    }
}
