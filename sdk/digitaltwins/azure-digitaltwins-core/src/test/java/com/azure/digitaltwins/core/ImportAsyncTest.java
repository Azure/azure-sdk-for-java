package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.digitaltwins.core.models.DigitalTwinsImportJob;
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
        .get("INPUT_BLOB_URI");
    protected static final String OUTPUT_FILE = Configuration.getGlobalConfiguration()
        .get("STORAGE_CONTAINER_URI") + "/output-jobId.ndjson";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void importLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String jobId = getRandomIntegerStringGenerator().apply(maxRandomDigits);
        String outputFile = OUTPUT_FILE.replace("jobId", jobId);
        DigitalTwinsImportJob digitalTwinsImportJob = new DigitalTwinsImportJob(IMPORT_FILE, outputFile);
        List<DigitalTwinsImportJob> responseList = new ArrayList<>();
        StepVerifier
            .create(asyncClient.createBulkImportJob(jobId, digitalTwinsImportJob)).thenConsumeWhile(digitalTwinsImportJobResponse ->
                responseList.add(digitalTwinsImportJobResponse.getValue()))
            .verifyComplete();

        assertTrue(responseList.size()> 0);
        assertEquals(responseList.get(0).getId(), jobId);

        List<String> importJobList = new ArrayList<>();
        StepVerifier
            .create(asyncClient.listBulkImportJobs())
            .thenConsumeWhile(importJob -> importJobList.add(importJob.getId()))
            .verifyComplete();

        importJobList.forEach(Assertions::assertNotNull);
        assertThat(importJobList)
            .as("Import list contains the newly created job.")
            .contains(jobId);

        StepVerifier
            .create(asyncClient.getBulkImportJob(jobId))
            .assertNext(importJobResponse -> assertThat(importJobResponse.getValue().getId())
                .isEqualTo(jobId)
                .as("Retrieved JobId -> Output Job"))
            .verifyComplete();

        StepVerifier
            .create(asyncClient.cancelBulkImportJob(jobId))
            .assertNext(importJobResponse -> assertThat(importJobResponse.getStatusCode())
                .isEqualTo(HTTP_OK))
            .verifyComplete();

        StepVerifier
            .create(asyncClient.deleteBulkImportJob(jobId))
            .assertNext(importJobResponse -> assertThat(importJobResponse.getStatusCode())
                .isEqualTo(HTTP_NO_CONTENT))
            .verifyComplete();

        StepVerifier
            .create(asyncClient.getBulkImportJob(jobId))
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
            .create(asyncClient.createBulkImportJob(jobId, digitalTwinsImportJob)).thenConsumeWhile(digitalTwinsImportJobResponse ->
                responseList.add(digitalTwinsImportJobResponse.getValue()))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HTTP_BAD_REQUEST));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void validatingTooManyRequests(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String jobId = getRandomIntegerStringGenerator().apply(maxRandomDigits);
        String outputFile = OUTPUT_FILE.replace("jobId", jobId);
        DigitalTwinsImportJob digitalTwinsImportJob = new DigitalTwinsImportJob(IMPORT_FILE, outputFile);
        List<DigitalTwinsImportJob> responseList = new ArrayList<>();
        StepVerifier
            .create(asyncClient.createBulkImportJob(jobId, digitalTwinsImportJob)).thenConsumeWhile(digitalTwinsImportJobResponse ->
                responseList.add(digitalTwinsImportJobResponse.getValue()))
            .verifyComplete();

        assertTrue(responseList.size()> 0);
        assertEquals(responseList.get(0).getId(), jobId);

        StepVerifier
            .create(asyncClient.createBulkImportJob(jobId, digitalTwinsImportJob)).thenConsumeWhile(digitalTwinsImportJobResponse ->
                responseList.add(digitalTwinsImportJobResponse.getValue()))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseStatus.TOO_MANY_REQUESTS.code()));
    }
}
