// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosVectorDataType;
import com.azure.cosmos.models.CosmosVectorDistanceFunction;
import com.azure.cosmos.models.CosmosVectorEmbedding;
import com.azure.cosmos.models.CosmosVectorEmbeddingPolicy;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.CosmosVectorIndexSpec;
import com.azure.cosmos.models.CosmosVectorIndexType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Ignore("TODO: Ignore these test cases until the public emulator with vector indexes is released.")
public class VectorIndexTest extends TestSuiteBase {
    protected static final int TIMEOUT = 30000;
    protected static final int SETUP_TIMEOUT = 20000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    protected static Logger logger = LoggerFactory.getLogger(VectorIndexTest.class.getSimpleName());
    private final ObjectMapper simpleObjectMapper = Utils.getSimpleObjectMapper();
    private final String databaseId = CosmosDatabaseForTest.generateId();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_VectorIndexTest() {
        // set up the client
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(DirectConnectionConfig.getDefaultConfig())
            .consistencyLevel(ConsistencyLevel.SESSION)
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();

        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT*10000)
    public void shouldCreateVectorEmbeddingPolicy() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath("/name/?");
        IncludedPath includedPath2 = new IncludedPath("/description/?");
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        indexingPolicy.setVectorIndexes(populateVectorIndexes());

        CosmosVectorEmbeddingPolicy cosmosVectorEmbeddingPolicy = new CosmosVectorEmbeddingPolicy();
        cosmosVectorEmbeddingPolicy.setCosmosVectorEmbeddings(populateEmbeddings());

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setVectorEmbeddingPolicy(cosmosVectorEmbeddingPolicy);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer createdCollection = database.getContainer(collectionDefinition.getId());
        CosmosContainerProperties collectionProperties = createdCollection.read().block().getProperties();
        validateCollectionProperties(collectionDefinition, collectionProperties);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void shouldFailOnEmptyVectorEmbeddingPolicy() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath("/name/?");
        IncludedPath includedPath2 = new IncludedPath("/description/?");
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        CosmosVectorIndexSpec cosmosVectorIndexSpec = new CosmosVectorIndexSpec();
        cosmosVectorIndexSpec.setPath("/vector1");
        cosmosVectorIndexSpec.setType(CosmosVectorIndexType.FLAT.toString());
        indexingPolicy.setVectorIndexes(ImmutableList.of(cosmosVectorIndexSpec));

        collectionDefinition.setIndexingPolicy(indexingPolicy);

        try {
            database.createContainer(collectionDefinition).block();
            fail("Container creation will fail as no vector embedding policy is being passed");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(400);
            assertThat(ex.getMessage()).contains("vector1 not matching in Embedding's path");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void shouldFailOnWrongVectorIndex() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath("/name/?");
        IncludedPath includedPath2 = new IncludedPath("/description/?");
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        CosmosVectorIndexSpec cosmosVectorIndexSpec = new CosmosVectorIndexSpec();
        cosmosVectorIndexSpec.setPath("/vector1");
        cosmosVectorIndexSpec.setType("NonFlat");
        indexingPolicy.setVectorIndexes(ImmutableList.of(cosmosVectorIndexSpec));
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        CosmosVectorEmbedding embedding = new CosmosVectorEmbedding();
        embedding.setPath("/vector1");
        embedding.setDataType(CosmosVectorDataType.FLOAT32);
        embedding.setDimensions(3L);
        embedding.setDistanceFunction(CosmosVectorDistanceFunction.COSINE);
        CosmosVectorEmbeddingPolicy cosmosVectorEmbeddingPolicy = new CosmosVectorEmbeddingPolicy();
        cosmosVectorEmbeddingPolicy.setCosmosVectorEmbeddings(ImmutableList.of(embedding));
        collectionDefinition.setVectorEmbeddingPolicy(cosmosVectorEmbeddingPolicy);

        try {
            database.createContainer(collectionDefinition).block();
            fail("Container creation will fail as wrong vector index type is being passed");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(400);
            assertThat(ex.getMessage()).contains("NonFlat is invalid, Valid types are 'flat' or 'quantizedFlat'");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void shouldCreateVectorIndexSimilarPathDifferentVectorType() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath("/name/?");
        IncludedPath includedPath2 = new IncludedPath("/description/?");
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        List<CosmosVectorIndexSpec> vectorIndexes = populateVectorIndexes();
        vectorIndexes.get(2).setPath("/vector2");
        indexingPolicy.setVectorIndexes(vectorIndexes);

        List<CosmosVectorEmbedding> embeddings = populateEmbeddings();
        embeddings.get(2).setPath("/vector2");
        CosmosVectorEmbeddingPolicy cosmosVectorEmbeddingPolicy = new CosmosVectorEmbeddingPolicy();
        cosmosVectorEmbeddingPolicy.setCosmosVectorEmbeddings(embeddings);

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setVectorEmbeddingPolicy(cosmosVectorEmbeddingPolicy);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer createdCollection = database.getContainer(collectionDefinition.getId());
        CosmosContainerProperties collectionProperties = createdCollection.read().block().getProperties();
        validateCollectionProperties(collectionDefinition, collectionProperties);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void shouldFailOnWrongVectorEmbeddingPolicy() {
        CosmosVectorEmbedding embedding = new CosmosVectorEmbedding();
        try {

            embedding.setDataType(null);
            fail("Embedding creation failed because cosmosVectorDataType argument is empty");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage()).isEqualTo("cosmosVectorDataType cannot be empty");
        }

        try {
            embedding.setDistanceFunction(null);
            fail("Embedding creation failed because cosmosVectorDistanceFunction argument is empty");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage()).isEqualTo("cosmosVectorDistanceFunction cannot be null");
        }

        try {
            embedding.setDimensions(null);
            fail("Embedding creation failed because dimensions argument is empty");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage()).isEqualTo("dimensions cannot be empty");
        }

        try {
            embedding.setDimensions(-1L);
            fail("Vector Embedding policy creation will fail for negative dimensions being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Dimensions for the embedding has to be a long value greater than 1 for the vector embedding policy");
        }
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void shouldValidateVectorEmbeddingPolicySerializationAndDeserialization() throws JsonProcessingException {
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setVectorIndexes(populateVectorIndexes());

        CosmosVectorEmbeddingPolicy cosmosVectorEmbeddingPolicy = new CosmosVectorEmbeddingPolicy();
        cosmosVectorEmbeddingPolicy.setCosmosVectorEmbeddings(populateEmbeddings());
        String vectorEmbeddingPolicyJson = getVectorEmbeddingPolicyAsString();
        String expectedVectorEmbeddingPolicyJson = simpleObjectMapper.writeValueAsString(cosmosVectorEmbeddingPolicy);
        assertThat(vectorEmbeddingPolicyJson).isEqualTo(expectedVectorEmbeddingPolicyJson);

        CosmosVectorEmbeddingPolicy expectedCosmosVectorEmbeddingPolicy = simpleObjectMapper.readValue(expectedVectorEmbeddingPolicyJson, CosmosVectorEmbeddingPolicy.class);
        validateVectorEmbeddingPolicy(cosmosVectorEmbeddingPolicy, expectedCosmosVectorEmbeddingPolicy);
    }

    private void validateCollectionProperties(CosmosContainerProperties collectionDefinition, CosmosContainerProperties collectionProperties) {
        assertThat(collectionProperties.getVectorEmbeddingPolicy()).isNotNull();
        assertThat(collectionProperties.getVectorEmbeddingPolicy().getVectorEmbeddings()).isNotNull();
        validateVectorEmbeddingPolicy(collectionProperties.getVectorEmbeddingPolicy(),
            collectionDefinition.getVectorEmbeddingPolicy());

        assertThat(collectionProperties.getIndexingPolicy().getVectorIndexes()).isNotNull();
        validateVectorIndexes(collectionDefinition.getIndexingPolicy().getVectorIndexes(), collectionProperties.getIndexingPolicy().getVectorIndexes());
    }

    private void validateVectorEmbeddingPolicy(CosmosVectorEmbeddingPolicy actual, CosmosVectorEmbeddingPolicy expected) {
        List<CosmosVectorEmbedding> actualEmbeddings = actual.getVectorEmbeddings();
        List<CosmosVectorEmbedding> expectedEmbeddings = expected.getVectorEmbeddings();
        assertThat(expectedEmbeddings).hasSameSizeAs(actualEmbeddings);
        for (int i = 0; i < expectedEmbeddings.size(); i++) {
            assertThat(expectedEmbeddings.get(i).getPath()).isEqualTo(actualEmbeddings.get(i).getPath());
            assertThat(expectedEmbeddings.get(i).getDataType()).isEqualTo(actualEmbeddings.get(i).getDataType());
            assertThat(expectedEmbeddings.get(i).getDimensions()).isEqualTo(actualEmbeddings.get(i).getDimensions());
            assertThat(expectedEmbeddings.get(i).getDistanceFunction()).isEqualTo(actualEmbeddings.get(i).getDistanceFunction());
        }
    }

    private void validateVectorIndexes(List<CosmosVectorIndexSpec> actual, List<CosmosVectorIndexSpec> expected) {
        assertThat(expected).hasSameSizeAs(actual);
        for (int i = 0; i < expected.size(); i++) {
            assertThat(expected.get(i).getPath()).isEqualTo(actual.get(i).getPath());
            assertThat(expected.get(i).getType()).isEqualTo(actual.get(i).getType());
        }
    }

    private List<CosmosVectorIndexSpec> populateVectorIndexes() {
        CosmosVectorIndexSpec cosmosVectorIndexSpec1 = new CosmosVectorIndexSpec();
        cosmosVectorIndexSpec1.setPath("/vector1");
        cosmosVectorIndexSpec1.setType(CosmosVectorIndexType.FLAT.toString());

        CosmosVectorIndexSpec cosmosVectorIndexSpec2 = new CosmosVectorIndexSpec();
        cosmosVectorIndexSpec2.setPath("/vector2");
        cosmosVectorIndexSpec2.setType(CosmosVectorIndexType.QUANTIZED_FLAT.toString());

        CosmosVectorIndexSpec cosmosVectorIndexSpec3 = new CosmosVectorIndexSpec();
        cosmosVectorIndexSpec3.setPath("/vector3");
        cosmosVectorIndexSpec3.setType(CosmosVectorIndexType.DISK_ANN.toString());

        return Arrays.asList(cosmosVectorIndexSpec1, cosmosVectorIndexSpec2, cosmosVectorIndexSpec3);
    }

    private List<CosmosVectorEmbedding> populateEmbeddings() {
        CosmosVectorEmbedding embedding1 = new CosmosVectorEmbedding();
        embedding1.setPath("/vector1");
        embedding1.setDataType(CosmosVectorDataType.INT8);
        embedding1.setDimensions(3L);
        embedding1.setDistanceFunction(CosmosVectorDistanceFunction.COSINE);

        CosmosVectorEmbedding embedding2 = new CosmosVectorEmbedding();
        embedding2.setPath("/vector2");
        embedding2.setDataType(CosmosVectorDataType.FLOAT32);
        embedding2.setDimensions(3L);
        embedding2.setDistanceFunction(CosmosVectorDistanceFunction.DOT_PRODUCT);

        CosmosVectorEmbedding embedding3 = new CosmosVectorEmbedding();
        embedding3.setPath("/vector3");
        embedding3.setDataType(CosmosVectorDataType.UINT8);
        embedding3.setDimensions(3L);
        embedding3.setDistanceFunction(CosmosVectorDistanceFunction.EUCLIDEAN);
        return Arrays.asList(embedding1, embedding2, embedding3);
    }

    private String getVectorEmbeddingPolicyAsString() {
        return "{\"vectorEmbeddings\":[" +
            "{\"path\":\"/vector1\",\"dataType\":\"int8\",\"dimensions\":3,\"distanceFunction\":\"cosine\"}," +
            "{\"path\":\"/vector2\",\"dataType\":\"float32\",\"dimensions\":3,\"distanceFunction\":\"dotproduct\"}," +
            "{\"path\":\"/vector3\",\"dataType\":\"uint8\",\"dimensions\":3,\"distanceFunction\":\"euclidean\"}" +
            "]}";
    }
}
