// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchAlias;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SearchAlias}-based operations.
 */
public class SearchAliasTests extends SearchTestBase {
    private SearchIndexClient indexClient;
    private SearchIndexAsyncClient indexAsyncClient;
    private SearchClient searchClient;
    private SearchAsyncClient searchAsyncClient;

    @BeforeAll
    public static void beforeAll() {
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
            // Alias changes take up to 10 seconds to propagate.
            TestHelpers.sleepIfRunningAgainstService(10000);
        }
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();

        SearchIndexClientBuilder indexClientBuilder = getSearchIndexClientBuilder();
        indexClient = indexClientBuilder.buildClient();
        indexAsyncClient = indexClientBuilder.buildAsyncClient();

        SearchClientBuilder searchClientBuilder = setupSearchClient(this::createHotelIndex);
        searchClient = searchClientBuilder.buildClient();
        searchAsyncClient = searchClientBuilder.buildAsyncClient();
    }

    private SearchClientBuilder setupSearchClient(Supplier<String> indexSupplier) {
        String indexName = indexSupplier.get();

        return getSearchClientBuilder(indexName);
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
            // Alias changes take up to 10 seconds to propagate.
            sleepIfRunningAgainstService(10000);
        }

        for (String indexName : cleanupClient.listIndexNames()) {
            cleanupClient.deleteIndex(indexName);
        }
    }

    @Test
    public void canCreateAliasSync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));
        SearchAlias searchAlias = indexClient.createAlias(expectedAlias);

        assertEquals(expectedAlias.getName(), searchAlias.getName());
        assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());
    }

    @Test
    public void canCreateAliasAsync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));

        StepVerifier.create(indexAsyncClient.createAlias(expectedAlias))
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
            Collections.singletonList(searchClient.getIndexName()))));
    }

    @Test
    public void cannotCreateAliasWithInvalidNameAsync() {
        StepVerifier.create(indexAsyncClient.createAlias(new SearchAlias("--invalid--alias-name",
                Collections.singletonList(searchClient.getIndexName()))))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void cannotCreateMultipleAliasesWithTheSameNameSync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));
        SearchAlias searchAlias = indexClient.createAlias(expectedAlias);

        assertEquals(expectedAlias.getName(), searchAlias.getName());
        assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());

        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias(expectedAlias.getName(),
            Collections.singletonList(searchClient.getIndexName()))));
    }

    @Test
    public void cannotCreateMultipleAliasesWithTheSameNameAsync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));

        StepVerifier.create(indexAsyncClient.createAlias(expectedAlias))
            .assertNext(searchAlias -> {
                assertEquals(expectedAlias.getName(), searchAlias.getName());
                assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());
            })
            .verifyComplete();

        StepVerifier.create(indexAsyncClient.createAlias(new SearchAlias(expectedAlias.getName(),
                Collections.singletonList(searchClient.getIndexName()))))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void cannotCreateAliasWithMultipleIndexesSync() {
        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias("my-alias",
            Arrays.asList(searchClient.getIndexName(), createHotelIndex()))));
    }

    @Test
    public void cannotCreateAliasWithMultipleIndexesAsync() {
        StepVerifier.create(indexAsyncClient.createAlias(new SearchAlias("my-alias",
                Arrays.asList(searchClient.getIndexName(), createHotelIndex()))))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void canCreateMultipleAliasesReferencingTheSameIndexSync() {
        SearchAlias firstExpectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));
        SearchAlias searchAlias = indexClient.createAlias(firstExpectedAlias);

        assertEquals(firstExpectedAlias.getName(), searchAlias.getName());
        assertEquals(firstExpectedAlias.getIndexes(), searchAlias.getIndexes());

        SearchAlias secondExpectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));
        searchAlias = indexClient.createAlias(secondExpectedAlias);

        assertEquals(secondExpectedAlias.getName(), searchAlias.getName());
        assertEquals(secondExpectedAlias.getIndexes(), searchAlias.getIndexes());
    }

    @Test
    public void canCreateMultipleAliasesReferencingTheSameIndexAsync() {
        SearchAlias firstExpectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));

        StepVerifier.create(indexAsyncClient.createAlias(firstExpectedAlias))
            .assertNext(searchAlias -> {
                assertEquals(firstExpectedAlias.getName(), searchAlias.getName());
                assertEquals(firstExpectedAlias.getIndexes(), searchAlias.getIndexes());
            })
            .verifyComplete();

        SearchAlias secondExpectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));

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
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())));

        SearchAlias expectedUpdatedAlias = new SearchAlias(aliasName, Collections.singletonList(createHotelIndex()));
        SearchAlias updatedAlias = indexClient.createOrUpdateAlias(expectedUpdatedAlias);

        assertEquals(expectedUpdatedAlias.getName(), updatedAlias.getName());
        assertEquals(expectedUpdatedAlias.getIndexes(), updatedAlias.getIndexes());
    }

    @Test
    public void canUpdateAliasAfterCreationAsync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())))
            .block();

        SearchAlias expectedUpdatedAlias = new SearchAlias(aliasName, Collections.singletonList(createHotelIndex()));

        StepVerifier.create(indexAsyncClient.createOrUpdateAlias(expectedUpdatedAlias))
            .assertNext(updatedAlias -> {
                assertEquals(expectedUpdatedAlias.getName(), updatedAlias.getName());
                assertEquals(expectedUpdatedAlias.getIndexes(), updatedAlias.getIndexes());
            })
            .verifyComplete();
    }

    // @Test
    public void cannotUpdateAliasWithDifferentETag() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())));

        SearchAlias expectedUpdatedAlias = new SearchAlias(aliasName, Collections.singletonList(createHotelIndex()));
        indexClient.createOrUpdateAlias(expectedUpdatedAlias);

        // Alias changes take up to 10 seconds to propagate.
        TestHelpers.sleepIfRunningAgainstService(10000);

        assertThrows(HttpResponseException.class, () -> indexClient.createOrUpdateAliasWithResponse(
            new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())), true, Context.NONE));
    }

    @Test
    public void canRetrieveAliasSync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));
        indexClient.createAlias(expectedAlias);

        SearchAlias actualAlias = indexClient.getAlias(expectedAlias.getName());

        assertEquals(expectedAlias.getName(), actualAlias.getName());
        assertEquals(expectedAlias.getIndexes(), actualAlias.getIndexes());
    }

    @Test
    public void canRetrieveAliasAsync() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));
        indexAsyncClient.createAlias(expectedAlias).block();

        StepVerifier.create(indexAsyncClient.getAlias(expectedAlias.getName()))
            .assertNext(actualAlias -> {
                assertEquals(expectedAlias.getName(), actualAlias.getName());
                assertEquals(expectedAlias.getIndexes(), actualAlias.getIndexes());
            })
            .verifyComplete();
    }

    @Test
    public void canDeleteAliasSync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())));

        assertDoesNotThrow(() -> indexClient.deleteAlias(aliasName));

        // Wait for 1 second for the alias to update
        TestHelpers.sleepIfRunningAgainstService(1000);

        assertThrows(HttpResponseException.class, () -> indexClient.getIndex(aliasName));
    }

    @Test
    public void canDeleteAliasAsync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())))
            .block();

        StepVerifier.create(indexAsyncClient.deleteAlias(aliasName)).verifyComplete();

        // Wait for 1 second for the alias to update
        TestHelpers.sleepIfRunningAgainstService(1000);

        StepVerifier.create(indexAsyncClient.getIndex(aliasName)).verifyError(HttpResponseException.class);
    }

    // @Test
    public void cannotDeleteAliasWithDifferentETag() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())));

        indexClient.createOrUpdateAlias(new SearchAlias(aliasName, Collections.singletonList(createHotelIndex())));

        // Alias changes take up to 10 seconds to propagate.
        TestHelpers.sleepIfRunningAgainstService(10000);

        assertThrows(HttpResponseException.class, () -> indexClient.deleteAliasWithResponse(
            new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())), true, Context.NONE));
    }

    @Test
    public void cannotDeleteIndexWithAliasSync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())));

        // Alias changes take up to 10 seconds to propagate.
        TestHelpers.sleepIfRunningAgainstService(10000);

        assertThrows(HttpResponseException.class, () -> indexClient.deleteIndex(searchClient.getIndexName()));
        assertDoesNotThrow(() -> indexClient.getIndex(searchClient.getIndexName()));
    }

    @Test
    public void cannotDeleteIndexWithAliasAsync() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())))
            .block();

        // Alias changes take up to 10 seconds to propagate.
        TestHelpers.sleepIfRunningAgainstService(10000);

        StepVerifier.create(indexAsyncClient.deleteIndex(searchAsyncClient.getIndexName()))
            .verifyError(HttpResponseException.class);

        StepVerifier.create(indexAsyncClient.getIndex(searchAsyncClient.getIndexName()))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void canListAliasesSync() {
        String firstAliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(firstAliasName,
            Collections.singletonList(searchClient.getIndexName())));

        String secondAliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(secondAliasName,
            Collections.singletonList(searchClient.getIndexName())));

        String thirdAliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(thirdAliasName,
            Collections.singletonList(searchClient.getIndexName())));

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
            Collections.singletonList(searchClient.getIndexName()))).block();

        String secondAliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(secondAliasName,
            Collections.singletonList(searchClient.getIndexName()))).block();

        String thirdAliasName = testResourceNamer.randomName("my-alias", 32);
        indexAsyncClient.createAlias(new SearchAlias(thirdAliasName,
            Collections.singletonList(searchClient.getIndexName()))).block();

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
            Collections.singletonList(searchClient.getIndexName())));
        indexClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName())));
        indexClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName())));

        SearchServiceStatistics serviceStatistics = indexClient.getServiceStatistics();
        assertEquals(3, serviceStatistics.getCounters().getAliasCounter().getUsage());
    }

    @Test
    public void canInspectAliasUsageInServiceStatisticsAsync() {
        indexAsyncClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()))).block();
        indexAsyncClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()))).block();
        indexAsyncClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()))).block();

        StepVerifier.create(indexAsyncClient.getServiceStatistics())
            .assertNext(serviceStatistics -> assertEquals(3,
                serviceStatistics.getCounters().getAliasCounter().getUsage()))
            .verifyComplete();
    }
}
