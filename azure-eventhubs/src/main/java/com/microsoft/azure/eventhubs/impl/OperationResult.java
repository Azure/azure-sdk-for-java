/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;


public interface OperationResult<T, E extends Throwable> {

    void onComplete(T result);

    void onError(E error);
}
