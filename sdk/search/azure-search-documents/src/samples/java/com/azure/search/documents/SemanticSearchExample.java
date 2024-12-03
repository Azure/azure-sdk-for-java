// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.models.QueryAnswer;
import com.azure.search.documents.models.QueryAnswerResult;
import com.azure.search.documents.models.QueryAnswerType;
import com.azure.search.documents.models.QueryCaption;
import com.azure.search.documents.models.QueryCaptionResult;
import com.azure.search.documents.models.QueryCaptionType;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SemanticErrorMode;
import com.azure.search.documents.models.SemanticSearchOptions;
import com.azure.search.documents.models.SemanticSearchResults;
import com.azure.search.documents.util.SearchPagedIterable;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.azure.search.documents.TestHelpers.waitForIndexing;

/**
 * This example shows how to work with {@link SemanticSearch} while performing searches.
 * <p>
 * This sample is based on the hotels-sample index available to install from the portal.
 * <p>
 * See https://docs.microsoft.com/azure/search/search-get-started-portal
 */
public class SemanticSearchExample {
    /**
     * From the Azure portal, get your Azure AI Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    private static final String INDEX_NAME = "hotels-semantic-sample-index";

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildClient();

        SearchClient searchClient = createSearchIndex(searchIndexClient);

        try {
            semanticSearch(searchClient);
        } finally {
            // Cleanup the example index.
            searchIndexClient.deleteIndex(INDEX_NAME);
        }
    }

    /**
     * Creates a Cognitive Search index for the hotels example data.
     * <p>
     * This method will also upload a set of documents.
     *
     * @param searchIndexClient The {@link SearchIndexClient} to use for creating the index.
     * @return The {@link SearchClient} to use for querying the index.
     */
    public static SearchClient createSearchIndex(SearchIndexClient searchIndexClient) {
        // Create the search index.
        SearchIndex searchIndex = new SearchIndex(INDEX_NAME)
            .setFields(
                new SearchField("HotelId", SearchFieldDataType.STRING)
                    .setKey(true),
                new SearchField("HotelName", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setSortable(true),
                new SearchField("Description", SearchFieldDataType.STRING)
                    .setSearchable(true),
                new SearchField("Category", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setSortable(true)
                    .setFacetable(true))
            .setSemanticSearch(new SemanticSearch().setConfigurations(Arrays.asList(new SemanticConfiguration(
                "my-semantic-config", new SemanticPrioritizedFields()
                .setTitleField(new SemanticField("HotelName"))
                .setContentFields(new SemanticField("Description"))
                .setKeywordsFields(new SemanticField("Category"))))));

        searchIndexClient.createOrUpdateIndex(searchIndex);

        SearchClient searchClient = searchIndexClient.getSearchClient(INDEX_NAME);
        searchClient.uploadDocuments(getIndexDocuments());

        waitForIndexing();

        return searchClient;
    }

    /**
     * Example of using semantic search.
     *
     * @param searchClient The {@link SearchClient} to use for querying the index.
     */
    public static void semanticSearch(SearchClient searchClient) {
        SearchOptions searchOptions = new SearchOptions()
            .setSemanticSearchOptions(new SemanticSearchOptions()
                .setSemanticConfigurationName("my-semantic-config")
                .setQueryAnswer(new QueryAnswer(QueryAnswerType.EXTRACTIVE))
                .setQueryCaption(new QueryCaption(QueryCaptionType.EXTRACTIVE))
                .setErrorMode(SemanticErrorMode.PARTIAL)
                .setMaxWaitDuration(Duration.ofSeconds(5)));

        SearchPagedIterable results = searchClient.search(
            "Is there any hotel located on the main commercial artery of the city in the heart of New York?",
            searchOptions, Context.NONE);

        int count = 0;
        System.out.println("Semantic Hybrid Search Results:");

        SemanticSearchResults semanticSearchResults = results.getSemanticResults();

        System.out.println("Semantic Results Type: " + semanticSearchResults.getResultsType());

        if (semanticSearchResults.getErrorReason() != null) {
            System.out.println("Semantic Error Reason: " + semanticSearchResults.getErrorReason());
        }

        System.out.println("Query Answers:");
        for (QueryAnswerResult result : semanticSearchResults.getQueryAnswers()) {
            System.out.println("Answer Highlights: " + result.getHighlights());
            System.out.println("Answer Text: " + result.getText());
        }

        for (SearchResult result : results) {
            count++;
            Hotel doc = result.getDocument(Hotel.class);
            System.out.printf("%s: %s%n", doc.getHotelId(), doc.getHotelName());

            if (result.getSemanticSearch().getQueryCaptions() != null) {
                QueryCaptionResult caption = result.getSemanticSearch().getQueryCaptions().get(0);
                if (!CoreUtils.isNullOrEmpty(caption.getHighlights())) {
                    System.out.println("Caption Highlights: " + caption.getHighlights());
                } else {
                    System.out.println("Caption Text: " + caption.getText());
                }
            }
        }

        System.out.println("Total number of search results: " + count);
    }

    /**
     * Hotel model.
     */
    public static final class Hotel implements JsonSerializable<Hotel> {
        @SimpleField(isKey = true)
        private String hotelId;
        @SearchableField(isFilterable = true, analyzerName = "en.lucene")
        private String hotelName;
        @SearchableField(analyzerName = "en.lucene")
        private String description;
        @SearchableField(isFilterable = true, isFacetable = true, isSortable = true)
        private String category;

        public Hotel() {
        }

        public String getHotelId() {
            return hotelId;
        }

        public Hotel setHotelId(String hotelId) {
            this.hotelId = hotelId;
            return this;
        }

        public String getHotelName() {
            return hotelName;
        }

        public Hotel setHotelName(String hotelName) {
            this.hotelName = hotelName;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Hotel setDescription(String description) {
            this.description = description;
            return this;
        }

        public String getCategory() {
            return category;
        }

        public Hotel setCategory(String category) {
            this.category = category;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("HotelId", hotelId)
                .writeStringField("HotelName", hotelName)
                .writeStringField("Description", description)
                .writeStringField("Category", category)
                .writeEndObject();
        }

        public static Hotel fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                Hotel hotel = new Hotel();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("HotelId".equals(fieldName)) {
                        hotel.hotelId = reader.getString();
                    } else if ("HotelName".equals(fieldName)) {
                        hotel.hotelName = reader.getString();
                    } else if ("Description".equals(fieldName)) {
                        hotel.description = reader.getString();
                    } else if ("Category".equals(fieldName)) {
                        hotel.category = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }

                return hotel;
            });
        }
    }

    /**
     * Gets a list of hotels.
     *
     * @return A list of hotels.
     */
    public static List<Hotel> getIndexDocuments() {
        return Arrays.asList(
            new Hotel()
                .setHotelId("1")
                .setHotelName("Fancy Stay")
                .setDescription("Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a "
                                + "spa, and a really helpful concierge. The location is perfect -- right downtown, close to all "
                                + "the tourist attractions. We highly recommend this hotel.")
                .setCategory("Luxury"),
            new Hotel()
                .setHotelId("2")
                .setHotelName("Roach Motel")
                .setDescription("Below average motel with a extremely rude staff, no complimentary breakfast, and "
                                + "noisy rooms riddled with cockroaches.")
                .setCategory("Budget"),
            new Hotel()
                .setHotelId("3")
                .setHotelName("EconoStay")
                .setDescription("Very popular hotel in town. It's located downtown, close to all tourist attractions.")
                .setCategory("Budget"),
            new Hotel()
                .setHotelId("4")
                .setHotelName("Modern Stay")
                .setDescription("Modern architecture, very polite staff and very clean. Also very affordable.")
                .setCategory("Luxury"),
            new Hotel()
                .setHotelId("5")
                .setHotelName("Secret Point")
                .setDescription("The hotel is ideally located on the main commercial artery of the city in the heart "
                                + "of New York. A few minutes away is Time's Square and the historic centre of the city, as well "
                                + "as other places of interest that make New York one of America's most attractive and "
                                + "cosmopolitan cities.")
                .setCategory("Boutique"));

        // Add more hotel documents here...
    }
}
