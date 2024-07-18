// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.batch;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.health.deidentification.DeidentificationAsyncClient;
import com.azure.health.deidentification.models.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncJobOperationsTest extends BatchOperationTestBase {
    protected DeidentificationAsyncClient deidentificationAsyncClient;
    private static final String OUTPUT_FOLDER = "_output";

    @Test
    void testCreateJobReturnsExpected() {
        deidentificationAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded006q";

        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = getStorageAccountSASUri();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageAccountSASUri, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationJob result = deidentificationAsyncClient.beginCreateJob(jobName, job).getSyncPoller().waitUntil(LongRunningOperationStatus.NOT_STARTED).getValue();

        assertNotNull(result);
        assertEquals(jobName, result.getName());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getLastUpdatedAt());
        assertNull(result.getStartedAt());
        assertEquals(JobStatus.NOT_STARTED, result.getStatus());
        assertNull(result.getError());
        assertNull(result.getRedactionFormat());
        assertNull(result.getSummary());
        assertEquals(inputPrefix, result.getSourceLocation().getPrefix());
        assertTrue(result.getSourceLocation().getLocation().contains("blob.core.windows.net"));
        assertEquals(OUTPUT_FOLDER, result.getTargetLocation().getPrefix());
        assertTrue(result.getTargetLocation().getLocation().contains("blob.core.windows.net"));
    }

    @Test
    void testCreateThenListReturnsExpected() {
        deidentificationAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded007q";

        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = getStorageAccountSASUri();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageAccountSASUri, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        deidentificationAsyncClient.beginCreateJob(jobName, job).getSyncPoller().waitUntil(LongRunningOperationStatus.NOT_STARTED);

        PagedFlux<DeidentificationJob> jobs = deidentificationAsyncClient.listJobs();
        jobs.byPage() // Retrieves Flux<PagedResponse<T>>, where each PagedResponse<T> represents a page
            .flatMap(page -> Flux.fromIterable(page.getElements())) // Converts each page into a Flux<T> of its items
            .filter(item -> item.getName().equals(jobName)).next() // Gets the first item that matches the condition
            .subscribe(item -> {
                assertNotNull(item.getCreatedAt());
                assertNotNull(item.getLastUpdatedAt());
                assertNull(item.getStartedAt());
                assertEquals(JobStatus.NOT_STARTED, item.getStatus());
                assertNull(item.getError());
                assertNull(item.getRedactionFormat());
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
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded008q";

        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = getStorageAccountSASUri();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageAccountSASUri, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        PollerFlux<DeidentificationJob, DeidentificationJob> poller = setPlaybackPollerFluxPollInterval(deidentificationAsyncClient.beginCreateJob(jobName, job));
        DeidentificationJob result = poller.getSyncPoller().waitForCompletion().getValue();

        assertEquals(JobStatus.SUCCEEDED, result.getStatus());

        PagedFlux<DocumentDetails> reports = deidentificationAsyncClient.listJobDocuments(jobName);

        reports.byPage() // Retrieves Flux<PagedResponse<T>>, where each PagedResponse<T> represents a page
            .flatMap(page -> Flux.fromIterable(page.getElements())) // Converts each page into a Flux<T> of its items
            .subscribe(item -> {
                assertEquals(item.getStatus(), OperationState.SUCCEEDED);
                assertTrue(item.getOutput().getPath().startsWith(OUTPUT_FOLDER));
                assertEquals(item.getId().length(), 36);
            });
    }

    @Test
    void testJobE2ECancelJobThenDeleteJobDeletesJob() {
        deidentificationAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded009q";

        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = getStorageAccountSASUri();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageAccountSASUri, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationJob result = deidentificationAsyncClient.beginCreateJob(jobName, job).getSyncPoller().waitUntil(LongRunningOperationStatus.NOT_STARTED).getValue();


        DeidentificationJob cancelledJob = deidentificationAsyncClient.cancelJob(jobName).block();

        assertEquals(JobStatus.CANCELED, cancelledJob.getStatus());

        deidentificationAsyncClient.deleteJob(jobName).block();

        assertThrows(ResourceNotFoundException.class, () -> {
            deidentificationAsyncClient.getJob(jobName).block();
        });
    }

    @Test
    void testJobE2ECannotAccessStorageCreateJobFails() {
        deidentificationAsyncClient = getDeidServicesClientBuilder().buildAsyncClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded0010q";

        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = "FAKE_STORAGE_ACCOUNT";
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageAccountSASUri, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        assertThrows(HttpResponseException.class, () -> deidentificationAsyncClient.beginCreateJob(jobName, job).getSyncPoller().waitUntil(LongRunningOperationStatus.NOT_STARTED));
    }
}
