// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.batch;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.health.deidentification.DeidentificationAsyncClient;
import com.azure.health.deidentification.models.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncJobOperationsTest extends BatchOperationTestBase {
    protected DeidentificationAsyncClient deidentificationAsyncClient;
    private static final String OUTPUT_FOLDER = "_output/";

    @Test
    void testCreateJobReturnsExpected() {
        deidentificationAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-006r";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        DeidentificationJob result = deidentificationAsyncClient.beginDeidentifyDocuments(jobName, job)
            .getSyncPoller()
            .waitUntil(LongRunningOperationStatus.NOT_STARTED)
            .getValue();

        assertNotNull(result);
        assertEquals(jobName, result.getName());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getLastUpdatedAt());
        assertNull(result.getStartedAt());
        assertEquals(OperationState.NOT_STARTED, result.getStatus());
        assertNull(result.getError());
        assertEquals("en-US", result.getCustomizations().getSurrogateLocale());
        assertEquals(inputPrefix, result.getSourceLocation().getPrefix());
        assertTrue(result.getSourceLocation().getLocation().contains("blob.core.windows.net"));
        assertEquals(OUTPUT_FOLDER, result.getTargetLocation().getPrefix());
        assertTrue(result.getTargetLocation().getLocation().contains("blob.core.windows.net"));
    }

    @Test
    void testCreateThenListReturnsExpected() {
        deidentificationAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-007r";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        deidentificationAsyncClient.beginDeidentifyDocuments(jobName, job)
            .getSyncPoller()
            .waitUntil(LongRunningOperationStatus.NOT_STARTED);

        PagedFlux<DeidentificationJob> jobs = deidentificationAsyncClient.listJobs();
        jobs.byPage() // Retrieves Flux<PagedResponse<T>>, where each PagedResponse<T> represents a page
            .flatMap(page -> Flux.fromIterable(page.getElements())) // Converts each page into a Flux<T> of its items
            .filter(item -> item.getName().equals(jobName))
            .next() // Gets the first item that matches the condition
            .subscribe(item -> {
                assertNotNull(item.getCreatedAt());
                assertNotNull(item.getLastUpdatedAt());
                assertNull(item.getStartedAt());
                assertEquals(OperationState.NOT_STARTED, item.getStatus());
                assertNull(item.getError());
                assertEquals("en-US", item.getCustomizations().getSurrogateLocale());
                assertNull(item.getSummary());
                assertEquals(inputPrefix, item.getSourceLocation().getPrefix());
                assertTrue(item.getSourceLocation().getLocation().contains("blob.core.windows.net"));
                assertEquals(OUTPUT_FOLDER, item.getTargetLocation().getPrefix());
                assertTrue(item.getTargetLocation().getLocation().contains("blob.core.windows.net"));
            });
    }

    @Test
    void testJobE2EWaitUntilSuccess() {
        deidentificationAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-008r";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation,
            new TargetStorageLocation(storageLocation, OUTPUT_FOLDER).setOverwrite(true));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        PollerFlux<DeidentificationJob, DeidentificationJob> poller
            = setPlaybackPollerFluxPollInterval(deidentificationAsyncClient.beginDeidentifyDocuments(jobName, job));
        DeidentificationJob result = poller.getSyncPoller().waitForCompletion().getValue();

        assertEquals(OperationState.SUCCEEDED, result.getStatus());

        PagedFlux<BinaryData> reports = deidentificationAsyncClient.listJobDocuments(jobName,
            new RequestOptions().addQueryParam("maxpagesize", String.valueOf(2)));
        Long count = reports.count().block();
        assertEquals(3, count);

        List<String> documentIdList = new ArrayList<>();
        StepVerifier.create(deidentificationAsyncClient
            .listJobDocuments(jobName, new RequestOptions().addQueryParam("maxpagesize", String.valueOf(2)))
            .byPage()
            .take(2)).thenConsumeWhile(documentDetailsPagedResponse -> {
                documentDetailsPagedResponse.getValue().forEach(detailsBinary -> {
                    DeidentificationDocumentDetails details
                        = detailsBinary.toObject(DeidentificationDocumentDetails.class);
                    assertFalse(documentIdList.contains(details.getId()));
                    documentIdList.add(details.getId());
                    assertEquals(OperationState.SUCCEEDED, details.getStatus());
                    assertNotNull(details.getOutput());
                    assertTrue(details.getOutput().getLocation().contains(OUTPUT_FOLDER));
                    assertEquals(36, details.getId().length());
                });
                return true;
            }).expectComplete().verify(Duration.ofSeconds(100));
    }

    @Test
    void testJobE2ECancelJobThenDeleteJobDeletesJob() {
        deidentificationAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-009r";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        DeidentificationJob result = deidentificationAsyncClient.beginDeidentifyDocuments(jobName, job)
            .getSyncPoller()
            .waitUntil(LongRunningOperationStatus.NOT_STARTED)
            .getValue();

        DeidentificationJob cancelledJob = deidentificationAsyncClient.cancelJob(jobName).block();

        assertEquals(OperationState.CANCELED, cancelledJob.getStatus());

        deidentificationAsyncClient.deleteJob(jobName).block();

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            deidentificationAsyncClient.getJob(jobName).block();
        });
        assertEquals(404, exception.getResponse().getStatusCode());
    }

    @Test
    void testJobE2ECannotAccessStorageCreateJobFails() {
        deidentificationAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-0010r";

        String inputPrefix = "example_patient_1";
        String storageLocation = "FAKE_STORAGE_ACCOUNT";
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        assertThrows(HttpResponseException.class,
            () -> deidentificationAsyncClient.beginDeidentifyDocuments(jobName, job)
                .getSyncPoller()
                .waitUntil(LongRunningOperationStatus.NOT_STARTED));
    }
}
