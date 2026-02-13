// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.testingmodels;

import com.azure.search.documents.indexes.BasicField;

public class Foo {
    @BasicField(name = "IntValue", isKey = BasicField.BooleanHelper.TRUE)
    private String intValue;
    @BasicField(name = "StringValue")
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
