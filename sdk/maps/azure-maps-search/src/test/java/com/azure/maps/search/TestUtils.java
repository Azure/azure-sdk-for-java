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
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TestUtils {
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    static MapsPolygon getPolygon(InputStream is) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(is)) {
            return MapsPolygon.fromJson(jsonReader);
        }
    }

    static List<MapsPolygon> getMultiPolygonsResults() throws IOException {
        List<MapsPolygon> result = new ArrayList<>();
        InputStream is = ClassLoader.getSystemResourceAsStream("polygon1.json");
        MapsPolygon polygon1 = TestUtils.getPolygon(is);
        InputStream is2 = ClassLoader.getSystemResourceAsStream("polygon2.json");
        MapsPolygon polygon2 = TestUtils.getPolygon(is2);
        result.add(polygon1);
        result.add(polygon2);
        return result;
    }

    static SearchAddressResult getExpectedFuzzySearchResults() throws IOException {
        return deserialize("searchaddressresult.json", SearchAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchPointOfInterestResults() throws IOException {
        return deserialize("searchpointofinterestresult.json", SearchAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchNearbyPointOfInterestResults() throws IOException {
        return deserialize("searchnearbypointofinterestresult.json", SearchAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchPointOfInterestCategoryResults() throws IOException {
        return deserialize("searchpointofinterestcategoryresult.json", SearchAddressResult::fromJson);
    }

    static PointOfInterestCategoryTreeResult getExpectedSearchPointOfInterestCategoryTreeResults() throws IOException {
        return deserialize("getpointofinterestcategorytreeresult.json", PointOfInterestCategoryTreeResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchAddressResults() throws IOException {
        return deserialize("searchaddressresult.json", SearchAddressResult::fromJson);
    }

    static ReverseSearchAddressResult getExpectedReverseSearchAddressResults() throws IOException {
        return deserialize("reversesearchaddressresult.json", ReverseSearchAddressResult::fromJson);
    }

    static ReverseSearchCrossStreetAddressResult getExpectedReverseSearchCrossStreetAddressResults()
        throws IOException {
        return deserialize("reversesearchcrossstreetaddressresult.json",
            ReverseSearchCrossStreetAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchStructuredAddress() throws IOException {
        return deserialize("searchstructuredaddressresult.json", SearchAddressResult::fromJson);
    }

    static GeoObject getGeoObject(File file) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(Files.readAllBytes(file.toPath()))) {
            return GeoObject.fromJson(jsonReader);
        }
    }

    static SearchAddressResult getExpectedSearchInsideGeometryCollection() throws IOException {
        return deserialize("searchinsidegeocollectionresult.json", SearchAddressResult::fromJson);
    }

    static SearchAddressResult getExpectedSearchInsideGeometry() throws IOException {
        return deserialize("searchinsidegeometryresult.json", SearchAddressResult::fromJson);
    }

    static GeoLineString getGeoLineString(File file) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(Files.readAllBytes(file.toPath()))) {
            return GeoLineString.fromJson(jsonReader);
        }
    }

    static SearchAddressResult getExpectedSearchAlongRoute() throws IOException {
        return deserialize("searchalongrouteresult.json", SearchAddressResult::fromJson);
    }

    static BatchSearchResult getExpectedBeginFuzzySearchBatch() throws IOException {
        return deserialize("beginfuzzysearchbatchresult.json", BatchSearchResult::fromJson);
    }

    static BatchSearchResult getExpectedBeginSearchAddressBatch() throws IOException {
        return deserialize("beginsearchaddressbatchresult.json", BatchSearchResult::fromJson);
    }

    static BatchReverseSearchResult getExpectedReverseSearchAddressBatch() throws IOException {
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
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        TestBase.getHttpClients().forEach(httpClient -> {
            Arrays.stream(MapsSearchServiceVersion.values())
                .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
        });
        return argumentsList.stream();
    }

    private static <T> T deserialize(String resourceName,
        ReadValueCallback<JsonReader, T> deserializer) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(ClassLoader.getSystemResourceAsStream(resourceName))) {
            return deserializer.read(jsonReader);
        }
    }
}
