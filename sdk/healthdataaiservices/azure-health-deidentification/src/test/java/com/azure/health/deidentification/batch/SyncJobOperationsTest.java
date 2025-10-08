// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.batch;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.health.deidentification.DeidentificationClient;
import com.azure.health.deidentification.models.DeidentificationJob;
import com.azure.health.deidentification.models.DeidentificationDocumentDetails;
import com.azure.health.deidentification.models.OperationStatus;
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
    private static final String OUTPUT_FOLDER = "_output";
    private static final String INPUT_PREFIX = "example_patient_1";

    @Test
    void testCreateJobReturnsExpected() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getJobName();
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, INPUT_PREFIX);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperationType(DeidentificationOperationType.SURROGATE);

        DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
            .waitUntil(LongRunningOperationStatus.NOT_STARTED)
            .getValue();

        assertNotNull(result);
        assertEquals(jobName, result.getJobName());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getLastUpdatedAt());
        assertNull(result.getStartedAt());
        assertEquals(OperationStatus.NOT_STARTED, result.getStatus());
        assertNull(result.getError());
        assertEquals("en-US", result.getCustomizations().getSurrogateLocale());
        assertNull(result.getSummary());
        assertEquals(INPUT_PREFIX, result.getSourceLocation().getPrefix());
        assertTrue(result.getSourceLocation().getLocation().contains("blob.core.windows.net"));
        assertEquals(OUTPUT_FOLDER, result.getTargetLocation().getPrefix());
        assertTrue(result.getTargetLocation().getLocation().contains("blob.core.windows.net"));
    }

    @Test
    void testCreateThenListReturnsExpected() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getJobName();
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, INPUT_PREFIX);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperationType(DeidentificationOperationType.SURROGATE);

        DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
            .waitUntil(LongRunningOperationStatus.NOT_STARTED)
            .getValue();

        PagedIterable<DeidentificationJob> jobs = deidentificationClient.listJobs();
        Iterator<DeidentificationJob> iterator = jobs.iterator();
        int jobsToLookThrough = 10;
        boolean jobFound = false;
        while (iterator.hasNext()) {
            DeidentificationJob currentJob = iterator.next();
            if (currentJob.getJobName().equals(jobName)) {
                jobFound = true;
                assertNotNull(currentJob.getCreatedAt());
                assertNotNull(currentJob.getLastUpdatedAt());
                assertNull(currentJob.getStartedAt());
                assertEquals(OperationStatus.NOT_STARTED, currentJob.getStatus());
                assertNull(currentJob.getError());
                assertEquals("en-US", currentJob.getCustomizations().getSurrogateLocale());
                assertEquals(INPUT_PREFIX, currentJob.getSourceLocation().getPrefix());
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
        String jobName = getJobName();
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, INPUT_PREFIX);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation,
            new TargetStorageLocation(storageLocation, OUTPUT_FOLDER).setOverwrite(true));
        job.setOperationType(DeidentificationOperationType.SURROGATE);

        SyncPoller<DeidentificationJob, DeidentificationJob> poller
            = setPlaybackSyncPollerPollInterval(deidentificationClient.beginDeidentifyDocuments(jobName, job));
        DeidentificationJob result = poller.waitForCompletion().getValue();
        assertEquals(OperationStatus.SUCCEEDED, result.getStatus());

        PagedIterable<DeidentificationDocumentDetails> reports = deidentificationClient.listJobDocuments(jobName);
        Iterator<DeidentificationDocumentDetails> iterator = reports.iterator();
        int results = 0;
        while (iterator.hasNext()) {
            DeidentificationDocumentDetails currentReport = iterator.next();
            assertEquals(OperationStatus.SUCCEEDED, currentReport.getStatus());
            assertTrue(currentReport.getOutputLocation().getLocation().contains(OUTPUT_FOLDER));
            assertEquals(36, currentReport.getId().length());
            results++;
        }
        assertEquals(3, results);
    }

    @Test
    void testJobE2ECancelJobThenDeleteJobDeletesJob() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getJobName();
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, INPUT_PREFIX);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperationType(DeidentificationOperationType.SURROGATE);

        DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
            .waitUntil(LongRunningOperationStatus.NOT_STARTED)
            .getValue();
        assertEquals(OperationStatus.NOT_STARTED, result.getStatus());

        DeidentificationJob cancelledJob = deidentificationClient.cancelJob(jobName);
        assertEquals(OperationStatus.CANCELED, cancelledJob.getStatus());

        deidentificationClient.deleteJob(jobName);

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            deidentificationClient.getJob(jobName);
        });
        assertEquals(404, exception.getResponse().getStatusCode());

    }

    @Test
    void testJobE2ECannotAccessStorageCreateJobFails() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getJobName();
        String storageLocation = "FAKE_STORAGE_ACCOUNT";
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, INPUT_PREFIX);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job
            = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperationType(DeidentificationOperationType.SURROGATE);

        assertThrows(HttpResponseException.class, () -> deidentificationClient.beginDeidentifyDocuments(jobName, job)
            .waitUntil(LongRunningOperationStatus.NOT_STARTED));
    }
}
