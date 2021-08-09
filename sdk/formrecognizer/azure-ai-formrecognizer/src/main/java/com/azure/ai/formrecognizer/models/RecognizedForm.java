// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.RecognizedFormHelper;
import com.azure.core.annotation.Immutable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a form that has been recognized by a trained or prebuilt model based on the provided input document.
 */
@Immutable
public final class RecognizedForm {

    /*
     * A map of the fields recognized from the input document.
     * For models trained with labels, this is the training-time label of the field. For models trained with forms
     * only, a unique name is generated for each field.
     */
    private final Map<String, FormField> fields;

    /*
     * Form type.
     */
    private final String formType;

    /*
     * First and last page number where the document is found.
     */
    private final FormPageRange pageRange;

    /*
     * List of extracted pages from the form.
     */
    private final List<FormPage> pages;

    private Float formTypeConfidence;

    private String modelId;

    static {
        RecognizedFormHelper.setAccessor(new RecognizedFormHelper.RecognizedFormAccessor() {
            @Override
            public void setFormTypeConfidence(RecognizedForm form, Float formTypeConfidence) {
                form.setFormTypeConfidence(formTypeConfidence);
            }

            @Override
            public void setModelId(RecognizedForm form, String modelId) {
                form.setModelId(modelId);
            }
        });
    }

    /**
     * Constructs a RecognizedForm object.
     *
     * @param fields Dictionary of named field values.
     * @param formType Form type.
     * @param pageRange First and last page number where the document is found.
     * @param pages List of extracted pages from the form.
     */
    public RecognizedForm(final Map<String, FormField> fields, final String formType,
        final FormPageRange pageRange, final List<FormPage> pages) {
        this.fields = fields == null ? null : Collections.unmodifiableMap(fields);
        this.formType = formType;
        this.pageRange = pageRange;
        this.pages = pages == null ? null : Collections.unmodifiableList(pages);
    }

    /**
     * A map of the fields recognized from the input document.
     * For models trained with labels, this is the training-time label of the field. For models trained with forms
     * only, a unique name is generated for each field.
     *
     * @return the unmodifiable map of recognized fields.
     */
    public Map<String, FormField> getFields() {
        return this.fields;
    }

    /**
     * Get the recognized form type.
     *
     * @return the formType value.
     */
    public String getFormType() {
        return this.formType;
    }

    /**
     * Get the first and last page number where the document is found.
     *
     * @return the pageRange value.
     */
    public FormPageRange getPageRange() {
        return this.pageRange;
    }

    /**
     * Get the list of extracted pages.
     *
     * @return the unmodifiable list of recognized pages.
     */
    public List<FormPage> getPages() {
        return this.pages;
    }

    /**
     * Get the confidence of the form type identified by the model.
     *
     * @return the formTypeConfidence value.
     */
    public Float getFormTypeConfidence() {
        return formTypeConfidence;
    }

    /**
     * Get the identifier of the model that was used for recognition, if not using a prebuilt model.
     *
     * @return the modelId value.
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * The private setter to set the formTypeConfidence property
     * via {@link RecognizedFormHelper.RecognizedFormAccessor}.
     *
     * @param formTypeConfidence The confidence of the form type identified by the model.
     */
    private void setFormTypeConfidence(Float formTypeConfidence) {
        this.formTypeConfidence = formTypeConfidence;
    }

    /**
     * The private setter to set the modelId property
     * via {@link RecognizedFormHelper.RecognizedFormAccessor}.
     *
     * @param modelId The identifier of the model that was used for recognition.
     */
    private void setModelId(String modelId) {
        this.modelId = modelId;
    }
}
