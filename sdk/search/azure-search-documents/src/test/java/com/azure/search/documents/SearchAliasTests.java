// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.TestMode;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchAlias;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SearchAlias}-based operations.
 */
public class SearchAliasTests extends SearchTestBase {
    private static final String HOTEL_INDEX_NAME1 = "azsearch-search-shared-hotel-instance-one";
    private static final String HOTEL_INDEX_NAME2 = "azsearch-search-shared-hotel-instance-two";
    private static SearchIndexClient searchIndexClient;

    private SearchIndexClient indexClient;
    private SearchIndexAsyncClient indexAsyncClient;

    @BeforeAll
    public static void beforeAll() {
        // When running against the live service ensure all aliases are deleted before running these tests.
        if (TEST_MODE == TestMode.PLAYBACK) {
            return; // Running in PLAYBACK, no need to clean-up.
        }

        searchIndexClient = setupSharedIndex(HOTEL_INDEX_NAME1, HOTELS_TESTS_INDEX_DATA_JSON, null);
        setupSharedIndex(HOTEL_INDEX_NAME2, HOTELS_TESTS_INDEX_DATA_JSON, null);
    }

    @AfterAll
    public static void afterAll() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME1);
            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME2);
        }
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        indexClient = getSearchIndexClientBuilder(true).buildClient();
        indexAsyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        // When running against the live service ensure all aliases are deleted before running these tests.
        if (TEST_MODE == TestMode.PLAYBACK) {
            return; // Running in PLAYBACK, no need to clean-up.
        }

        SearchIndexClient cleanupClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildClient();

        boolean aliasDeleted = false;
        for (SearchAlias alias : cleanupClient.listAliases()) {
            cleanupClient.deleteAlias(alias.getName());
            aliasDeleted = true;
        }

        if (aliasDeleted) {
            // Give 3 seconds for alias deletion to propagate.
            sleepIfRunningAgainstService(3000);
        }
    }

    @Test
    public void canCreateAndGetAliasSync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1));
        SearchAlias searchAlias = indexClient.createAlias(expectedAlias);

        assertEquals(expectedAlias.getName(), searchAlias.getName());
        assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());

        searchAlias = indexClient.getAlias(expectedAlias.getName());

        assertEquals(expectedAlias.getName(), searchAlias.getName());
        assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());
    }

    @Test
    public void canCreateAliasAsync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1));

        StepVerifier.create(indexAsyncClient.createAlias(expectedAlias))
            .assertNext(searchAlias -> {
                assertEquals(expectedAlias.getName(), searchAlias.getName());
                assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());
            })
            .verifyComplete();

        StepVerifier.create(indexAsyncClient.getAlias(expectedAlias.getName()))
            .assertNext(searchAlias -> {
                assertEquals(expectedAlias.getName(), searchAlias.getName());
                assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());
            })
            .verifyComplete();
    }

    @Test
    public void cannotCreateAliasOnNonExistentIndexSync() {
        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias("my-alias",
            Collections.singletonList("index-that-does-not-exist"))));
    }

    @Test
    public void cannotCreateAliasOnNonExistentIndexAsync() {
        StepVerifier.create(indexAsyncClient.createAlias(new SearchAlias("my-alias",
                Collections.singletonList("index-that-does-not-exist"))))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void cannotCreateAliasWithInvalidNameSync() {
        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias("--invalid--alias-name",
            Collections.singletonList(HOTEL_INDEX_NAME1))));
    }

    @Test
    public void cannotCreateAliasWithInvalidNameAsync() {
        StepVerifier.create(indexAsyncClient.createAlias(new SearchAlias("--invalid--alias-name",
                Collections.singletonList(HOTEL_INDEX_NAME1))))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void cannotCreateMultipleAliasesWithTheSameNameSync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1));
        SearchAlias searchAlias = indexClient.createAlias(expectedAlias);

        assertEquals(expectedAlias.getName(), searchAlias.getName());
        assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());

        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias(expectedAlias.getName(),
            Collections.singletonList(HOTEL_INDEX_NAME1))));
    }

    @Test
    public void cannotCreateMultipleAliasesWithTheSameNameAsync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1));

        StepVerifier.create(indexAsyncClient.createAlias(expectedAlias))
            .assertNext(searchAlias -> {
                assertEquals(expectedAlias.getName(), searchAlias.getName());
                assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());
            })
            .verifyComplete();

        StepVerifier.create(indexAsyncClient.createAlias(new SearchAlias(expectedAlias.getName(),
                Collections.singletonList(HOTEL_INDEX_NAME1))))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void cannotCreateAliasWithMultipleIndexesSync() {
        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias("my-alias",
            Arrays.asList(HOTEL_INDEX_NAME1, HOTEL_INDEX_NAME2))));
    }

    @Test
    public void cannotCreateAliasWithMultipleIndexesAsync() {
        StepVerifier.create(indexAsyncClient.createAlias(new SearchAlias("my-alias",
                Arrays.asList(HOTEL_INDEX_NAME1, HOTEL_INDEX_NAME2))))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void canCreateMultipleAliasesReferencingTheSameIndexSync() {
        SearchAlias firstExpectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1));
        SearchAlias searchAlias = indexClient.createAlias(firstExpectedAlias);

        assertEquals(firstExpectedAlias.getName(), searchAlias.getName());
        assertEquals(firstExpectedAlias.getIndexes(), searchAlias.getIndexes());

        SearchAlias secondExpectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1));
        searchAlias = indexClient.createAlias(secondExpectedAlias);

        assertEquals(secondExpectedAlias.getName(), searchAlias.getName());
        assertEquals(secondExpectedAlias.getIndexes(), searchAlias.getIndexes());
    }

    @Test
    public void canCreateMultipleAliasesReferencingTheSameIndexAsync() {
        SearchAlias firstExpectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1));

        StepVerifier.create(indexAsyncClient.createAlias(firstExpectedAlias))
            .assertNext(searchAlias -> {
                assertEquals(firstExpectedAlias.getName(), searchAlias.getName());
                assertEquals(firstExpectedAlias.getIndexes(), searchAlias.getIndexes());
            })
            .verifyComplete();

        SearchAlias secondExpectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1));

        StepVerifier.create(indexAsyncClient.createAlias(secondExpectedAlias))
            .assertNext(searchAlias -> {
                assertEquals(secondExpectedAlias.getName(), searchAlias.getName());
                assertEquals(secondExpectedAlias.getIndexes(), searchAlias.getIndexes());
            })
            .verifyComplete();
    }

    @Test
    public void canUpdateAliasAfterCreationSync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(HOTEL_INDEX_NAME1)));

        SearchAlias expectedUpdatedAlias = new SearchAlias(aliasName, Collections.singletonList(HOTEL_INDEX_NAME2));
        SearchAlias updatedAlias = indexClient.createOrUpdateAlias(expectedUpdatedAlias);

        assertEquals(expectedUpdatedAlias.getName(), updatedAlias.getName());
        assertEquals(expectedUpdatedAlias.getIndexes(), updatedAlias.getIndexes());
    }

    @Test
    public void canUpdateAliasAfterCreationAsync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(HOTEL_INDEX_NAME1)))
            .block();

        SearchAlias expectedUpdatedAlias = new SearchAlias(aliasName, Collections.singletonList(HOTEL_INDEX_NAME2));

        StepVerifier.create(indexAsyncClient.createOrUpdateAlias(expectedUpdatedAlias))
            .assertNext(updatedAlias -> {
                assertEquals(expectedUpdatedAlias.getName(), updatedAlias.getName());
                assertEquals(expectedUpdatedAlias.getIndexes(), updatedAlias.getIndexes());
            })
            .verifyComplete();
    }

    @Test
    public void canDeleteAliasSync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(HOTEL_INDEX_NAME1)));

        assertDoesNotThrow(() -> indexClient.deleteAlias(aliasName));

        // Wait for 1 second for the alias to update
        TestHelpers.sleepIfRunningAgainstService(1000);

        assertThrows(HttpResponseException.class, () -> indexClient.getIndex(aliasName));
    }

    @Test
    public void canDeleteAliasAsync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(HOTEL_INDEX_NAME1)))
            .block();

        StepVerifier.create(indexAsyncClient.deleteAlias(aliasName)).verifyComplete();

        // Wait for 1 second for the alias to update
        TestHelpers.sleepIfRunningAgainstService(1000);

        StepVerifier.create(indexAsyncClient.getIndex(aliasName)).verifyError(HttpResponseException.class);
    }

    @Test
    public void cannotDeleteIndexWithAliasSync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(HOTEL_INDEX_NAME1)));

        // Give 3 seconds for alias deletion to propagate.
        sleepIfRunningAgainstService(3000);

        assertThrows(HttpResponseException.class, () -> indexClient.deleteIndex(HOTEL_INDEX_NAME1));
        assertDoesNotThrow(() -> indexClient.getIndex(HOTEL_INDEX_NAME1));
    }

    @Test
    public void cannotDeleteIndexWithAliasAsync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(HOTEL_INDEX_NAME1)))
            .block();

        // Give 3 seconds for alias deletion to propagate.
        sleepIfRunningAgainstService(3000);

        StepVerifier.create(indexAsyncClient.deleteIndex(HOTEL_INDEX_NAME1))
            .verifyError(HttpResponseException.class);

        StepVerifier.create(indexAsyncClient.getIndex(HOTEL_INDEX_NAME1))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void canListAliasesSync() {
        String firstAliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(firstAliasName,
            Collections.singletonList(HOTEL_INDEX_NAME1)));

        String secondAliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(secondAliasName,
            Collections.singletonList(HOTEL_INDEX_NAME1)));

        String thirdAliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(thirdAliasName,
            Collections.singletonList(HOTEL_INDEX_NAME1)));

        List<SearchAlias> aliases = indexClient.listAliases().stream().collect(Collectors.toList());
        assertEquals(3, aliases.size());
        assertTrue(aliases.stream().anyMatch(alias -> alias.getName().equals(firstAliasName)));
        assertTrue(aliases.stream().anyMatch(alias -> alias.getName().equals(secondAliasName)));
        assertTrue(aliases.stream().anyMatch(alias -> alias.getName().equals(thirdAliasName)));
    }

    @Test
    public void canListAliasesAsync() {
        String firstAliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(firstAliasName,
            Collections.singletonList(HOTEL_INDEX_NAME1))).block();

        String secondAliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(secondAliasName,
            Collections.singletonList(HOTEL_INDEX_NAME1))).block();

        String thirdAliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(thirdAliasName,
            Collections.singletonList(HOTEL_INDEX_NAME1))).block();

        StepVerifier.create(indexAsyncClient.listAliases().collectList())
            .assertNext(aliases -> {
                assertEquals(3, aliases.size());
                assertTrue(aliases.stream().anyMatch(alias -> alias.getName().equals(firstAliasName)));
                assertTrue(aliases.stream().anyMatch(alias -> alias.getName().equals(secondAliasName)));
                assertTrue(aliases.stream().anyMatch(alias -> alias.getName().equals(thirdAliasName)));
            })
            .verifyComplete();
    }

    @Test
    public void canInspectAliasUsageInServiceStatisticsSync() {
        indexClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1)));
        indexClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1)));
        indexClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1)));

        // Give 3 seconds for alias creation to propagate.
        sleepIfRunningAgainstService(3000);

        SearchServiceStatistics serviceStatistics = indexClient.getServiceStatistics();
        assertEquals(3, serviceStatistics.getCounters().getAliasCounter().getUsage());
    }

    @Test
    public void canInspectAliasUsageInServiceStatisticsAsync() {
        indexAsyncClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1))).block();
        indexAsyncClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1))).block();
        indexAsyncClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(HOTEL_INDEX_NAME1))).block();

        // Give 3 seconds for alias creation to propagate.
        sleepIfRunningAgainstService(3000);

        StepVerifier.create(indexAsyncClient.getServiceStatistics())
            .assertNext(serviceStatistics -> assertEquals(3,
                serviceStatistics.getCounters().getAliasCounter().getUsage()))
            .verifyComplete();
    }
}
