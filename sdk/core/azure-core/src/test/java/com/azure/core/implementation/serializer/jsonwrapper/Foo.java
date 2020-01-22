// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper;

import java.util.Objects;

public class Foo {
    private int intValue;
    private String stringValue;

    public Foo() {
    }

    public Foo(final int intValue, final String stringValue) {
        this.intValue = intValue;
        this.stringValue = stringValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(final int intValue) {
        this.intValue = intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Foo foo = (Foo) o;
        return (intValue == foo.intValue) && Objects.equals(stringValue, foo.stringValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intValue, stringValue);
    }
}
