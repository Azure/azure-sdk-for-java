// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosVectorDataType;
import com.azure.cosmos.models.CosmosVectorDistanceFunction;
import com.azure.cosmos.models.CosmosVectorEmbedding;
import com.azure.cosmos.models.CosmosVectorEmbeddingPolicy;
import com.azure.cosmos.models.CosmosVectorIndexSpec;
import com.azure.cosmos.models.CosmosVectorIndexType;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * One query over all four embedding types, executed once per connection-mode CI lane.
 * The query lane uses Direct TCP; the HTTP/2 query lane hard-disables thin-client routing to exercise
 * Compute; the thinclient and thinclientEndpointProbe lanes exercise the proxy.
 * Routing flags are never changed in-process because they are evaluated lazily by every client.
 */
public class VectorSearchQueryTest extends TestSuiteBase {
    private static final String PK_FIELD = "mypk";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final CosmosVectorDataType[] VECTOR_TYPES = {
        CosmosVectorDataType.FLOAT16, CosmosVectorDataType.FLOAT32,
        CosmosVectorDataType.INT8, CosmosVectorDataType.UINT8
    };
    private static final String VECTOR_QUERY = "SELECT TOP 3 c.id, "
        + "VectorDistance(c.float16, @vector, false, {'dataType':'float16', "
        + "'distanceFunction':'euclidean'}) AS float16Score, "
        + "VectorDistance(c.float32, @vector, false, {'dataType':'float32', "
        + "'distanceFunction':'euclidean'}) AS float32Score, "
        + "VectorDistance(c.int8, @vector, false, {'dataType':'int8', "
        + "'distanceFunction':'euclidean'}) AS int8Score, "
        + "VectorDistance(c.uint8, @vector, false, {'dataType':'uint8', "
        + "'distanceFunction':'euclidean'}) AS uint8Score "
        + "FROM c ORDER BY VectorDistance(c.float16, @vector, false, "
        + "{'dataType':'float16', 'distanceFunction':'euclidean'})";

    private final ConnectionMode connectionMode;
    private CosmosAsyncClient client;
    private CosmosAsyncClient setupClient;
    private CosmosAsyncDatabase database;
    private boolean expectThinClient;
    private boolean requireComputeHttp2;

    @DataProvider
    public static Object[][] vectorQueryClientBuilders() {
        return Configs.isHttp2Enabled()
            ? clientBuildersWithGateway()
            : clientBuilderSolelyDirectWithSessionConsistency();
    }

    @Factory(dataProvider = "vectorQueryClientBuilders")
    public VectorSearchQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        connectionMode = CosmosBridgeInternal.getConnectionPolicy(clientBuilder).getConnectionMode();
    }

    @BeforeClass(groups = {"query", "thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        boolean gatewayHttp2 = connectionMode == ConnectionMode.GATEWAY && Configs.isHttp2Enabled();
        expectThinClient = gatewayHttp2 && !Boolean.FALSE.equals(Configs.isThinClientEnabled());
        requireComputeHttp2 = gatewayHttp2 && !expectThinClient;

        client = getClientBuilder().buildAsyncClient();
        setupClient = createDirectRxDocumentClient(
            ConsistencyLevel.SESSION, Protocol.TCP, false, null, true, true).buildAsyncClient();
        database = createTestDatabase(setupClient, "vector-query");
    }

    @AfterClass(groups = {"query", "thinclient", "thinclientEndpointProbe"},
        timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        try {
            safeDeleteDatabase(database);
        } finally {
            safeClose(client);
            safeClose(setupClient);
        }
    }

    /**
     * The first document operation from the client under test queries a fresh container with all four
     * embedding types. Distinct vectors make missing projections or queries against the wrong path observable.
     * The timeout includes container creation, readiness polling, and document seeding.
     */
    @Test(groups = {"query", "thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT * 2)
    public void testVectorSearchAcrossEmbeddingTypes() {
        String containerId = "vecSearch_" + UUID.randomUUID();
        CosmosAsyncContainer container = createVectorContainer(containerId);
        String[] docIds = {"near", "middle", "far"};
        Number[][][] embeddings = {
            {{1.5, 0, 0}, {1.25, 0, 0}, {-2, 0, 0}, {5, 0, 0}},
            {{2, 0, 0}, {3.5, 0, 0}, {-3, 0, 0}, {128, 0, 0}},
            {{4, 0, 0}, {5.25, 0, 0}, {-128, 0, 0}, {255, 0, 0}}
        };
        double[][] expectedScores = {
            {0.5, 0.25, 3, 4},
            {1, 2.5, 4, 127},
            {3, 4.25, 129, 254}
        };

        // The float16 case uses quantizedFlat's full-scan fallback below its training threshold.
        String vecPk = UUID.randomUUID().toString();
        String sessionToken = null;
        for (int i = 0; i < embeddings.length; i++) {
            ObjectNode doc = OBJECT_MAPPER.createObjectNode();
            doc.put("id", docIds[i]);
            doc.put(PK_FIELD, vecPk);
            for (int j = 0; j < VECTOR_TYPES.length; j++) {
                doc.set(VECTOR_TYPES[j].toString(), OBJECT_MAPPER.valueToTree(embeddings[i][j]));
            }
            CosmosItemResponse<ObjectNode> response = container.createItem(doc, new PartitionKey(vecPk), null).block();
            assertThat(response.getStatusCode()).isEqualTo(201);
            sessionToken = response.getSessionToken();
        }

        SqlQuerySpec query = new SqlQuerySpec(VECTOR_QUERY,
            Collections.singletonList(new SqlParameter("@vector", new int[] {1, 0, 0})));
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setSessionToken(sessionToken);

        List<ObjectNode> results = queryItems(containerId, query, options);
        assertThat(results).extracting(doc -> doc.get("id").asText()).containsExactly(docIds);
        for (int i = 0; i < results.size(); i++) {
            ObjectNode result = results.get(i);
            assertThat(result.size()).isEqualTo(1 + VECTOR_TYPES.length);
            for (int j = 0; j < VECTOR_TYPES.length; j++) {
                String scoreField = VECTOR_TYPES[j] + "Score";
                assertThat(result.get(scoreField)).as("%s projection for %s", scoreField, docIds[i]).isNotNull();
                assertThat(result.get(scoreField).isNumber()).isTrue();
                assertThat(result.get(scoreField).asDouble()).as("%s for %s", scoreField, docIds[i])
                    .isCloseTo(expectedScores[i][j], within(0.0001));
            }
        }
    }

    private CosmosAsyncContainer createVectorContainer(String containerId) {
        CosmosContainerProperties props = new CosmosContainerProperties(containerId, "/" + PK_FIELD);
        List<CosmosVectorEmbedding> embeddings = new ArrayList<>();
        List<CosmosVectorIndexSpec> vectorIndexes = new ArrayList<>();
        List<ExcludedPath> excludedPaths = new ArrayList<>();
        excludedPaths.add(new ExcludedPath("/\"_etag\"/?"));
        for (CosmosVectorDataType dataType : VECTOR_TYPES) {
            String path = "/" + dataType;
            CosmosVectorEmbedding embedding = new CosmosVectorEmbedding();
            embedding.setPath(path);
            embedding.setDataType(dataType);
            embedding.setEmbeddingDimensions(3);
            embedding.setDistanceFunction(CosmosVectorDistanceFunction.EUCLIDEAN);
            embeddings.add(embedding);

            CosmosVectorIndexSpec vectorIndex = new CosmosVectorIndexSpec();
            vectorIndex.setPath(path);
            vectorIndex.setType((dataType == CosmosVectorDataType.FLOAT16
                ? CosmosVectorIndexType.QUANTIZED_FLAT : CosmosVectorIndexType.FLAT).toString());
            vectorIndexes.add(vectorIndex);
            excludedPaths.add(new ExcludedPath(path + "/*"));
        }
        CosmosVectorEmbeddingPolicy policy = new CosmosVectorEmbeddingPolicy();
        policy.setCosmosVectorEmbeddings(embeddings);
        props.setVectorEmbeddingPolicy(policy);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        indexingPolicy.setIncludedPaths(Collections.singletonList(new IncludedPath("/*")));
        indexingPolicy.setExcludedPaths(excludedPaths);
        indexingPolicy.setVectorIndexes(vectorIndexes);
        props.setIndexingPolicy(indexingPolicy);

        CosmosAsyncContainer container = createCollection(database, props, new CosmosContainerRequestOptions());
        CosmosContainerProperties actual = container.read().block().getProperties();
        assertThat(actual.getVectorEmbeddingPolicy()).isNotNull();
        assertThat(actual.getVectorEmbeddingPolicy().getVectorEmbeddings())
            .extracting(CosmosVectorEmbedding::getDataType).containsExactlyInAnyOrder(VECTOR_TYPES);
        for (CosmosVectorEmbedding embedding : actual.getVectorEmbeddingPolicy().getVectorEmbeddings()) {
            assertThat(embedding.getPath()).isEqualTo("/" + embedding.getDataType());
            assertThat(embedding.getEmbeddingDimensions()).isEqualTo(3);
            assertThat(embedding.getDistanceFunction()).isEqualTo(CosmosVectorDistanceFunction.EUCLIDEAN);
        }
        assertThat(actual.getIndexingPolicy().getVectorIndexes()).hasSize(VECTOR_TYPES.length);
        for (CosmosVectorIndexSpec expectedIndex : vectorIndexes) {
            assertThat(actual.getIndexingPolicy().getVectorIndexes())
                .filteredOn(index -> expectedIndex.getPath().equals(index.getPath()))
                .singleElement().satisfies(index -> assertThat(index.getType()).isEqualTo(expectedIndex.getType()));
        }
        return container;
    }

    private List<ObjectNode> queryItems(String containerId, SqlQuerySpec query, CosmosQueryRequestOptions options) {
        CosmosAsyncContainer container = client.getDatabase(database.getId()).getContainer(containerId);
        List<ObjectNode> results = new ArrayList<>();
        List<CosmosDiagnostics> diagnostics = new ArrayList<>();
        for (FeedResponse<ObjectNode> page : container.queryItems(query, options, ObjectNode.class).byPage().toIterable()) {
            results.addAll(page.getResults());
            assertThat(page.getCosmosDiagnostics()).isNotNull();
            diagnostics.add(page.getCosmosDiagnostics());
        }
        assertQueryRouting(diagnostics);
        return results;
    }

    private void assertQueryRouting(List<CosmosDiagnostics> diagnostics) {
        assertThat(diagnostics).isNotEmpty();
        List<CosmosDiagnosticsRequestInfo> queryRequests = new ArrayList<>();
        for (CosmosDiagnostics diagnostic : diagnostics) {
            assertThat(diagnostic.getDiagnosticsContext()).isNotNull();
            if (expectThinClient) {
                assertThinClientEndpointUsed(diagnostic);
            }
            queryRequests.addAll(diagnostic.getDiagnosticsContext().getRequestInfo().stream()
                .filter(request -> "Document:Query".equals(request.getRequestType()))
                .collect(Collectors.toList()));
        }
        assertThat(queryRequests).as("Diagnostics must contain document query requests, not only query plans")
            .isNotEmpty();
        for (CosmosDiagnosticsRequestInfo request : queryRequests) {
            URI endpoint = URI.create(request.getEndpoint());
            if (connectionMode == ConnectionMode.DIRECT) {
                assertThat(endpoint.getScheme()).isEqualTo("rntbd");
            } else {
                assertThat(endpoint.getScheme()).isEqualTo("https");
                if (!expectThinClient) {
                    assertThat(endpoint.getPort()).as("Compute/Gateway queries must not use the thin-client proxy")
                        .isNotEqualTo(10250);
                }
            }
        }
        if (requireComputeHttp2) {
            List<ClientSideRequestStatistics.GatewayStatistics> queryStatistics = diagnostics.stream()
                .flatMap(diagnostic -> ImplementationBridgeHelpers.CosmosDiagnosticsHelper
                    .getCosmosDiagnosticsAccessor().getClientSideRequestStatistics(diagnostic).stream())
                .flatMap(statistics -> statistics.getGatewayStatisticsList().stream())
                .filter(statistics -> statistics.getResourceType() == ResourceType.Document
                    && statistics.getOperationType() == OperationType.Query)
                .collect(Collectors.toList());
            assertThat(queryStatistics).isNotEmpty();
            for (ClientSideRequestStatistics.GatewayStatistics statistics : queryStatistics) {
                assertThat(statistics.isHttp2()).as("Compute queries must negotiate HTTP/2").isTrue();
            }
        }
    }
}
