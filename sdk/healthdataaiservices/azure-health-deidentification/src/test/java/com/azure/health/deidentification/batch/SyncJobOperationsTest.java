package com.azure.health.deidentification.batch;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.health.deidentification.DeidentificationClient;
import com.azure.health.deidentification.DeidentificationClientBuilder;
import com.azure.health.deidentification.models.*;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import com.azure.health.deidentification.testutils.Utils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class SyncJobOperationsTest extends TestProxyTestBase {
    protected DeidentificationClient deidentificationClient;
    private static long pollingInterval = 2000;
    private static String OUTPUT_FOLDER = "_output";

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

        DeidentificationJob job = new DeidentificationJob(
            new SourceStorageLocation(storageAccountSASUri, inputPrefix, List.of("*")),
            new TargetStorageLocation(storageAccountSASUri, OUTPUT_FOLDER),
            OperationType.SURROGATE,
            DocumentDataType.PLAINTEXT);

        SyncPoller<DeidentificationJob, DeidentificationJob> poller = deidentificationClient.beginCreateJob(jobName, job)
            .setPollInterval(Duration.ofSeconds(4));
    }

    @Test
    void testCreateThenListReturnsExpected() {

    }

    @Test
    void testJobE2EWaitUntilSuccess() {

    }

    @Test
    void testJobE2ECancelJobThenDeleteJobDeletesJob() {

    }

    @Test
    void testJobE2ECannotAccessStorageCreateJobFails() {

    }

}
