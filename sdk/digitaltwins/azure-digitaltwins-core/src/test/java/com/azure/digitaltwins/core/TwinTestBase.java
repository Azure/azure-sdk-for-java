// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

/**
 * This abstract test class defines all the tests that both the sync and async twin test classes need to implement.
 */
public abstract class TwinTestBase extends DigitalTwinsTestBase {

    @Test
    public abstract void digitalTwinLifecycle(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException;

    @Test
    public abstract void digitalTwinWithNumericStringProperty(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException;

    @Test
    public abstract void twinNotExistThrowsNotFoundException(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    @Test
    public abstract void createOrReplaceTwinFailsWhenIfNoneMatchStar(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException;

    @Test
    public abstract void createOrReplaceTwinSucceedsWhenNoIfNoneHeader(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException;

    @Test
    public abstract void patchTwinFailsWhenETagDoesNotMatch(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException;

    @Test
    public abstract void patchTwinSucceedsWhenETagMatches(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException;

    @Test
    public abstract void deleteTwinFailsWhenETagDoesNotMatch(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException;

    @Test
    public abstract void deleteTwinSucceedsWhenETagMatches(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException;
}
