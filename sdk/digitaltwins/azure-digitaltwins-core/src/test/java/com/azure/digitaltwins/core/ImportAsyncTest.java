package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.digitaltwins.core.models.DigitalTwinsImportJob;
import com.azure.digitaltwins.core.models.Status;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImportAsyncTest extends ImportTestBase {

    int maxRandomDigits = 8;

    protected static final String IMPORT_FILE = Configuration.getGlobalConfiguration()
        .get("INPUT_BLOB_URI", "https://importjobcontainer.blob.core.windows.net/import/bulkinput1k2k.ndjson");
    protected static final String OUTPUT_FILE = Configuration.getGlobalConfiguration()
        .get("STORAGE_CONTAINER_URI",  "https://importjobcontainer.blob.core.windows.net/output") + "/output-jobId.txt";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void importLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String jobId = getRandomIntegerStringGenerator().apply(maxRandomDigits);
        String outputFile = OUTPUT_FILE.replace("jobId", jobId);
        DigitalTwinsImportJob digitalTwinsImportJob = new DigitalTwinsImportJob(IMPORT_FILE, outputFile);
        List<DigitalTwinsImportJob> responseList = new ArrayList<>();

        // Validate create import job
        StepVerifier
            .create(asyncClient.createImportJob(jobId, digitalTwinsImportJob)).thenConsumeWhile(responseList::add)
            .verifyComplete();

        assertTrue(responseList.size()> 0);
        assertEquals(responseList.get(0).getId(), jobId);

        // Validate list import job
        List<String> importJobList = new ArrayList<>();
        StepVerifier
            .create(asyncClient.listImportJobs())
            .thenConsumeWhile(importJob -> importJobList.add(importJob.getId()))
            .verifyComplete();

        importJobList.forEach(Assertions::assertNotNull);
        assertThat(importJobList)
            .as("Import list contains the newly created job.")
            .contains(jobId);

        // Validate get import job
        StepVerifier
            .create(asyncClient.getImportJob(jobId))
            .assertNext(importJobResponse -> assertThat(importJobResponse.getId())
                .isEqualTo(jobId)
                .as("Retrieved JobId -> Output Job"))
            .verifyComplete();

        // Validate cancel import job
        StepVerifier
            .create(asyncClient.cancelImportJob(jobId))
            .assertNext(importJobResponse -> assertTrue(
                importJobResponse.getStatus()== Status.CANCELLING ||
                importJobResponse.getStatus()==Status.SUCCEEDED))
            .verifyComplete();

        // Validate delete import job
        StepVerifier
            .create(asyncClient.deleteImportJob(jobId))
            .verifyComplete();

        // Validate get import job
        StepVerifier
            .create(asyncClient.getImportJobWithResponse(jobId))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void validatingBadRequest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String jobId = getRandomIntegerStringGenerator().apply(maxRandomDigits);
        DigitalTwinsImportJob digitalTwinsImportJob = new DigitalTwinsImportJob("invalidUri", "invalidOutputUri");
        List<DigitalTwinsImportJob> responseList = new ArrayList<>();
        StepVerifier
            .create(asyncClient.createImportJobWithResponse(jobId, digitalTwinsImportJob)).thenConsumeWhile(digitalTwinsImportJobResponse ->
                responseList.add(digitalTwinsImportJobResponse.getValue()))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HTTP_BAD_REQUEST));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void validatingDuplicateRequests(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String jobId = getRandomIntegerStringGenerator().apply(maxRandomDigits);
        String outputFile = OUTPUT_FILE.replace("jobId", jobId);
        DigitalTwinsImportJob digitalTwinsImportJob = new DigitalTwinsImportJob(IMPORT_FILE, outputFile);
        List<DigitalTwinsImportJob> responseList = new ArrayList<>();
        StepVerifier
            .create(asyncClient.createImportJobWithResponse(jobId, digitalTwinsImportJob)).thenConsumeWhile(digitalTwinsImportJobResponse ->
                responseList.add(digitalTwinsImportJobResponse.getValue()))
            .verifyComplete();

        assertTrue(responseList.size()> 0);
        assertEquals(responseList.get(0).getId(), jobId);

        StepVerifier
            .create(asyncClient.createImportJobWithResponse(jobId, digitalTwinsImportJob)).thenConsumeWhile(digitalTwinsImportJobResponse ->
                responseList.add(digitalTwinsImportJobResponse.getValue()))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseStatus.CONFLICT.code()));
    }
}
