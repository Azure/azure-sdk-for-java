package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Fluent
abstract class MultipartPart<T> {
    protected static final String BOUNDARY_DELIMETER = "--";
    protected static final String CONTENT_TYPE_PREFIX = "multipart/mixed; boundary=";

    private final String boundary;
    private final List<T> contents = new ArrayList<>();

    public MultipartPart(String boundaryPrefix) {
        this.boundary = boundaryPrefix + "_" + UUID.randomUUID().toString();
    }

    public String getContentType() {
        return CONTENT_TYPE_PREFIX + boundary;
    }

    protected MultipartPart<T> addContent(T content) {
        contents.add(content);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Object content : contents) {
            sb.append(BOUNDARY_DELIMETER).append(boundary).append("\n")
                .append(content.toString()).append("\n");
        }
        sb.append(BOUNDARY_DELIMETER).append(boundary).append(BOUNDARY_DELIMETER).append("\n");
        return sb.toString();
    }
}
