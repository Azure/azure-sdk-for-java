// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.util;

/**
 * A simulated failure.
 */
public class SimulatedFailure extends RuntimeException {
    public SimulatedFailure() {
        super("simulated failure");
    }
}
