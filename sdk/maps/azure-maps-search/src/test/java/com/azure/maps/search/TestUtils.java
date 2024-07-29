// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoObject;
import com.azure.core.test.TestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.ReadValueCallback;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.MapsPolygon;
import com.azure.maps.search.models.PointOfInterestCategoryTreeResult;
import com.azure.maps.search.models.ReverseSearchAddressResult;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressResult;
import com.azure.maps.search.models.SearchAddressResult;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TestUtils {
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    static List<MapsPolygon> getMultiPolygonsResults() {
        List<MapsPolygon> result = new ArrayList<>();
        result.add(deserialize("polygon1.json", MapsPolygon::fromJson));
        result.add(deserialize("polygon2.json", MapsPolygon::fromJson));
        return result;
    }

    static SearchAddressResult getExpectedFuzzySearchResults() {
        return deserialize("searchaddressresult.json", SearchAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchPointOfInterestResults() {
        return deserialize("searchpointofinterestresult.json", SearchAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchNearbyPointOfInterestResults() {
        return deserialize("searchnearbypointofinterestresult.json", SearchAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchPointOfInterestCategoryResults() {
        return deserialize("searchpointofinterestcategoryresult.json", SearchAddressResult::fromJson);
    }

    static PointOfInterestCategoryTreeResult getExpectedSearchPointOfInterestCategoryTreeResults() {
        return deserialize("getpointofinterestcategorytreeresult.json", PointOfInterestCategoryTreeResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchAddressResults() {
        return deserialize("searchaddressresult.json", SearchAddressResult::fromJson);
    }

    static ReverseSearchAddressResult getExpectedReverseSearchAddressResults() {
        return deserialize("reversesearchaddressresult.json", ReverseSearchAddressResult::fromJson);
    }

    static ReverseSearchCrossStreetAddressResult getExpectedReverseSearchCrossStreetAddressResults() {
        return deserialize("reversesearchcrossstreetaddressresult.json",
            ReverseSearchCrossStreetAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchStructuredAddress() {
        return deserialize("searchstructuredaddressresult.json", SearchAddressResult::fromJson);
    }

    static GeoObject getGeoObject(File file) {
        try (JsonReader jsonReader = JsonProviders.createReader(Files.readAllBytes(file.toPath()))) {
            return GeoObject.fromJson(jsonReader);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static SearchAddressResult getExpectedSearchInsideGeometryCollection() {
        return deserialize("searchinsidegeocollectionresult.json", SearchAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchInsideGeometry() {
        return deserialize("searchinsidegeometryresult.json", SearchAddressResult::fromJson);
    }

    static GeoLineString getGeoLineString(File file) {
        try (JsonReader jsonReader = JsonProviders.createReader(Files.readAllBytes(file.toPath()))) {
            return GeoLineString.fromJson(jsonReader);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static SearchAddressResult getExpectedSearchAlongRoute() {
        return deserialize("searchalongrouteresult.json", SearchAddressResult::fromJson);
    }

    static BatchSearchResult getExpectedBeginFuzzySearchBatch() {
        return deserialize("beginfuzzysearchbatchresult.json", BatchSearchResult::fromJson);
    }

    static BatchSearchResult getExpectedBeginSearchAddressBatch() {
        return deserialize("beginsearchaddressbatchresult.json", BatchSearchResult::fromJson);
    }

    static BatchReverseSearchResult getExpectedReverseSearchAddressBatch() {
        return deserialize("beginreversesearchaddressbatchresult.json", BatchReverseSearchResult::fromJson);
    }

    // file inside helpers --> implementation --> com/azure/maps/search/java --> resources

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

    private static <T> T deserialize(String resourceName,
        ReadValueCallback<JsonReader, T> deserializer) {
        try (JsonReader jsonReader = JsonProviders.createReader(ClassLoader.getSystemResourceAsStream(resourceName))) {
            return deserializer.read(jsonReader);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
