// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoObject;
import com.azure.core.test.TestBase;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.MapsPolygon;
import com.azure.maps.search.models.PointOfInterestCategoryTreeResult;
import com.azure.maps.search.models.ReverseSearchAddressResult;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressResult;
import com.azure.maps.search.models.SearchAddressResult;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import org.junit.jupiter.params.provider.Arguments;

import java.io.ByteArrayOutputStream;
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

    static final String FAKE_API_KEY = "fakeKeyPlaceholder";
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    static MapsPolygon getPolygon(InputStream is) throws IOException {
        JsonSerializer serializer = JsonSerializerProviders.createInstance(true);
        TypeReference<MapsPolygon> interimType = new TypeReference<MapsPolygon>() { };
        byte[] data = null;
        data = toByteArray(is);
        MapsPolygon polygon = serializer.deserializeFromBytes(data, interimType);
        return polygon;
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

    static SearchAddressResult getSearchAddressResult(byte[] data) throws IOException {
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<SearchAddressResult> interimType = new TypeReference<SearchAddressResult>() { };
        SearchAddressResult searchAddressResult = null;
        searchAddressResult = jacksonAdapter.<SearchAddressResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        return searchAddressResult;
    }

    static SearchAddressResult getExpectedFuzzySearchResults() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("searchaddressresult.json");
        byte[] data = null;
        data = toByteArray(is);
        return getSearchAddressResult(data);
    }

    static SearchAddressResult getExpectedSearchPointOfInterestResults() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("searchpointofinterestresult.json");
        byte[] data = null;
        data = toByteArray(is);
        return getSearchAddressResult(data);
    }

    static SearchAddressResult getExpectedSearchNearbyPointOfInterestResults() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("searchnearbypointofinterestresult.json");
        byte[] data = null;
        data = toByteArray(is);
        return getSearchAddressResult(data);
    }

    static SearchAddressResult getExpectedSearchPointOfInterestCategoryResults() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("searchpointofinterestcategoryresult.json");
        byte[] data = null;
        data = toByteArray(is);
        return getSearchAddressResult(data);
    }

    static PointOfInterestCategoryTreeResult getExpectedSearchPointOfInterestCategoryTreeResults() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("getpointofinterestcategorytreeresult.json");
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<PointOfInterestCategoryTreeResult> interimType = new TypeReference<PointOfInterestCategoryTreeResult>() { };
        byte[] data = null;
        data = toByteArray(is);
        PointOfInterestCategoryTreeResult pointOfInterestCategoryTreeResult = null;
        pointOfInterestCategoryTreeResult = jacksonAdapter.<PointOfInterestCategoryTreeResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        return pointOfInterestCategoryTreeResult;
    }

    static SearchAddressResult getExpectedSearchAddressResults() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("searchaddressresult.json");
        byte[] data = null;
        data = toByteArray(is);
        return getSearchAddressResult(data);
    }

    static ReverseSearchAddressResult getExpectedReverseSearchAddressResults() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("reversesearchaddressresult.json");
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<ReverseSearchAddressResult> interimType = new TypeReference<ReverseSearchAddressResult>() { };
        byte[] data = null;
        data = toByteArray(is);
        ReverseSearchAddressResult reverseSearchAddressResult = null;
        reverseSearchAddressResult = jacksonAdapter.<ReverseSearchAddressResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        return reverseSearchAddressResult;
    }

    static ReverseSearchCrossStreetAddressResult getExpectedReverseSearchCrossStreetAddressResults() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("reversesearchcrossstreetaddressresult.json");
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<ReverseSearchCrossStreetAddressResult> interimType = new TypeReference<ReverseSearchCrossStreetAddressResult>() { };
        byte[] data = null;
        data = toByteArray(is);
        ReverseSearchCrossStreetAddressResult reverseSearchCrossStreetAddressResult = null;
        reverseSearchCrossStreetAddressResult = jacksonAdapter.<ReverseSearchCrossStreetAddressResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        return reverseSearchCrossStreetAddressResult;
    }

    static SearchAddressResult getExpectedSearchStructuredAddress() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("searchstructuredaddressresult.json");
        byte[] data = null;
        data = toByteArray(is);
        return getSearchAddressResult(data);
    }

    static GeoObject getGeoObject(File file) throws IOException {
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<GeoObject> interimType = new TypeReference<GeoObject>() { };
        byte[] data = Files.readAllBytes(file.toPath());
        GeoObject obj = jacksonAdapter.<GeoObject>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        return obj;
    }

    static SearchAddressResult getExpectedSearchInsideGeometryCollection() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("searchinsidegeocollectionresult.json");
        byte[] data = null;
        data = toByteArray(is);
        return getSearchAddressResult(data);
    }

    static SearchAddressResult getExpectedSearchInsideGeometry() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("searchinsidegeometryresult.json");
        byte[] data = null;
        data = toByteArray(is);
        return getSearchAddressResult(data);
    }

    static GeoLineString getGeoLineString(File file) throws IOException {
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<GeoLineString> interimType = new TypeReference<GeoLineString>() { };
        byte[] data = Files.readAllBytes(file.toPath());
        GeoLineString obj = jacksonAdapter.<GeoLineString>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        return obj;
    }

    static SearchAddressResult getExpectedSearchAlongRoute() throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("searchalongrouteresult.json");
        byte[] data = null;
        data = toByteArray(is);
        return getSearchAddressResult(data);
    }

    static BatchSearchResult getExpectedBeginFuzzySearchBatch() throws StreamReadException, DatabindException, IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("beginfuzzysearchbatchresult.json");
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<BatchSearchResult> interimType = new TypeReference<BatchSearchResult>() { };
        byte[] data = toByteArray(is);
        BatchSearchResult expectedFuzzySearchBatch = jacksonAdapter.<BatchSearchResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        return expectedFuzzySearchBatch;
    }

    static BatchSearchResult getExpectedBeginSearchAddressBatch() throws StreamReadException, DatabindException, IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("beginsearchaddressbatchresult.json");
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<BatchSearchResult> interimType = new TypeReference<BatchSearchResult>() { };
        byte[] data = toByteArray(is);
        BatchSearchResult expectedSearchAddressBatch = jacksonAdapter.<BatchSearchResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        return expectedSearchAddressBatch;
    }

    static BatchReverseSearchResult getExpectedReverseSearchAddressBatch() throws StreamReadException, DatabindException, IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("beginreversesearchaddressbatchresult.json");
        SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        TypeReference<BatchReverseSearchResult> interimType = new TypeReference<BatchReverseSearchResult>() { };
        byte[] data = toByteArray(is);
        BatchReverseSearchResult expectedReverseSearchAddressBatch = jacksonAdapter.<BatchReverseSearchResult>deserialize(data, interimType.getJavaType(),
            SerializerEncoding.JSON);
        return expectedReverseSearchAddressBatch;
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
        TestBase.getHttpClients()
            .forEach(httpClient -> {
                Arrays.stream(MapsSearchServiceVersion.values())
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

    // Code referenced from
    // https://www.techiedelight.com/convert-inputstream-byte-array-java/#:~:text=Convert%20InputStream%20to%20byte%20array%20in%20Java%201,Commons%20IO%20...%204%204.%20Using%20sun.misc.IOUtils%20
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        // read bytes from the input stream and store them in the buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into the output stream
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
}
