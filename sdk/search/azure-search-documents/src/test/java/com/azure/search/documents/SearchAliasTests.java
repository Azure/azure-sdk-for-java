// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchAlias;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    private SearchClient searchClient;

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
        indexClient = getSearchIndexClientBuilder().buildClient();
        searchClient = setupSearchClient(this::createHotelIndex);
    }

    private SearchClient setupSearchClient(Supplier<String> indexSupplier) {
        String indexName = indexSupplier.get();

        return getSearchClientBuilder(indexName).buildClient();
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
    public void canCreateAlias() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));
        SearchAlias searchAlias = indexClient.createAlias(expectedAlias);

        assertEquals(expectedAlias.getName(), searchAlias.getName());
        assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());
    }

    @Test
    public void cannotCreateAliasOnNonExistentIndex() {
        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias("my-alias",
            Collections.singletonList("index-that-does-not-exist"))));
    }

    @Test
    public void cannotCreateAliasWithInvalidName() {
        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias("--invalid--alias-name",
            Collections.singletonList(searchClient.getIndexName()))));
    }

    @Test
    public void cannotCreateMultipleAliasesWithTheSameName() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));
        SearchAlias searchAlias = indexClient.createAlias(expectedAlias);

        assertEquals(expectedAlias.getName(), searchAlias.getName());
        assertEquals(expectedAlias.getIndexes(), searchAlias.getIndexes());

        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias(expectedAlias.getName(),
            Collections.singletonList(searchClient.getIndexName()))));
    }

    @Test
    public void cannotCreateAliasWithMultipleIndexes() {
        assertThrows(HttpResponseException.class, () -> indexClient.createAlias(new SearchAlias("my-alias",
            Arrays.asList(searchClient.getIndexName(), createHotelIndex()))));
    }

    @Test
    public void canCreateMultipleAliasesReferencingTheSameIndex() {
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
    public void canUpdateAliasAfterCreation() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())));

        SearchAlias expectedUpdatedAlias = new SearchAlias(aliasName, Collections.singletonList(createHotelIndex()));
        SearchAlias updatedAlias = indexClient.createOrUpdateAlias(expectedUpdatedAlias);

        assertEquals(expectedUpdatedAlias.getName(), updatedAlias.getName());
        assertEquals(expectedUpdatedAlias.getIndexes(), updatedAlias.getIndexes());
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
    public void canRetrieveAlias() {
        SearchAlias expectedAlias = new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName()));
        indexClient.createAlias(expectedAlias);

        SearchAlias actualAlias = indexClient.getAlias(expectedAlias.getName());

        assertEquals(expectedAlias.getName(), actualAlias.getName());
        assertEquals(expectedAlias.getIndexes(), actualAlias.getIndexes());
    }

    @Test
    public void canDeleteAlias() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())));

        assertDoesNotThrow(() -> indexClient.deleteIndex(aliasName));
        assertThrows(HttpResponseException.class, () -> indexClient.getIndex(aliasName));
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
    public void cannotDeleteIndexWithAlias() {
        String aliasName = testResourceNamer.randomName("my-alias", 32);
        indexClient.createAlias(new SearchAlias(aliasName, Collections.singletonList(searchClient.getIndexName())));

        // Alias changes take up to 10 seconds to propagate.
        TestHelpers.sleepIfRunningAgainstService(10000);

        assertThrows(HttpResponseException.class, () -> indexClient.deleteIndex(searchClient.getIndexName()));
        assertDoesNotThrow(() -> indexClient.getIndex(searchClient.getIndexName()));
    }

    @Test
    public void canListAliases() {
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
    public void canInspectAliasUsageInServiceStatistics() {
        indexClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName())));
        indexClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName())));
        indexClient.createAlias(new SearchAlias(testResourceNamer.randomName("my-alias", 32),
            Collections.singletonList(searchClient.getIndexName())));

        SearchServiceStatistics serviceStatistics = indexClient.getServiceStatistics();
        assertEquals(3, serviceStatistics.getCounters().getAliasCounter().getUsage());
    }
}
