// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosFullTextIndex;
import com.azure.cosmos.models.CosmosFullTextPath;
import com.azure.cosmos.models.CosmosFullTextPolicy;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IndexingPolicy;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.cosmos.rx.TestSuiteBase.createCollection;
import static com.azure.cosmos.rx.TestSuiteBase.createTestDatabase;
import static com.azure.cosmos.rx.TestSuiteBase.safeClose;
import static com.azure.cosmos.rx.TestSuiteBase.safeDeleteDatabase;
import static org.assertj.core.api.Assertions.assertThat;

// Runs in the query integration suite against an emulator/account with full-text search support.
@Test(groups = "query", singleThreaded = true)
public class HybridSearchRankingTest {
    private static final String RANKING_PROPERTY = "COSMOS.HYBRID_SEARCH_USE_COMPETITION_RANKING";
    private static final int DOCUMENT_COUNT = 260;
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private CosmosAsyncContainer tierContainer;
    private List<ObjectNode> documentsByRid;

    @BeforeClass(groups = "query", timeOut = 120000)
    public void setUp() {
        client = new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY).buildAsyncClient();
        database = createTestDatabase(client, "hybrid-ranking");

        CosmosFullTextIndex titleIndex = new CosmosFullTextIndex();
        titleIndex.setPath("/title");
        CosmosFullTextIndex textIndex = new CosmosFullTextIndex();
        textIndex.setPath("/text");
        IndexingPolicy indexing = new IndexingPolicy();
        indexing.setCosmosFullTextIndexes(Arrays.asList(titleIndex, textIndex));
        CosmosFullTextPath titlePath = new CosmosFullTextPath();
        titlePath.setPath("/title");
        titlePath.setLanguage("en-US");
        CosmosFullTextPath textPath = new CosmosFullTextPath();
        textPath.setPath("/text");
        textPath.setLanguage("en-US");
        CosmosFullTextPolicy fullText = new CosmosFullTextPolicy();
        fullText.setDefaultLanguage("en-US");
        fullText.setPaths(Arrays.asList(titlePath, textPath));
        CosmosContainerProperties properties = new CosmosContainerProperties("ties", "/pk");
        properties.setIndexingPolicy(indexing);
        properties.setFullTextPolicy(fullText);
        container = createCollection(database, properties, new CosmosContainerRequestOptions(), 10000);
        CosmosContainerProperties tierProperties = new CosmosContainerProperties("tiers", "/pk");
        tierProperties.setIndexingPolicy(indexing);
        tierProperties.setFullTextPolicy(fullText);
        tierContainer = createCollection(database, tierProperties, new CosmosContainerRequestOptions(), 400);
        String[] tierTexts = {"beta filler filler filler filler", "beta beta filler filler filler",
            "beta beta beta filler filler", "beta beta beta beta filler", "beta beta beta beta beta"};
        for (int i = 0; i < 5; i++) {
            ObjectNode document = JsonNodeFactory.instance.objectNode();
            document.put("id", "tier-" + i);
            document.put("pk", "tiers");
            document.put("title", i == 0 ? "alpha alpha" : i == 4 ? "filler filler" : "alpha filler");
            document.put("text", tierTexts[i]);
            tierContainer.createItem(document).block();
        }

        // Identical fields guarantee tied component scores without reproducing the service's BM25 implementation.
        // More than 256 inserts also exercise RID byte boundaries rather than assuming encoded RID order.
        for (int i = 0; i < DOCUMENT_COUNT; i++) {
            ObjectNode document = JsonNodeFactory.instance.objectNode();
            document.put("id", "doc-" + i);
            document.put("pk", Integer.toString(i % 2));
            document.put("title", "alpha");
            document.put("text", "beta");
            container.createItem(document).block();
        }
        documentsByRid = container.queryItems("SELECT * FROM c", new CosmosQueryRequestOptions(), ObjectNode.class)
            .collectList().block();
        assertThat(documentsByRid).hasSize(DOCUMENT_COUNT);
        documentsByRid.sort((left, right) -> Long.compareUnsigned(documentRid(right), documentRid(left)));
        List<ObjectNode> encodedOrder = new ArrayList<>(documentsByRid);
        encodedOrder.sort((left, right) -> right.get("_rid").asText().compareTo(left.get("_rid").asText()));
        assertThat(ids(encodedOrder)).as("Fixture must distinguish numeric from encoded RID ordering")
            .isNotEqualTo(ids(documentsByRid));
    }

    @AfterClass(groups = "query", alwaysRun = true, timeOut = 30000)
    public void tearDown() {
        safeDeleteDatabase(database);
        safeClose(client);
    }

    @DataProvider(name = "rankingModes")
    public Object[][] rankingModes() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = "query", dataProvider = "rankingModes", timeOut = 60000)
    public void tiedComponentsRespectRankingModeAndDirection(boolean competition) {
        String previous = System.getProperty(RANKING_PROPERTY);
        try {
            System.setProperty(RANKING_PROPERTY, Boolean.toString(competition));
            for (double[] weights : new double[][] {{1, 1}, {-1, -1}, {1, -1}, {2, -1}}) {
                List<String> expected = expectedOrder(weights, competition);
                String orderBy = orderBy(weights);
                List<FeedResponse<ObjectNode>> pages = container.queryItems(
                    "SELECT TOP 260 * FROM c " + orderBy, new CosmosQueryRequestOptions(), ObjectNode.class)
                    .byPage(17).collectList().block();
                List<ObjectNode> actual = pages.stream().flatMap(page -> page.getResults().stream())
                    .collect(Collectors.toList());
                assertThat(ids(actual)).as("weights=%s, competition=%s", Arrays.toString(weights), competition)
                    .containsExactlyElementsOf(expected);
                assertThat(pages).hasSizeGreaterThan(1);
                assertThat(actual).allSatisfy(document -> {
                    assertThat(document.get("title").asText()).isEqualTo("alpha");
                    assertThat(document.get("text").asText()).isEqualTo("beta");
                });

                // Keep the full fixture in the service's bounded component candidate lists.
                List<ObjectNode> window = container.queryItems(
                    "SELECT * FROM c " + orderBy + " OFFSET 3 LIMIT 257",
                    new CosmosQueryRequestOptions(), ObjectNode.class).collectList().block();
                assertThat(ids(window)).containsExactlyElementsOf(expected.subList(3, DOCUMENT_COUNT));
            }
        } finally {
            restoreRankingProperty(previous);
        }
    }

    @Test(groups = "query", timeOut = 30000)
    public void competitionRanksSkipTiedPositions() {
        List<ObjectNode> documents = tierContainer.queryItems("SELECT * FROM c",
            new CosmosQueryRequestOptions(), ObjectNode.class).collectList().block();
        assertThat(documents).hasSize(5);
        documents.sort((left, right) -> Long.compareUnsigned(documentRid(right), documentRid(left)));
        List<ObjectNode> titleOrder = new ArrayList<>(documents);
        // Equal document lengths: two occurrences outrank one, and no occurrence scores zero.
        titleOrder.sort((left, right) -> Integer.compare(titleTier(left), titleTier(right)));
        String previous = System.getProperty(RANKING_PROPERTY);
        try {
            for (boolean competition : new boolean[] {false, true}) {
                System.setProperty(RANKING_PROPERTY, Boolean.toString(competition));
                List<ObjectNode> expected = new ArrayList<>(documents);
                int[] titleRanks = competition ? new int[] {1, 2, 2, 2, 5} : new int[] {1, 2, 3, 4, 5};
                expected.sort((left, right) -> {
                    double leftScore = 1.0 / (60 + titleRanks[titleOrder.indexOf(left)])
                        + 1.0 / (60 + textRank(left));
                    double rightScore = 1.0 / (60 + titleRanks[titleOrder.indexOf(right)])
                        + 1.0 / (60 + textRank(right));
                    int comparison = Double.compare(rightScore, leftScore);
                    return comparison != 0 ? comparison : Long.compareUnsigned(documentRid(right), documentRid(left));
                });
                List<ObjectNode> results = tierContainer.queryItems("SELECT TOP 5 * FROM c "
                    + orderBy(new double[] {1, 1}), new CosmosQueryRequestOptions(), ObjectNode.class)
                    .collectList().block();
                assertThat(ids(results)).as("competition=%s", competition).containsExactlyElementsOf(ids(expected));
                List<ObjectNode> topTwo = tierContainer.queryItems("SELECT TOP 2 * FROM c "
                    + orderBy(new double[] {1, 1}), new CosmosQueryRequestOptions(), ObjectNode.class)
                    .collectList().block();
                assertThat(ids(topTwo)).containsExactlyElementsOf(ids(expected.subList(0, 2)));
            }
        } finally {
            restoreRankingProperty(previous);
        }
    }

    private static int titleTier(ObjectNode document) {
        String title = document.get("title").asText();
        return "alpha alpha".equals(title) ? 0 : "alpha filler".equals(title) ? 1 : 2;
    }

    private static int textRank(ObjectNode document) {
        // Texts have equal lengths and one through five occurrences of beta, so all scores are distinct.
        return 5 - Integer.parseInt(document.get("id").asText().substring("tier-".length()));
    }

    @Test(groups = "query", timeOut = 30000)
    public void emptyHybridQueryReturnsNoDocuments() {
        List<ObjectNode> results = container.queryItems(
            "SELECT TOP 10 * FROM c WHERE c.id = 'missing' " + orderBy(new double[] {1, 1}),
            new CosmosQueryRequestOptions(), ObjectNode.class).collectList().block();
        assertThat(results).isEmpty();
    }

    private List<String> expectedOrder(double[] weights, boolean competition) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < documentsByRid.size(); i++) {
            positions.add(i);
        }
        // In competition mode every tied score has rank 1. Ordinal rank follows the plan direction;
        // negative weights reverse that direction before their absolute magnitude is used in fusion.
        positions.sort((left, right) -> {
            int comparison = Double.compare(score(right, weights, competition), score(left, weights, competition));
            return comparison != 0 ? comparison : Integer.compare(left, right);
        });
        return positions.stream().map(position -> documentsByRid.get(position).get("id").asText())
            .collect(Collectors.toList());
    }

    private static double score(int descendingPosition, double[] weights, boolean competition) {
        double score = 0;
        for (double weight : weights) {
            int rank = competition ? 1 : weight < 0 ? DOCUMENT_COUNT - descendingPosition : descendingPosition + 1;
            score += Math.abs(weight) / (60 + rank);
        }
        return score;
    }

    private static String orderBy(double[] weights) {
        return "ORDER BY RANK RRF(FullTextScore(c.title, 'alpha'), FullTextScore(c.text, 'beta'), "
            + Arrays.toString(weights) + ")";
    }

    private static long documentRid(ObjectNode document) {
        byte[] bytes = Base64.getDecoder().decode(document.get("_rid").asText().replace('-', '/'));
        return ByteBuffer.wrap(bytes, 8, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    private static List<String> ids(List<ObjectNode> documents) {
        return documents.stream().map(document -> document.get("id").asText()).collect(Collectors.toList());
    }

    private static void restoreRankingProperty(String previous) {
        if (previous == null) {
            System.clearProperty(RANKING_PROPERTY);
        } else {
            System.setProperty(RANKING_PROPERTY, previous);
        }
    }
}
