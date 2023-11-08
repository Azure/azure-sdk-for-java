// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util.polling.implementation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestActionResult {
    private final String name;

    @JsonCreator
    public TestActionResult(@JsonProperty(value = "name", required = true) String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Action result: " + name;
    }
}
