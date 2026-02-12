// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPosition;
import com.azure.core.test.TestMode;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.SearchOptions;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.convertFromMapStringObject;

/**
 * This class tests indexes using OData type GeographyPoint.
 */
public class GeographyPointTests extends SearchTestBase {
    private static final List<SearchField> SEARCH_FIELDS = Arrays.asList(
        new SearchField("id", SearchFieldDataType.STRING).setKey(true),
        new SearchField("geography_point", SearchFieldDataType.GEOGRAPHY_POINT).setFilterable(true).setSortable(true),
        new SearchField("description", SearchFieldDataType.STRING).setSearchable(true));

    private static final GeoPoint SPACE_NEEDLE = new GeoPoint(-122.348616, 47.622151);
    private static final GeoPoint PIKES_PLACE_MARKET = new GeoPoint(-122.340529, 47.608564);
    private static final GeoPoint PARADISE_VISITOR_CENTER = new GeoPoint(-121.736604, 46.786549);
    private static final GeoPoint EMPIRE_STATE_BUILDER = new GeoPoint(new GeoPosition(-73.985707, 40.748521));

    private static final String INDEX_NAME = "azs-java-shared-geography-point-index";

    private static SearchIndexClient searchIndexClient;

    private SearchClient searchClient;
    private SearchAsyncClient searchAsyncClient;

    private static List<SimpleDocument> getDocuments() {
        return Arrays.asList(new SimpleDocument("1", SPACE_NEEDLE, "Tourist location"),
            new SimpleDocument("2", PIKES_PLACE_MARKET, "Tourist location"),
            new SimpleDocument("3", PARADISE_VISITOR_CENTER, "Tourist location"),
            new SimpleDocument("4", EMPIRE_STATE_BUILDER, "Tourist location"));
    }

    private static Map<String, SimpleDocument> getExpectedDocuments() {
        return getDocuments().stream().collect(Collectors.toMap(SimpleDocument::getId, Function.identity()));
    }

    @BeforeAll
    public static void createSharedIndex() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
                .credential(TestHelpers.getTestTokenCredential())
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            searchIndexClient.createIndex(new SearchIndex(INDEX_NAME, SEARCH_FIELDS));
            searchIndexClient.getSearchClient(INDEX_NAME)
                .index(new IndexDocumentsBatch(getDocuments().stream()
                    .map(document -> new IndexAction().setActionType(IndexActionType.UPLOAD)
                        .setAdditionalProperties(TestHelpers.convertToMapStringObject(document)))
                    .collect(Collectors.toList())));

            TestHelpers.sleepIfRunningAgainstService(2000);
        }
    }

    @AfterAll
    public static void deleteSharedIndex() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(INDEX_NAME);
        }
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();

        searchClient = getSearchClientBuilder(INDEX_NAME, true).buildClient();
        searchAsyncClient = getSearchClientBuilder(INDEX_NAME, false).buildAsyncClient();
    }

    @Test
    public void canRoundTripGeographyPointsSync() {
        Map<String, SimpleDocument> expectedDocuments = getExpectedDocuments();
        Map<String, SimpleDocument> actualDocuments = new HashMap<>();

        actualDocuments.put("1", convertFromMapStringObject(searchClient.getDocument("1").getAdditionalProperties(),
            SimpleDocument::fromJson));
        actualDocuments.put("2", convertFromMapStringObject(searchClient.getDocument("2").getAdditionalProperties(),
            SimpleDocument::fromJson));
        actualDocuments.put("3", convertFromMapStringObject(searchClient.getDocument("3").getAdditionalProperties(),
            SimpleDocument::fromJson));
        actualDocuments.put("4", convertFromMapStringObject(searchClient.getDocument("4").getAdditionalProperties(),
            SimpleDocument::fromJson));

        compareMaps(expectedDocuments, actualDocuments, Assertions::assertEquals);

        actualDocuments = searchClient.search(new SearchOptions().setSearchText("Tourist location").setOrderBy("id"))
            .stream()
            .map(doc -> convertFromMapStringObject(doc.getAdditionalProperties(), SimpleDocument::fromJson))
            .collect(Collectors.toMap(SimpleDocument::getId, Function.identity()));

        compareMaps(expectedDocuments, actualDocuments, Assertions::assertEquals);
    }

    @Test
    public void canRoundTripGeographyPointsAsync() {
        Map<String, SimpleDocument> expectedDocuments = getExpectedDocuments();

        Mono<Map<String, SimpleDocument>> getDocumentsByIdMono = Flux.just("1", "2", "3", "4")
            .flatMap(id -> searchAsyncClient.getDocument(id))
            .map(doc -> convertFromMapStringObject(doc.getAdditionalProperties(), SimpleDocument::fromJson))
            .collectMap(SimpleDocument::getId);

        StepVerifier.create(getDocumentsByIdMono)
            .assertNext(actualDocuments -> compareMaps(expectedDocuments, actualDocuments, Assertions::assertEquals))
            .verifyComplete();

        Mono<Map<String, SimpleDocument>> searchDocumentsMono
            = searchAsyncClient.search(new SearchOptions().setSearchText("Tourist location").setOrderBy("id"))
                .map(doc -> convertFromMapStringObject(doc.getAdditionalProperties(), SimpleDocument::fromJson))
                .collectMap(SimpleDocument::getId);

        StepVerifier.create(searchDocumentsMono)
            .assertNext(actualDocuments -> compareMaps(expectedDocuments, actualDocuments, Assertions::assertEquals))
            .verifyComplete();
    }

    public static final class SimpleDocument implements JsonSerializable<SimpleDocument> {
        @JsonProperty("id")
        private final String id;

        @JsonProperty("geography_point")
        private final GeoPoint geoPoint;

        @JsonProperty("description")
        private final String description;

        @JsonCreator
        public SimpleDocument(@JsonProperty("id") String id, @JsonProperty("geography_point") GeoPoint geoPoint,
            @JsonProperty("description") String description) {
            this.id = id;
            this.geoPoint = geoPoint;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public GeoPoint getGeoPoint() {
            return geoPoint;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SimpleDocument)) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            SimpleDocument other = (SimpleDocument) obj;
            return Objects.equals(id, other.id)
                && Objects.equals(geoPoint.getCoordinates(), other.geoPoint.getCoordinates())
                && Objects.equals(description, other.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, geoPoint.getCoordinates(), description);
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("id", id)
                .writeJsonField("geography_point", geoPoint)
                .writeStringField("description", description)
                .writeEndObject();
        }

        public static SimpleDocument fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                String id = null;
                GeoPoint geoPoint = null;
                String description = null;

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("id".equals(fieldName)) {
                        id = reader.getString();
                    } else if ("geography_point".equals(fieldName)) {
                        geoPoint = GeoPoint.fromJson(reader);
                    } else if ("description".equals(fieldName)) {
                        description = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }

                return new SimpleDocument(id, geoPoint, description);
            });
        }
    }
}
