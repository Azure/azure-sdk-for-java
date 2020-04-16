// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.Map;

/**
 * The RecognizedForm model.
 */
@Immutable
public final class RecognizedForm {

    /*
     * Dictionary of named field values.
     */
    private final Map<String, FormField<?>> fields;

    /*
     * Form type.
     */
    private final String formType;

    /*
     * First and last page number where the document is found.
     */
    private final PageRange pageRange;

    /*
     * List of extracted pages from the form.
     */
    private final IterableStream<FormPage> pages;

    /**
     * Constructs a RecognizedForm object.
     *
     * @param fields Dictionary of named field values.
     * @param formType Form type.
     * @param pageRange First and last page number where the document is found.
     * @param pages List of extracted pages from the form.
     */
    public RecognizedForm(final Map<String, FormField<?>> fields, final String formType, final PageRange pageRange,
        final IterableStream<FormPage> pages) {
        this.fields = fields;
        this.formType = formType;
        this.pageRange = pageRange;
        this.pages = pages;
    }

    /**
     * Get the dictionary of named field values.
     *
     * @return the fields value.
     */
    public Map<String, FormField<?>> getFields() {
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
    public PageRange getPageRange() {
        return this.pageRange;
    }

    /**
     * Get the list of extracted pages.
     *
     * @return the pages value.
     */
    public IterableStream<FormPage> getPages() {
        return this.pages;
    }
}
