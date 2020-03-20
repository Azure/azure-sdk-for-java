package com.azure.search.documents.models.field;

import com.azure.search.documents.models.Field;

public class StrictField {
    private SimpleField simpleField;
    private ComplexField complexField;

    public void setSimpleField(final SimpleField simpleField) {
        this.simpleField = simpleField;
        this.complexField = null;
    }

    public void setComplexField(final ComplexField complexField) {
        this.complexField = complexField;
        this.simpleField = null;
    }


    public Field toField() {
        if (simpleField == null && complexField == null) {
            throw new RuntimeException();
        }
        if (simpleField != null) {
            return simpleField.toField();
        }
        return complexField.toField();
    }
}
