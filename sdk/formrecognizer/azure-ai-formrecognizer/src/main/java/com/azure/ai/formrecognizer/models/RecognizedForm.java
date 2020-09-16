// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The RecognizedForm model.
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
}
