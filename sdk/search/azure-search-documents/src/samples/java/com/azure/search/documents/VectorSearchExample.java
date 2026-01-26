// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.implementation.models.SearchPostOptions;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.QueryAnswerResult;
import com.azure.search.documents.models.QueryAnswerType;
import com.azure.search.documents.models.QueryCaptionResult;
import com.azure.search.documents.models.QueryCaptionType;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.SearchPagedIterable;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.VectorFilterMode;
import com.azure.search.documents.models.VectorQuery;
import com.azure.search.documents.models.VectorizedQuery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.waitForIndexing;

/**
 * This example shows how to work with {@link VectorSearch} while performing searches.
 * <p>
 * This sample is based on the hotels-sample index available to install from the portal with an additional field for
 * vector description.
 *
 * @see <a href="https://docs.microsoft.com/azure/search/search-get-started-portal">Search get started</a>
 */
public class VectorSearchExample {
    /**
     * From the Azure portal, get your Azure AI Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    private static final String INDEX_NAME = "hotels-vector-sample-index";

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildClient();

        SearchClient searchClient = createSearchIndex(searchIndexClient);

        try {
            singleVectorSearch(searchClient);
            singleVectorSearchWithFilter(searchClient);
            simpleHybridSearch(searchClient);
            multiVectorSearch(searchClient);
            semanticHybridSearch(searchClient);
        } finally {
            // Cleanup the example index.
            searchIndexClient.deleteIndex(INDEX_NAME);
        }
    }

    /**
     * Creates a Cognitive Search index for the hotels example data with an additional field for vector search, using
     * the data type {@code SearchFieldDataType.collection(SearchFieldDataType.SINGLE)}.
     * <p>
     * This method will also upload a set of documents with pre-computed vector descriptions.
     *
     * @param searchIndexClient The {@link SearchIndexClient} to use for creating the index.
     * @return The {@link SearchClient} to use for querying the index.
     */
    public static SearchClient createSearchIndex(SearchIndexClient searchIndexClient) {
        // Create the search index, including the new SearchFieldDataType.Single field for vector description.
        SearchIndex searchIndex = new SearchIndex(INDEX_NAME,
            new SearchField("HotelId", SearchFieldDataType.STRING)
                .setKey(true)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(true),
            new SearchField("HotelName", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setFilterable(true)
                .setSortable(true),
            new SearchField("Description", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setFilterable(true),
            new SearchField("DescriptionVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                .setSearchable(true)
                .setVectorSearchDimensions(1536)
                // This must match a vector search configuration name.
                .setVectorSearchProfileName("my-vector-profile"),
            new SearchField("Category", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(true))
            // VectorSearch configuration is required for a vector field.
            // The name used for the vector search algorithm configuration must match the configuration used by the
            // search field used for vector search.
            .setVectorSearch(new VectorSearch()
                .setProfiles(new VectorSearchProfile("my-vector-profile", "my-vector-config"))
                .setAlgorithms(new HnswAlgorithmConfiguration("my-vector-config")))
            .setSemanticSearch(new SemanticSearch().setConfigurations(new SemanticConfiguration("my-semantic-config",
                new SemanticPrioritizedFields()
                    .setTitleField(new SemanticField("HotelName"))
                    .setContentFields(new SemanticField("Description"))
                    .setKeywordsFields(new SemanticField("Category")))));

        searchIndexClient.createOrUpdateIndex(searchIndex);

        SearchClient searchClient = searchIndexClient.getSearchClient(INDEX_NAME);
        searchClient.indexDocuments(new IndexDocumentsBatch(getIndexDocuments().stream()
            .map(doc -> new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(doc))
            .collect(Collectors.toList())));

        waitForIndexing();

        return searchClient;
    }

    /**
     * Example of using vector search without any other search parameters, such as a search query or filters.
     *
     * @param searchClient The {@link SearchClient} to use for querying the index.
     */
    public static void singleVectorSearch(SearchClient searchClient) {
        // Example of using vector search without using a search query or any filters.
        List<Float> vectorizedResult = VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION; // "Top hotels in town"
        VectorQuery vectorizableQuery = new VectorizedQuery(vectorizedResult)
            .setKNearestNeighbors(3)
            // Set the fields to compare the vector against. This is a comma-delimited list of field names.
            .setFields("DescriptionVector");

        SearchPagedIterable searchResults = searchClient.search(new SearchPostOptions()
                .setVectorQueries(vectorizableQuery));

        int count = 0;
        System.out.println("Single Vector Search Results:");
        for (SearchResult searchResult : searchResults) {
            count++;
            Map<String, Object> doc = searchResult.getAdditionalProperties();
            System.out.printf("%s: %s%n", doc.get("HotelId"), doc.get("HotelName"));
        }
        System.out.println("Total number of search results: " + count);
    }

    /**
     * Example of using vector search with a post-search filter.
     *
     * @param searchClient The {@link SearchClient} to use for querying the index.
     */
    public static void singleVectorSearchWithFilter(SearchClient searchClient) {
        // Example of using vector search with a filter.
        List<Float> vectorizedResult = VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION; // "Top hotels in town"
        VectorQuery vectorizableQuery = new VectorizedQuery(vectorizedResult)
            .setKNearestNeighbors(3)
            // Set the fields to compare the vector against. This is a comma-delimited list of field names.
            .setFields("DescriptionVector");

        SearchPagedIterable searchResults = searchClient.search(new SearchPostOptions()
            .setVectorQueries(vectorizableQuery)
            .setVectorFilterMode(VectorFilterMode.POST_FILTER)
            .setFilter("Category eq 'Luxury'"));

        int count = 0;
        System.out.println("Single Vector Search With Filter Results:");
        for (SearchResult searchResult : searchResults) {
            count++;
            Map<String, Object> doc = searchResult.getAdditionalProperties();
            System.out.printf("%s: %s%n", doc.get("HotelId"), doc.get("HotelName"));
        }
        System.out.println("Total number of search results: " + count);
    }

    /**
     * Example of using vector search with a query in addition to vectorization.
     *
     * @param searchClient The {@link SearchClient} to use for querying the index.
     */
    public static void simpleHybridSearch(SearchClient searchClient) {
        // Example of using vector search with a query in addition to vectorization.
        List<Float> vectorizedResult = VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION; // "Top hotels in town"
        VectorQuery vectorizableQuery = new VectorizedQuery(vectorizedResult)
            .setKNearestNeighbors(3)
            // Set the fields to compare the vector against. This is a comma-delimited list of field names.
            .setFields("DescriptionVector");

        SearchPagedIterable searchResults = searchClient.search(new SearchPostOptions()
            .setSearchText("Top hotels in town")
            .setVectorQueries(vectorizableQuery));

        int count = 0;
        System.out.println("Simple Hybrid Search Results:");
        for (SearchResult searchResult : searchResults) {
            count++;
            Map<String, Object> doc = searchResult.getAdditionalProperties();
            System.out.printf("%s: %s%n", doc.get("HotelId"), doc.get("HotelName"));
        }
        System.out.println("Total number of search results: " + count);
    }

    /**
     * Example of using vector search with a semantic query in addition to vectorization.
     * <p>
     * Due to limited availability of Semantic Search this method isn't called in the example. If you know you have
     * access to this feature, you can uncomment the line calling this method in the 'main' method.
     *
     * @param searchClient The {@link SearchClient} to use for querying the index.
     */
    public static void semanticHybridSearch(SearchClient searchClient) {
        // Example of using vector search with a semantic query in addition to vectorization.
        List<Float> vectorizedResult = VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION; // "Top hotels in town"
        VectorQuery vectorizableQuery = new VectorizedQuery(vectorizedResult)
            .setKNearestNeighbors(3)
            // Set the fields to compare the vector against. This is a comma-delimited list of field names.
            .setFields("DescriptionVector");

        SearchPostOptions searchOptions = new SearchPostOptions().setSearchText(
            "Is there any hotel located on the main commercial artery of the city in the heart of New York?")
            .setQueryType(QueryType.SEMANTIC)
            .setVectorQueries(vectorizableQuery)
            .setSemanticConfigurationName("my-semantic-config")
            .setAnswers(QueryAnswerType.EXTRACTIVE)
            .setCaptions(QueryCaptionType.EXTRACTIVE);

        AtomicInteger count = new AtomicInteger();
        searchClient.search(searchOptions).streamByPage().forEach(page -> {
            System.out.println("Semantic Hybrid Search Results:");

            System.out.println("Query Answer:");
            for (QueryAnswerResult result : page.getAnswers()) {
                System.out.println("Answer Highlights: " + result.getHighlights());
                System.out.println("Answer Text: " + result.getText());
            }

            for (SearchResult result : page.getElements()) {
                count.incrementAndGet();
                Map<String, Object> doc = result.getAdditionalProperties();
                System.out.printf("%s: %s%n", doc.get("HotelId"), doc.get("HotelName"));

                if (result.getCaptions() != null) {
                    QueryCaptionResult caption = result.getCaptions().get(0);
                    if (!CoreUtils.isNullOrEmpty(caption.getHighlights())) {
                        System.out.println("Caption Highlights: " + caption.getHighlights());
                    } else {
                        System.out.println("Caption Text: " + caption.getText());
                    }
                }
            }
        });

        System.out.println("Total number of search results: " + count.get());
    }

    public static void multiVectorSearch(SearchClient searchClient) {
        // Example of using multiple vectors in search without using a search query or any filters.
        List<Float> vectorizedResult = VectorSearchEmbeddings.HOTEL1_VECTORIZE_DESCRIPTION;
        VectorQuery firstVectorizableQuery = new VectorizedQuery(vectorizedResult)
            .setKNearestNeighbors(3)
            // Set the fields to compare the vector against. This is a comma-delimited list of field names.
            .setFields("DescriptionVector");

        List<Float> secondVectorizedResult = VectorSearchEmbeddings.HOTEL2_VECTORIZE_DESCRIPTION;
        VectorQuery secondVectorizableQuery = new VectorizedQuery(secondVectorizedResult)
            .setKNearestNeighbors(3)
            // Set the fields to compare the vector against. This is a comma-delimited list of field names.
            .setFields("DescriptionVector");

        List<Float> thirdVectorizedResult = VectorSearchEmbeddings.HOTEL3_VECTORIZE_DESCRIPTION;
        VectorQuery thirdVectorizableQuery = new VectorizedQuery(thirdVectorizedResult)
            .setKNearestNeighbors(3)
            // Set the fields to compare the vector against. This is a comma-delimited list of field names.
            .setFields("DescriptionVector");

        SearchPagedIterable searchResults = searchClient.search(new SearchPostOptions()
            .setVectorQueries(firstVectorizableQuery, secondVectorizableQuery, thirdVectorizableQuery));

        int count = 0;
        System.out.println("Multi Vector Search Results:");
        for (SearchResult searchResult : searchResults) {
            count++;
            Map<String, Object> doc = searchResult.getAdditionalProperties();
            System.out.printf("%s: %s%n", doc.get("HotelId"), doc.get("HotelName"));
        }
        System.out.println("Total number of search results: " + count);
    }

    /**
     * Hotel model with an additional field for the vector description.
     */
    public static final class VectorHotel implements JsonSerializable<VectorHotel> {
        @SimpleField(name = "HotelId", isKey = true)
        private String hotelId;
        @SearchableField(name = "HotelName", isFilterable = true, isSortable = true, analyzerName = "en.lucene")
        private String hotelName;
        @SearchableField(name = "Description", analyzerName = "en.lucene")
        private String description;
        @SearchableField(name = "DescriptionVector", vectorSearchDimensions = 1536, vectorSearchProfileName = "my-vector-profile")
        private List<Float> descriptionVector;
        @SearchableField(name = "Category", isFilterable = true, isFacetable = true, isSortable = true)
        private String category;

        public VectorHotel() {
        }

        public String getHotelId() {
            return hotelId;
        }

        public VectorHotel setHotelId(String hotelId) {
            this.hotelId = hotelId;
            return this;
        }

        public String getHotelName() {
            return hotelName;
        }

        public VectorHotel setHotelName(String hotelName) {
            this.hotelName = hotelName;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public VectorHotel setDescription(String description) {
            this.description = description;
            return this;
        }

        public List<Float> getDescriptionVector() {
            return descriptionVector == null ? null : Collections.unmodifiableList(descriptionVector);
        }

        public VectorHotel setDescriptionVector(List<Float> descriptionVector) {
            this.descriptionVector = descriptionVector == null ? null : new ArrayList<>(descriptionVector);
            return this;
        }

        public String getCategory() {
            return category;
        }

        public VectorHotel setCategory(String category) {
            this.category = category;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("HotelId", hotelId)
                .writeStringField("HotelName", hotelName)
                .writeStringField("Description", description)
                .writeArrayField("DescriptionVector", descriptionVector, JsonWriter::writeFloat)
                .writeStringField("Category", category)
                .writeEndObject();
        }

        public static VectorHotel fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                VectorHotel vectorHotel = new VectorHotel();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("HotelId".equals(fieldName)) {
                        vectorHotel.hotelId = reader.getString();
                    } else if ("HotelName".equals(fieldName)) {
                        vectorHotel.hotelName = reader.getString();
                    } else if ("Description".equals(fieldName)) {
                        vectorHotel.description = reader.getString();
                    } else if ("DescriptionVector".equals(fieldName)) {
                        vectorHotel.descriptionVector = reader.readArray(JsonReader::getFloat);
                    } else if ("Category".equals(fieldName)) {
                        vectorHotel.category = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }

                return vectorHotel;
            });
        }
    }

    /**
     * Gets a list of hotels with vectorized descriptions.
     *
     * @return A list of hotels.
     */
    public static List<Map<String, Object>> getIndexDocuments() {
        List<VectorHotel> hotels = Arrays.asList(
            new VectorHotel()
                .setHotelId("1")
                .setHotelName("Fancy Stay")
                .setDescription("Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a "
                    + "spa, and a really helpful concierge. The location is perfect -- right downtown, close to all "
                    + "the tourist attractions. We highly recommend this hotel.")
                .setDescriptionVector(VectorSearchEmbeddings.HOTEL1_VECTORIZE_DESCRIPTION)
                .setCategory("Luxury"),
            new VectorHotel()
                .setHotelId("2")
                .setHotelName("Roach Motel")
                .setDescription("Below average motel with a extremely rude staff, no complimentary breakfast, and "
                    + "noisy rooms riddled with cockroaches.")
                .setDescriptionVector(VectorSearchEmbeddings.HOTEL2_VECTORIZE_DESCRIPTION)
                .setCategory("Budget"),
            new VectorHotel()
                .setHotelId("3")
                .setHotelName("EconoStay")
                .setDescription("Very popular hotel in town. It's located downtown, close to all tourist attractions.")
                .setDescriptionVector(VectorSearchEmbeddings.HOTEL3_VECTORIZE_DESCRIPTION)
                .setCategory("Budget"),
            new VectorHotel()
                .setHotelId("4")
                .setHotelName("Modern Stay")
                .setDescription("Modern architecture, very polite staff and very clean. Also very affordable.")
                .setDescriptionVector(VectorSearchEmbeddings.HOTEL4_VECTORIZE_DESCRIPTION)
                .setCategory("Luxury"),
            new VectorHotel()
                .setHotelId("5")
                .setHotelName("Secret Point")
                .setDescription("The hotel is ideally located on the main commercial artery of the city in the heart "
                    + "of New York. A few minutes away is Time's Square and the historic centre of the city, as well "
                    + "as other places of interest that make New York one of America's most attractive and "
                    + "cosmopolitan cities.")
                .setDescriptionVector(VectorSearchEmbeddings.HOTEL9_VECTORIZE_DESCRIPTION)
                .setCategory("Boutique"));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
                jsonWriter.writeArray(hotels, JsonWriter::writeJson);
            }

            try (JsonReader jsonReader = JsonProviders.createReader(outputStream.toByteArray())) {
                return jsonReader.readArray(elem -> elem.readMap(JsonReader::readUntyped));
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        // Add more hotel documents here...
    }
}
