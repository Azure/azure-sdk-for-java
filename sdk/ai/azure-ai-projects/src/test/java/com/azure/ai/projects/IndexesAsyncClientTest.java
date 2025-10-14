// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.AzureAISearchIndex;
import com.azure.ai.projects.models.Index;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class IndexesAsyncClientTest extends ClientTestBase {

    private AIProjectClientBuilder clientBuilder;
    private IndexesAsyncClient indexesAsyncClient;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        indexesAsyncClient = clientBuilder.buildIndexesAsyncClient();
    }

    /**
     * Helper method to verify an Index has valid properties.
     * @param index The index to validate
     * @param expectedName The expected name of the index, or null if no specific name is expected
     * @param expectedVersion The expected version of the index, or null if no specific version is expected
     */
    private void assertValidIndex(Index index, String expectedName, String expectedVersion) {
        Assertions.assertNotNull(index);
        Assertions.assertNotNull(index.getName());
        Assertions.assertNotNull(index.getVersion());
        Assertions.assertNotNull(index.getType());

        if (expectedName != null) {
            Assertions.assertEquals(expectedName, index.getName());
        }

        if (expectedVersion != null) {
            Assertions.assertEquals(expectedVersion, index.getVersion());
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListIndexesAsync(HttpClient httpClient) {
        setup(httpClient);

        // Collect indexes into a list for verification
        List<Index> indexList = new ArrayList<>();

        // Verify that listing indexes returns results
        StepVerifier.create(indexesAsyncClient.listLatestIndexVersions().doOnNext(index -> {
            indexList.add(index);
            assertValidIndex(index, null, null);
        })).expectComplete().verify(Duration.ofMinutes(1));

        System.out.println("Index list retrieved successfully"
            + (indexList.isEmpty() ? " (empty list)" : " with " + indexList.size() + " index(es)"));
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListIndexVersionsAsync(HttpClient httpClient) {
        setup(httpClient);

        String indexName = Configuration.getGlobalConfiguration().get("TEST_INDEX_NAME", "test-index");
        List<Index> versionList = new ArrayList<>();

        // Verify that listing index versions returns results or appropriate error
        StepVerifier.create(indexesAsyncClient.listIndexVersions(indexName).doOnNext(index -> {
            versionList.add(index);
            assertValidIndex(index, indexName, null);
        }).onErrorResume(e -> {
            // If the index doesn't exist, this will throw a ResourceNotFoundException
            // We'll handle this case by printing a message
            System.out.println("Index not found for version listing: " + indexName);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
            return Mono.empty();
        })).expectComplete().verify(Duration.ofMinutes(1));

        System.out.println("Index versions for '" + indexName + "' retrieved successfully"
            + (versionList.isEmpty() ? " (empty list)" : " with " + versionList.size() + " version(s)"));
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetIndexAsync(HttpClient httpClient) {
        setup(httpClient);

        String indexName = Configuration.getGlobalConfiguration().get("TEST_INDEX_NAME", "test-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("TEST_INDEX_VERSION", "1.0");

        StepVerifier.create(indexesAsyncClient.getIndexVersion(indexName, indexVersion).doOnNext(index -> {
            // Verify the index properties
            assertValidIndex(index, indexName, indexVersion);
            System.out
                .println("Index retrieved successfully: " + index.getName() + " (version " + index.getVersion() + ")");
            System.out.println("Index type: " + index.getType());
        }).onErrorResume(e -> {
            // If the index doesn't exist, this will throw a ResourceNotFoundException
            System.out.println("Index not found: " + indexName + " (version " + indexVersion + ")");
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
            return Mono.empty();
        })).expectComplete().verify(Duration.ofMinutes(1));
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateOrUpdateIndexAsync(HttpClient httpClient) {
        setup(httpClient);

        // Configuration for creating/updating an index
        String indexName = Configuration.getGlobalConfiguration().get("TEST_INDEX_NAME", "test-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("TEST_INDEX_VERSION", "1.0");
        String aiSearchConnectionName
            = Configuration.getGlobalConfiguration().get("TEST_AI_SEARCH_CONNECTION_NAME", "test-search-connection");
        String aiSearchIndexName
            = Configuration.getGlobalConfiguration().get("TEST_AI_SEARCH_INDEX_NAME", "test-search-index");

        // Create an AzureAISearchIndex
        AzureAISearchIndex searchIndex
            = new AzureAISearchIndex().setConnectionName(aiSearchConnectionName).setIndexName(aiSearchIndexName);

        StepVerifier.create(indexesAsyncClient.createOrUpdateIndexVersion(indexName, indexVersion, searchIndex)
            .doOnNext(createdIndex -> {
                // Verify the created/updated index
                assertValidIndex(createdIndex, indexName, indexVersion);

                // Verify it's the correct type
                Assertions.assertTrue(createdIndex instanceof AzureAISearchIndex);
                AzureAISearchIndex createdSearchIndex = (AzureAISearchIndex) createdIndex;
                Assertions.assertEquals(aiSearchConnectionName, createdSearchIndex.getConnectionName());
                Assertions.assertEquals(aiSearchIndexName, createdSearchIndex.getIndexName());

                System.out.println("Index created/updated successfully: " + createdIndex.getName() + " (version "
                    + createdIndex.getVersion() + ")");
            })).expectNextCount(1).expectComplete().verify(Duration.ofMinutes(1));
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testDeleteIndexAsync(HttpClient httpClient) {
        setup(httpClient);

        String indexName = Configuration.getGlobalConfiguration().get("TEST_INDEX_NAME", "test-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("TEST_INDEX_VERSION", "1.0");

        // First verify the index exists
        indexesAsyncClient.getIndexVersion(indexName, indexVersion)
            .doOnNext(index -> assertValidIndex(index, indexName, indexVersion))
            .flatMap(index -> indexesAsyncClient.deleteIndexVersion(indexName, indexVersion))
            .doOnSuccess(unused -> System.out.println("Index deletion request submitted"))
            .then(Mono.delay(Duration.ofSeconds(2))) // Give some time for the deletion to complete
            .then(indexesAsyncClient.getIndexVersion(indexName, indexVersion))
            .doOnNext(deletedIndex -> Assertions
                .fail("Index should have been deleted but was found: " + deletedIndex.getName()))
            .onErrorResume(e -> {
                // Expected exception
                Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
                System.out.println("Index successfully deleted: " + indexName + " (version " + indexVersion + ")");
                return Mono.empty();
            })
            .onErrorResume(e -> {
                // If the index doesn't exist already for the initial get, this is fine
                System.out.println("Index not found for deletion: " + indexName + " (version " + indexVersion + ")");
                Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
                return Mono.empty();
            })
            .block(Duration.ofMinutes(1));
    }
}
