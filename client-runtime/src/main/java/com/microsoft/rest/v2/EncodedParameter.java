package com.microsoft.rest.v2;

public class EncodedParameter {
    private final String name;
    private final String encodedValue;

    public EncodedParameter(String name, String encodedValue) {
        this.name = name;
        this.encodedValue = encodedValue;
    }

    public String getName() {
        return name;
    }

    public String getEncodedValue() {
        return encodedValue;
    }

    public boolean equals(Object rhs) {
        return rhs instanceof EncodedParameter ? equals((EncodedParameter) rhs) : false;
    }

    public boolean equals(EncodedParameter rhs) {
        return rhs != null &&
                 name.equals(rhs.name) &&
                 encodedValue.equals(rhs.encodedValue);
    }
}
