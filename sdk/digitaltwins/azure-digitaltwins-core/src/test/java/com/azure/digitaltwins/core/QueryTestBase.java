// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Test;

public abstract class QueryTestBase extends DigitalTwinsTestBase {
    @Test
    public abstract void validQuerySucceeds(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws InterruptedException;
}
