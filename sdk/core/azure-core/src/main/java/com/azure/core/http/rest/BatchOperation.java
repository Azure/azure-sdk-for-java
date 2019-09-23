// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

public interface BatchOperation<T> {
    T getValue(BatchResult response);
    Response<T> getRawResponse(BatchResult response);
}
