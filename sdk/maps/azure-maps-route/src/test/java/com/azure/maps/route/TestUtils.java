// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.ReadValueCallback;
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteRangeResult;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.io.UncheckedIOException;
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
        return TestBase.getHttpClients()
            .flatMap(httpClient -> Arrays.stream(MapsRouteServiceVersion.values())
                .map(serviceVersion -> Arguments.of(httpClient, serviceVersion)));
    }

    static RouteMatrixResult getExpectedBeginRequestRouteMatrix() {
        return deserialize(RouteMatrixResult::fromJson, "beginrequestroutematrix.json");
    }

    static RouteMatrixResult getExpectedGetRequestRouteMatrix() {
        return deserialize(RouteMatrixResult::fromJson, "getrequestroutematrix.json");
    }

    static RouteDirections getExpectedRouteDirections() {
        return deserialize(RouteDirections::fromJson, "getroutedirections.json");
    }

    static RouteDirections getExpectedRouteDirectionsWithAdditionalParameters() {
        return deserialize(RouteDirections::fromJson, "getroutedirectionsadditionalparams.json");
    }

    static RouteRangeResult getExpectedRouteRange() {
        return deserialize(RouteRangeResult::fromJson, "getrouterange.json");
    }

    static RouteDirectionsBatchResult getExpectedBeginRequestRouteDirectionsBatch() {
        return deserialize(RouteDirectionsBatchResult::fromJson, "beginrequestroutedirectionsbatch.json");
    }

    static RouteDirectionsBatchResult getExpectedBeginRequestRouteDirectionsBatchBatchId() {
        return deserialize(RouteDirectionsBatchResult::fromJson, "beginrequestroutedirectionsbatchbatchid.json");
    }

    private static <T> T deserialize(ReadValueCallback<JsonReader, T> deserializer, String resourceName) {
        try (JsonReader jsonReader = JsonProviders.createReader(ClassLoader.getSystemResourceAsStream(resourceName))) {
            return deserializer.read(jsonReader);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
