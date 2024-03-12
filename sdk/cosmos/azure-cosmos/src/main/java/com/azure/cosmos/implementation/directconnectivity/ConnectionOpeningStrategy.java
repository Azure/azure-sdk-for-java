package com.azure.cosmos.implementation.directconnectivity;

public enum ConnectionOpeningStrategy {
    // Rntbd layer will keep opening new connections until reach maxConnectionsPerEndpoint
    AGGRESSIVE,
    // Current defaut logic
    CONCURRENCY
}
