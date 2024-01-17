// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

@FunctionalInterface
public interface OnEventListener {
    void onEvent(ServerSentEvent sse);
}
