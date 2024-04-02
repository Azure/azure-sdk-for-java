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
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.VectorEmbeddingPolicy;
import com.azure.cosmos.models.VectorIndexSpec;
import com.azure.cosmos.models.VectorIndexType;
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
    public void before_UniqueIndexTest() {
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

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
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

        VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(populateEmbeddings());

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);

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

        VectorIndexSpec vectorIndexSpec = new VectorIndexSpec("/vector1");
        vectorIndexSpec.setType(VectorIndexType.FLAT.toString());
        indexingPolicy.setVectorIndexes(ImmutableList.of(vectorIndexSpec));

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
    public void shouldFailOnWrongVectorEmbeddingPolicy() {
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

        VectorIndexSpec vectorIndexSpec = new VectorIndexSpec("/vector1");
        vectorIndexSpec.setType(VectorIndexType.FLAT.toString());
        indexingPolicy.setVectorIndexes(ImmutableList.of(vectorIndexSpec));
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        CosmosVectorEmbedding embedding = new CosmosVectorEmbedding(
            "/vector1",
            "String",
            3L,
            CosmosVectorDistanceFunction.COSINE.toString());

        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for wrong vector date type being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid vector data type for the vector embedding policy.");
        }

        embedding.setCosmosVectorDataType("");
        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for empty vector date type being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Vector data type cannot be empty for the vector embedding policy.");
        }

        embedding.setCosmosVectorDataType(CosmosVectorDataType.FLOAT32.toString());
        embedding.setCosmosVectorDistanceFunction("COS");
        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for wrong distance function being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid distance function for the vector embedding policy.");
        }

        embedding.setCosmosVectorDistanceFunction("");
        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for empty distance function being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Distance function cannot be empty for the vector embedding policy.");
        }

        embedding.setCosmosVectorDistanceFunction(CosmosVectorDistanceFunction.COSINE.toString());
        embedding.setDimensions(-1L);
        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for negative dimensions being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Dimensions for the embedding has to be a long value greater than 1 for the vector embedding policy");
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

        VectorIndexSpec vectorIndexSpec = new VectorIndexSpec("/vector1");
        vectorIndexSpec.setType("NonFlat");
        indexingPolicy.setVectorIndexes(ImmutableList.of(vectorIndexSpec));
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        CosmosVectorEmbedding embedding = new CosmosVectorEmbedding(
            "/vector1",
            CosmosVectorDistanceFunction.COSINE.toString(),
            3L,
            CosmosVectorDistanceFunction.COSINE.toString());
        VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
        collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);

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

        List<VectorIndexSpec> vectorIndexes = populateVectorIndexes();
        vectorIndexes.get(2).setPath("/vector2");
        indexingPolicy.setVectorIndexes(vectorIndexes);

        List<CosmosVectorEmbedding> embeddings = populateEmbeddings();
        embeddings.get(2).setPath("/vector2");
        VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(embeddings);

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer createdCollection = database.getContainer(collectionDefinition.getId());
        CosmosContainerProperties collectionProperties = createdCollection.read().block().getProperties();
        validateCollectionProperties(collectionDefinition, collectionProperties);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void shouldValidateVectorEmbeddingPolicySerializationAndDeserialization() throws JsonProcessingException {
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setVectorIndexes(populateVectorIndexes());

        VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(populateEmbeddings());
        String vectorEmbeddingPolicyJson = getVectorEmbeddingPolicyAsString();
        String expectedVectorEmbeddingPolicyJson = simpleObjectMapper.writeValueAsString(vectorEmbeddingPolicy);
        assertThat(vectorEmbeddingPolicyJson).isEqualTo(expectedVectorEmbeddingPolicyJson);

        VectorEmbeddingPolicy expectedVectorEmbeddingPolicy = simpleObjectMapper.readValue(expectedVectorEmbeddingPolicyJson, VectorEmbeddingPolicy.class);
        validateVectorEmbeddingPolicy(vectorEmbeddingPolicy, expectedVectorEmbeddingPolicy);
    }

    private void validateCollectionProperties(CosmosContainerProperties collectionDefinition, CosmosContainerProperties collectionProperties) {
        assertThat(collectionProperties.getVectorEmbeddingPolicy()).isNotNull();
        assertThat(collectionProperties.getVectorEmbeddingPolicy().getCosmosVectorEmbeddings()).isNotNull();
        validateVectorEmbeddingPolicy(collectionProperties.getVectorEmbeddingPolicy(),
            collectionDefinition.getVectorEmbeddingPolicy());

        assertThat(collectionProperties.getIndexingPolicy().getVectorIndexes()).isNotNull();
        validateVectorIndexes(collectionDefinition.getIndexingPolicy().getVectorIndexes(), collectionProperties.getIndexingPolicy().getVectorIndexes());
    }

    private void validateVectorEmbeddingPolicy(VectorEmbeddingPolicy actual, VectorEmbeddingPolicy expected) {
        List<CosmosVectorEmbedding> actualEmbeddings = actual.getCosmosVectorEmbeddings();
        List<CosmosVectorEmbedding> expectedEmbeddings = expected.getCosmosVectorEmbeddings();
        assertThat(expectedEmbeddings).hasSameSizeAs(actualEmbeddings);
        for (int i = 0; i < expectedEmbeddings.size(); i++) {
            assertThat(expectedEmbeddings.get(i).getPath()).isEqualTo(actualEmbeddings.get(i).getPath());
            assertThat(expectedEmbeddings.get(i).getCosmosVectorDataType()).isEqualTo(actualEmbeddings.get(i).getCosmosVectorDataType());
            assertThat(expectedEmbeddings.get(i).getDimensions()).isEqualTo(actualEmbeddings.get(i).getDimensions());
            assertThat(expectedEmbeddings.get(i).getCosmosVectorDistanceFunction()).isEqualTo(actualEmbeddings.get(i).getCosmosVectorDistanceFunction());
        }
    }

    private void validateVectorIndexes(List<VectorIndexSpec> actual, List<VectorIndexSpec> expected) {
        assertThat(expected).hasSameSizeAs(actual);
        for (int i = 0; i < expected.size(); i++) {
            assertThat(expected.get(i).getPath()).isEqualTo(actual.get(i).getPath());
            assertThat(expected.get(i).getType()).isEqualTo(actual.get(i).getType());
        }
    }

    private List<VectorIndexSpec> populateVectorIndexes() {
        VectorIndexSpec vectorIndexSpec1 = new VectorIndexSpec("/vector1");
        vectorIndexSpec1.setType(VectorIndexType.FLAT.toString());

        VectorIndexSpec vectorIndexSpec2 = new VectorIndexSpec("/vector2");
        vectorIndexSpec2.setType(VectorIndexType.QUANTIZED_FLAT.toString());

        VectorIndexSpec vectorIndexSpec3 = new VectorIndexSpec("/vector3");
        vectorIndexSpec3.setType(VectorIndexType.DISK_ANN.toString());

        return Arrays.asList(vectorIndexSpec1, vectorIndexSpec2, vectorIndexSpec3);
    }

    private List<CosmosVectorEmbedding> populateEmbeddings() {
        CosmosVectorEmbedding embedding1 = new CosmosVectorEmbedding(
            "/vector1",
            CosmosVectorDataType.FLOAT32.toString(),
            3L,
            CosmosVectorDistanceFunction.COSINE.toString());

        CosmosVectorEmbedding embedding2 = new CosmosVectorEmbedding(
            "/vector2",
            CosmosVectorDataType.INT8.toString(),
            3L,
            CosmosVectorDistanceFunction.DOT_PRODUCT.toString());

        CosmosVectorEmbedding embedding3 = new CosmosVectorEmbedding(
            "/vector3",
            CosmosVectorDataType.UINT8.toString(),
            3L,
            CosmosVectorDistanceFunction.EUCLIDEAN.toString());
        return Arrays.asList(embedding1, embedding2, embedding3);
    }

    private String getVectorEmbeddingPolicyAsString() {
        return "{\"vectorEmbeddings\":[" +
            "{\"path\":\"/vector1\",\"dataType\":\"float32\",\"dimensions\":3,\"distanceFunction\":\"cosine\"}," +
            "{\"path\":\"/vector2\",\"dataType\":\"int8\",\"dimensions\":3,\"distanceFunction\":\"dotproduct\"}," +
            "{\"path\":\"/vector3\",\"dataType\":\"uint8\",\"dimensions\":3,\"distanceFunction\":\"euclidean\"}" +
            "]}";
    }
}
