// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.messaging.eventhubs.stress.config.RateMeter;
import com.azure.messaging.eventhubs.stress.util.ScenarioOptions;
import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for event hubs test scenarios
 */
public abstract class EventHubsScenario {
    @Autowired
    protected ScenarioOptions options;

    @Autowired
    protected TelemetryClient telemetryClient;

    @Autowired
    protected RateMeter rateMeter;

    public abstract void run();
}
