// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.ReadValueCallback;
import com.azure.maps.render.models.Copyright;
import com.azure.maps.render.models.CopyrightCaption;
import com.azure.maps.render.models.MapAttribution;
import com.azure.maps.render.models.MapTileset;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestUtils {

    static MapAttribution getExpectedMapAttribution() {
        return deserialize(MapAttribution::fromJson, "mapattribution.json");
    }

    static CopyrightCaption getExpectedCopyrightCaption() {
        return deserialize(CopyrightCaption::fromJson, "copyrightcaption.json");
    }

    private static <T> T deserialize(ReadValueCallback<JsonReader, T> deserializer, String resourceName) {
        try (JsonReader jsonReader = JsonProviders.createReader(ClassLoader.getSystemResourceAsStream(resourceName))) {
            return deserializer.read(jsonReader);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
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
            .flatMap(httpClient -> Arrays.stream(MapsRenderServiceVersion.values())
                .map(serviceVersion -> Arguments.of(httpClient, serviceVersion)));
    }
}
