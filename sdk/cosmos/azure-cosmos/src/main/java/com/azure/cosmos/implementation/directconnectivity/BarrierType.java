// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

public enum BarrierType {
    None,
    GlobalStrongWrite,
    NRegionSynchronousCommit
}
