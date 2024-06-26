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
import java.io.UncheckedIOException;
import java.time.Duration;
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
        return TestBase.getHttpClients()
            .flatMap(httpClient -> Arrays.stream(TimeZoneServiceVersion.values())
                .map(serviceVersion -> Arguments.of(httpClient, serviceVersion)));
    }

    static TimeZoneResult getExpectedTimezoneById() {
        return deserialize("gettimezonebyid.json", TimeZoneResult::fromJson);
    }

    static TimeZoneResult getExpectedTimezoneByCoordinates() {
        return deserialize("gettimezonebycoordinates.json", TimeZoneResult::fromJson);
    }

    static List<TimeZoneWindows> getExpectedWindowsTimezoneIds() {
        return deserialize("getwindowstimezonesids.json", reader -> reader.readArray(TimeZoneWindows::fromJson));
    }

    static List<IanaId> getExpectedIanaTimezoneIds() {
        return deserialize("getianatimezoneids.json", reader -> reader.readArray(IanaId::fromJson));
    }

    static TimeZoneIanaVersionResult getExpectedIanaVersion() {
        return deserialize("gettimezoneianaversionresult.json", TimeZoneIanaVersionResult::fromJson);
    }

    static List<IanaId> getExpectedConvertWindowsTimezoneToIana() {
        return deserialize("getconvertwindowstimezonetoiana.json", reader -> reader.readArray(IanaId::fromJson));
    }

    private static <T> T deserialize(String resourceName, ReadValueCallback<JsonReader, T> deserializer) {
        try (JsonReader jsonReader = JsonProviders.createReader(ClassLoader.getSystemResourceAsStream(resourceName))) {
            return deserializer.read(jsonReader);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
