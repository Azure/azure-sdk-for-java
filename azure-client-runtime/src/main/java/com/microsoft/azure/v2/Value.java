package com.microsoft.azure.v2;

class Value<T> {
    private T value;

    Value() {
    }

    Value(T value) {
        set(value);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}