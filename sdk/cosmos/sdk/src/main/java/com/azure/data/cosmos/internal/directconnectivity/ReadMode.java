// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

public enum ReadMode {
    Primary, // Test hook
    Strong,
    BoundedStaleness,
    Any
}

