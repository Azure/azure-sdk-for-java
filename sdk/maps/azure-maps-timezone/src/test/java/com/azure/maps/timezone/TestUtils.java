// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.ReadValueCallback;
import com.azure.maps.timezone.models.IanaId;
import com.azure.maps.timezone.models.TimeZoneIanaVersionResult;
import com.azure.maps.timezone.models.TimeZoneResult;
import com.azure.maps.timezone.models.TimeZoneWindows;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        TestBase.getHttpClients()
            .forEach(httpClient -> Arrays.stream(TimeZoneServiceVersion.values())
                .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion))));
        return argumentsList.stream();
    }

    static TimeZoneResult getExpectedTimezoneById() throws IOException {
        return deserialize("gettimezonebyid.json", TimeZoneResult::fromJson);
    }

    static TimeZoneResult getExpectedTimezoneByCoordinates() throws IOException {
        return deserialize("gettimezonebycoordinates.json", TimeZoneResult::fromJson);
    }

    static List<TimeZoneWindows> getExpectedWindowsTimezoneIds() throws IOException {
        return deserialize("getwindowstimezonesids.json", reader -> reader.readArray(TimeZoneWindows::fromJson));
    }

    static List<IanaId> getExpectedIanaTimezoneIds() throws IOException {
        return deserialize("getianatimezoneids.json", reader -> reader.readArray(IanaId::fromJson));
    }

    static TimeZoneIanaVersionResult getExpectedIanaVersion() throws IOException {
        return deserialize("gettimezoneianaversionresult.json", TimeZoneIanaVersionResult::fromJson);
    }

    static List<IanaId> getExpectedConvertWindowsTimezoneToIana() throws IOException {
        return deserialize("getconvertwindowstimezonetoiana.json", reader -> reader.readArray(IanaId::fromJson));
    }

    private static <T> T deserialize(String resourceName, ReadValueCallback<JsonReader, T> deserializer)
        throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(ClassLoader.getSystemResourceAsStream(resourceName))) {
            return deserializer.read(jsonReader);
        }
    }
}
