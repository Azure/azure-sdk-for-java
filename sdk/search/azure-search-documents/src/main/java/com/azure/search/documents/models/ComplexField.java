// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A helper Field model to build a simple search field.
 */
public class ComplexField {
    private String name;
    private ComplexDataType dataType;
    private List<Field> fieldList;

    public String getName() {
        return name;
    }

    public ComplexField setName(String name) {
        this.name = name;
        return this;
    }

    public ComplexDataType getDataType() {
        return dataType;
    }

    public ComplexField setDataType(ComplexDataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public ComplexField setSimpleFieldList(List<SimpleField> fieldList) {
        this.fieldList = fieldList.stream().map(SimpleField::build).collect(Collectors.toList());
        return this;
    }

    public ComplexField setComplexFieldList(List<ComplexField> fieldList) {
        this.fieldList = fieldList.stream().map(ComplexField::build).collect(Collectors.toList());
        return this;
    }

    public Field build() {
        return new Field().setName(name).setType(dataType.toDataType()).setFields(fieldList);
    }
}
