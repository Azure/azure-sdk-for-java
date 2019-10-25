// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer;

public class Response<T> {
    private final int status;
    private final String body;
    private final T root;

    public Response(int code, String content, T value) {
        status = code;
        body = content;
        root = value;
    }

    public int status() {
        return status;
    }

    public String body() {
        return body;
    }

    public T root() {
        return root;
    }
}
