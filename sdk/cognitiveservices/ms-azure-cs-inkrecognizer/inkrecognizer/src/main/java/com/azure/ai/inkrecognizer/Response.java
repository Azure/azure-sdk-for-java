// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

public class Response<T> {
    public final int status;
    public final String body;
    public final T root;

    public Response(int code, String content, T value) {
        status = code;
        body = content;
        root = value;
    }
}
