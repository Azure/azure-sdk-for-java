// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A helper Field model to build a simple search field.
 */
public class ComplexField extends FieldBase {
    private List<Field> subFields;

    /**
     * Initializes a new instance of the {@link ComplexField} class.
     *
     * @param name The name of the field, which must be unique within the index or parent field.
     * @param collection Whether the field is a collection of strings.
     */
    public ComplexField(String name, boolean collection) {
        super(name, collection ? DataType.collection(DataType.EDM_COMPLEX_TYPE) : DataType.EDM_COMPLEX_TYPE);
    }

    /**
     * Gets a collection of {@link SimpleField} or {@link ComplexField} child fields.
     *
     * @return The list of sub-fields.
     */
    public List<Field> getSubFields() {
        return subFields;
    }

    /**
     * Sets a collection of {@link SimpleField} or {@link ComplexField} child fields.
     *
     * @param subFields The list of sub-fields.
     * @return The {@link ComplexField} object itself.
     */
    public ComplexField setSubFields(List<Field> subFields) {
        this.subFields = subFields;
        return this;
    }

    /**
     * Convert ComplexField to {@link Field}.
     *
     * @return The {@link Field} object.
     */
    public Field build() {
        return new Field().setName(super.getName()).setType(super.getDataType()).setFields(subFields)
            .setKey(false).setFilterable(false).setSortable(false).setHidden(false).setSearchable(false)
            .setFacetable(false);
    }
}
