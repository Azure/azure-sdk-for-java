// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

public enum FaultInjectionOperationType {
    READ,
    QUERY,
    CREATE,
    UPSERT,
    REPLACE,
    DELETE

    // Add support for metadata request type
}
