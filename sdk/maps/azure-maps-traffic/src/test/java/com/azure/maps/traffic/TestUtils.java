// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.ReadValueCallback;
import com.azure.maps.traffic.models.TrafficFlowSegmentData;
import com.azure.maps.traffic.models.TrafficIncidentDetail;
import com.azure.maps.traffic.models.TrafficIncidentViewport;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestUtils {
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    static TrafficFlowSegmentData getExpectedTrafficFlowSegment() {
        return deserialize(TrafficFlowSegmentData::fromJson, "trafficflowsegment.json");
    }

    static TrafficIncidentDetail getExpectedTrafficIncidentDetail() {
        return deserialize(TrafficIncidentDetail::fromJson, "trafficincidentdetail.json");
    }

    static TrafficIncidentViewport getExpectedTrafficIncidentViewport() {
        return deserialize(TrafficIncidentViewport::fromJson, "trafficincidentviewport.json");
    }

    private static <T> T deserialize(ReadValueCallback<JsonReader, T> deserializer, String resourceName) {
        try (JsonReader jsonReader = JsonProviders.createReader(ClassLoader.getSystemResourceAsStream(resourceName))) {
            return deserializer.read(jsonReader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    public static Stream<Arguments> getTestParameters() {
        return TestBase.getHttpClients()
            .flatMap(httpClient -> Arrays.stream(TrafficServiceVersion.values())
                .map(serviceVersion -> Arguments.of(httpClient, serviceVersion)));
    }
}
