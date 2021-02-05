// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeBatchActionsOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeBatchActionsResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ExtractKeyPhrasesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizePiiEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsActionResultPropertiesHelper;
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
import com.azure.ai.textanalytics.models.AnalyzeBatchActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeBatchActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeBatchActionsResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
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
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.parseOperationId;
import static com.azure.ai.textanalytics.implementation.Utility.toExtractKeyPhrasesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeEntitiesResultCollectionResponse;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizePiiEntitiesResultCollection;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

class AnalyzeBatchActionsAsyncClient {
    private final ClientLogger logger = new ClientLogger(AnalyzeBatchActionsAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    AnalyzeBatchActionsAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    PollerFlux<AnalyzeBatchActionsOperationDetail, PagedFlux<AnalyzeBatchActionsResult>> beginAnalyzeBatchActions(
        Iterable<TextDocumentInput> documents, TextAnalyticsActions actions, AnalyzeBatchActionsOptions options,
        Context context) {
        try {
            inputDocumentsValidation(documents);
            options = getNotNullAnalyzeBatchActionsOptions(options);
            final Context finalContext = getNotNullContext(context);
            final AnalyzeBatchInput analyzeBatchInput =
                new AnalyzeBatchInput()
                    .setAnalysisInput(new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(getJobManifestTasks(actions));
            analyzeBatchInput.setDisplayName(actions.getDisplayName());
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                // TODO: Be able to set the poll interval manually by user.
                //  https://github.com/Azure/azure-sdk-for-java/issues/18827
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    service.analyzeWithResponseAsync(analyzeBatchInput,
                        finalContext.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                        .map(analyzeResponse -> {
                            final AnalyzeBatchActionsOperationDetail textAnalyticsOperationResult =
                                new AnalyzeBatchActionsOperationDetail();
                            AnalyzeBatchActionsOperationDetailPropertiesHelper
                                .setOperationId(textAnalyticsOperationResult,
                                    parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return textAnalyticsOperationResult;
                        })),
                pollingOperation(operationId -> service.analyzeStatusWithResponseAsync(operationId,
                    finalIncludeStatistics, null, null, finalContext)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperation(operationId -> Mono.just(getAnalyzeOperationFluxPage(
                    operationId, null, null, finalIncludeStatistics, finalContext)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    PollerFlux<AnalyzeBatchActionsOperationDetail, PagedIterable<AnalyzeBatchActionsResult>>
        beginAnalyzeBatchActionsIterable(Iterable<TextDocumentInput> documents, TextAnalyticsActions actions,
            AnalyzeBatchActionsOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            options = getNotNullAnalyzeBatchActionsOptions(options);
            final Context finalContext = getNotNullContext(context);
            final AnalyzeBatchInput analyzeBatchInput =
                new AnalyzeBatchInput()
                    .setAnalysisInput(new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(getJobManifestTasks(actions));
            analyzeBatchInput.setDisplayName(actions.getDisplayName());
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                // TODO: Be able to set the poll interval manually by user.
                //  https://github.com/Azure/azure-sdk-for-java/issues/18827
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    service.analyzeWithResponseAsync(analyzeBatchInput,
                        finalContext.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                        .map(analyzeResponse -> {
                            final AnalyzeBatchActionsOperationDetail operationDetail =
                                new AnalyzeBatchActionsOperationDetail();
                            AnalyzeBatchActionsOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })),
                pollingOperation(operationId -> service.analyzeStatusWithResponseAsync(operationId,
                    finalIncludeStatistics, null, null, finalContext)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperationIterable(operationId -> Mono.just(new PagedIterable<>(getAnalyzeOperationFluxPage(
                    operationId, null, null, finalIncludeStatistics, finalContext))))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private JobManifestTasks getJobManifestTasks(TextAnalyticsActions actions) {
        return new JobManifestTasks()
            .setEntityRecognitionTasks(actions.getRecognizeEntitiesOptions() == null ? null
                : StreamSupport.stream(actions.getRecognizeEntitiesOptions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final EntitiesTask entitiesTask = new EntitiesTask();
                        entitiesTask.setParameters(
                            // TODO: currently, service does not set their default values for model version, we
                            // temporally set the default value to 'latest' until service correct it.
                            // https://github.com/Azure/azure-sdk-for-java/issues/17625
                            new EntitiesTaskParameters()
                                .setModelVersion(getNotNullModelVersion(action.getModelVersion())));
                        return entitiesTask;
                    }).collect(Collectors.toList()))
            .setEntityRecognitionPiiTasks(actions.getRecognizePiiEntitiesOptions() == null ? null
                : StreamSupport.stream(actions.getRecognizePiiEntitiesOptions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final PiiTask piiTask = new PiiTask();
                        piiTask.setParameters(
                            new PiiTaskParameters()
                                // TODO: currently, service does not set their default values for model version, we
                                // temporally set the default value to 'latest' until service correct it.
                                // https://github.com/Azure/azure-sdk-for-java/issues/17625
                                .setModelVersion(getNotNullModelVersion(action.getModelVersion()))
                                .setDomain(PiiTaskParametersDomain.fromString(
                                    action.getDomainFilter() == null ? null
                                        : action.getDomainFilter().toString())));
                        return piiTask;
                    }).collect(Collectors.toList()))
            .setKeyPhraseExtractionTasks(actions.getExtractKeyPhrasesOptions() == null ? null
                : StreamSupport.stream(actions.getExtractKeyPhrasesOptions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final KeyPhrasesTask keyPhrasesTask = new KeyPhrasesTask();
                        keyPhrasesTask.setParameters(
                            // TODO: currently, service does not set their default values for model version, we
                            // temporally set the default value to 'latest' until service correct it.
                            // https://github.com/Azure/azure-sdk-for-java/issues/17625
                            new KeyPhrasesTaskParameters()
                                .setModelVersion(getNotNullModelVersion(action.getModelVersion())));
                        return keyPhrasesTask;
                    }).collect(Collectors.toList()));
    }

    private Function<PollingContext<AnalyzeBatchActionsOperationDetail>, Mono<AnalyzeBatchActionsOperationDetail>>
        activationOperation(Mono<AnalyzeBatchActionsOperationDetail> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeBatchActionsOperationDetail>, Mono<PollResponse<AnalyzeBatchActionsOperationDetail>>>
        pollingOperation(Function<String, Mono<Response<AnalyzeJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeBatchActionsOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                final String operationId = operationResultPollResponse.getValue().getOperationId();
                return pollingFunction.apply(operationId)
                    .flatMap(modelResponse -> processAnalyzedModelResponse(modelResponse, operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeBatchActionsOperationDetail>, Mono<PagedFlux<AnalyzeBatchActionsResult>>>
        fetchingOperation(Function<String, Mono<PagedFlux<AnalyzeBatchActionsResult>>> fetchingFunction) {
        return pollingContext -> {
            try {
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                final String operationId = pollingContext.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(operationId);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeBatchActionsOperationDetail>, Mono<PagedIterable<AnalyzeBatchActionsResult>>>
        fetchingOperationIterable(Function<String, Mono<PagedIterable<AnalyzeBatchActionsResult>>> fetchingFunction) {
        return pollingContext -> {
            try {
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                final String operationId = pollingContext.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(operationId);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    PagedFlux<AnalyzeBatchActionsResult> getAnalyzeOperationFluxPage(String operationId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new PagedFlux<>(
            () -> getPage(null, operationId, top, skip, showStats, context),
            continuationToken -> getPage(continuationToken, operationId, top, skip, showStats, context));
    }

    Mono<PagedResponse<AnalyzeBatchActionsResult>> getPage(String continuationToken, String operationId, Integer top,
        Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Integer> continuationTokenMap = parseNextLink(continuationToken);
            final Integer topValue = continuationTokenMap.getOrDefault("$top", null);
            final Integer skipValue = continuationTokenMap.getOrDefault("$skip", null);
            return service.analyzeStatusWithResponseAsync(operationId, showStats, topValue, skipValue, context)
                .map(this::toAnalyzeTasksPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
        } else {
            return service.analyzeStatusWithResponseAsync(operationId, showStats, top, skip, context)
                .map(this::toAnalyzeTasksPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
        }
    }

    private PagedResponse<AnalyzeBatchActionsResult> toAnalyzeTasksPagedResponse(Response<AnalyzeJobState> response) {
        final AnalyzeJobState analyzeJobState = response.getValue();
        return new PagedResponseBase<Void, AnalyzeBatchActionsResult>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            Arrays.asList(toAnalyzeTasks(analyzeJobState)),
            analyzeJobState.getNextLink(),
            null);
    }

    private AnalyzeBatchActionsResult toAnalyzeTasks(AnalyzeJobState analyzeJobState) {
        TasksStateTasks tasksStateTasks = analyzeJobState.getTasks();
        final List<TasksStateTasksEntityRecognitionPiiTasksItem> piiTasksItems =
            tasksStateTasks.getEntityRecognitionPiiTasks();
        final List<TasksStateTasksEntityRecognitionTasksItem> entityRecognitionTasksItems =
            tasksStateTasks.getEntityRecognitionTasks();
        final List<TasksStateTasksKeyPhraseExtractionTasksItem> keyPhraseExtractionTasks =
            tasksStateTasks.getKeyPhraseExtractionTasks();
        IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults = null;
        IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults = null;
        IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults = null;
        if (!CoreUtils.isNullOrEmpty(entityRecognitionTasksItems)) {
            recognizeEntitiesActionResults = IterableStream.of(entityRecognitionTasksItems.stream()
                .map(taskItem -> {
                    RecognizeEntitiesActionResult actionResult = new RecognizeEntitiesActionResult();
                    RecognizeEntitiesActionResultPropertiesHelper.setResult(actionResult,
                        toRecognizeEntitiesResultCollectionResponse(taskItem.getResults()));
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        taskItem.getLastUpdateDateTime());
                    return actionResult;
                })
                .collect(Collectors.toList()));
        }
        if (!CoreUtils.isNullOrEmpty(piiTasksItems)) {
            recognizePiiEntitiesActionResults = IterableStream.of(piiTasksItems.stream()
                .map(taskItem -> {
                    RecognizePiiEntitiesActionResult actionResult = new RecognizePiiEntitiesActionResult();
                    RecognizePiiEntitiesActionResultPropertiesHelper.setResult(actionResult,
                        toRecognizePiiEntitiesResultCollection(taskItem.getResults()));
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        taskItem.getLastUpdateDateTime());
                    return actionResult;
                })
                .collect(Collectors.toList()));
        }
        if (!CoreUtils.isNullOrEmpty(keyPhraseExtractionTasks)) {
            extractKeyPhrasesActionResults = IterableStream.of(keyPhraseExtractionTasks.stream()
                .map(taskItem -> {
                    ExtractKeyPhrasesActionResult actionResult = new ExtractKeyPhrasesActionResult();
                    ExtractKeyPhrasesActionResultPropertiesHelper.setResult(actionResult,
                        toExtractKeyPhrasesResultCollection(taskItem.getResults()));
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        taskItem.getLastUpdateDateTime());
                    return actionResult;
                })
                .collect(Collectors.toList()));
        }
        final AnalyzeBatchActionsResult analyzeBatchActionsResult = new AnalyzeBatchActionsResult();

        final RequestStatistics requestStatistics = analyzeJobState.getStatistics();
        TextDocumentBatchStatistics batchStatistics = null;
        if (requestStatistics != null) {
            batchStatistics = new TextDocumentBatchStatistics(
                requestStatistics.getDocumentsCount(), requestStatistics.getErroneousDocumentsCount(),
                requestStatistics.getValidDocumentsCount(), requestStatistics.getTransactionsCount()
            );
        }

        AnalyzeBatchActionsResultPropertiesHelper.setStatistics(analyzeBatchActionsResult, batchStatistics);
        AnalyzeBatchActionsResultPropertiesHelper.setRecognizeEntitiesActionResults(analyzeBatchActionsResult,
            recognizeEntitiesActionResults);
        AnalyzeBatchActionsResultPropertiesHelper.setRecognizePiiEntitiesActionResults(analyzeBatchActionsResult,
            recognizePiiEntitiesActionResults);
        AnalyzeBatchActionsResultPropertiesHelper.setExtractKeyPhrasesActionResults(analyzeBatchActionsResult,
            extractKeyPhrasesActionResults);
        return analyzeBatchActionsResult;
    }

    private Mono<PollResponse<AnalyzeBatchActionsOperationDetail>> processAnalyzedModelResponse(
        Response<AnalyzeJobState> analyzeJobStateResponse,
        PollResponse<AnalyzeBatchActionsOperationDetail> operationResultPollResponse) {

        LongRunningOperationStatus status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        if (analyzeJobStateResponse.getValue() != null && analyzeJobStateResponse.getValue().getStatus() != null) {
            switch (analyzeJobStateResponse.getValue().getStatus()) {
                case NOT_STARTED:
                case RUNNING:
                    status = LongRunningOperationStatus.IN_PROGRESS;
                    break;
                case SUCCEEDED:
                    status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                    break;
                case CANCELLED:
                    status = LongRunningOperationStatus.USER_CANCELLED;
                    break;
                default:
                    status = LongRunningOperationStatus.fromString(
                        analyzeJobStateResponse.getValue().getStatus().toString(), true);
                    break;
            }
        }
        AnalyzeBatchActionsOperationDetailPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getDisplayName());
        AnalyzeBatchActionsOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getCreatedDateTime());
        AnalyzeBatchActionsOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getExpirationDateTime());
        AnalyzeBatchActionsOperationDetailPropertiesHelper.setLastModifiedAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getLastUpdateDateTime());
        final TasksStateTasks tasksResult = analyzeJobStateResponse.getValue().getTasks();
        AnalyzeBatchActionsOperationDetailPropertiesHelper.setActionsFailed(operationResultPollResponse.getValue(),
            tasksResult.getFailed());
        AnalyzeBatchActionsOperationDetailPropertiesHelper.setActionsInProgress(operationResultPollResponse.getValue(),
            tasksResult.getInProgress());
        AnalyzeBatchActionsOperationDetailPropertiesHelper.setActionsSucceeded(
            operationResultPollResponse.getValue(), tasksResult.getCompleted());
        AnalyzeBatchActionsOperationDetailPropertiesHelper.setActionsInTotal(operationResultPollResponse.getValue(),
            tasksResult.getTotal());
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }

    private Context getNotNullContext(Context context) {
        return context == null ? Context.NONE : context;
    }

    private AnalyzeBatchActionsOptions getNotNullAnalyzeBatchActionsOptions(AnalyzeBatchActionsOptions options) {
        return options == null ? new AnalyzeBatchActionsOptions() : options;
    }

    private String getNotNullModelVersion(String modelVersion) {
        return modelVersion == null ? "latest" : modelVersion;
    }
}
