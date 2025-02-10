// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizer;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.VectorSearchOptions;
import com.azure.search.documents.models.VectorizableTextQuery;
import com.azure.search.documents.util.SearchPagedIterable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This sample demonstrates how to create a vector fields index with reduced dimensions, upload reduced embeddings into
 * the index, and query the documents. To accomplish this, you can utilize Azure OpenAI embedding models: a smaller and
 * highly efficient {@code text-embedding-3-small} model or a larger and more powerful {@code text-embedding-3-large}
 * model. These models are significantly more efficient and require less storage space.
 */
public class VectorSearchReducedEmbeddings {
    public static void main(String[] args) {
        SearchIndex vectorIndex = defineVectorIndex();

        // After creating an instance of the 'SearchIndex',we need to instantiate the 'SearchIndexClient' and call the
        // 'createIndex' method to create the search index.
        createVectorIndex(vectorIndex);

        // Now, we can instantiate the 'SearchClient' and upload the documents to the 'Hotel' index we created earlier.
        SearchClient searchClient = new SearchClientBuilder().endpoint(
                Configuration.getGlobalConfiguration().get("SEARCH_ENDPOINT"))
            .indexName("hotel")
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("SEARCH_API_KEY")))
            .buildClient();

        // Next, we will create sample hotel documents. The vector field requires submitting text input to an embedding
        // model that converts human-readable text into a vector representation. To convert a text query string provided
        // by a user into a vector representation, your application should utilize an embedding library that offers this
        // functionality.
        indexDocuments(searchClient, getHotelDocuments());

        // When using 'VectorizableTextQuery', the query for a vector field should be the text that will be vectorized
        // based on the 'Vectorizer' configuration in order to perform a vector search.
        //
        // Let's query the index and make sure everything works as implemented. You can also refer to
        // https://learn.microsoft.com/azure/search/vector-search-how-to-query for more information on querying vector
        // data.
    }

    /**
     * Let's consider the example of a 'Hotel'. First, we need to create an index for storing hotel information. In this
     * index, we will define vector fields called 'DescriptionVector' and 'CategoryVector'. To configure the vector
     * field, you need to provide the model dimensions, which indicate the size of the embeddings generated for this
     * field. You can pass reduced dimensions and the name of the vector search profile that specifies the algorithm
     * configuration, along with 'Vectorizer'.
     * <p>
     * In order to get the reduced embeddings using either the {@code text-embedding-3-small} or
     * {@code text-embedding-3-large} models, it is necessary to include the 'Dimensions' parameter. This parameter
     * configures the desired number of dimensions for the output vector. Therefore, for {@link AzureOpenAIVectorizer},
     * we will retrieve the 'VectorSearchDimensions' that is already specified in the corresponding index field
     * definition. However, to ensure that dimensions are only passed along in the vectorizer for a model that supports
     * it, we need to pass a required property named 'ModelName'. This property enables the service to determine which
     * model we are using, and dimensions will only be passed along when it is for a known supported model name.
     * <p>
     * We will create an instace of {@code SearchIndex} and define 'Hotel' fields.
     */
    public static SearchIndex defineVectorIndex() {
        String vectorSearchProfileName = "my-vector-profile";
        String vectorSearchHnswConfig = "my-hnsw-vector-config";
        String deploymentId = "my-text-embedding-3-small";
        int modelDimensions = 256; // Here's the reduced model dimensions
        String indexName = "hotel";
        return new SearchIndex(indexName).setFields(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true)
                    .setFilterable(true)
                    .setSortable(true)
                    .setFacetable(true), new SearchField("HotelName", SearchFieldDataType.STRING).setSearchable(true)
                    .setFilterable(true)
                    .setSortable(true),
                new SearchField("Description", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true),
                new SearchField("DescriptionVector",
                    SearchFieldDataType.collection(SearchFieldDataType.SINGLE)).setSearchable(true)
                    .setFilterable(true)
                    .setVectorSearchDimensions(modelDimensions)
                    .setVectorSearchProfileName(vectorSearchProfileName),
                new SearchField("Category", SearchFieldDataType.STRING).setSearchable(true)
                    .setFilterable(true)
                    .setSortable(true)
                    .setFacetable(true),
                new SearchField("CategoryVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE)).setSearchable(
                        true)
                    .setFilterable(true)
                    .setVectorSearchDimensions(modelDimensions)
                    .setVectorSearchProfileName(vectorSearchProfileName))
            .setVectorSearch(new VectorSearch().setProfiles(
                    new VectorSearchProfile(vectorSearchProfileName, vectorSearchHnswConfig).setVectorizerName("openai"))
                .setAlgorithms(new HnswAlgorithmConfiguration(vectorSearchHnswConfig))
                .setVectorizers(Collections.singletonList(new AzureOpenAIVectorizer("openai").setParameters(
                    new AzureOpenAIVectorizerParameters().setResourceUrl(
                            Configuration.getGlobalConfiguration().get("OPENAI_ENDPOINT"))
                        .setApiKey(Configuration.getGlobalConfiguration().get("OPENAI_KEY"))
                        .setDeploymentName(deploymentId)
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING_3_LARGE)))));
    }

    public static void createVectorIndex(SearchIndex vectorIndex) {
        // Instantiate the 'SearchIndexClient' and call the 'createIndex' method to create the search index.
        String endpoint = Configuration.getGlobalConfiguration().get("SEARCH_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("SEARCH_API_KEY");
        AzureKeyCredential credential = new AzureKeyCredential(key);

        SearchIndexClient indexClient = new SearchIndexClientBuilder().endpoint(endpoint)
            .credential(credential)
            .buildClient();

        indexClient.createIndex(vectorIndex);
    }

    // Simple model type for Hotel

    /**
     * Hotel model with an additional field for the vector description.
     */
    public static final class VectorHotel implements JsonSerializable<VectorHotel> {
        @SimpleField(isKey = true)
        private String hotelId;
        @SearchableField(isFilterable = true, isSortable = true, analyzerName = "en.lucene")
        private String hotelName;
        @SearchableField(analyzerName = "en.lucene")
        private String description;
        @SearchableField(vectorSearchDimensions = 256, vectorSearchProfileName = "my-vector-profile")
        private List<Float> descriptionVector;
        @SearchableField(isFilterable = true, isFacetable = true, isSortable = true)
        private String category;
        @SearchableField(vectorSearchDimensions = 256, vectorSearchProfileName = "my-vector-profile")
        private List<Float> categoryVector;

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

        public List<Float> getCategoryVector() {
            return categoryVector == null ? null : Collections.unmodifiableList(categoryVector);
        }

        public VectorHotel setCategoryVector(List<Float> categoryVector) {
            this.categoryVector = categoryVector == null ? null : new ArrayList<>(categoryVector);
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
                .writeArrayField("DescriptionVector", categoryVector, JsonWriter::writeFloat)
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
                    } else if ("CategoryVector".equals(fieldName)) {
                        vectorHotel.categoryVector = reader.readArray(JsonReader::getFloat);
                    } else {
                        reader.skipChildren();
                    }
                }

                return vectorHotel;
            });
        }
    }

    /**
     * Get Embeddings using {@code azure-ai-openai} library.
     * <p>
     * You can use Azure OpenAI embedding models, {@code text-embedding-3-small} or {@code text-embedding-3-large}, to
     * get the reduced embeddings. With these models, you can specify the desired number of dimensions for the output
     * vector by passing the 'Dimensions' property. This enables you to customize the output according to your needs.
     * <p>
     * For more details about how to generate embeddings, refer to the
     * <a href="https://learn.microsoft.com/azure/search/vector-search-how-to-generate-embeddings">documentation</a>.
     * Here's an example of how you can get embeddings using
     * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/README.md">azure-ai-openai</a>
     * library.
     */
    public static List<Float> getEmbeddings(String input) {
        // Get embeddings using Azure OpenAI
        String endpoint = Configuration.getGlobalConfiguration().get("OPENAI_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("OPENAI_API_KEY");
        KeyCredential credential = new KeyCredential(key);

        OpenAIClient openAIClient = new OpenAIClientBuilder().endpoint(endpoint).credential(credential).buildClient();
        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(Collections.singletonList(input)).setModel(
            "my-text-embedding-3-small").setDimensions(256);

        Embeddings embeddings = openAIClient.getEmbeddings("my-text-embedding-3-small", embeddingsOptions);
        return embeddings.getData().get(0).getEmbedding();
    }

    public static List<VectorHotel> getHotelDocuments() {
        // In the sample code below, we are using 'getEmbeddings' method mentioned above to get embeddings for the
        // vector fields named 'DescriptionVector' and 'CategoryVector'.
        return Arrays.asList(new VectorHotel().setHotelId("1")
                .setHotelName("Fancy Stay")
                .setDescription("Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a "
                    + "spa, and a really helpful concierge. The location is perfect -- right downtown, close to "
                    + "all the tourist attractions. We highly recommend this hotel.")
                .setDescriptionVector(getEmbeddings(
                    "Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a spa, "
                        + "and a really helpful concierge. The location is perfect -- right downtown, close to all "
                        + "the tourist attractions. We highly recommend this hotel."))
                .setCategory("Luxury")
                .setCategoryVector(getEmbeddings("Luxury")), new VectorHotel().setHotelId("2")
                .setHotelName("Roach Motel")
                .setDescription("Cheapest hotel in town. Infact, a motel.")
                .setDescriptionVector(getEmbeddings("Cheapest hotel in town. Infact, a motel."))
                .setCategory("Budget")
                .setCategoryVector(getEmbeddings("Budget"))
            // Add more hotel documents here...
        );
    }

    public static void indexDocuments(SearchClient searchClient, List<VectorHotel> hotelDocuments) {
        searchClient.indexDocuments(new IndexDocumentsBatch<VectorHotel>().addUploadActions(hotelDocuments));
    }

    /**
     * In this vector query, the 'VectorQueries' contains the vectorizable text of the query input. The 'Fields'
     * property specifies which vector fields are searched. The 'KNearestNeighborsCount' property specifies the number
     * of nearest neighbors to return as top hits.
     */
    public static void vectorSearch(SearchClient searchClient) {
        SearchPagedIterable response = searchClient.search(null, new SearchOptions().setVectorSearchOptions(
            new VectorSearchOptions().setQueries(
                new VectorizableTextQuery("Luxury hotels in town").setKNearestNeighborsCount(3)
                    .setFields("DescriptionVector"))), Context.NONE);

        int count = 0;
        System.out.println("Vector Search Results:");

        for (SearchResult result : response) {
            count++;
            VectorHotel doc = result.getDocument(VectorHotel.class);
            System.out.println(doc.getHotelId() + ": " + doc.getHotelName());
        }

        System.out.println("Total number of search results: " + count);
    }
}
