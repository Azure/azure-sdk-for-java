// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Fluent
public abstract class MultipartPart<T> {
    private static final String CONTENT_TYPE_PREFIX = "multipart/mixed; boundary=";

    private final String boundary;
    private final List<T> contents = new ArrayList<>();

    public MultipartPart(String boundaryPrefix) {
        this.boundary = boundaryPrefix + "_" + UUID.randomUUID();
    }

    public String getContentType() {
        return CONTENT_TYPE_PREFIX + boundary;
    }

    public String getBoundary() {
        return boundary;
    }

    public List<T> getContents() {
        return contents;
    }

    protected MultipartPart<T> addContent(T content) {
        contents.add(content);

        return this;
    }
}
