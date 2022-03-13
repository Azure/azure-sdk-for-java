// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class Teacher {
    @JsonProperty(value = "students")
    private Map<String, Student> students;

    public Teacher setStudents(Map<String, Student> students) {
        this.students = students;
        return this;
    }
}
