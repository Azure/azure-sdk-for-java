package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.digitaltwins.core.models.DigitalTwinsImportJob;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImportTest extends ImportTestBase {

    int maxRandomDigits = 8;

    protected static final String IMPORT_FILE = Configuration.getGlobalConfiguration()
        .get("INPUT_BLOB_URI", "https://importjobcontainer.blob.core.windows.net/import/bulkinput1k2k.ndjson");
    protected static final String OUTPUT_FILE = Configuration.getGlobalConfiguration()
        .get("STORAGE_CONTAINER_URI",  "https://importjobcontainer.blob.core.windows.net/output") + "/output-jobId.txt";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void importLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        String jobId = getRandomIntegerStringGenerator().apply(maxRandomDigits);
        String outputFile = OUTPUT_FILE.replace("jobId", jobId);
        DigitalTwinsImportJob digitalTwinsImportJob = new DigitalTwinsImportJob(IMPORT_FILE, outputFile);

        // Validate create import job
        DigitalTwinsImportJob response = client.createImportJob(jobId, digitalTwinsImportJob);
        assertEquals(response.getId(), jobId);

        // Validate list import job
        Iterable<DigitalTwinsImportJob> importJobList = client.listImportJobs();
        importJobList.forEach(Assertions::assertNotNull);
        assertTrue(assertId(importJobList, response));

        // Validate get import job
        DigitalTwinsImportJob importJob = client.getImportJob(jobId);
        assertEquals(importJob.getId(), jobId);

        // Validate cancel import job
        Response<DigitalTwinsImportJob> cancelJob = client.cancelImportJobWithResponse(jobId);
        assertEquals(cancelJob.getStatusCode(), HTTP_OK);

        // Validate delete import job
        Response<Void> deleteImportJob = client.deleteImportJobWithResponse(jobId);
        assertEquals(deleteImportJob.getStatusCode(), HTTP_NO_CONTENT);

        assertRestException(
            () -> client.getImportJob(jobId),
            HttpURLConnection.HTTP_NOT_FOUND);

    }

    private boolean assertId(Iterable<DigitalTwinsImportJob> importJobList, DigitalTwinsImportJob response) {
        for(DigitalTwinsImportJob digitalTwinsImportJob: importJobList) {
            if(digitalTwinsImportJob.getId().equals(response.getId())){
                return true;
            }
        }
        return false;
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void validatingBadRequest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        String jobId = getRandomIntegerStringGenerator().apply(maxRandomDigits);
        DigitalTwinsImportJob digitalTwinsImportJob = new DigitalTwinsImportJob("invalidUri", "invalidOutputUri");
        assertRestException(
            () -> client.createImportJob(jobId, digitalTwinsImportJob),
            HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void validatingDuplicateRequests(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        String jobId = getRandomIntegerStringGenerator().apply(maxRandomDigits);
        String outputFile = OUTPUT_FILE.replace("jobId", jobId);
        DigitalTwinsImportJob digitalTwinsImportJob = new DigitalTwinsImportJob(IMPORT_FILE, outputFile);

        DigitalTwinsImportJob response = client.createImportJobWithResponse(jobId, digitalTwinsImportJob).getValue();
        assertEquals(response.getId(), jobId);

        assertRestException(
            () -> client.createImportJob(jobId, digitalTwinsImportJob),
            HttpResponseStatus.CONFLICT.code());
    }
}
