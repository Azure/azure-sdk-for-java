// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class of testing serialization of server sent events.
 */
public class TestModel {
    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    public String getName() {
        return name;
    }

    public TestModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public TestModel setValue(String value) {
        this.value = value;
        return this;
    }
}
