package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VectorIndexTest {
    protected static final int TIMEOUT = 3000000;
    protected static final int SETUP_TIMEOUT = 2000000;
    protected static final int SHUTDOWN_TIMEOUT = 2000000;

    protected static Logger logger = LoggerFactory.getLogger(VectorIndexTest.class.getSimpleName());
    private final String databaseId = "Vector_index_db";
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    private CosmosAsyncContainer collection;

    @Test(groups = { "long" }, timeOut = TIMEOUT)
    public void insertWithVectorIndex() throws Exception {
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

        // setting vectorIndexes
        indexingPolicy.setVectorIndexes(getVectorIndexSpec());

        // setting vector embedding policy
        VectorEmbeddingPolicy vectorEmbeddingPolicy = new VectorEmbeddingPolicy(getEmbeddings());

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setVectorEmbeddingPolicy(vectorEmbeddingPolicy);

        ObjectMapper om = new ObjectMapper();

//        JsonNode doc1 = om.readValue("{\"name\":\"Alexander Pushkin\",\"vector1\":[0.2, 0.5, 0.7],\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", JsonNode.class);
//        JsonNode doc2 = om.readValue("{\"name\":\"Alexander Pushkin\",\"vector1\":[0.6, 0.5, 0.7],\"description\":\"playwright\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);
//        JsonNode doc3 = om.readValue("{\"name\":\"ABC DEF\",\"vector1\":[0.2, 0.9, 0.7],\"description\":\"poet\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);

        database.createContainer(collectionDefinition).block();
        collection = database.getContainer(collectionDefinition.getId());

//        InternalObjectNode properties = BridgeInternal.getProperties(collection.createItem(doc1).block());
    }

    private List<VectorIndexSpec> getVectorIndexSpec() {
        VectorIndexSpec vectorIndexSpec1 = new VectorIndexSpec("/vector1");
        vectorIndexSpec1.setVectorIndexType(VectorIndexType.FLAT);

        VectorIndexSpec vectorIndexSpec2 = new VectorIndexSpec("/vector2");
        vectorIndexSpec2.setVectorIndexType(VectorIndexType.QUANTIZED_FLAT);

        VectorIndexSpec vectorIndexSpec3 = new VectorIndexSpec("/vector3");
        vectorIndexSpec3.setVectorIndexType(VectorIndexType.DISK_ANN);

        return ImmutableList.of(vectorIndexSpec1, vectorIndexSpec2, vectorIndexSpec3);
    }

    private List<Embedding> getEmbeddings() {
        Embedding embedding1 = new Embedding();
        embedding1.setPath("/vector1");
        embedding1.setDistanceFunction(DistanceFunction.COSINE);
        embedding1.setDimensions(3L);
        embedding1.setVectorDataType(VectorDataType.FLOAT32);

        Embedding embedding2 = new Embedding();
        embedding2.setPath("/vector2");
        embedding2.setDistanceFunction(DistanceFunction.DOT_PRODUCT);
        embedding2.setDimensions(3L);
        embedding2.setVectorDataType(VectorDataType.INT8);

        Embedding embedding3 = new Embedding();
        embedding3.setPath("/vector1");
        embedding3.setDistanceFunction(DistanceFunction.EUCLIDEAN);
        embedding3.setDimensions(3L);
        embedding3.setVectorDataType(VectorDataType.UINT8);

        return ImmutableList.of(embedding1, embedding2, embedding3);
    }

    @BeforeClass(groups = { "long" }, timeOut = SETUP_TIMEOUT)
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

    @AfterClass(groups = { "long" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }

    static protected CosmosAsyncDatabase createDatabase(CosmosAsyncClient client, String databaseId) {
        CosmosDatabaseProperties databaseSettings = new CosmosDatabaseProperties(databaseId);
        client.createDatabase(databaseSettings).block();
        return client.getDatabase(databaseSettings.getId());
    }

    static protected void safeDeleteDatabase(CosmosAsyncDatabase database) {
        if (database != null) {
            try {
                database.delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeClose(CosmosAsyncClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                logger.error("failed to close client", e);
            }
        }
    }
}
