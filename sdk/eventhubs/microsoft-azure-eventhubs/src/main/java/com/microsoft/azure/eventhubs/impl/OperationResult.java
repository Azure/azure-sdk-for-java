// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;


public interface OperationResult<T, E extends Throwable> {

    void onComplete(T result);

    void onError(E error);
}
