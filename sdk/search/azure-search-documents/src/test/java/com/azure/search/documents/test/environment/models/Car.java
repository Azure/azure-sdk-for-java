// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.SimpleField;

public class Car {
    @SimpleField(name = "Color")
    private String color;

    @SimpleField(name = "Type")
    private String type;

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
