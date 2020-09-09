package com.azure.digitaltwins.core;

import org.junit.jupiter.api.Test;

import com.azure.core.http.HttpClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * This abstract test class defines all the tests that both the sync and async component test classes need to implement.
 * It also houses some component test specific helper functions.
 */
public abstract class ComponentsTestBase extends DigitalTwinsTestBase {
    @Test
    public abstract void componentLifcycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);
}
