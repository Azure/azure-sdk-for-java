package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.WebApiSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.WebApiSkill} and
 * {@link WebApiSkill} mismatch.
 */
public final class WebApiSkillConverter {
    public static WebApiSkill convert(com.azure.search.documents.models.WebApiSkill obj) {
        return DefaultConverter.convert(obj, WebApiSkill.class);
    }

    public static com.azure.search.documents.models.WebApiSkill convert(WebApiSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.WebApiSkill.class);
    }
}
