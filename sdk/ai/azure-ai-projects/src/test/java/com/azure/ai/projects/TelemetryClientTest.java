// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.generated.AIProjectClientTestBase;
import com.azure.ai.projects.implementation.models.GetAppInsightsResponse;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TelemetryClientTest extends AIProjectClientTestBase {

    @BeforeEach
    void setUp() {
        this.beforeTest();
    }

    @Test
    @Disabled
    void testGetAppInsights() {
        String appInsightsUrl = Configuration.getGlobalConfiguration().get("APP_INSIGHTS_URL", "");
        GetAppInsightsResponse appInsightsResponse = this.telemetryClient.getAppInsights(appInsightsUrl);
        assertNotNull(appInsightsResponse);
    }
}
