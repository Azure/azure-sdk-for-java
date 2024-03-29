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
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.DistanceFunction;
import com.azure.cosmos.models.Embedding;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.VectorDataType;
import com.azure.cosmos.models.VectorEmbeddingPolicy;
import com.azure.cosmos.models.VectorIndexSpec;
import com.azure.cosmos.models.VectorIndexType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Ignore("TODO: Ignore these test cases until the public emulator with vector indexes is released.")
public class VectorIndexTest extends TestSuiteBase {
    protected static final int TIMEOUT = 30000;
    protected static final int SETUP_TIMEOUT = 20000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    protected static Logger logger = LoggerFactory.getLogger(VectorIndexTest.class.getSimpleName());
    private final String databaseId = CosmosDatabaseForTest.generateId();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    @BeforeClass(groups = {"long"}, timeOut = SETUP_TIMEOUT)
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

    @AfterClass(groups = {"long"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
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

    @Test(groups = {"long"}, timeOut = TIMEOUT)
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
        vectorIndexSpec.setType(VectorIndexType.FLAT.getValue());
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

    @Test(groups = {"long"}, timeOut = TIMEOUT)
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
        vectorIndexSpec.setType(VectorIndexType.FLAT.getValue());
        indexingPolicy.setVectorIndexes(ImmutableList.of(vectorIndexSpec));
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        Embedding embedding = new Embedding();
        embedding.setPath("/vector1");
        embedding.setDistanceFunction(DistanceFunction.COSINE.getValue());
        embedding.setDimensions(3L);
        embedding.setVectorDataType("String");

        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for wrong vector date type being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid vector data type for the vector embedding policy.");
        }

        embedding.setVectorDataType("");
        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for empty vector date type being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Vector data type cannot be empty for the vector embedding policy.");
        }

        embedding.setVectorDataType(VectorDataType.FLOAT32.getValue());
        embedding.setDistanceFunction("COS");
        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for wrong distance function being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid distance function for the vector embedding policy.");
        }

        embedding.setDistanceFunction("");
        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for empty distance function being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Distance function cannot be empty for the vector embedding policy.");
        }

        embedding.setDistanceFunction(DistanceFunction.COSINE.getValue());
        embedding.setDimensions(-1L);
        try {
            VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(ImmutableList.of(embedding));
            collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);
            fail("Vector Embedding policy creation will fail for negative dimensions being passed");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Dimensions for the embedding has to be a long value greater than 1 for the vector embedding policy");
        }
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
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

        Embedding embedding = new Embedding();
        embedding.setPath("/vector1");
        embedding.setDistanceFunction(DistanceFunction.COSINE.getValue());
        embedding.setDimensions(3L);
        embedding.setVectorDataType(VectorDataType.INT8.getValue());
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

    @Test(groups = {"long"}, timeOut = TIMEOUT)
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

        List<Embedding> embeddings = populateEmbeddings();
        embeddings.get(2).setPath("/vector2");
        VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(embeddings);

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer createdCollection = database.getContainer(collectionDefinition.getId());
        CosmosContainerProperties collectionProperties = createdCollection.read().block().getProperties();
        validateCollectionProperties(collectionDefinition, collectionProperties);
    }

    private void validateCollectionProperties(CosmosContainerProperties collectionDefinition, CosmosContainerProperties collectionProperties) {
        assertThat(collectionProperties.getVectorEmbeddingPolicy()).isNotNull();
        assertThat(collectionProperties.getVectorEmbeddingPolicy().getEmbeddings()).isNotNull();
        List<Embedding> embeddings = collectionProperties.getVectorEmbeddingPolicy().getEmbeddings();
        assertThat(embeddings).hasSameSizeAs(collectionDefinition.getVectorEmbeddingPolicy().getEmbeddings());
        for (int i = 0; i < embeddings.size(); i++) {
            assertThat(embeddings.get(0).getPath()).isEqualTo(
                collectionDefinition.getVectorEmbeddingPolicy().getEmbeddings().get(0).getPath());
        }

        assertThat(collectionProperties.getIndexingPolicy().getVectorIndexes()).isNotNull();
        List<VectorIndexSpec> vectorIndexes = collectionProperties.getIndexingPolicy().getVectorIndexes();
        assertThat(vectorIndexes).hasSameSizeAs(collectionDefinition.getIndexingPolicy().getVectorIndexes());
        for (int i = 0; i < vectorIndexes.size(); i++) {
            assertThat(vectorIndexes.get(0).getPath()).isEqualTo(
                collectionDefinition.getIndexingPolicy().getVectorIndexes().get(0).getPath());
        }
    }

    private List<VectorIndexSpec> populateVectorIndexes() {
        VectorIndexSpec vectorIndexSpec1 = new VectorIndexSpec("/vector1");
        vectorIndexSpec1.setType(VectorIndexType.FLAT.getValue());

        VectorIndexSpec vectorIndexSpec2 = new VectorIndexSpec("/vector2");
        vectorIndexSpec2.setType(VectorIndexType.QUANTIZED_FLAT.getValue());

        VectorIndexSpec vectorIndexSpec3 = new VectorIndexSpec("/vector3");
        vectorIndexSpec3.setType(VectorIndexType.DISK_ANN.getValue());

        return List.of(vectorIndexSpec1, vectorIndexSpec2, vectorIndexSpec3);
    }

    private List<Embedding> populateEmbeddings() {
        Embedding embedding1 = new Embedding();
        embedding1.setPath("/vector1");
        embedding1.setDistanceFunction(DistanceFunction.COSINE.getValue());
        embedding1.setDimensions(3L);
        embedding1.setVectorDataType(VectorDataType.FLOAT32.getValue());

        Embedding embedding2 = new Embedding();
        embedding2.setPath("/vector2");
        embedding2.setDistanceFunction(DistanceFunction.DOT_PRODUCT.getValue());
        embedding2.setDimensions(3L);
        embedding2.setVectorDataType(VectorDataType.INT8.getValue());

        Embedding embedding3 = new Embedding();
        embedding3.setPath("/vector3");
        embedding3.setDistanceFunction(DistanceFunction.EUCLIDEAN.getValue());
        embedding3.setDimensions(3L);
        embedding3.setVectorDataType(VectorDataType.UINT8.getValue());

        return List.of(embedding1, embedding2, embedding3);
    }
}
