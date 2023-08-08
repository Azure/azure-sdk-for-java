// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

public interface Operation<T> {

    void run(OperationResult<T, Exception> operationCallback);
}
