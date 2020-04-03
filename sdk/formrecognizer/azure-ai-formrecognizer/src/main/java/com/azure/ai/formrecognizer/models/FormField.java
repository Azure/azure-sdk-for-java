// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The FormField Model.
 */
@Immutable
public class FormField {

    /*
     * The recognized form type id.
     */
    private final String formTypeId;

    /*
     * The list of recognized fields.
     */
    private final List<String> fields;

    /**
     * Constructs a FormField object.
     *
     * @param formTypeId The recognized form type Id.
     * @param fields The list of recognized fields.
     */
    public FormField(final String formTypeId, final List<String> fields) {
        this.formTypeId = formTypeId;
        this.fields = fields;
    }

    /**
     * The recognized form type id.
     *
     * @return The recognized form type Id.
     */
    public String getFormTypeId() {
        return this.formTypeId;
    }

    /**
     * List of recognized fields.
     *
     * @return The list of recognized fields.
     */
    public List<String> getFields() {
        return this.fields;
    }
}
