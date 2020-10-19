// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.RetryWithException;

interface LastRetryWithExceptionHolder {
    void setLastRetryWithException(RetryWithException lastRetryWithException);
}
