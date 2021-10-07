// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.DocTypeInfoHelper;

import java.util.Map;

/**
 * The DocTypeInfo model.
 */
public final class DocTypeInfo {
    /*
     * Model description.
     */
    private String description;

    /*
     * Description of the document semantic schema using a JSON Schema style
     * syntax.
     */
    private Map<String, DocumentFieldSchema> fieldSchema;

    /*
     * Estimated confidence for each field.
     */
    private Map<String, Float> fieldConfidence;

    /**
     * Get the description property: Model description.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description property: Model description.
     *
     * @param description the description value to set.
     */
    void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the fieldSchema property: Description of the document semantic schema using a JSON Schema style syntax.
     *
     * @return the fieldSchema value.
     */
    public Map<String, DocumentFieldSchema> getFieldSchema() {
        return this.fieldSchema;
    }

    /**
     * Set the fieldSchema property: Description of the document semantic schema using a JSON Schema style syntax.
     *
     * @param fieldSchema the fieldSchema value to set.
     */
    void setFieldSchema(Map<String, DocumentFieldSchema> fieldSchema) {
        this.fieldSchema = fieldSchema;
    }

    /**
     * Get the fieldConfidence property: Estimated confidence for each field.
     *
     * @return the fieldConfidence value.
     */
    public Map<String, Float> getFieldConfidence() {
        return this.fieldConfidence;
    }

    /**
     * Set the fieldConfidence property: Estimated confidence for each field.
     *
     * @param fieldConfidence the fieldConfidence value to set.
     */
    void setFieldConfidence(Map<String, Float> fieldConfidence) {
        this.fieldConfidence = fieldConfidence;
    }
    static {
        DocTypeInfoHelper.setAccessor(new DocTypeInfoHelper.DocTypeInfoAccessor() {
            @Override
            public void setDescription(DocTypeInfo docTypeInfo, String description) {
                docTypeInfo.setDescription(description);
            }

            @Override
            public void setFieldSchema(DocTypeInfo docTypeInfo, Map<String, DocumentFieldSchema> fieldSchema) {
                docTypeInfo.setFieldSchema(fieldSchema);
            }

            @Override
            public void setFieldConfidence(DocTypeInfo docTypeInfo, Map<String, Float> fieldConfidence) {
                docTypeInfo.setFieldConfidence(fieldConfidence);
            }
        });
    }
}
