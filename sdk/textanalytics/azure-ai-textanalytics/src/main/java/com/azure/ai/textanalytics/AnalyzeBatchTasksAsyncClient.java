// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeBatchOperationResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeBatchResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeBatchInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeJobState;
import com.azure.ai.textanalytics.implementation.models.EntitiesTask;
import com.azure.ai.textanalytics.implementation.models.EntitiesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.JobManifestTasks;
import com.azure.ai.textanalytics.implementation.models.KeyPhrasesTask;
import com.azure.ai.textanalytics.implementation.models.KeyPhrasesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.PiiTask;
import com.azure.ai.textanalytics.implementation.models.PiiTaskParameters;
import com.azure.ai.textanalytics.implementation.models.PiiTaskParametersDomain;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasks;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityRecognitionPiiTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityRecognitionTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksKeyPhraseExtractionTasksItem;
import com.azure.ai.textanalytics.models.BatchActions;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasksOperationResult;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasksOptions;
import com.azure.ai.textanalytics.models.AnalyzeBatchTasksResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.parseModelId;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.toExtractKeyPhrasesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeEntitiesResultCollectionResponse;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizePiiEntitiesResultCollection;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

class AnalyzeBatchTasksAsyncClient {
    private final ClientLogger logger = new ClientLogger(AnalyzeBatchTasksAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    AnalyzeBatchTasksAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    PollerFlux<AnalyzeBatchTasksOperationResult, PagedFlux<AnalyzeBatchTasksResult>> beginAnalyzeTasks(
        Iterable<TextDocumentInput> documents, BatchActions tasks, AnalyzeBatchTasksOptions options,
        Context context) {
        try {
            inputDocumentsValidation(documents);
            if (options == null) {
                options = new AnalyzeBatchTasksOptions();
            }
            final AnalyzeBatchInput analyzeBatchInput =
                new AnalyzeBatchInput()
                    .setAnalysisInput(new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(getJobManifestTasks(tasks));
            analyzeBatchInput.setDisplayName(options.getDisplayName()); // setDisplayName() returns JobDescriptor
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL, // TODO: after poller has the poll interval, change it back to it.
                activationOperation(
                    service.analyzeWithResponseAsync(analyzeBatchInput,
                        context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                        .map(analyzeResponse -> {
                            final AnalyzeBatchTasksOperationResult textAnalyticsOperationResult =
                                new AnalyzeBatchTasksOperationResult();
                            AnalyzeBatchOperationResultPropertiesHelper.setOperationId(textAnalyticsOperationResult,
                                parseModelId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return textAnalyticsOperationResult;
                        })),
                pollingOperation(resultID -> service.analyzeStatusWithResponseAsync(resultID,
                    finalIncludeStatistics, null, null, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperation(resultId -> Mono.just(getAnalyzeOperationFluxPage(
                    resultId, null, null, finalIncludeStatistics, context)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    PollerFlux<AnalyzeBatchTasksOperationResult, PagedIterable<AnalyzeBatchTasksResult>> beginAnalyzeTasksIterable(
        Iterable<TextDocumentInput> documents, BatchActions tasks, AnalyzeBatchTasksOptions options,
        Context context) {
        try {
            inputDocumentsValidation(documents);
            if (options == null) {
                options = new AnalyzeBatchTasksOptions();
            }
            final AnalyzeBatchInput analyzeBatchInput =
                new AnalyzeBatchInput()
                    .setAnalysisInput(new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(getJobManifestTasks(tasks));
            analyzeBatchInput.setDisplayName(options.getDisplayName()); // setDisplayName() returns JobDescriptor
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL, // TODO: after poller has the poll interval, change it back to it.
                activationOperation(
                    service.analyzeWithResponseAsync(analyzeBatchInput,
                        context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                        .map(analyzeResponse -> {
                            final AnalyzeBatchTasksOperationResult textAnalyticsOperationResult =
                                new AnalyzeBatchTasksOperationResult();
                            AnalyzeBatchOperationResultPropertiesHelper.setOperationId(textAnalyticsOperationResult,
                                parseModelId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return textAnalyticsOperationResult;
                        })),
                pollingOperation(resultID -> service.analyzeStatusWithResponseAsync(resultID,
                    finalIncludeStatistics, null, null, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperationIterable(resultId -> Mono.just(new PagedIterable<>(getAnalyzeOperationFluxPage(
                    resultId, null, null, finalIncludeStatistics, context))))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private JobManifestTasks getJobManifestTasks(BatchActions tasks) {
        return new JobManifestTasks()
            .setEntityRecognitionTasks(tasks.getRecognizeEntitiesOptions() == null ? null
                : StreamSupport.stream(tasks.getRecognizeEntitiesOptions().spliterator(), false).map(
                    entitiesTask -> {
                        if (entitiesTask == null) {
                            return null;
                        }
                        final EntitiesTask entitiesTaskImpl = new EntitiesTask();
                        entitiesTaskImpl.setParameters(
                            // TODO: currently, service does not set their default values for model version, we
                            // temporally set the default value to 'latest' until service correct it.
                            // https://github.com/Azure/azure-sdk-for-java/issues/17625
                            new EntitiesTaskParameters().setModelVersion(
                                entitiesTask.getModelVersion() == null ? "latest" : entitiesTask.getModelVersion()));
                        return entitiesTaskImpl;
                    }).collect(Collectors.toList()))
            .setEntityRecognitionPiiTasks(tasks.getRecognizePiiEntitiesOptions() == null ? null
                : StreamSupport.stream(tasks.getRecognizePiiEntitiesOptions().spliterator(), false).map(
                    piiEntitiesTask -> {
                        if (piiEntitiesTask == null) {
                            return null;
                        }
                        final PiiTask piiTaskImpl = new PiiTask();
                        piiTaskImpl.setParameters(
                            new PiiTaskParameters()
                                // TODO: currently, service does not set their default values for model version, we
                                // temporally set the default value to 'latest' until service correct it.
                                // https://github.com/Azure/azure-sdk-for-java/issues/17625
                                .setModelVersion(piiEntitiesTask.getModelVersion() == null ?
                                                     "latest" : piiEntitiesTask.getModelVersion())
                                .setDomain(PiiTaskParametersDomain.fromString(
                                    piiEntitiesTask.getDomainFilter() == null ? null
                                        : piiEntitiesTask.getDomainFilter().toString())));
                        return piiTaskImpl;
                    }).collect(Collectors.toList()))
            .setKeyPhraseExtractionTasks(tasks.getExtractKeyPhrasesOptions() == null ? null
                : StreamSupport.stream(tasks.getExtractKeyPhrasesOptions().spliterator(), false).map(
                    keyPhrasesTask -> {
                        if (keyPhrasesTask == null) {
                            return null;
                        }
                        final KeyPhrasesTask keyPhrasesTaskImpl = new KeyPhrasesTask();
                        keyPhrasesTaskImpl.setParameters(
                            // TODO: currently, service does not set their default values for model version, we
                            // temporally set the default value to 'latest' until service correct it.
                            // https://github.com/Azure/azure-sdk-for-java/issues/17625
                            new KeyPhrasesTaskParameters()
                                .setModelVersion(keyPhrasesTask.getModelVersion() == null
                                                     ? "latest" : keyPhrasesTask.getModelVersion()));
                        return keyPhrasesTaskImpl;
                    }).collect(Collectors.toList()));
    }

    private Function<PollingContext<AnalyzeBatchTasksOperationResult>, Mono<AnalyzeBatchTasksOperationResult>>
        activationOperation(Mono<AnalyzeBatchTasksOperationResult> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeBatchTasksOperationResult>, Mono<PollResponse<AnalyzeBatchTasksOperationResult>>>
        pollingOperation(Function<String, Mono<Response<AnalyzeJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeBatchTasksOperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                final String resultID = operationResultPollResponse.getValue().getOperationId();
                return pollingFunction.apply(resultID)
                    .flatMap(modelResponse -> processAnalyzedModelResponse(modelResponse, operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeBatchTasksOperationResult>, Mono<PagedFlux<AnalyzeBatchTasksResult>>>
        fetchingOperation(Function<String, Mono<PagedFlux<AnalyzeBatchTasksResult>>> fetchingFunction) {
        return pollingContext -> {
            try {
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                final String resultUUID = pollingContext.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(resultUUID);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeBatchTasksOperationResult>, Mono<PagedIterable<AnalyzeBatchTasksResult>>>
        fetchingOperationIterable(Function<String, Mono<PagedIterable<AnalyzeBatchTasksResult>>> fetchingFunction) {
        return pollingContext -> {
            try {
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                final String resultUUID = pollingContext.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(resultUUID);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    PagedFlux<AnalyzeBatchTasksResult> getAnalyzeOperationFluxPage(String analyzeTasksId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new PagedFlux<>(
            () -> getPage(null, analyzeTasksId, top, skip, showStats, context),
            continuationToken -> getPage(continuationToken, analyzeTasksId, top, skip, showStats, context));
    }

    Mono<PagedResponse<AnalyzeBatchTasksResult>> getPage(String continuationToken, String analyzeTasksId, Integer top,
        Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Integer> continuationTokenMap = parseNextLink(continuationToken);
            final Integer topValue = continuationTokenMap.getOrDefault("$top", null);
            final Integer skipValue = continuationTokenMap.getOrDefault("$skip", null);
            return service.analyzeStatusWithResponseAsync(analyzeTasksId, showStats, topValue, skipValue, context)
                .map(this::toAnalyzeTasksPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
        } else {
            return service.analyzeStatusWithResponseAsync(analyzeTasksId, showStats, top, skip, context)
                .map(this::toAnalyzeTasksPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
        }
    }

    private PagedResponse<AnalyzeBatchTasksResult> toAnalyzeTasksPagedResponse(Response<AnalyzeJobState> response) {
        final AnalyzeJobState analyzeJobState = response.getValue();
        return new PagedResponseBase<Void, AnalyzeBatchTasksResult>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            Arrays.asList(toAnalyzeTasks(analyzeJobState)),
            analyzeJobState.getNextLink(),
            null);
    }

    private AnalyzeBatchTasksResult toAnalyzeTasks(AnalyzeJobState analyzeJobState) {
        TasksStateTasks tasksStateTasks = analyzeJobState.getTasks();
        final List<TasksStateTasksEntityRecognitionPiiTasksItem> piiTasksItems =
            tasksStateTasks.getEntityRecognitionPiiTasks();
        final List<TasksStateTasksEntityRecognitionTasksItem> entityRecognitionTasksItems =
            tasksStateTasks.getEntityRecognitionTasks();
        final List<TasksStateTasksKeyPhraseExtractionTasksItem> keyPhraseExtractionTasks =
            tasksStateTasks.getKeyPhraseExtractionTasks();
        IterableStream<RecognizeEntitiesResultCollection> entitiesResultCollections = null;
        IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesResultCollections = null;
        IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesResultCollections = null;
        if (!CoreUtils.isNullOrEmpty(entityRecognitionTasksItems)) {
            entitiesResultCollections = IterableStream.of(entityRecognitionTasksItems.stream()
                .map(taskItem -> toRecognizeEntitiesResultCollectionResponse(taskItem.getResults()))
                .collect(Collectors.toList()));
        }
        if (!CoreUtils.isNullOrEmpty(piiTasksItems)) {
            piiEntitiesResultCollections = IterableStream.of(piiTasksItems.stream()
                .map(taskItem -> toRecognizePiiEntitiesResultCollection(taskItem.getResults()))
                .collect(Collectors.toList()));
        }
        if (!CoreUtils.isNullOrEmpty(keyPhraseExtractionTasks)) {
            keyPhrasesResultCollections = IterableStream.of(keyPhraseExtractionTasks.stream()
                .map(taskItem -> toExtractKeyPhrasesResultCollection(taskItem.getResults()))
                .collect(Collectors.toList()));
        }
        final AnalyzeBatchTasksResult analyzeBatchTasksResult = new AnalyzeBatchTasksResult();
        // TODO: throw errors
//        AnalyzeBatchResultPropertiesHelper.setErrors(analyzeBatchResult,
//            IterableStream.of(analyzeJobState.getErrors()
//                                  .stream()
//                                  .map(Utility::toTextAnalyticsError)
//                                  .collect(Collectors.toList())));

        final RequestStatistics requestStatistics = analyzeJobState.getStatistics();
        TextDocumentBatchStatistics batchStatistics = null;
        if (requestStatistics != null) {
            batchStatistics = new TextDocumentBatchStatistics(
                requestStatistics.getDocumentsCount(), requestStatistics.getErroneousDocumentsCount(),
                requestStatistics.getValidDocumentsCount(), requestStatistics.getTransactionsCount()
            );
        }

        AnalyzeBatchResultPropertiesHelper.setStatistics(analyzeBatchTasksResult, batchStatistics);
        AnalyzeBatchResultPropertiesHelper.setEntityRecognitionResults(analyzeBatchTasksResult,
            entitiesResultCollections);
        AnalyzeBatchResultPropertiesHelper.setPiiEntityRecognitionResults(analyzeBatchTasksResult,
            piiEntitiesResultCollections);
        AnalyzeBatchResultPropertiesHelper.setKeyPhraseExtractionResults(analyzeBatchTasksResult,
            keyPhrasesResultCollections);
        return analyzeBatchTasksResult;
    }

    private Mono<PollResponse<AnalyzeBatchTasksOperationResult>> processAnalyzedModelResponse(
        Response<AnalyzeJobState> analyzeJobStateResponse,
        PollResponse<AnalyzeBatchTasksOperationResult> operationResultPollResponse) {

        LongRunningOperationStatus status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        if (analyzeJobStateResponse.getValue() != null && analyzeJobStateResponse.getValue().getStatus() != null) {
            // TODO: analyzeJobStateResponse.getValue().getStatus() could return a null value
            switch (analyzeJobStateResponse.getValue().getStatus()) {
                case CANCELLING:
                    status = LongRunningOperationStatus.fromString("CANCELLING", false);
                    break;
                case NOT_STARTED:
                case RUNNING:
                    status = LongRunningOperationStatus.IN_PROGRESS;
                    break;
                case SUCCEEDED:
                    status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                    break;
                case REJECTED:
                    status = LongRunningOperationStatus.fromString("REJECTED", true);
                    break;
                case CANCELLED:
                    status = LongRunningOperationStatus.USER_CANCELLED;
                    break;
                case FAILED:
                    //                final TextAnalyticsException exception = new TextAnalyticsException("Analyze operation failed",
                    //                    null, null);
                    //                TextAnalyticsExceptionPropertiesHelper.setErrors(exception,
                    //                    IterableStream.of(analyzeJobStateResponse.getValue().getErrors().stream()
                    //                        .map(error -> new TextAnalyticsError(
                    //                                TextAnalyticsErrorCode.fromString(error.getCode().toString()),
                    //                                error.getMessage(), null)).collect(Collectors.toList())));
                    //                throw logger.logExceptionAsError(exception);
                    status = LongRunningOperationStatus.fromString("FAILED", true);
                    break;
                case PARTIALLY_COMPLETED:
                    status = LongRunningOperationStatus.fromString("PARTIALLY_COMPLETED", true);
                    break;
                default:
                    status = LongRunningOperationStatus.fromString(
                        analyzeJobStateResponse.getValue().getStatus().toString(), true);
                    break;
            }
        }
        AnalyzeBatchOperationResultPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getDisplayName());
        AnalyzeBatchOperationResultPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getCreatedDateTime());
        AnalyzeBatchOperationResultPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getExpirationDateTime());
        AnalyzeBatchOperationResultPropertiesHelper.setLastModifiedAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getLastUpdateDateTime());
        final TasksStateTasks tasksResult = analyzeJobStateResponse.getValue().getTasks();
        AnalyzeBatchOperationResultPropertiesHelper.setFailedTasks(operationResultPollResponse.getValue(),
            tasksResult.getFailed());
        AnalyzeBatchOperationResultPropertiesHelper.setInProgressTasks(operationResultPollResponse.getValue(),
            tasksResult.getInProgress());
        AnalyzeBatchOperationResultPropertiesHelper.setSuccessfullyCompletedTasks(
            operationResultPollResponse.getValue(), tasksResult.getCompleted());
        AnalyzeBatchOperationResultPropertiesHelper.setTotalTasks(operationResultPollResponse.getValue(),
            tasksResult.getTotal());
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }
}
