package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Test;

public abstract class PublishTelemetryTestBase extends DigitalTwinsTestBase {
    @Test
    public abstract void publishTelemetryLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);
}
