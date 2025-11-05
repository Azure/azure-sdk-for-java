// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.test.TestMode;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.BinaryQuantizationCompression;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.RescoringOptions;
import com.azure.search.documents.indexes.models.ScalarQuantizationCompression;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchCompressionRescoreStorageMethod;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests Vector search functionality.
 */

@Execution(ExecutionMode.SAME_THREAD)
public class VectorSearchTests extends SearchTestBase {
    private final List<String> indexesToDelete = new ArrayList<>();

    @AfterEach
    public void deleteIndexes() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
                .credential(TestHelpers.getTestTokenCredential())
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            for (String index : indexesToDelete) {
                searchIndexClient.deleteIndex(index);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateExistingIndexToAddVectorFieldsAsync() {
        String indexName = randomIndexName("addvectorasync");
        SearchIndex searchIndex
            = new SearchIndex(indexName).setFields(new SearchField("Id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("Name", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true));

        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        searchIndexClient.createIndex(searchIndex).block();
        indexesToDelete.add(indexName);

        // Upload data
        SearchDocument document = new SearchDocument();
        document.put("Id", "1");
        document.put("Name", "Countryside Hotel");

        SearchAsyncClient searchClient = searchIndexClient.getSearchAsyncClient(indexName);
        searchClient.uploadDocuments(Collections.singletonList(document)).block();

        waitForIndexing();

        // Get the document
        StepVerifier.create(searchClient.getDocument("1", SearchDocument.class)).assertNext(response -> {
            assertEquals(document.get("Id"), response.get("Id"));
            assertEquals(document.get("Name"), response.get("Name"));
        }).verifyComplete();

        // Update created index to add vector field

        // Get created index
        Mono<SearchIndex> getAndUpdateIndex = searchIndexClient.getIndex(indexName).flatMap(createdIndex -> {
            // Add vector
            SearchField vectorField
                = new SearchField("DescriptionVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                    .setSearchable(true)
                    .setHidden(false)
                    .setVectorSearchDimensions(1536)
                    .setVectorSearchProfileName("my-vector-profile");

            createdIndex.getFields().add(vectorField);

            createdIndex.setVectorSearch(new VectorSearch()
                .setProfiles(
                    Collections.singletonList(new VectorSearchProfile("my-vector-profile", "my-vector-config")))
                .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config"))));

            return searchIndexClient.createOrUpdateIndex(createdIndex);
        });

        // Update index
        StepVerifier.create(getAndUpdateIndex).assertNext(response -> {
            assertEquals(indexName, response.getName());
            assertEquals(3, response.getFields().size());
        }).verifyComplete();

        // Update document to add vector field's data

        // Get the document
        Mono<SearchDocument> getAndUpdateDocument
            = searchClient.getDocument("1", SearchDocument.class).flatMap(resultDoc -> {
                // Update document to add vector field data
                resultDoc.put("DescriptionVector", VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION);
                return searchClient.mergeDocuments(Collections.singletonList(resultDoc));

            }).flatMap(ignored -> {
                // Equivalent of 'waitForIndexing()' where in PLAYBACK getting the document is called right away,
                // but for LIVE and RECORD it waits two seconds for the document to be available.
                if (TEST_MODE == TestMode.PLAYBACK) {
                    return searchClient.getDocument("1", SearchDocument.class);
                } else {
                    waitForIndexing();
                    return searchClient.getDocument("1", SearchDocument.class);
                }
            });

        // Get the document
        StepVerifier.create(getAndUpdateDocument).assertNext(response -> {
            assertEquals(document.get("Id"), response.get("Id"));
            assertEquals(document.get("Name"), response.get("Name"));
            assertNotNull(response.get("DescriptionVector"));
            compareFloatListToDeserializedFloatList((List<Number>) response.get("DescriptionVector"));
        }).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateExistingIndexToAddVectorFieldsSync() {
        String indexName = randomIndexName("addvectorsync");
        SearchIndex searchIndex
            = new SearchIndex(indexName).setFields(new SearchField("Id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("Name", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true));

        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        searchIndexClient.createIndex(searchIndex);
        indexesToDelete.add(indexName);
        // Upload data
        SearchDocument document = new SearchDocument();
        document.put("Id", "1");
        document.put("Name", "Countryside Hotel");

        SearchClient searchClient = searchIndexClient.getSearchClient(indexName);
        searchClient.uploadDocuments(Collections.singletonList(document));

        waitForIndexing();

        // Get the document
        SearchDocument responseDocument = searchClient.getDocument("1", SearchDocument.class);

        assertEquals(document.get("Id"), responseDocument.get("Id"));
        assertEquals(document.get("Name"), responseDocument.get("Name"));

        // Update created index to add vector field

        // Get created index
        SearchIndex createdIndex = searchIndexClient.getIndex(indexName);

        // Add vector
        SearchField vectorField
            = new SearchField("DescriptionVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                .setSearchable(true)
                .setHidden(false)
                .setVectorSearchDimensions(1536)
                .setVectorSearchProfileName("my-vector-profile");

        createdIndex.getFields().add(vectorField);

        createdIndex.setVectorSearch(new VectorSearch()
            .setProfiles(Collections.singletonList(new VectorSearchProfile("my-vector-profile", "my-vector-config")))
            .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config"))));

        // Update index
        SearchIndex responseIndex = searchIndexClient.createOrUpdateIndex(createdIndex);

        assertEquals(indexName, responseIndex.getName());
        assertEquals(3, responseIndex.getFields().size());

        // Update document to add vector field's data

        // Get the document
        SearchDocument resultDoc = searchClient.getDocument("1", SearchDocument.class);

        // Update document to add vector field data
        resultDoc.put("DescriptionVector", VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION);

        searchClient.mergeDocuments(Collections.singletonList(resultDoc));
        waitForIndexing();

        // Get the document
        responseDocument = searchClient.getDocument("1", SearchDocument.class);

        assertEquals(document.get("Id"), responseDocument.get("Id"));
        assertEquals(document.get("Name"), responseDocument.get("Name"));
        compareFloatListToDeserializedFloatList((List<Number>) responseDocument.get("DescriptionVector"));
    }

    // create a test that synchronously tests the ability to use VectorSearchCompression.truncationDimension to reduce
    // the dimensionality of the vector
    @Test
    public void testVectorSearchCompressionTruncationDimensionSync() {
        // create a new index with a vector field
        String indexName = randomIndexName("compressiontruncationdimension");
        String compressionName = "vector-compression-100";

        SearchIndex searchIndex = new SearchIndex(indexName)
            .setFields(new SearchField("Id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("Name", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true),
                new SearchField("DescriptionVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                    .setSearchable(true)
                    .setHidden(false)
                    .setVectorSearchDimensions(1536)
                    .setVectorSearchProfileName("my-vector-profile"))
            .setVectorSearch(new VectorSearch()
                .setProfiles(
                    Collections.singletonList(new VectorSearchProfile("my-vector-profile", "my-vector-config")))
                .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config")))
                .setCompressions(new BinaryQuantizationCompression(compressionName).setTruncationDimension(100)));

        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        searchIndexClient.createIndex(searchIndex);

        indexesToDelete.add(indexName);

        SearchIndex retrievedIndex = searchIndexClient.getIndex(indexName);
        assertEquals(1, retrievedIndex.getVectorSearch().getCompressions().size());
        assertEquals(compressionName, retrievedIndex.getVectorSearch().getCompressions().get(0).getCompressionName());

    }

    // create a test that asynchronously tests the ability to use VectorSearchCompression.truncationDimension to reduce
    // the dimensionality of the vector
    @Test
    public void testVectorSearchCompressionTruncationDimensionAsync() {
        // create a new index with a vector field
        String indexName = randomIndexName("compressiontruncationdimensionasync");
        String compressionName = "vector-compression-100";

        SearchIndex searchIndex = new SearchIndex(indexName)
            .setFields(new SearchField("Id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("Name", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true),
                new SearchField("DescriptionVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                    .setSearchable(true)
                    .setHidden(false)
                    .setVectorSearchDimensions(1536)
                    .setVectorSearchProfileName("my-vector-profile"))
            .setVectorSearch(new VectorSearch()
                .setProfiles(
                    Collections.singletonList(new VectorSearchProfile("my-vector-profile", "my-vector-config")))
                .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config")))
                .setCompressions(new ScalarQuantizationCompression(compressionName).setTruncationDimension(100)));

        SearchIndexAsyncClient searchIndexAsyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        searchIndexAsyncClient.createIndex(searchIndex).block();
        waitForIndexing();
        indexesToDelete.add(indexName);

        StepVerifier.create(searchIndexAsyncClient.getIndex(indexName)).assertNext(retrievedIndex -> {
            assertEquals(1, retrievedIndex.getVectorSearch().getCompressions().size());
            assertEquals(compressionName,
                retrievedIndex.getVectorSearch().getCompressions().get(0).getCompressionName());
        }).verifyComplete();
    }

    // write a test that asynchronously tests the ability to upload a vector field to an index using
    // BinaryQuantizationCompression
    @Test
    public void testVectorSearchCompressionBinaryQuantizationAsync() {
        // create a new index with a vector field
        String indexName = randomIndexName("compressionbinaryquantizationasync");
        String compressionName = "binary-vector-compression";

        SearchIndex searchIndex
            = new SearchIndex(indexName)
                .setFields(new SearchField("Id", SearchFieldDataType.STRING).setKey(true),
                    new SearchField("Name", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true),
                    new SearchField("BinaryCompressedVector",
                        SearchFieldDataType.collection(SearchFieldDataType.SINGLE)).setSearchable(true)
                            .setHidden(false)
                            .setVectorSearchDimensions(5)
                            .setVectorSearchProfileName("my-vector-profile"))
                .setVectorSearch(new VectorSearch()
                    .setProfiles(
                        Collections.singletonList(new VectorSearchProfile("my-vector-profile", "my-vector-config")))
                    .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config")))
                    .setCompressions(new BinaryQuantizationCompression(compressionName)));

        SearchIndexAsyncClient searchIndexAsyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        searchIndexAsyncClient.createIndex(searchIndex).block();
        indexesToDelete.add(indexName);

        StepVerifier.create(searchIndexAsyncClient.getIndex(indexName)).assertNext(retrievedIndex -> {
            assertEquals(1, retrievedIndex.getVectorSearch().getCompressions().size());
            assertEquals(compressionName,
                retrievedIndex.getVectorSearch().getCompressions().get(0).getCompressionName());
        }).verifyComplete();
    }

    // write a test that synchronously tests the ability to upload a vector field to an index using
    // BinaryQuantizationCompression
    @Test
    public void testVectorSearchCompressionBinaryQuantizationSync() {
        // create a new index with a vector field
        String indexName = randomIndexName("compressionbinaryquantizationsync");
        String compressionName = "binary-vector-compression";

        SearchIndex searchIndex
            = new SearchIndex(indexName)
                .setFields(new SearchField("Id", SearchFieldDataType.STRING).setKey(true),
                    new SearchField("Name", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true),
                    new SearchField("BinaryCompressedVector",
                        SearchFieldDataType.collection(SearchFieldDataType.SINGLE)).setSearchable(true)
                            .setHidden(false)
                            .setVectorSearchDimensions(5)
                            .setVectorSearchProfileName("my-vector-profile"))
                .setVectorSearch(new VectorSearch()
                    .setProfiles(
                        Collections.singletonList(new VectorSearchProfile("my-vector-profile", "my-vector-config")))
                    .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config")))
                    .setCompressions(new BinaryQuantizationCompression(compressionName)));

        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        searchIndexClient.createIndex(searchIndex);
        indexesToDelete.add(indexName);

        SearchIndex retrievedIndex = searchIndexClient.getIndex(indexName);
        assertEquals(1, retrievedIndex.getVectorSearch().getCompressions().size());
        assertEquals(compressionName, retrievedIndex.getVectorSearch().getCompressions().get(0).getCompressionName());
    }

    @Test
    public void testVectorSearchCompressionsEnableRescoringDiscardOriginalsSync() {
        String indexName = randomIndexName("compressiontruncationdimension");
        String compressionName = "vector-compression-100";
        RescoringOptions rescoringOptions = new RescoringOptions().setRescoringEnabled(true)
            .setRescoreStorageMethod(VectorSearchCompressionRescoreStorageMethod.DISCARD_ORIGINALS);

        SearchIndex searchIndex = new SearchIndex(indexName)
            .setFields(new SearchField("Id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("Name", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true),
                new SearchField("DescriptionVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                    .setSearchable(true)
                    .setHidden(false)
                    .setVectorSearchDimensions(1536)
                    .setVectorSearchProfileName("my-vector-profile"))
            .setVectorSearch(new VectorSearch()
                .setProfiles(
                    Collections.singletonList(new VectorSearchProfile("my-vector-profile", "my-vector-config")))
                .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config")))
                .setCompressions(new BinaryQuantizationCompression(compressionName).setTruncationDimension(100)
                    .setRescoringOptions(rescoringOptions)));

        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        searchIndexClient.createIndex(searchIndex);

        indexesToDelete.add(indexName);

        SearchIndex retrievedIndex = searchIndexClient.getIndex(indexName);
        assertEquals(1, retrievedIndex.getVectorSearch().getCompressions().size());
        BinaryQuantizationCompression compression
            = (BinaryQuantizationCompression) retrievedIndex.getVectorSearch().getCompressions().get(0);
        assertEquals(compressionName, compression.getCompressionName());
        assertEquals(true, compression.getRescoringOptions().isRescoringEnabled());
        assertEquals(VectorSearchCompressionRescoreStorageMethod.DISCARD_ORIGINALS,
            compression.getRescoringOptions().getRescoreStorageMethod());
    }

    @Test
    public void testVectorSearchCompressionsEnableRescoringDiscardOriginalsAsync() {
        String indexName = randomIndexName("compressiontruncationdimension");
        String compressionName = "vector-compression-100";
        RescoringOptions rescoringOptions = new RescoringOptions().setRescoringEnabled(true)
            .setRescoreStorageMethod(VectorSearchCompressionRescoreStorageMethod.DISCARD_ORIGINALS);

        SearchIndex searchIndex = new SearchIndex(indexName)
            .setFields(new SearchField("Id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("Name", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true),
                new SearchField("DescriptionVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                    .setSearchable(true)
                    .setHidden(false)
                    .setVectorSearchDimensions(1536)
                    .setVectorSearchProfileName("my-vector-profile"))
            .setVectorSearch(new VectorSearch()
                .setProfiles(
                    Collections.singletonList(new VectorSearchProfile("my-vector-profile", "my-vector-config")))
                .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config")))
                .setCompressions(new BinaryQuantizationCompression(compressionName).setTruncationDimension(100)
                    .setRescoringOptions(rescoringOptions)));

        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        searchIndexClient.createIndex(searchIndex).block();

        indexesToDelete.add(indexName);

        StepVerifier.create(searchIndexClient.getIndex(indexName)).assertNext(retrievedIndex -> {
            assertEquals(1, retrievedIndex.getVectorSearch().getCompressions().size());
            BinaryQuantizationCompression compression
                = (BinaryQuantizationCompression) retrievedIndex.getVectorSearch().getCompressions().get(0);
            assertEquals(compressionName, compression.getCompressionName());
            assertEquals(true, compression.getRescoringOptions().isRescoringEnabled());
            assertEquals(VectorSearchCompressionRescoreStorageMethod.DISCARD_ORIGINALS,
                compression.getRescoringOptions().getRescoreStorageMethod());
        }).verifyComplete();
    }

    private static void compareFloatListToDeserializedFloatList(List<Number> actual) {
        if (actual == null) {
            assertNull(VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION);
            return;
        }

        assertEquals(VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION.size(), actual.size());

        Object obj = actual.get(0);
        if (obj instanceof Float || obj instanceof Double) {
            for (int i = 0; i < VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION.size(); i++) {
                assertEquals(VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION.get(i), actual.get(i).floatValue());
            }
        } else {
            throw new IllegalStateException(
                "Deserialization of a float list returned an unexpected type. Type was: " + obj.getClass().getName());
        }
    }
}
