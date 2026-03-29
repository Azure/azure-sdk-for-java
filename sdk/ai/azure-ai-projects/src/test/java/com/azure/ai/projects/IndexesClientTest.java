// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.AzureAISearchIndex;
import com.azure.ai.projects.models.AIProjectIndex;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class IndexesClientTest extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListIndexes(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        IndexesClient indexesClient = getIndexesClient(httpClient, serviceVersion);

        // Verify that listing indexes returns results
        Iterable<AIProjectIndex> indexes = indexesClient.listLatestIndexVersions();
        Assertions.assertNotNull(indexes);

        // Verify that at least one index can be retrieved if available
        boolean hasAtLeastOneIndex = false;
        for (AIProjectIndex index : indexes) {
            hasAtLeastOneIndex = true;
            Assertions.assertNotNull(index);
            // Some indexes may have partial data - just validate non-null index object
            System.out.println("  Found index: name=" + index.getName() + " version=" + index.getVersion() + " type="
                + index.getType());
        }

        System.out.println(
            "Index list retrieved successfully" + (hasAtLeastOneIndex ? " with at least one index" : " (empty list)"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListIndexVersions(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        IndexesClient indexesClient = getIndexesClient(httpClient, serviceVersion);

        // First, get the name of an existing index from the list
        Iterable<AIProjectIndex> indexes = indexesClient.listLatestIndexVersions();
        String indexName = null;
        for (AIProjectIndex index : indexes) {
            indexName = index.getName();
            break;
        }

        if (indexName == null) {
            System.out.println("No indexes available - skipping version listing test");
            return;
        }

        // Verify that listing index versions returns results
        Iterable<AIProjectIndex> indexVersions = indexesClient.listIndexVersions(indexName);
        Assertions.assertNotNull(indexVersions);

        boolean hasAtLeastOneVersion = false;
        for (AIProjectIndex index : indexVersions) {
            hasAtLeastOneVersion = true;
            assertValidIndex(index, indexName, null);
            break;
        }

        Assertions.assertTrue(hasAtLeastOneVersion, "Expected at least one version for index: " + indexName);
        System.out.println("Index versions for '" + indexName + "' retrieved successfully");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetIndex(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        IndexesClient indexesClient = getIndexesClient(httpClient, serviceVersion);

        // First, get the name and version of an existing index from the list
        Iterable<AIProjectIndex> indexes = indexesClient.listLatestIndexVersions();
        String indexName = null;
        String indexVersion = null;
        for (AIProjectIndex index : indexes) {
            indexName = index.getName();
            indexVersion = index.getVersion();
            break;
        }

        if (indexName == null) {
            System.out.println("No indexes available - skipping get test");
            return;
        }

        AIProjectIndex index = indexesClient.getIndexVersion(indexName, indexVersion);

        // Verify the index properties
        assertValidIndex(index, indexName, indexVersion);

        System.out
            .println("Index retrieved successfully: " + index.getName() + " (version " + index.getVersion() + ")");
        System.out.println("Index type: " + index.getType());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateOrUpdateIndex(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        IndexesClient indexesClient = getIndexesClient(httpClient, serviceVersion);

        String indexName = "java-sdk-test-index";
        String indexVersion = "1";
        String aiSearchConnectionName
            = Configuration.getGlobalConfiguration().get("TEST_AI_SEARCH_CONNECTION_NAME", "test-search-connection");
        String aiSearchIndexName = "java-sdk-test-search-index";

        AzureAISearchIndex searchIndex
            = new AzureAISearchIndex().setConnectionName(aiSearchConnectionName).setIndexName(aiSearchIndexName);

        AIProjectIndex createdIndex = indexesClient.createOrUpdateIndexVersion(indexName, indexVersion, searchIndex);

        // Verify the created/updated index
        assertValidIndex(createdIndex, indexName, indexVersion);
        Assertions.assertInstanceOf(AzureAISearchIndex.class, createdIndex);
        AzureAISearchIndex createdSearchIndex = (AzureAISearchIndex) createdIndex;
        Assertions.assertEquals(aiSearchIndexName, createdSearchIndex.getIndexName());

        System.out.println("Index created/updated successfully: " + createdIndex.getName() + " (version "
            + createdIndex.getVersion() + ")");

        // Clean up
        indexesClient.deleteIndexVersion(indexName, indexVersion);
    }
}
