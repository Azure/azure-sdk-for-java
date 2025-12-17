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
import com.azure.cosmos.models.QuantizerType;
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
import java.util.Objects;
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
        ArrayList<String> paths = new ArrayList<>(Arrays.asList("/mypk"));
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition()
            .setPaths(paths);

        ExcludedPath excludedPath = new ExcludedPath("/*");
        IncludedPath includedPath1 = new IncludedPath("/name/?");
        IncludedPath includedPath2 = new IncludedPath("/description/?");

        IndexingPolicy indexingPolicy = new IndexingPolicy()
            .setIndexingMode(IndexingMode.CONSISTENT)
            .setExcludedPaths(Collections.singletonList(excludedPath))
            .setIncludedPaths(ImmutableList.of(includedPath1, includedPath2))
            .setVectorIndexes(populateVectorIndexes());

        CosmosVectorEmbeddingPolicy cosmosVectorEmbeddingPolicy = new CosmosVectorEmbeddingPolicy();
        cosmosVectorEmbeddingPolicy.setCosmosVectorEmbeddings(populateEmbeddings());

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef)
            .setIndexingPolicy(indexingPolicy)
            .setVectorEmbeddingPolicy(cosmosVectorEmbeddingPolicy);

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
    public void shouldFailVectorIndexSimilarPathDifferentVectorType() {
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
        CosmosVectorEmbeddingPolicy cosmosVectorEmbeddingPolicy = new CosmosVectorEmbeddingPolicy();
        cosmosVectorEmbeddingPolicy.setCosmosVectorEmbeddings(embeddings);

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setVectorEmbeddingPolicy(cosmosVectorEmbeddingPolicy);

        try {
            database.createContainer(collectionDefinition).block();
            fail("Container creation will fail as duplicate path is provided in vector indexes");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(400);
            assertThat(ex.getMessage()).contains("Duplicate Path :/vector2 found in Vector Indexing Policy.");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void shouldFailVectorEmbeddingSimilarPath() {
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
        indexingPolicy.setVectorIndexes(vectorIndexes);

        List<CosmosVectorEmbedding> embeddings = populateEmbeddings();
        embeddings.get(2).setPath("/vector2");
        CosmosVectorEmbeddingPolicy cosmosVectorEmbeddingPolicy = new CosmosVectorEmbeddingPolicy();
        cosmosVectorEmbeddingPolicy.setCosmosVectorEmbeddings(embeddings);

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setVectorEmbeddingPolicy(cosmosVectorEmbeddingPolicy);

        try {
            database.createContainer(collectionDefinition).block();
            fail("Container creation will fail as duplicate path is provided in vector embedding policy");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(400);
            assertThat(ex.getMessage()).contains("Duplicate Path :/vector2 found in Vector Embedding Policy.");
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void shouldFailOnWrongVectorIndex() {
        try {
            CosmosVectorIndexSpec cosmosVectorIndexSpec = new CosmosVectorIndexSpec();
            cosmosVectorIndexSpec.setPath("/vector1");
            cosmosVectorIndexSpec.setType("NonFlat");
            fail("Container creation will fail as wrong vector index type is being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("NonFlat is an invalid index type. Valid index types are 'flat', 'quantizedFlat' or 'diskANN'.");
        }
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void shouldFailOnWrongVectorEmbeddingPolicy() {
        CosmosVectorEmbedding embedding = new CosmosVectorEmbedding();
        try {

            embedding.setDataType(null);
            fail("Embedding creation failed because cosmosVectorDataType argument is null");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage()).isEqualTo("cosmosVectorDataType cannot be null");
        }

        try {
            embedding.setDistanceFunction(null);
            fail("Embedding creation failed because cosmosVectorDistanceFunction argument is null");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage()).isEqualTo("cosmosVectorDistanceFunction cannot be null");
        }

        try {
            embedding.setEmbeddingDimensions(null);
            fail("Embedding creation failed because dimensions argument is null");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage()).isEqualTo("dimensions cannot be null");
        }

        try {
            embedding.setEmbeddingDimensions(-1);
            fail("Vector Embedding policy creation will fail for negative dimensions being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Dimensions for the embedding has to be a int value greater than 0 for the vector embedding policy");
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

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void shouldValidateVectorIndexesSerializationAndDeserialization() throws JsonProcessingException {
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setVectorIndexes(populateVectorIndexes());
        List<CosmosVectorIndexSpec> expectedVectorIndexes = indexingPolicy.getVectorIndexes();

        // Validate Vector Indexes Serialization
        String actualVectorIndexesJSON = simpleObjectMapper.writeValueAsString(expectedVectorIndexes);
        String expectedVectorIndexesJSON = getVectorIndexesAsString();
        assertThat(actualVectorIndexesJSON).isEqualTo(expectedVectorIndexesJSON);

        // Validate Vector Indexes Deserialization
        List<CosmosVectorIndexSpec> actualVectorIndexes = Arrays.asList(simpleObjectMapper.readValue(actualVectorIndexesJSON, CosmosVectorIndexSpec[].class));
        validateVectorIndexes(actualVectorIndexes, expectedVectorIndexes);
    }

    private void validateCollectionProperties(CosmosContainerProperties collectionDefinition, CosmosContainerProperties collectionProperties) {
        assertThat(collectionProperties.getVectorEmbeddingPolicy()).isNotNull();
        assertThat(collectionProperties.getVectorEmbeddingPolicy().getVectorEmbeddings()).isNotNull();
        validateVectorEmbeddingPolicy(collectionProperties.getVectorEmbeddingPolicy(),
            collectionDefinition.getVectorEmbeddingPolicy());

        assertThat(collectionProperties.getIndexingPolicy().getVectorIndexes()).isNotNull();
        validateVectorIndexes(collectionProperties.getIndexingPolicy().getVectorIndexes(), collectionDefinition.getIndexingPolicy().getVectorIndexes());
    }

    private void validateVectorEmbeddingPolicy(CosmosVectorEmbeddingPolicy actual, CosmosVectorEmbeddingPolicy expected) {
        List<CosmosVectorEmbedding> actualEmbeddings = actual.getVectorEmbeddings();
        List<CosmosVectorEmbedding> expectedEmbeddings = expected.getVectorEmbeddings();
        assertThat(expectedEmbeddings).hasSameSizeAs(actualEmbeddings);
        for (int i = 0; i < expectedEmbeddings.size(); i++) {
            assertThat(expectedEmbeddings.get(i).getPath()).isEqualTo(actualEmbeddings.get(i).getPath());
            assertThat(expectedEmbeddings.get(i).getDataType()).isEqualTo(actualEmbeddings.get(i).getDataType());
            assertThat(expectedEmbeddings.get(i).getEmbeddingDimensions()).isEqualTo(actualEmbeddings.get(i).getEmbeddingDimensions());
            assertThat(expectedEmbeddings.get(i).getDistanceFunction()).isEqualTo(actualEmbeddings.get(i).getDistanceFunction());
        }
    }

    private void validateVectorIndexes(List<CosmosVectorIndexSpec> actual, List<CosmosVectorIndexSpec> expected) {
        assertThat(actual).hasSameSizeAs(expected);
        for (int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getPath()).isEqualTo(expected.get(i).getPath());
            assertThat(actual.get(i).getType()).isEqualTo(expected.get(i).getType());
            if (Objects.equals(actual.get(i).getType(), CosmosVectorIndexType.QUANTIZED_FLAT.toString()) ||
                Objects.equals(actual.get(i).getType(), CosmosVectorIndexType.DISK_ANN.toString())) {
                assertThat(actual.get(i).getQuantizerType()).isEqualTo(expected.get(i).getQuantizerType());
                assertThat(actual.get(i).getQuantizationSizeInBytes()).isEqualTo(expected.get(i).getQuantizationSizeInBytes());
                assertThat(actual.get(i).getVectorIndexShardKeys()).isEqualTo(expected.get(i).getVectorIndexShardKeys());
            }
            if (Objects.equals(actual.get(i).getType(), CosmosVectorIndexType.DISK_ANN.toString())) {
                assertThat(actual.get(i).getIndexingSearchListSize()).isEqualTo(expected.get(i).getIndexingSearchListSize());
            }

        }
    }

    private List<CosmosVectorIndexSpec> populateVectorIndexes() {
        CosmosVectorIndexSpec cosmosVectorIndexSpec1 = new CosmosVectorIndexSpec()
            .setPath("/vector1")
            .setType(CosmosVectorIndexType.FLAT.toString());

        CosmosVectorIndexSpec cosmosVectorIndexSpec2 = new CosmosVectorIndexSpec()
            .setPath("/vector2")
            .setType(CosmosVectorIndexType.QUANTIZED_FLAT.toString())
            .setQuantizerType(QuantizerType.PRODUCT)
            .setQuantizationSizeInBytes(2)
            .setVectorIndexShardKeys(Arrays.asList("/zipCode"));

        CosmosVectorIndexSpec cosmosVectorIndexSpec3 = new CosmosVectorIndexSpec()
            .setPath("/vector3")
            .setType(CosmosVectorIndexType.DISK_ANN.toString())
            .setQuantizerType(QuantizerType.PRODUCT)
            .setQuantizationSizeInBytes(2)
            .setIndexingSearchListSize(30)
            .setVectorIndexShardKeys(Arrays.asList("/country/city"));

        CosmosVectorIndexSpec cosmosVectorIndexSpec4 = new CosmosVectorIndexSpec()
            .setPath("/vector4")
            .setType(CosmosVectorIndexType.DISK_ANN.toString())
            .setQuantizerType(QuantizerType.SPHERICAL)
            .setIndexingSearchListSize(30)
            .setVectorIndexShardKeys(Arrays.asList("/country/city"));

        return Arrays.asList(cosmosVectorIndexSpec1, cosmosVectorIndexSpec2, cosmosVectorIndexSpec3, cosmosVectorIndexSpec4);
    }

    private List<CosmosVectorEmbedding> populateEmbeddings() {
        CosmosVectorEmbedding embedding1 = new CosmosVectorEmbedding()
            .setPath("/vector1")
            .setDataType(CosmosVectorDataType.INT8)
            .setEmbeddingDimensions(3)
            .setDistanceFunction(CosmosVectorDistanceFunction.COSINE);

        CosmosVectorEmbedding embedding2 = new CosmosVectorEmbedding()
            .setPath("/vector2")
            .setDataType(CosmosVectorDataType.FLOAT32)
            .setEmbeddingDimensions(3)
            .setDistanceFunction(CosmosVectorDistanceFunction.DOT_PRODUCT);

        CosmosVectorEmbedding embedding3 = new CosmosVectorEmbedding()
            .setPath("/vector3")
            .setDataType(CosmosVectorDataType.UINT8)
            .setEmbeddingDimensions(3)
            .setDistanceFunction(CosmosVectorDistanceFunction.EUCLIDEAN);

        CosmosVectorEmbedding embedding4 = new CosmosVectorEmbedding()
            .setPath("/vector4")
            .setDataType(CosmosVectorDataType.UINT8)
            .setEmbeddingDimensions(3)
            .setDistanceFunction(CosmosVectorDistanceFunction.EUCLIDEAN);

        return Arrays.asList(embedding1, embedding2, embedding3, embedding4);
    }

    private String getVectorEmbeddingPolicyAsString() {
        return "{\"vectorEmbeddings\":[" +
            "{\"path\":\"/vector1\",\"dataType\":\"int8\",\"dimensions\":3,\"distanceFunction\":\"cosine\"}," +
            "{\"path\":\"/vector2\",\"dataType\":\"float32\",\"dimensions\":3,\"distanceFunction\":\"dotproduct\"}," +
            "{\"path\":\"/vector3\",\"dataType\":\"uint8\",\"dimensions\":3,\"distanceFunction\":\"euclidean\"}," +
            "{\"path\":\"/vector4\",\"dataType\":\"uint8\",\"dimensions\":3,\"distanceFunction\":\"euclidean\"}" +
            "]}";
    }

    private String getVectorIndexesAsString() {
        return "[" +
            "{\"type\":\"flat\",\"path\":\"/vector1\"}," +
            "{\"type\":\"quantizedFlat\",\"vectorIndexShardKeys\":[\"/zipCode\"],\"quantizerType\":\"product\",\"path\":\"/vector2\",\"quantizationByteSize\":2}," +
            "{\"type\":\"diskANN\",\"indexingSearchListSize\":30,\"vectorIndexShardKeys\":[\"/country/city\"],\"quantizerType\":\"product\",\"path\":\"/vector3\",\"quantizationByteSize\":2}," +
            "{\"type\":\"diskANN\",\"indexingSearchListSize\":30,\"vectorIndexShardKeys\":[\"/country/city\"],\"quantizerType\":\"spherical\",\"path\":\"/vector4\"}" +
        "]";
    }
}
