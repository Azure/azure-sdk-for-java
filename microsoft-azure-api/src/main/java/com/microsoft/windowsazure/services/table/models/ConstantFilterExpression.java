package com.microsoft.windowsazure.services.table.models;

public class ConstantFilterExpression extends FilterExpression {
    private Object value;

    public Object getValue() {
        return value;
    }

    public ConstantFilterExpression setValue(Object value) {
        this.value = value;
        return this;
    }
}
