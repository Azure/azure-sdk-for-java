// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

public class MultipartField {
    private final String wireName;
    private final String value;

    public MultipartField(String wireName, String value) {
        this.wireName = wireName;
        this.value = value;
    }

    public String getWireName() {
        return wireName;
    }

    public String getValue() {
        return value;
    }
}
