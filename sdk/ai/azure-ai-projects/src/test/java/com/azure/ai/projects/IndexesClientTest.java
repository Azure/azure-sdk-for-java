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

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class IndexesClientTest extends ClientTestBase {

    private AIProjectClientBuilder clientBuilder;
    private IndexesClient indexesClient;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        indexesClient = clientBuilder.buildIndexesClient();
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
    public void testListIndexes(HttpClient httpClient) {
        setup(httpClient);

        // Verify that listing indexes returns results
        Iterable<Index> indexes = indexesClient.listLatest();
        Assertions.assertNotNull(indexes);

        // Verify that at least one index can be retrieved if available
        boolean hasAtLeastOneIndex = false;
        for (Index index : indexes) {
            hasAtLeastOneIndex = true;
            assertValidIndex(index, null, null);
            break;
        }

        // Note: This test will pass even if there are no indexes,
        // as we're only verifying the API works correctly
        System.out.println(
            "Index list retrieved successfully" + (hasAtLeastOneIndex ? " with at least one index" : " (empty list)"));
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListIndexVersions(HttpClient httpClient) {
        setup(httpClient);

        String indexName = Configuration.getGlobalConfiguration().get("TEST_INDEX_NAME", "test-index");

        try {
            // Verify that listing index versions returns results
            Iterable<Index> indexVersions = indexesClient.listVersions(indexName);
            Assertions.assertNotNull(indexVersions);

            // Verify that at least one index version can be retrieved if available
            boolean hasAtLeastOneVersion = false;
            for (Index index : indexVersions) {
                hasAtLeastOneVersion = true;
                assertValidIndex(index, indexName, null);
                break;
            }

            System.out.println("Index versions for '" + indexName + "' retrieved successfully"
                + (hasAtLeastOneVersion ? " with at least one version" : " (empty list)"));
        } catch (Exception e) {
            // If the index doesn't exist, this will throw a ResourceNotFoundException
            // We'll handle this case by printing a message and passing the test
            System.out.println("Index not found for version listing: " + indexName);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetIndex(HttpClient httpClient) {
        setup(httpClient);

        String indexName = Configuration.getGlobalConfiguration().get("TEST_INDEX_NAME", "test-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("TEST_INDEX_VERSION", "1.0");

        try {
            Index index = indexesClient.getVersion(indexName, indexVersion);

            // Verify the index properties
            assertValidIndex(index, indexName, indexVersion);

            System.out
                .println("Index retrieved successfully: " + index.getName() + " (version " + index.getVersion() + ")");
            System.out.println("Index type: " + index.getType());
        } catch (Exception e) {
            // If the index doesn't exist, this will throw a ResourceNotFoundException
            // We'll handle this case by printing a message and passing the test
            System.out.println("Index not found: " + indexName + " (version " + indexVersion + ")");
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateOrUpdateIndex(HttpClient httpClient) {
        setup(httpClient);

        // Configuration for creating/updating an index
        String indexName = Configuration.getGlobalConfiguration().get("TEST_INDEX_NAME", "test-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("TEST_INDEX_VERSION", "1.0");
        String aiSearchConnectionName
            = Configuration.getGlobalConfiguration().get("TEST_AI_SEARCH_CONNECTION_NAME", "test-search-connection");
        String aiSearchIndexName
            = Configuration.getGlobalConfiguration().get("TEST_AI_SEARCH_INDEX_NAME", "test-search-index");

        try {
            // Create an AzureAISearchIndex
            AzureAISearchIndex searchIndex
                = new AzureAISearchIndex().setConnectionName(aiSearchConnectionName).setIndexName(aiSearchIndexName);

            // Create or update the index
            Index createdIndex = indexesClient.createOrUpdate(indexName, indexVersion, searchIndex);

            // Verify the created/updated index
            assertValidIndex(createdIndex, indexName, indexVersion);

            // Verify it's the correct type
            Assertions.assertTrue(createdIndex instanceof AzureAISearchIndex);
            AzureAISearchIndex createdSearchIndex = (AzureAISearchIndex) createdIndex;
            Assertions.assertEquals(aiSearchConnectionName, createdSearchIndex.getConnectionName());
            Assertions.assertEquals(aiSearchIndexName, createdSearchIndex.getIndexName());

            System.out.println("Index created/updated successfully: " + createdIndex.getName() + " (version "
                + createdIndex.getVersion() + ")");
        } catch (Exception e) {
            System.out.println("Failed to create/update index: " + e.getMessage());
            throw e;
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testDeleteIndex(HttpClient httpClient) {
        setup(httpClient);

        String indexName = Configuration.getGlobalConfiguration().get("TEST_INDEX_NAME", "test-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("TEST_INDEX_VERSION", "1.0");

        try {
            // First verify the index exists
            Index index = indexesClient.getVersion(indexName, indexVersion);
            assertValidIndex(index, indexName, indexVersion);

            // Delete the index
            indexesClient.deleteVersion(indexName, indexVersion);

            // Try to get the deleted index - should throw ResourceNotFoundException
            try {
                Index deletedIndex = indexesClient.getVersion(indexName, indexVersion);
                Assertions.fail("Index should have been deleted but was found: " + deletedIndex.getName());
            } catch (Exception e) {
                // Expected exception
                Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
                System.out.println("Index successfully deleted: " + indexName + " (version " + indexVersion + ")");
            }
        } catch (Exception e) {
            // If the index doesn't exist already, this is fine for the test
            System.out.println("Index not found for deletion: " + indexName + " (version " + indexVersion + ")");
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }
}
