// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestUtils {
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    public static Stream<Arguments> getTestParameters() {
        return TestBase.getHttpClients().flatMap(httpClient -> Arrays.stream(MapsSearchServiceVersion.values())
            .map(serviceVersion -> Arguments.of(httpClient, serviceVersion)));
    }

}
