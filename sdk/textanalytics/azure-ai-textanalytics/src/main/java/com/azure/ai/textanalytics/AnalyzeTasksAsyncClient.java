// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeTasksResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsErrorInformationPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsExceptionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsOperationResultPropertiesHelper;
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
import com.azure.ai.textanalytics.implementation.models.TasksStateTasks;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityRecognitionPiiTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityRecognitionTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksKeyPhraseExtractionTasksItem;
import com.azure.ai.textanalytics.models.AnalyzeTasksOptions;
import com.azure.ai.textanalytics.models.AnalyzeTasksResult;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorInformation;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsOperationResult;
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

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.parseModelId;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.toBatchStatistics;
import static com.azure.ai.textanalytics.implementation.Utility.toExtractKeyPhrasesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toJobState;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeEntitiesResultCollectionResponse;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizePiiEntitiesResultCollection;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

class AnalyzeTasksAsyncClient {
    private final ClientLogger logger = new ClientLogger(AnalyzeTasksAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    AnalyzeTasksAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    PollerFlux<TextAnalyticsOperationResult, PagedFlux<AnalyzeTasksResult>> beginAnalyzeTasks(
        Iterable<TextDocumentInput> documents, AnalyzeTasksOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            if (options == null) {
                options = new AnalyzeTasksOptions();
            }
            final AnalyzeBatchInput analyzeBatchInput =
                new AnalyzeBatchInput()
                    .setAnalysisInput(new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(getJobManifestTasks(options));
            analyzeBatchInput.setDisplayName(options.getDisplayName()); // setDisplayName() returns JobDescriptor
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final Integer finalTop = options.getTop();
            final Integer finalSkip = options.getSkip();
            return new PollerFlux<>(
                options.getPollInterval(),
                activationOperation(
                    service.analyzeWithResponseAsync(analyzeBatchInput,
                        context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                        .map(analyzeResponse -> {
                            final TextAnalyticsOperationResult textAnalyticsOperationResult =
                                new TextAnalyticsOperationResult();
                            TextAnalyticsOperationResultPropertiesHelper.setResultId(textAnalyticsOperationResult,
                                parseModelId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return textAnalyticsOperationResult;
                        })),
                pollingOperation(resultID -> service.analyzeStatusWithResponseAsync(resultID,
                    finalIncludeStatistics, finalTop, finalSkip, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperation(resultId -> Mono.just(getAnalyzeOperationFluxPage(
                    resultId, finalTop, finalSkip, finalIncludeStatistics, context)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    PollerFlux<TextAnalyticsOperationResult, PagedIterable<AnalyzeTasksResult>> beginAnalyzeTasksIterable(
        Iterable<TextDocumentInput> documents, AnalyzeTasksOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            if (options == null) {
                options = new AnalyzeTasksOptions();
            }
            final AnalyzeBatchInput analyzeBatchInput =
                new AnalyzeBatchInput()
                    .setAnalysisInput(new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(getJobManifestTasks(options));
            analyzeBatchInput.setDisplayName(options.getDisplayName()); // setDisplayName() returns JobDescriptor
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final Integer finalTop = options.getTop();
            final Integer finalSkip = options.getSkip();
            return new PollerFlux<>(
                options.getPollInterval(),
                activationOperation(
                    service.analyzeWithResponseAsync(analyzeBatchInput,
                        context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                        .map(analyzeResponse -> {
                            final TextAnalyticsOperationResult textAnalyticsOperationResult =
                                new TextAnalyticsOperationResult();
                            TextAnalyticsOperationResultPropertiesHelper.setResultId(textAnalyticsOperationResult,
                                parseModelId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return textAnalyticsOperationResult;
                        })),
                pollingOperation(resultID -> service.analyzeStatusWithResponseAsync(resultID,
                    finalIncludeStatistics, finalTop, finalSkip, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperationIterable(resultId -> Mono.just(new PagedIterable<>(getAnalyzeOperationFluxPage(
                    resultId, finalTop, finalSkip, finalIncludeStatistics, context))))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private JobManifestTasks getJobManifestTasks(AnalyzeTasksOptions options) {
        return new JobManifestTasks()
            .setEntityRecognitionTasks(options.getEntitiesRecognitionTasks() == null ? null
                : options.getEntitiesRecognitionTasks().stream().map(
                    entitiesTask -> {
                        if (entitiesTask == null) {
                            return null;
                        }
                        final EntitiesTask entitiesTaskImpl = new EntitiesTask();
                        final com.azure.ai.textanalytics.models.EntitiesTaskParameters entitiesTaskParameters =
                            entitiesTask.getParameters();
                        if (entitiesTaskParameters == null) {
                            return entitiesTaskImpl;
                        }
                        entitiesTaskImpl.setParameters(
                            new EntitiesTaskParameters().setModelVersion(entitiesTaskParameters.getModelVersion()));
                        return entitiesTaskImpl;
                    }).collect(Collectors.toList()))
            .setEntityRecognitionPiiTasks(options.getPiiEntitiesRecognitionTasks() == null ? null
                : options.getPiiEntitiesRecognitionTasks().stream().map(
                    piiEntitiesTask -> {
                        if (piiEntitiesTask == null) {
                            return null;
                        }
                        final PiiTask piiTaskImpl = new PiiTask();
                        final com.azure.ai.textanalytics.models.PiiTaskParameters piiTaskParameters =
                            piiEntitiesTask.getParameters();
                        if (piiTaskParameters == null) {
                            return piiTaskImpl;
                        }
                        piiTaskImpl.setParameters(
                            new PiiTaskParameters()
                                .setModelVersion(piiTaskParameters.getModelVersion())
                                .setDomain(PiiTaskParametersDomain.fromString(
                                    piiTaskParameters.getDomain() == null ? null
                                        : piiTaskParameters.getDomain().toString())));
                        return piiTaskImpl;
                    }).collect(Collectors.toList()))
            .setKeyPhraseExtractionTasks(options.getKeyPhrasesExtractionTasks() == null ? null
                : options.getKeyPhrasesExtractionTasks().stream().map(
                    keyPhrasesTask -> {
                        if (keyPhrasesTask == null) {
                            return null;
                        }
                        final com.azure.ai.textanalytics.models.KeyPhrasesTaskParameters keyPhrasesTaskParameters
                            = keyPhrasesTask.getParameters();
                        final KeyPhrasesTask keyPhrasesTaskImpl = new KeyPhrasesTask();
                        if (keyPhrasesTaskParameters == null) {
                            return keyPhrasesTaskImpl;
                        }
                        keyPhrasesTaskImpl.setParameters(
                            new KeyPhrasesTaskParameters().setModelVersion(keyPhrasesTaskParameters.getModelVersion()));
                        return keyPhrasesTaskImpl;
                    }).collect(Collectors.toList()));
    }

    private Function<PollingContext<TextAnalyticsOperationResult>, Mono<TextAnalyticsOperationResult>>
        activationOperation(Mono<TextAnalyticsOperationResult> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<TextAnalyticsOperationResult>, Mono<PollResponse<TextAnalyticsOperationResult>>>
        pollingOperation(Function<String, Mono<Response<AnalyzeJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<TextAnalyticsOperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                final String resultID = operationResultPollResponse.getValue().getResultId();
                return pollingFunction.apply(resultID)
                    .flatMap(modelResponse -> processAnalyzedModelResponse(modelResponse, operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<TextAnalyticsOperationResult>, Mono<PagedFlux<AnalyzeTasksResult>>>
        fetchingOperation(Function<String, Mono<PagedFlux<AnalyzeTasksResult>>> fetchingFunction) {
        return pollingContext -> {
            try {
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                final String resultUUID = pollingContext.getLatestResponse().getValue().getResultId();
                return fetchingFunction.apply(resultUUID);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<TextAnalyticsOperationResult>, Mono<PagedIterable<AnalyzeTasksResult>>>
        fetchingOperationIterable(Function<String, Mono<PagedIterable<AnalyzeTasksResult>>> fetchingFunction) {
        return pollingContext -> {
            try {
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                final String resultUUID = pollingContext.getLatestResponse().getValue().getResultId();
                return fetchingFunction.apply(resultUUID);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    PagedFlux<AnalyzeTasksResult> getAnalyzeOperationFluxPage(String analyzeTasksId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new PagedFlux<>(
            () -> getPage(null, analyzeTasksId, top, skip, showStats, context),
            continuationToken -> getPage(continuationToken, analyzeTasksId, top, skip, showStats, context));
    }

    Mono<PagedResponse<AnalyzeTasksResult>> getPage(String continuationToken, String analyzeTasksId, Integer top,
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

    private PagedResponse<AnalyzeTasksResult> toAnalyzeTasksPagedResponse(Response<AnalyzeJobState> response) {
        final AnalyzeJobState analyzeJobState = response.getValue();
        return new PagedResponseBase<Void, AnalyzeTasksResult>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            Arrays.asList(toAnalyzeTasks(analyzeJobState)),
            analyzeJobState.getNextLink(),
            null);
    }

    private AnalyzeTasksResult toAnalyzeTasks(AnalyzeJobState analyzeJobState) {
        TasksStateTasks tasksStateTasks = analyzeJobState.getTasks();
        final List<TasksStateTasksEntityRecognitionPiiTasksItem> piiTasksItems =
            tasksStateTasks.getEntityRecognitionPiiTasks();
        final List<TasksStateTasksEntityRecognitionTasksItem> entityRecognitionTasksItems =
            tasksStateTasks.getEntityRecognitionTasks();
        final List<TasksStateTasksKeyPhraseExtractionTasksItem> keyPhraseExtractionTasks =
            tasksStateTasks.getKeyPhraseExtractionTasks();
        List<RecognizeEntitiesResultCollection> entitiesResultCollections = null;
        List<RecognizePiiEntitiesResultCollection> piiEntitiesResultCollections = null;
        List<ExtractKeyPhrasesResultCollection> keyPhrasesResultCollections = null;
        if (!CoreUtils.isNullOrEmpty(entityRecognitionTasksItems)) {
            entitiesResultCollections = entityRecognitionTasksItems.stream()
                .map(taskItem -> toRecognizeEntitiesResultCollectionResponse(taskItem.getResults()))
                .collect(Collectors.toList());
        }
        if (!CoreUtils.isNullOrEmpty(piiTasksItems)) {
            piiEntitiesResultCollections = piiTasksItems.stream()
                .map(taskItem -> toRecognizePiiEntitiesResultCollection(taskItem.getResults()))
                .collect(Collectors.toList());
        }
        if (!CoreUtils.isNullOrEmpty(keyPhraseExtractionTasks)) {
            keyPhrasesResultCollections = keyPhraseExtractionTasks.stream()
                .map(taskItem -> toExtractKeyPhrasesResultCollection(taskItem.getResults()))
                .collect(Collectors.toList());
        }
        final AnalyzeTasksResult analyzeTasksResult = new AnalyzeTasksResult(
            analyzeJobState.getJobId(),
            analyzeJobState.getCreatedDateTime(),
            analyzeJobState.getLastUpdateDateTime(),
            toJobState(analyzeJobState.getStatus()),
            analyzeJobState.getDisplayName(),
            analyzeJobState.getExpirationDateTime());
        AnalyzeTasksResultPropertiesHelper.setErrors(analyzeTasksResult,
            analyzeJobState.getErrors().stream().map(Utility::toTextAnalyticsError).collect(Collectors.toList()));
        AnalyzeTasksResultPropertiesHelper.setStatistics(analyzeTasksResult,
            analyzeJobState.getStatistics() == null ? null : toBatchStatistics(analyzeJobState.getStatistics()));
        AnalyzeTasksResultPropertiesHelper.setCompleted(analyzeTasksResult, tasksStateTasks.getCompleted());
        AnalyzeTasksResultPropertiesHelper.setFailed(analyzeTasksResult, tasksStateTasks.getFailed());
        AnalyzeTasksResultPropertiesHelper.setInProgress(analyzeTasksResult, tasksStateTasks.getInProgress());
        AnalyzeTasksResultPropertiesHelper.setTotal(analyzeTasksResult, tasksStateTasks.getTotal());
        AnalyzeTasksResultPropertiesHelper.setEntityRecognitionTasks(analyzeTasksResult, entitiesResultCollections);
        AnalyzeTasksResultPropertiesHelper.setEntityRecognitionPiiTasks(analyzeTasksResult,
            piiEntitiesResultCollections);
        AnalyzeTasksResultPropertiesHelper.setKeyPhraseExtractionTasks(analyzeTasksResult, keyPhrasesResultCollections);
        return analyzeTasksResult;
    }

    private Mono<PollResponse<TextAnalyticsOperationResult>> processAnalyzedModelResponse(
        Response<AnalyzeJobState> analyzeJobStateResponse,
        PollResponse<TextAnalyticsOperationResult> operationResultPollResponse) {

        LongRunningOperationStatus status;
        switch (analyzeJobStateResponse.getValue().getStatus()) {
            case NOT_STARTED:
            case CANCELLING:
            case RUNNING:
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case SUCCEEDED:
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case CANCELLED:
                status = LongRunningOperationStatus.USER_CANCELLED;
                break;
            case FAILED:
                final TextAnalyticsException exception = new TextAnalyticsException("Analyze operation failed",
                    null, null);
                TextAnalyticsExceptionPropertiesHelper.setErrorInformationList(exception,
                    analyzeJobStateResponse.getValue().getErrors().stream()
                        .map(error -> {
                            final TextAnalyticsErrorInformation textAnalyticsErrorInformation =
                                new TextAnalyticsErrorInformation();
                            TextAnalyticsErrorInformationPropertiesHelper.setErrorCode(textAnalyticsErrorInformation,
                                TextAnalyticsErrorCode.fromString(error.getCode().toString()));
                            TextAnalyticsErrorInformationPropertiesHelper.setMessage(textAnalyticsErrorInformation,
                                error.getMessage());
                            return textAnalyticsErrorInformation;
                        }).collect(Collectors.toList()));
                throw logger.logExceptionAsError(exception);
            default:
                status = LongRunningOperationStatus.fromString(
                    analyzeJobStateResponse.getValue().getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }
}
