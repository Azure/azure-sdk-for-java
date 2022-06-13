// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.messaging.servicebus.stress.config.RateMeter;
import com.azure.messaging.servicebus.stress.util.ScenarioOptions;
import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for service bus test scenarios
 */
public abstract class ServiceBusScenario {
    @Autowired
    protected ScenarioOptions options;

    @Autowired
    protected TelemetryClient telemetryClient;

    @Autowired
    protected RateMeter rateMeter;

    public abstract void run();
}
