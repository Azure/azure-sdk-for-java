// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

public enum ConnectionOpeningStrategy {
    // Rntbd layer will keep opening new connections until reach maxConnectionsPerEndpoint
    AGGRESSIVE,
    // Current defaut logic
    CONCURRENCY
}
