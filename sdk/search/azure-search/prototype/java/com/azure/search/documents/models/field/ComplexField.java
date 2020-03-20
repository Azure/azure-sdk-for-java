package com.azure.search.documents.models.field;

import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.NonEmptyList;
import com.azure.search.documents.models.dataType.ComplexDataType;
import java.util.stream.Collectors;

public class ComplexField  {
    private String name;
    private ComplexDataType complexDataType;
    private NonEmptyList<StrictField> fields;

    public ComplexField(final String name, final ComplexDataType complexDataType,
        final NonEmptyList<StrictField> fields) {
        this.name = name;
        this.complexDataType = complexDataType;
        this.fields = fields;
    }

    public Field toField(){
        return new Field().setName(name).setType(complexDataType.toDataType())
            .setFields(fields.asList().stream().map(StrictField::toField).collect(Collectors.toList()));
    }
}
