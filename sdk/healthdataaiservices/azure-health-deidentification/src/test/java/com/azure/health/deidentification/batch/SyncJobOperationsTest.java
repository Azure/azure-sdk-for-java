// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.batch;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.health.deidentification.DeidentificationClient;
import com.azure.health.deidentification.models.DeidentificationJob;
import com.azure.health.deidentification.models.DocumentDataType;
import com.azure.health.deidentification.models.DocumentDetails;
import com.azure.health.deidentification.models.JobStatus;
import com.azure.health.deidentification.models.OperationState;
import com.azure.health.deidentification.models.OperationType;
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

    @Test
    void testCreateJobReturnsExpected() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded001w";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationJob result = deidentificationClient.beginCreateJob(jobName, job)
            .waitUntil(LongRunningOperationStatus.NOT_STARTED)
            .getValue();

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
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded002w";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationJob result = deidentificationClient.beginCreateJob(jobName, job)
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
                assertEquals(JobStatus.NOT_STARTED, currentJob.getStatus());
                assertNull(currentJob.getError());
                assertNull(currentJob.getRedactionFormat());
                assertNull(currentJob.getSummary());
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
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded003w";
        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        SyncPoller<DeidentificationJob, DeidentificationJob> poller = setPlaybackSyncPollerPollInterval(deidentificationClient.beginCreateJob(jobName, job));
        DeidentificationJob result = poller
            .waitForCompletion()
            .getValue();
        assertEquals(JobStatus.SUCCEEDED, result.getStatus());

        PagedIterable<DocumentDetails> reports = deidentificationClient.listJobDocuments(jobName);
        Iterator<DocumentDetails> iterator = reports.iterator();
        int results = 0;
        while (iterator.hasNext()) {
            DocumentDetails currentReport = iterator.next();
            assertEquals(currentReport.getStatus(), OperationState.SUCCEEDED);
            assertTrue(currentReport.getOutput().getPath().startsWith(OUTPUT_FOLDER));
            assertEquals(currentReport.getId().length(), 36);
            results++;
        }
        assertEquals(2, results);
    }

    @Test
    void testJobE2ECancelJobThenDeleteJobDeletesJob() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded004w";

        String inputPrefix = "example_patient_1";
        String storageLocation = getStorageAccountLocation();
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationJob result = deidentificationClient.beginCreateJob(jobName, job)
            .waitUntil(LongRunningOperationStatus.NOT_STARTED)
            .getValue();
        assertEquals(JobStatus.NOT_STARTED, result.getStatus());

        DeidentificationJob cancelledJob = deidentificationClient.cancelJob(jobName);
        assertEquals(JobStatus.CANCELED, cancelledJob.getStatus());

        deidentificationClient.deleteJob(jobName);

        assertThrows(ResourceNotFoundException.class, () -> {
            deidentificationClient.getJob(jobName);
        });
    }

    @Test
    void testJobE2ECannotAccessStorageCreateJobFails() {
        deidentificationClient = getDeidServicesClientBuilder().buildClient();
        String jobName = getTestMode() == TestMode.LIVE ? getJobName() : "recorded005w";

        String inputPrefix = "example_patient_1";
        String storageLocation = "FAKE_STORAGE_ACCOUNT";
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, OUTPUT_FOLDER));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        assertThrows(HttpResponseException.class, () -> deidentificationClient.beginCreateJob(jobName, job).waitUntil(LongRunningOperationStatus.NOT_STARTED));

    }
}
