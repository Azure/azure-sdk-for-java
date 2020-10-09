// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.SimpleField;

public class Foo {
    @SimpleField(isKey = true)
    private String intValue;
    private String stringValue;

    public Foo() {
    }

    public Foo(final String intValue, final String stringValue) {
        this.intValue = intValue;
        this.stringValue = stringValue;
    }

    public String getIntValue() {
        return intValue;
    }

    public void setIntValue(final String intValue) {
        this.intValue = intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(final String stringValue) {
        this.stringValue = stringValue;
    }
}
