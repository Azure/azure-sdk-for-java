package com.microsoft.windowsazure.services.table.models;

public class ConstantFilter extends Filter {
    private Object value;

    public Object getValue() {
        return value;
    }

    public ConstantFilter setValue(Object value) {
        this.value = value;
        return this;
    }
}
