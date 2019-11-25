// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestResourceNamer;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionResult;
import com.azure.search.models.IndexerExecutionStatus;
import com.azure.search.models.IndexerStatus;
import com.azure.search.models.IndexingSchedule;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * This test will run only in PLAYBACK mode (even if test mode is set to recording).
 * injects mock status query , which results in service
 * returning a well-known mock response
 */
public class IndexerAsyncTest extends SearchServiceTestBase {

    private SearchServiceAsyncClient client;

    @Override
    public void setupTest() {
        final String testName = getTestName();

        try {
            interceptorManager = new InterceptorManager(testName, TestMode.PLAYBACK);
        } catch (IOException e) {
            Assert.fail();
        }
        testResourceNamer = new TestResourceNamer(testName, TestMode.PLAYBACK, interceptorManager.getRecordedData());

        beforeTest();
    }

    @Override
    protected void beforeTest() {
        client = getSearchServiceClientBuilder().buildAsyncClient();
    }

    @Override
    protected void afterTest() {
    }

    @Override
    protected SearchServiceClientBuilder getSearchServiceClientBuilder() {
        endpoint = String.format("https://%s.%s", "fake-service", "search.windows.net");
        return new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(interceptorManager.getPlaybackClient());
    }

    @Test
    public void canRunIndexerAndGetIndexerStatus() {

        // create an indexer
        Indexer indexer =
            createTestIndexer("indexer")
                .setDataSourceName(SQL_DATASOURCE_NAME)
                .setIsDisabled(false);
        client.createOrUpdateIndexer(indexer);

        StepVerifier.create(client.getIndexerStatus(indexer.getName()))
            .assertNext(indexerExecutionInfo -> {
                Assert.assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
            })
            .verifyComplete();

        StepVerifier.create(client.runIndexerWithResponse(indexer.getName(),  null, null))
            .assertNext(response -> {
                Assert.assertEquals(HttpStatus.SC_ACCEPTED, response.getStatusCode());
            })
            .verifyComplete();

        StepVerifier.create(client.getIndexerStatus(indexer.getName()))
            .assertNext(indexerExecutionInfo -> {
                Assert.assertEquals(IndexerStatus.RUNNING, indexerExecutionInfo.getStatus());
                Assert.assertEquals(IndexerExecutionStatus.IN_PROGRESS, indexerExecutionInfo.getLastResult().getStatus());
                Assert.assertEquals(2, indexerExecutionInfo.getExecutionHistory().size());

                IndexerExecutionResult newestResult = indexerExecutionInfo.getExecutionHistory().get(0);
                IndexerExecutionResult oldestResult = indexerExecutionInfo.getExecutionHistory().get(1);

                Assert.assertEquals(IndexerExecutionStatus.SUCCESS, newestResult.getStatus());
                Assert.assertEquals(11, newestResult.getItemCount());
                Assert.assertEquals(0, newestResult.getFailedItemCount());
                assertStartAndEndTimeValid(newestResult);

                Assert.assertEquals(IndexerExecutionStatus.TRANSIENT_FAILURE, oldestResult.getStatus());
                Assert.assertEquals("Document key cannot be missing or empty", oldestResult.getErrorMessage());
                assertStartAndEndTimeValid(newestResult);
            })
            .verifyComplete();
    }

    private void assertStartAndEndTimeValid(IndexerExecutionResult result) {
        Assert.assertTrue(result.getStartTime() != null);
        Assert.assertNotEquals(OffsetDateTime.now(), result.getStartTime());
        Assert.assertTrue(result.getEndTime() != null);
        Assert.assertNotEquals(OffsetDateTime.now(), result.getEndTime());
    }

    private Indexer createTestIndexer(String indexerName) {
        return new Indexer()
            .setName(indexerName)
            .setTargetIndexName("indexforindexers")
            .setSchedule(new IndexingSchedule().setInterval(Duration.ofDays(1)));
    }


}

