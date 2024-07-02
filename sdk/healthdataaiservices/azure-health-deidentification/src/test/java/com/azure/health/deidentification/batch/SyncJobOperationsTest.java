// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.batch;

import com.azure.core.credential.AccessToken;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.health.deidentification.DeidentificationClient;
import com.azure.health.deidentification.DeidentificationClientBuilder;
import com.azure.health.deidentification.models.*;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import com.azure.health.deidentification.testutils.Utils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncJobOperationsTest extends TestProxyTestBase {
    protected DeidentificationClient deidentificationClient;
    private static final String OUTPUT_FOLDER = "_output";

    @Override
    protected void beforeTest() {
        DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            deidentificationClientbuilder.httpClient(interceptorManager.getPlaybackClient())
                .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            deidentificationClientbuilder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            deidentificationClientbuilder.credential(new DefaultAzureCredentialBuilder().build());
        }
        deidentificationClient = deidentificationClientbuilder.buildClient();
    }

    @Test
    void testCreateJobReturnsExpected() {
        String jobName = Utils.generateJobName("test01");
        JobStatus statusToWait = JobStatus.RUNNING;
        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_SAS_URI");
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        DeidentificationJob job = new DeidentificationJob(
            new SourceStorageLocation(storageAccountSASUri, inputPrefix, extensions),
            new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER),
            OperationType.SURROGATE,
            DocumentDataType.PLAINTEXT);

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
        String jobName = Utils.generateJobName("test02");
        JobStatus statusToWait = JobStatus.RUNNING;
        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_SAS_URI");
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        DeidentificationJob job = new DeidentificationJob(
            new SourceStorageLocation(storageAccountSASUri, inputPrefix, extensions),
            new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER),
            OperationType.SURROGATE,
            DocumentDataType.PLAINTEXT);

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
        String jobName = Utils.generateJobName("test03");
        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_SAS_URI");
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        DeidentificationJob job = new DeidentificationJob(
            new SourceStorageLocation(storageAccountSASUri, inputPrefix, extensions),
            new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER),
            OperationType.SURROGATE,
            DocumentDataType.PLAINTEXT);

        DeidentificationJob result = deidentificationClient.beginCreateJob(jobName, job)
            .waitForCompletion()
            .getValue();
        assertEquals(JobStatus.SUCCEEDED, result.getStatus());

        PagedIterable<HealthFileDetails> reports = deidentificationClient.listJobFiles(jobName);
        Iterator<HealthFileDetails> iterator = reports.iterator();
        int results = 0;
        while (iterator.hasNext()) {
            HealthFileDetails currentReport = iterator.next();
            assertEquals(currentReport.getStatus(), OperationState.SUCCEEDED);
            assertTrue(currentReport.getOutput().getPath().startsWith(OUTPUT_FOLDER));
            assertEquals(currentReport.getId().length(), 36);
            results++;
        }
        assertEquals(2, results);
    }

    @Test
    void testJobE2ECancelJobThenDeleteJobDeletesJob() {
        String jobName = Utils.generateJobName("test04");
        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_SAS_URI");
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        DeidentificationJob job = new DeidentificationJob(
            new SourceStorageLocation(storageAccountSASUri, inputPrefix, extensions),
            new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER),
            OperationType.SURROGATE,
            DocumentDataType.PLAINTEXT);

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
        String jobName = Utils.generateJobName("test05");
        String inputPrefix = "example_patient_1";
        String storageAccountSASUri = "FAKE_STORAGE_ACCOUNT";
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        DeidentificationJob job = new DeidentificationJob(
            new SourceStorageLocation(storageAccountSASUri, inputPrefix, extensions),
            new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER),
            OperationType.SURROGATE,
            DocumentDataType.PLAINTEXT);

        assertThrows(HttpResponseException.class, () -> deidentificationClient.beginCreateJob(jobName, job).waitUntil(LongRunningOperationStatus.NOT_STARTED));

    }
}
