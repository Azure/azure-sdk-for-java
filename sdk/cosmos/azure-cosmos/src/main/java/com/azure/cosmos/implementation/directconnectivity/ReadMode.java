// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

public enum ReadMode {
    Primary, // Test hook
    Strong,
    BoundedStaleness,
    Any
}

