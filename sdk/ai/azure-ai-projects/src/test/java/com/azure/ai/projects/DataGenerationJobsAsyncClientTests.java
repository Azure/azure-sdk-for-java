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
import com.openai.client.OpenAIClientAsync;
import com.openai.models.evals.EvalCreateResponse;
import com.openai.models.evals.EvalDeleteParams;
import com.openai.models.evals.runs.RunCreateResponse;
import com.openai.models.evals.runs.RunRetrieveParams;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.models.evals.runs.outputitems.OutputItemListParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DataGenerationJobsAsyncClientTests extends ClientTestBase {
    private static final FoundryFeaturesOptInKeys DATA_GENERATION_PREVIEW
        = FoundryFeaturesOptInKeys.DATA_GENERATION_JOBS_V1_PREVIEW;

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationJobsListAsyncSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DataGenerationJobsAsyncClient dataGenerationJobsAsyncClient
            = getClientBuilder(httpClient, serviceVersion).buildDataGenerationJobsAsyncClient();

        StepVerifier.create(
            dataGenerationJobsAsyncClient.listGenerationJobs(DATA_GENERATION_PREVIEW, 5, PageOrder.DESC, null, null)
                .take(5)
                .doOnNext(job -> {
                    Assertions.assertNotNull(job);
                    Assertions.assertNotNull(job.getId());
                })
                .then())
            .verifyComplete();
    }

    @Timeout(value = 20, unit = TimeUnit.MINUTES)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationJobWithEvaluationAsyncSample(HttpClient httpClient,
        AIProjectsServiceVersion serviceVersion) {
        AIProjectClientBuilder projectClientBuilder = getClientBuilder(httpClient, serviceVersion);
        DataGenerationJobsAsyncClient dataGenerationJobsAsyncClient
            = projectClientBuilder.buildDataGenerationJobsAsyncClient();
        DatasetsAsyncClient datasetsAsyncClient = projectClientBuilder.buildDatasetsAsyncClient();
        OpenAIClientAsync openAIAsyncClient = projectClientBuilder.buildOpenAIAsyncClient();

        String modelName = getRecordedConfig("FOUNDRY_MODEL_NAME");
        String datasetName = testResourceNamer.randomName("dataset-generation-eval-", 64);

        Mono<Void> scenario = dataGenerationJobsAsyncClient
            .createGenerationJob(DataGenerationJobWithEvaluationSample.createDataGenerationJob(modelName, datasetName),
                DATA_GENERATION_PREVIEW, testResourceNamer.randomUuid())
            .flatMap(job -> waitForDataGenerationJob(dataGenerationJobsAsyncClient, job.getId(), 5, 180)
                .flatMap(completedJob -> {
                    if (!JobStatus.SUCCEEDED.equals(completedJob.getStatus())) {
                        ApiError error = completedJob.getError();
                        String message = error == null ? "<no error message>" : error.getMessage();
                        return Mono.error(new AssertionError(String.format("Job `%s` ended with status `%s`: %s",
                            completedJob.getId(), completedJob.getStatus(), message)));
                    }

                    DatasetDataGenerationJobOutput output
                        = DataGenerationJobWithEvaluationSample.findDatasetOutput(completedJob);
                    return datasetsAsyncClient.getDatasetVersion(output.getName(), output.getVersion())
                        .flatMap(dataset -> runEvaluation(openAIAsyncClient, dataset, modelName))
                        .then(dataGenerationJobsAsyncClient.deleteGenerationJob(completedJob.getId(),
                            DATA_GENERATION_PREVIEW));
                }));

        StepVerifier.create(scenario).verifyComplete();
    }

    private Mono<Void> runEvaluation(OpenAIClientAsync openAIAsyncClient, DatasetVersion dataset, String modelName) {
        Assertions.assertNotNull(dataset);
        Assertions.assertNotNull(dataset.getId());

        return deferFuture(() -> openAIAsyncClient.evals()
            .create(DataGenerationJobWithEvaluationSample.createEvaluationParams(modelName))).flatMap(eval -> {
                Assertions.assertNotNull(eval);

                Mono<RunCreateResponse> createRun = deferFuture(() -> openAIAsyncClient.evals()
                    .runs()
                    .create(DataGenerationJobWithEvaluationSample.createEvaluationRunParams(eval.id(), dataset.getId(),
                        modelName)));

                return createRun.flatMap(evalRun -> assertEvaluationRunCompleted(openAIAsyncClient, eval, evalRun))
                    .then(deferFuture(
                        () -> openAIAsyncClient.evals().delete(EvalDeleteParams.builder().evalId(eval.id()).build())))
                    .then();
            });
    }

    private Mono<Void> assertEvaluationRunCompleted(OpenAIClientAsync openAIAsyncClient, EvalCreateResponse eval,
        RunCreateResponse evalRun) {
        Assertions.assertNotNull(evalRun);

        return waitForEvaluationRun(openAIAsyncClient, eval.id(), evalRun.id(), 5, 180).flatMap(completedRun -> {
            Assertions.assertEquals("completed", completedRun.status());
            Assertions.assertNotNull(completedRun.resultCounts());

            OutputItemListParams outputItemsParams
                = OutputItemListParams.builder().evalId(eval.id()).runId(evalRun.id()).build();
            return deferFuture(() -> openAIAsyncClient.evals().runs().outputItems().list(outputItemsParams))
                .flatMapMany(page -> Flux.fromIterable(page.items()))
                .count()
                .doOnNext(outputItemCount -> Assertions.assertTrue(outputItemCount > 0))
                .then();
        });
    }

    private Mono<DataGenerationJob> waitForDataGenerationJob(
        DataGenerationJobsAsyncClient dataGenerationJobsAsyncClient, String jobId, int pollIntervalSeconds,
        int maxAttempts) {
        return pollDataGenerationJob(dataGenerationJobsAsyncClient, jobId, pollIntervalSeconds, maxAttempts, 0);
    }

    private Mono<DataGenerationJob> pollDataGenerationJob(DataGenerationJobsAsyncClient dataGenerationJobsAsyncClient,
        String jobId, int pollIntervalSeconds, int maxAttempts, int attempts) {
        return sleepBeforePolling(pollIntervalSeconds, attempts)
            .then(dataGenerationJobsAsyncClient.getGenerationJob(jobId, DATA_GENERATION_PREVIEW))
            .flatMap(job -> DataGenerationJobWithEvaluationSample.isTerminalStatus(job.getStatus())
                || attempts >= maxAttempts
                    ? Mono.just(job)
                    : pollDataGenerationJob(dataGenerationJobsAsyncClient, jobId, pollIntervalSeconds, maxAttempts,
                        attempts + 1));
    }

    private Mono<RunRetrieveResponse> waitForEvaluationRun(OpenAIClientAsync openAIAsyncClient, String evalId,
        String runId, int pollIntervalSeconds, int maxAttempts) {
        return pollEvaluationRun(openAIAsyncClient, evalId, runId, pollIntervalSeconds, maxAttempts, 0);
    }

    private Mono<RunRetrieveResponse> pollEvaluationRun(OpenAIClientAsync openAIAsyncClient, String evalId,
        String runId, int pollIntervalSeconds, int maxAttempts, int attempts) {
        return sleepBeforePolling(pollIntervalSeconds, attempts)
            .then(deferFuture(() -> openAIAsyncClient.evals()
                .runs()
                .retrieve(RunRetrieveParams.builder().evalId(evalId).runId(runId).build())))
            .flatMap(evalRun -> "completed".equals(evalRun.status())
                || "failed".equals(evalRun.status())
                || attempts >= maxAttempts
                    ? Mono.just(evalRun)
                    : pollEvaluationRun(openAIAsyncClient, evalId, runId, pollIntervalSeconds, maxAttempts,
                        attempts + 1));
    }

    private Mono<Void> sleepBeforePolling(int pollIntervalSeconds, int attempts) {
        if (attempts == 0) {
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> sleepIfRunningAgainstService(pollIntervalSeconds * 1000L));
    }

    private <T> Mono<T> deferFuture(Supplier<CompletableFuture<T>> futureSupplier) {
        return Mono.defer(() -> Mono.fromFuture(futureSupplier.get()));
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
