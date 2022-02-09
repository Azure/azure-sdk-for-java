// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.spark;

public interface OperationContext {
    String getCorrelationActivityId();
}
