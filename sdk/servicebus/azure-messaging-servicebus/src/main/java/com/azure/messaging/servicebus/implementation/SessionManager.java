// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

/**
 * Manages sessions that are received.
 */
public class SessionManager {
    private final int maximumConcurrentSessions;

    public SessionManager(int maximumConcurrentSessions) {
        this.maximumConcurrentSessions = maximumConcurrentSessions;
    }
}
