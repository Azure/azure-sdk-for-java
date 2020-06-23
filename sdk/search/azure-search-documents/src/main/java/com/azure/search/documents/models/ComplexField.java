// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import java.util.List;

/**
 * A helper Field model to build a complex field which uses {@code DataType.EDM_COMPLEX_TYPE} or collection of
 * {@code DataType.EDM_COMPLEX_TYPE}.
 */
public class ComplexField extends FieldBase {
    private List<Field> fields;

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
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Sets a collection of {@link SimpleField} or {@link ComplexField} child fields.
     *
     * @param fields The list of sub-fields.
     * @return The {@link ComplexField} object itself.
     */
    public ComplexField setFields(List<Field> fields) {
        this.fields = fields;
        return this;
    }

    /**
     * Convert ComplexField to {@link Field}.
     *
     * @return The {@link Field} object.
     */
    public Field build() {
        return new Field().setName(super.getName())
            .setType(super.getDataType())
            .setFields(fields)
            .setKey(false)
            .setFilterable(false)
            .setSortable(false)
            .setHidden(false)
            .setSearchable(false)
            .setFacetable(false);
    }
}
