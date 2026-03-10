// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

public enum BarrierType {
    NONE,
    GLOBAL_STRONG_WRITE,
    N_REGION_SYNCHRONOUS_COMMIT
}
