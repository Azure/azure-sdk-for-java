// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.core.util.Configuration;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.openai.core.JsonValue;
import com.openai.models.evals.EvalCreateParams;
import com.openai.models.evals.EvalCreateResponse;
import com.openai.models.evals.EvalDeleteParams;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource.Source.FileContent.Content;
import com.openai.models.evals.runs.RunCancelParams;
import com.openai.models.evals.runs.RunCreateParams;
import com.openai.models.evals.runs.RunCreateResponse;
import com.openai.models.evals.runs.RunRetrieveParams;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.models.evals.runs.outputitems.OutputItemListParams;
import com.openai.models.evals.runs.outputitems.OutputItemListResponse;
import com.openai.services.async.EvalServiceAsync;
import com.openai.services.blocking.EvalService;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Shared data, polling, result reporting, and cleanup for the evaluation samples.
 */
final class EvaluationSampleUtils {
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(5);
    private static final Duration RUN_TIMEOUT = Duration.ofMinutes(5);

    private EvaluationSampleUtils() {
    }

    static String requiredSetting(String name) {
        return Objects.requireNonNull(Configuration.getGlobalConfiguration().get(name),
            "Set the " + name + " environment variable before running this sample.");
    }

    static EvalCreateParams.DataSourceConfig.Custom dataSourceConfig() {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String field : Arrays.asList("query", "response", "ground_truth")) {
            properties.put(field, Collections.singletonMap("type", "string"));
        }
        return EvalCreateParams.DataSourceConfig.Custom.builder()
            .itemSchema(EvalCreateParams.DataSourceConfig.Custom.ItemSchema.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(properties))
                .putAdditionalProperty("required", JsonValue.from(properties.keySet()))
                .build())
            .includeSampleSchema(false)
            .build();
    }

    static CreateEvalJsonlRunDataSource inlineData() {
        return CreateEvalJsonlRunDataSource.builder()
            .fileContentSource(Arrays.asList(
                row("What is the capital of France?", "Paris is the capital of France.",
                    "Paris is the capital of France."),
                row("How many days are in a week?", "There are seven days in a week.",
                    "There are seven days in a week."),
                row("What is the capital of Japan?", "The capital of Japan is Osaka.",
                    "The capital of Japan is Tokyo.")))
            .build();
    }

    private static Content row(String query, String response, String groundTruth) {
        return Content.builder()
            .item(Content.Item.builder()
                .putAdditionalProperty("query", JsonValue.from(query))
                .putAdditionalProperty("response", JsonValue.from(response))
                .putAdditionalProperty("ground_truth", JsonValue.from(groundTruth))
                .build())
            .build();
    }

    static RunRetrieveResponse evaluate(EvalService evaluations, EvalCreateParams evaluation,
        CreateEvalJsonlRunDataSource dataSource) {
        EvalCreateResponse created = evaluations.create(evaluation);
        System.out.printf("Created evaluation: %s%n", created.id());
        try {
            RunCreateResponse run = evaluations.runs().create(RunCreateParams.builder()
                .evalId(created.id()).name("java-sample-run").dataSource(dataSource).build());
            RunRetrieveParams retrieve = RunRetrieveParams.builder().evalId(created.id()).runId(run.id()).build();
            SyncPoller<RunRetrieveResponse, RunRetrieveResponse> poller = SyncPoller.createPoller(POLL_INTERVAL,
                context -> pollResponse(evaluations.runs().retrieve(retrieve)),
                context -> pollResponse(evaluations.runs().retrieve(retrieve)),
                (context, response) -> {
                    evaluations.runs().cancel(RunCancelParams.builder().evalId(created.id()).runId(run.id()).build());
                    return evaluations.runs().retrieve(retrieve);
                },
                context -> context.getLatestResponse().getValue());
            RunRetrieveResponse completed = poller.waitForCompletion(RUN_TIMEOUT).getValue();
            requireSuccessfulRun(completed);
            printSummary(completed);
            for (OutputItemListResponse item : evaluations.runs().outputItems().list(OutputItemListParams.builder()
                .evalId(created.id()).runId(run.id()).build()).autoPager()) {
                printItem(item);
            }
            return completed;
        } finally {
            evaluations.delete(EvalDeleteParams.builder().evalId(created.id()).build());
            System.out.printf("Deleted evaluation: %s%n", created.id());
        }
    }

    static Mono<RunRetrieveResponse> evaluateAsync(EvalServiceAsync evaluations, EvalCreateParams evaluation,
        CreateEvalJsonlRunDataSource dataSource) {
        return Mono.usingWhen(Mono.fromFuture(() -> evaluations.create(evaluation)),
            created -> {
                System.out.printf("Created evaluation: %s%n", created.id());
                return Mono.fromFuture(() -> evaluations.runs().create(RunCreateParams.builder()
                    .evalId(created.id()).name("java-async-sample-run").dataSource(dataSource).build()))
                    .flatMap(run -> {
                        RunRetrieveParams retrieve = RunRetrieveParams.builder()
                            .evalId(created.id()).runId(run.id()).build();
                        PollerFlux<RunRetrieveResponse, RunRetrieveResponse> poller = PollerFlux.create(POLL_INTERVAL,
                            context -> Mono.fromFuture(() -> evaluations.runs().retrieve(retrieve))
                                .map(EvaluationSampleUtils::pollResponse),
                            context -> Mono.fromFuture(() -> evaluations.runs().retrieve(retrieve))
                                .map(EvaluationSampleUtils::pollResponse),
                            (context, response) -> Mono.fromFuture(() -> evaluations.runs()
                                .cancel(RunCancelParams.builder().evalId(created.id()).runId(run.id()).build()))
                                .then(Mono.fromFuture(() -> evaluations.runs().retrieve(retrieve))),
                            context -> Mono.just(context.getLatestResponse().getValue()));
                        return poller.last().timeout(RUN_TIMEOUT).map(response -> response.getValue());
                    })
                    .doOnNext(EvaluationSampleUtils::requireSuccessfulRun)
                    .doOnNext(EvaluationSampleUtils::printSummary)
                    .flatMap(run -> Mono.fromFuture(() -> evaluations.runs().outputItems()
                        .list(OutputItemListParams.builder().evalId(created.id()).runId(run.id()).build()))
                        .flatMap(page -> Mono.using(page::autoPager,
                            pager -> Mono.fromFuture(() -> pager.subscribe(EvaluationSampleUtils::printItem)
                                .onCompleteFuture()),
                            pager -> pager.close()))
                        .thenReturn(run));
            },
            created -> Mono.fromFuture(() -> evaluations
                .delete(EvalDeleteParams.builder().evalId(created.id()).build()))
                .doOnSuccess(ignored -> System.out.printf("Deleted evaluation: %s%n", created.id())));
    }

    static PollResponse<RunRetrieveResponse> pollResponse(RunRetrieveResponse run) {
        LongRunningOperationStatus status;
        switch (run.status()) {
            case "completed":
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case "failed":
                status = LongRunningOperationStatus.FAILED;
                break;
            case "canceled":
            case "cancelled":
                status = LongRunningOperationStatus.USER_CANCELLED;
                break;
            case "queued":
            case "in_progress":
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            default:
                throw new IllegalStateException("Unrecognized evaluation run status: " + run.status());
        }
        return new PollResponse<>(status, run);
    }

    static void requireSuccessfulRun(RunRetrieveResponse run) {
        if (!"completed".equals(run.status())) {
            throw new IllegalStateException("Evaluation run " + run.id() + " ended with status " + run.status()
                + ": " + run._error());
        }
        if (run.resultCounts().total() == 0 || run.resultCounts().errored() != 0) {
            throw new IllegalStateException("Evaluation run " + run.id() + " returned empty or errored results: "
                + run.resultCounts());
        }
    }

    private static void printSummary(RunRetrieveResponse run) {
        System.out.printf("Run %s: %s%n", run.id(), run.status());
        System.out.printf("Result counts: %s%n", run.resultCounts());
        System.out.printf("Report URL: %s%n", run.reportUrl());
    }

    private static void printItem(OutputItemListResponse item) {
        System.out.printf("Item %s: status=%s, results=%s%n", item.id(), item.status(), item.results());
    }
}
