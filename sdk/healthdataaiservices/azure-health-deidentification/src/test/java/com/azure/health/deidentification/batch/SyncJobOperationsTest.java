// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.batch;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.health.deidentification.DeidentificationClient;
import com.azure.health.deidentification.models.DeidentificationJob;
import com.azure.health.deidentification.models.DeidentificationDocumentDetails;
import com.azure.health.deidentification.models.OperationState;
import com.azure.health.deidentification.models.DeidentificationOperationType;
import com.azure.health.deidentification.models.SourceStorageLocation;
import com.azure.health.deidentification.models.TargetStorageLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncJobOperationsTest extends BatchOperationTestBase {
    protected DeidentificationClient deidentificationClient;
    private static final String OUTPUT_FOLDER = "_output/";

    @Test
    void testCreateJobReturnsExpected() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-001r";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
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
        assertNull(result.getSummary());
        assertEquals(inputPrefix, result.getSourceLocation().getPrefix());
        assertTrue(result.getSourceLocation().getLocation().contains("blob.core.windows.net"));
        assertEquals(OUTPUT_FOLDER, result.getTargetLocation().getPrefix());
        assertTrue(result.getTargetLocation().getLocation().contains("blob.core.windows.net"));
    }

    @Test
    void testCreateThenListReturnsExpected() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-002r";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
            .waitUntil(LongRunningOperationStatus.NOT_STARTED)
            .getValue();

        PagedIterable<DeidentificationJob> jobs = deidentificationClient.listJobs();
        Iterator<DeidentificationJob> iterator = jobs.iterator();
        int jobsToLookThrough = 10;
        boolean jobFound = false;
        while (iterator.hasNext()) {
            DeidentificationJob currentJob = iterator.next();
            if (currentJob.getName().equals(jobName)) {
                jobFound = true;
                assertNotNull(currentJob.getCreatedAt());
                assertNotNull(currentJob.getLastUpdatedAt());
                assertNull(currentJob.getStartedAt());
                assertEquals(OperationState.NOT_STARTED, currentJob.getStatus());
                assertNull(currentJob.getError());
                assertEquals("en-US", currentJob.getCustomizations().getSurrogateLocale());
                assertEquals(inputPrefix, currentJob.getSourceLocation().getPrefix());
                assertTrue(currentJob.getSourceLocation().getLocation().contains("blob.core.windows.net"));
                assertEquals(OUTPUT_FOLDER, currentJob.getTargetLocation().getPrefix());
                assertTrue(currentJob.getTargetLocation().getLocation().contains("blob.core.windows.net"));
            }
            if (jobFound || --jobsToLookThrough <= 0) {
                break;
            }
        }
        assertTrue(jobFound, "Job not found in list of jobs.");
    }

    @Test
    void testJobE2EWaitUntilSuccess() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-003r";
        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation,
            new TargetStorageLocation(storageLocation, OUTPUT_FOLDER).setOverwrite(true));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        SyncPoller<DeidentificationJob, DeidentificationJob> poller
            = setPlaybackSyncPollerPollInterval(deidentificationClient.beginDeidentifyDocuments(jobName, job));
        DeidentificationJob result = poller.waitForCompletion().getValue();
        assertEquals(OperationState.SUCCEEDED, result.getStatus());

        PagedIterable<DeidentificationDocumentDetails> reports = deidentificationClient.listJobDocuments(jobName);
        Iterator<DeidentificationDocumentDetails> iterator = reports.iterator();
        int results = 0;
        while (iterator.hasNext()) {
            DeidentificationDocumentDetails currentReport = iterator.next();
            assertEquals(OperationState.SUCCEEDED, currentReport.getStatus());
            assertTrue(currentReport.getOutput().getLocation().contains(OUTPUT_FOLDER));
            assertEquals(36, currentReport.getId().length());
            results++;
        }
        assertEquals(3, results);
    }

    @Test
    void testJobE2ECancelJobThenDeleteJobDeletesJob() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-004r";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
            .waitUntil(LongRunningOperationStatus.NOT_STARTED)
            .getValue();
        assertEquals(OperationState.NOT_STARTED, result.getStatus());

        DeidentificationJob cancelledJob = deidentificationClient.cancelJob(jobName);
        assertEquals(OperationState.CANCELED, cancelledJob.getStatus());

        deidentificationClient.deleteJob(jobName);

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            deidentificationClient.getJob(jobName);
        });
        assertEquals(404, exception.getResponse().getStatusCode());

    }

    @Test
    void testJobE2ECannotAccessStorageCreateJobFails() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded8-005r";

        String inputPrefix = "example_patient_1";
        String storageLocation = "FAKE_STORAGE_ACCOUNT";
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        assertThrows(HttpResponseException.class, () -> deidentificationClient.beginDeidentifyDocuments(jobName, job)
            .waitUntil(LongRunningOperationStatus.NOT_STARTED));
    }
}
