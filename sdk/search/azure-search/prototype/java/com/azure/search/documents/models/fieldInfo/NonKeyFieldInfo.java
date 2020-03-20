package com.azure.search.documents.models.fieldInfo;

import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.dataType.SimpleDataType;

public class NonKeyFieldInfo {
    private final SimpleDataType simpleDataType;
    private final Boolean isKey;

    public NonKeyFieldInfo(final SimpleDataType simpleDataType, final Boolean isKey) {
        this.simpleDataType = simpleDataType;
        this.isKey = isKey;
    }


    public SimpleDataType getSimpleDataType() {
        return simpleDataType;
    }

    public Boolean isKey() {
        return isKey;
    }

    public Field toField() {
        return new Field().setType(simpleDataType.toDataType()).setKey(isKey);
    }
}
