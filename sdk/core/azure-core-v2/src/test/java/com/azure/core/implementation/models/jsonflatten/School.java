// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.v2.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@SuppressWarnings({ "unused", "FieldCanBeLocal" })
@JsonFlatten
public class School {
    @JsonProperty(value = "teacher")
    private Teacher teacher;

    @JsonProperty(value = "properties.name")
    private String name;

    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    public School setTeacher(Teacher teacher) {
        this.teacher = teacher;
        return this;
    }

    public School setName(String name) {
        this.name = name;
        return this;
    }

    public School setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
