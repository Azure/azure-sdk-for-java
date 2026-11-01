// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosVectorDataType;
import com.azure.cosmos.models.CosmosVectorDistanceFunction;
import com.azure.cosmos.models.CosmosVectorEmbedding;
import com.azure.cosmos.models.CosmosVectorEmbeddingPolicy;
import com.azure.cosmos.models.CosmosVectorIndexSpec;
import com.azure.cosmos.models.CosmosVectorIndexType;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.cosmos.rx.TestSuiteBase.createCollection;
import static com.azure.cosmos.rx.TestSuiteBase.createTestDatabase;
import static com.azure.cosmos.rx.TestSuiteBase.safeClose;
import static com.azure.cosmos.rx.TestSuiteBase.safeDeleteDatabase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class VectorEmbeddingQueryTest {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final ObjectMapper MAPPER = Utils.getSimpleObjectMapper();
    private static final List<CosmosVectorDataType> DATA_TYPES = Arrays.asList(
        CosmosVectorDataType.FLOAT16, CosmosVectorDataType.FLOAT32,
        CosmosVectorDataType.INT8, CosmosVectorDataType.UINT8);
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    @AfterClass(groups = {"query"}, alwaysRun = true, timeOut = 120_000)
    public void afterClass() {
        try {
            safeDeleteDatabase(database);
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"unit"})
    public void allVectorDataTypesRoundTrip() throws JsonProcessingException {
        CosmosVectorEmbeddingPolicy policy = containerProperties().getVectorEmbeddingPolicy();
        CosmosVectorEmbeddingPolicy deserialized = MAPPER.readValue(
            MAPPER.writeValueAsString(policy), CosmosVectorEmbeddingPolicy.class);
        validateEmbeddingPolicy(deserialized);
    }

    @Test(groups = {"query"}, timeOut = 900_000)
    public void createAndQueryAllVectorDataTypes() {
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode()
            .consistencyLevel(ConsistencyLevel.SESSION)
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();
        database = createTestDatabase(client, "vector-embedding-types");
        CosmosAsyncContainer container = createCollection(
            database, containerProperties(), new CosmosContainerRequestOptions());
        CosmosContainerProperties properties = container.read().block(REQUEST_TIMEOUT).getProperties();
        validateEmbeddingPolicy(properties.getVectorEmbeddingPolicy());
        assertThat(properties.getIndexingPolicy().getVectorIndexes()).hasSize(DATA_TYPES.size());
        for (CosmosVectorDataType dataType : DATA_TYPES) {
            assertThat(properties.getIndexingPolicy().getVectorIndexes())
                .filteredOn(index -> index.getPath().equals("/" + dataType))
                .singleElement()
                .satisfies(index -> assertThat(index.getType())
                    .isEqualTo(CosmosVectorIndexType.QUANTIZED_FLAT.toString()));
        }

        List<ObjectNode> documents = Arrays.asList(document("near", 0), document("middle", 1), document("far", 2));
        for (ObjectNode document : documents) {
            assertThat(container.createItem(document).block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(201);
        }

        // Below the quantizedFlat training threshold, queries use its full-scan fallback.
        // This tests embedding types, not approximate-index recall or index training.
        for (CosmosVectorDataType dataType : DATA_TYPES) {
            for (boolean bruteForce : new boolean[] {false, true}) {
                validateQuery(container, dataType, bruteForce, documents.get(0), "near", "middle", "far");
                validateQuery(container, dataType, bruteForce, documents.get(2), "far", "middle", "near");
            }
        }
    }

    private static CosmosContainerProperties containerProperties() {
        List<CosmosVectorEmbedding> embeddings = new ArrayList<>();
        List<CosmosVectorIndexSpec> indexes = new ArrayList<>();
        List<ExcludedPath> excludedPaths = new ArrayList<>();
        for (CosmosVectorDataType dataType : DATA_TYPES) {
            String path = "/" + dataType;
            embeddings.add(new CosmosVectorEmbedding()
                .setPath(path)
                .setDataType(dataType)
                .setEmbeddingDimensions(3)
                .setDistanceFunction(CosmosVectorDistanceFunction.EUCLIDEAN));
            indexes.add(new CosmosVectorIndexSpec()
                .setPath(path)
                .setType(CosmosVectorIndexType.QUANTIZED_FLAT.toString()));
            excludedPaths.add(new ExcludedPath(path + "/*"));
        }
        CosmosVectorEmbeddingPolicy embeddingPolicy = new CosmosVectorEmbeddingPolicy();
        embeddingPolicy.setCosmosVectorEmbeddings(embeddings);
        return new CosmosContainerProperties("all-vector-types", "/pk")
            .setVectorEmbeddingPolicy(embeddingPolicy)
            .setIndexingPolicy(new IndexingPolicy()
                .setIndexingMode(IndexingMode.CONSISTENT)
                .setIncludedPaths(Collections.singletonList(new IncludedPath("/*")))
                .setExcludedPaths(excludedPaths)
                .setVectorIndexes(indexes));
    }

    private static ObjectNode document(String id, int distance) {
        ObjectNode document = MAPPER.createObjectNode();
        document.put("id", id);
        document.put("pk", "vectors");
        document.putArray("float16").add(-1.5 + distance).add(0.5).add(2.0);
        document.putArray("float32").add(-1.25 + distance).add(0.25).add(2.5);
        document.putArray("int8").add(-128 + distance).add(-1).add(127);
        document.putArray("uint8").add(255 - distance).add(0).add(128);
        return document;
    }

    private static void validateEmbeddingPolicy(CosmosVectorEmbeddingPolicy policy) {
        assertThat(policy).isNotNull();
        assertThat(policy.getVectorEmbeddings()).hasSize(DATA_TYPES.size());
        for (CosmosVectorDataType dataType : DATA_TYPES) {
            assertThat(policy.getVectorEmbeddings())
                .filteredOn(embedding -> embedding.getPath().equals("/" + dataType))
                .singleElement()
                .satisfies(embedding -> {
                    assertThat(embedding.getDataType()).isEqualTo(dataType);
                    assertThat(embedding.getEmbeddingDimensions()).isEqualTo(3);
                    assertThat(embedding.getDistanceFunction()).isEqualTo(CosmosVectorDistanceFunction.EUCLIDEAN);
                });
        }
    }

    private static void validateQuery(CosmosAsyncContainer container, CosmosVectorDataType dataType,
        boolean bruteForce, ObjectNode queryDocument, String... expectedIds) {
        String distance = "VectorDistance(c." + dataType + ", @vector, " + bruteForce
            + ", {'dataType': '" + dataType + "', 'distanceFunction': 'euclidean'})";
        SqlQuerySpec query = new SqlQuerySpec(
            "SELECT TOP 3 c.id, " + distance + " AS distance FROM c ORDER BY " + distance,
            Collections.singletonList(new SqlParameter("@vector", queryDocument.get(dataType.toString()))));
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey("vectors"));
        List<ObjectNode> results = container.queryItems(query, options, ObjectNode.class)
            .collectList().block(REQUEST_TIMEOUT);

        assertThat(results)
            .as("%s bruteForce=%s query=%s", dataType, bruteForce, queryDocument.get("id"))
            .extracting(result -> result.path("id").asText())
            .containsExactly(expectedIds);
        for (ObjectNode result : results) {
            assertThat(result.path("distance").isNumber()).isTrue();
        }
        assertThat(results.get(0).get("distance").asDouble()).isCloseTo(0.0, offset(0.0001));
        assertThat(results.get(1).get("distance").asDouble()).isCloseTo(1.0, offset(0.0001));
        assertThat(results.get(2).get("distance").asDouble())
            .isGreaterThan(results.get(1).get("distance").asDouble());
    }
}
