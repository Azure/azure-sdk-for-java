// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.projects.models.ApiError;
import com.azure.ai.projects.models.DataGenerationJob;
import com.azure.ai.projects.models.DatasetDataGenerationJobOutput;
import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.FoundryFeaturesOptInKeys;
import com.azure.ai.projects.models.JobStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.openai.client.OpenAIClient;
import com.openai.models.evals.EvalCreateResponse;
import com.openai.models.evals.EvalDeleteParams;
import com.openai.models.evals.runs.RunCreateResponse;
import com.openai.models.evals.runs.RunRetrieveParams;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.models.evals.runs.outputitems.OutputItemListParams;
import com.openai.models.evals.runs.outputitems.OutputItemListResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DataGenerationJobsClientTests extends ClientTestBase {
    private static final FoundryFeaturesOptInKeys DATA_GENERATION_PREVIEW
        = FoundryFeaturesOptInKeys.DATA_GENERATION_JOBS_V1_PREVIEW;

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationJobsListSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DataGenerationJobsClient dataGenerationJobsClient
            = getClientBuilder(httpClient, serviceVersion).buildDataGenerationJobsClient();

        Iterable<DataGenerationJob> jobs
            = dataGenerationJobsClient.listGenerationJobs(DATA_GENERATION_PREVIEW, 5, PageOrder.DESC, null, null);
        Assertions.assertNotNull(jobs);

        int count = 0;
        for (DataGenerationJob job : jobs) {
            Assertions.assertNotNull(job);
            Assertions.assertNotNull(job.getId());
            count++;
            if (count >= 5) {
                break;
            }
        }
    }

    @Timeout(value = 20, unit = TimeUnit.MINUTES)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationJobWithEvaluationSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion)
        throws InterruptedException {
        AIProjectClientBuilder projectClientBuilder = getClientBuilder(httpClient, serviceVersion);
        DataGenerationJobsClient dataGenerationJobsClient = projectClientBuilder.buildDataGenerationJobsClient();
        DatasetsClient datasetsClient = projectClientBuilder.buildDatasetsClient();
        OpenAIClient openAIClient = projectClientBuilder.buildOpenAIClient();

        String modelName = getRecordedConfig("FOUNDRY_MODEL_NAME");
        String datasetName = testResourceNamer.randomName("dataset-generation-eval-", 64);

        DataGenerationJob job = dataGenerationJobsClient.createGenerationJob(
            DataGenerationJobWithEvaluationSample.createDataGenerationJob(modelName, datasetName),
            DATA_GENERATION_PREVIEW, testResourceNamer.randomUuid());

        job = waitForDataGenerationJob(dataGenerationJobsClient, job.getId(), 5, 180);
        if (!JobStatus.SUCCEEDED.equals(job.getStatus())) {
            ApiError error = job.getError();
            String message = error == null ? "<no error message>" : error.getMessage();
            Assertions
                .fail(String.format("Job `%s` ended with status `%s`: %s", job.getId(), job.getStatus(), message));
        }

        DatasetDataGenerationJobOutput output = DataGenerationJobWithEvaluationSample.findDatasetOutput(job);
        DatasetVersion dataset = datasetsClient.getDatasetVersion(output.getName(), output.getVersion());
        Assertions.assertNotNull(dataset);
        Assertions.assertNotNull(dataset.getId());

        EvalCreateResponse eval
            = openAIClient.evals().create(DataGenerationJobWithEvaluationSample.createEvaluationParams(modelName));
        Assertions.assertNotNull(eval);

        RunCreateResponse evalRun = openAIClient.evals()
            .runs()
            .create(
                DataGenerationJobWithEvaluationSample.createEvaluationRunParams(eval.id(), dataset.getId(), modelName));
        Assertions.assertNotNull(evalRun);

        RunRetrieveResponse completedRun = waitForEvaluationRun(openAIClient, eval.id(), evalRun.id(), 5, 180);
        Assertions.assertEquals("completed", completedRun.status());
        Assertions.assertNotNull(completedRun.resultCounts());

        int outputItemCount = 0;
        for (OutputItemListResponse ignored : openAIClient.evals()
            .runs()
            .outputItems()
            .list(OutputItemListParams.builder().evalId(eval.id()).runId(evalRun.id()).build())
            .autoPager()) {
            outputItemCount++;
        }
        Assertions.assertTrue(outputItemCount > 0);

        openAIClient.evals().delete(EvalDeleteParams.builder().evalId(eval.id()).build());
        dataGenerationJobsClient.deleteGenerationJob(job.getId(), DATA_GENERATION_PREVIEW);
    }

    private DataGenerationJob waitForDataGenerationJob(DataGenerationJobsClient dataGenerationJobsClient, String jobId,
        int pollIntervalSeconds, int maxAttempts) throws InterruptedException {
        DataGenerationJob job;
        int attempts = 0;
        do {
            sleepIfRunningAgainstService(pollIntervalSeconds * 1000L);
            job = dataGenerationJobsClient.getGenerationJob(jobId, DATA_GENERATION_PREVIEW);
            attempts++;
        } while (!DataGenerationJobWithEvaluationSample.isTerminalStatus(job.getStatus()) && attempts < maxAttempts);
        return job;
    }

    private RunRetrieveResponse waitForEvaluationRun(OpenAIClient openAIClient, String evalId, String runId,
        int pollIntervalSeconds, int maxAttempts) throws InterruptedException {
        RunRetrieveResponse evalRun
            = openAIClient.evals().runs().retrieve(RunRetrieveParams.builder().evalId(evalId).runId(runId).build());
        int attempts = 0;
        while (!"completed".equals(evalRun.status()) && !"failed".equals(evalRun.status()) && attempts < maxAttempts) {
            sleepIfRunningAgainstService(pollIntervalSeconds * 1000L);
            evalRun
                = openAIClient.evals().runs().retrieve(RunRetrieveParams.builder().evalId(evalId).runId(runId).build());
            attempts++;
        }
        return evalRun;
    }

    private String getRecordedConfig(String name) {
        if (getTestMode() == TestMode.PLAYBACK) {
            return testResourceNamer.recordValueFromConfig(name);
        }

        String value = Configuration.getGlobalConfiguration().get(name);
        if (getTestMode() == TestMode.RECORD) {
            testResourceNamer.recordValueFromConfig(name);
        }
        return value;
    }
}
