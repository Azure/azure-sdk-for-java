// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

public enum Language {
    JAVA("Java"), KOTLIN("Kotlin");

    private final String language;

    Language(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return language;
    }
}
