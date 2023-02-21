// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Test;

/**
 * This abstract test class defines all the tests that both the sync and async Import test classes need to implement. It also
 * houses some test specific helper functions.
 */
public abstract class ImportTestBase extends DigitalTwinsTestBase {

    @Test
    public abstract void importLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    @Test
    public abstract void validatingBadRequest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    @Test
    public abstract void validatingDuplicateRequests(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);
}
