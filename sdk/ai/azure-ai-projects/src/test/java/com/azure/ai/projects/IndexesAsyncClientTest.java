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
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class IndexesAsyncClientTest extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListIndexesAsync(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        IndexesAsyncClient indexesAsyncClient = getIndexesAsyncClient(httpClient, serviceVersion);

        // Collect indexes into a list for verification
        List<AIProjectIndex> indexList = new ArrayList<>();

        StepVerifier.create(indexesAsyncClient.listLatest().doOnNext(indexList::add))
            .thenConsumeWhile(index -> true)
            .verifyComplete();

        // Verify we got results
        Assertions.assertFalse(indexList.isEmpty(), "Expected at least one index");
        System.out.println("Index list retrieved with " + indexList.size() + " index(es)");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListIndexVersionsAsync(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        IndexesAsyncClient indexesAsyncClient = getIndexesAsyncClient(httpClient, serviceVersion);

        // Use an index name that we know exists from the list
        String indexName = indexesAsyncClient.listLatest()
            .filter(index -> index.getName() != null)
            .next()
            .map(AIProjectIndex::getName)
            .block(Duration.ofSeconds(30));

        if (indexName == null) {
            System.out.println("No indexes available - skipping version listing test");
            return;
        }

        List<AIProjectIndex> versionList = new ArrayList<>();

        StepVerifier.create(indexesAsyncClient.listVersions(indexName).doOnNext(versionList::add))
            .thenConsumeWhile(index -> true)
            .verifyComplete();

        Assertions.assertFalse(versionList.isEmpty(), "Expected at least one version for index: " + indexName);
        System.out
            .println("Index versions for '" + indexName + "' retrieved with " + versionList.size() + " version(s)");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetIndexAsync(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        IndexesAsyncClient indexesAsyncClient = getIndexesAsyncClient(httpClient, serviceVersion);

        // Use an index we know exists from the list
        AIProjectIndex existingIndex = indexesAsyncClient.listLatest()
            .filter(index -> index.getName() != null && index.getVersion() != null)
            .next()
            .block(Duration.ofSeconds(30));

        if (existingIndex == null) {
            System.out.println("No indexes available - skipping get test");
            return;
        }

        String indexName = existingIndex.getName();
        String indexVersion = existingIndex.getVersion();

        StepVerifier.create(indexesAsyncClient.getVersion(indexName, indexVersion)).assertNext(index -> {
            assertValidIndex(index, indexName, indexVersion);
            System.out
                .println("Index retrieved successfully: " + index.getName() + " (version " + index.getVersion() + ")");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateOrUpdateIndexAsync(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        IndexesAsyncClient indexesAsyncClient = getIndexesAsyncClient(httpClient, serviceVersion);

        String indexName = "java-sdk-test-index-async";
        String indexVersion = "1";
        String aiSearchConnectionName
            = Configuration.getGlobalConfiguration().get("TEST_AI_SEARCH_CONNECTION_NAME", "test-search-connection");
        String aiSearchIndexName = "java-sdk-test-search-index";

        AzureAISearchIndex searchIndex
            = new AzureAISearchIndex().setConnectionName(aiSearchConnectionName).setIndexName(aiSearchIndexName);

        StepVerifier.create(indexesAsyncClient.createOrUpdateVersion(indexName, indexVersion, searchIndex))
            .assertNext(createdIndex -> {
                assertValidIndex(createdIndex, indexName, indexVersion);
                Assertions.assertInstanceOf(AzureAISearchIndex.class, createdIndex);
                AzureAISearchIndex createdSearchIndex = (AzureAISearchIndex) createdIndex;
                Assertions.assertEquals(aiSearchIndexName, createdSearchIndex.getIndexName());
                System.out.println("Index created/updated successfully: " + createdIndex.getName());
            })
            .verifyComplete();

        // Clean up
        indexesAsyncClient.deleteVersion(indexName, indexVersion).block(Duration.ofSeconds(30));
    }
}
