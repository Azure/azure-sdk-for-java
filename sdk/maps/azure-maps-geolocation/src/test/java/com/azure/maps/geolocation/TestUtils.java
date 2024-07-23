// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.maps.geolocation.models.IpAddressToLocationResult;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestUtils {
    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    public static Stream<Arguments> getTestParameters() {
        return TestBase.getHttpClients()
            .flatMap(httpClient -> Arrays.stream(GeolocationServiceVersion.values())
                .map(serviceVersion -> Arguments.of(httpClient, serviceVersion)));
    }

    static IpAddressToLocationResult getExpectedLocation() {
        try (JsonReader jsonReader = JsonProviders.createReader(
            ClassLoader.getSystemResourceAsStream("getlocation.json"))) {
            return IpAddressToLocationResult.fromJson(jsonReader);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
