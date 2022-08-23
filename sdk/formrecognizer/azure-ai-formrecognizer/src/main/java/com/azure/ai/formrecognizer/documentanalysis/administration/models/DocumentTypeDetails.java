// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentTypeDetailsHelper;

import java.util.Map;

/**
 * The DocumentTypeDetails model representing detailed information about the document type.
 */
public final class DocumentTypeDetails {
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

    private DocumentModelBuildMode buildMode;

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

    /**
     * Get the buildMode property: Custom model build mode.
     *
     * @return the buildMode value.
     */
    public DocumentModelBuildMode getBuildMode() {
        return buildMode;
    }

    void setBuildMode(DocumentModelBuildMode buildMode) {
        this.buildMode = buildMode;
    }

    static {
        DocumentTypeDetailsHelper.setAccessor(new DocumentTypeDetailsHelper.DocumentTypeDetailsAccessor() {
            @Override
            public void setDescription(DocumentTypeDetails documentTypeDetails, String description) {
                documentTypeDetails.setDescription(description);
            }

            @Override
            public void setFieldSchema(DocumentTypeDetails documentTypeDetails, Map<String, DocumentFieldSchema> fieldSchema) {
                documentTypeDetails.setFieldSchema(fieldSchema);
            }

            @Override
            public void setFieldConfidence(DocumentTypeDetails documentTypeDetails, Map<String, Float> fieldConfidence) {
                documentTypeDetails.setFieldConfidence(fieldConfidence);
            }

            @Override
            public void setBuildMode(DocumentTypeDetails documentTypeDetails, DocumentModelBuildMode buildMode) {
                documentTypeDetails.setBuildMode(buildMode);
            }
        });
    }
}
