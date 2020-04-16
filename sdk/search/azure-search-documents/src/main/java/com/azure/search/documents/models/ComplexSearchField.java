// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A helper Field model to build a simple search field.
 */
public class ComplexSearchField {
    private String name;
    private ComplexDataType dataType;
    private List<Field> fieldList;

    public String getName() {
        return name;
    }

    public ComplexSearchField setName(String name) {
        this.name = name;
        return this;
    }

    public ComplexDataType getDataType() {
        return dataType;
    }

    public ComplexSearchField setDataType(ComplexDataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public ComplexSearchField setSimpleFieldList(List<SimpleSearchField> fieldList) {
        this.fieldList = fieldList.stream().map(SimpleSearchField::build).collect(Collectors.toList());
        return this;
    }

    public ComplexSearchField setComplexFieldList(List<ComplexSearchField> fieldList) {
        this.fieldList = fieldList.stream().map(ComplexSearchField::build).collect(Collectors.toList());
        return this;
    }

    public Field build() {
        return new Field().setName(name).setType(dataType.toDataType()).setFields(fieldList);
    }
}
