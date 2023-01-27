// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;


import com.azure.ai.textanalytics.implementation.AnalyzeTextsImpl;
import com.azure.ai.textanalytics.implementation.RecognizeCustomEntitiesOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeCustomEntitiesResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsExceptionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobState;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobsInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROResult;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROTask;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsCancelJobHeaders;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsSubmitJobHeaders;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesLROTask;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesResult;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.CustomEntityRecognitionLROResult;
import com.azure.ai.textanalytics.implementation.models.Error;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.State;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesPagedFlux;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesPagedIterable;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesResultCollection;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.textanalytics.implementation.Utility.enableSyncRestProxy;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExists;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.parseOperationId;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeCustomEntitiesResultCollection;
import static com.azure.ai.textanalytics.implementation.models.State.CANCELLED;
import static com.azure.ai.textanalytics.implementation.models.State.NOT_STARTED;
import static com.azure.ai.textanalytics.implementation.models.State.RUNNING;
import static com.azure.ai.textanalytics.implementation.models.State.SUCCEEDED;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

class RecognizeCustomEntitiesUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(RecognizeCustomEntitiesUtilClient.class);

    private final AnalyzeTextsImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    RecognizeCustomEntitiesUtilClient(AnalyzeTextsImpl service, TextAnalyticsServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    PollerFlux<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedFlux> recognizeCustomEntities(
        Iterable<TextDocumentInput> documents, String projectName, String deploymentName,
        RecognizeCustomEntitiesOptions options, Context context) {
        try {
            throwIfTargetServiceVersionFound(this.serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1),
                getUnsupportedServiceApiVersionMessage("beginRecognizeCustomEntities", serviceVersion,
                    TextAnalyticsServiceVersion.V2022_05_01));
            inputDocumentsValidation(documents);
            options = getNotNullRecognizeCustomEntitiesOptions(options);
            final Context finalContext = getNotNullContext(context)
                .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final StringIndexType finalStringIndexType = StringIndexType.UTF16CODE_UNIT;
            final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final String displayName = options.getDisplayName();

            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    service.submitJobWithResponseAsync(
                        new AnalyzeTextJobsInput()
                            .setDisplayName(displayName)
                            .setAnalysisInput(
                                new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents)))
                            .setTasks(Arrays.asList(
                                new CustomEntitiesLROTask().setParameters(
                                    new CustomEntitiesTaskParameters()
                                        .setStringIndexType(finalStringIndexType)
                                        .setProjectName(projectName)
                                        .setDeploymentName(deploymentName)
                                        .setLoggingOptOut(finalLoggingOptOut)))),
                        finalContext)
                        .map(responseBase -> {
                            final RecognizeCustomEntitiesOperationDetail operationDetail =
                                new RecognizeCustomEntitiesOperationDetail();
                            RecognizeCustomEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(responseBase.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })),
                pollingOperationTextJob(
                    operationId -> service.jobStatusWithResponseAsync(operationId,
                        finalIncludeStatistics, null, null, finalContext)),
                cancelOperationTextJob(
                    operationId -> service.cancelJobWithResponseAsync(operationId, finalContext)),
                fetchingOperationTextJob(
                    operationId -> Mono.just(getRecognizeCustomEntitiesPagedFlux(operationId, null, null,
                        finalIncludeStatistics, finalContext)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable>
        recognizeCustomEntitiesPagedIterable(Iterable<TextDocumentInput> documents,
            String projectName, String deploymentName, RecognizeCustomEntitiesOptions options, Context context) {
        try {
            throwIfTargetServiceVersionFound(this.serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1),
                getUnsupportedServiceApiVersionMessage("beginRecognizeCustomEntities", serviceVersion,
                    TextAnalyticsServiceVersion.V2022_05_01));
            inputDocumentsValidation(documents);
            options = getNotNullRecognizeCustomEntitiesOptions(options);
            final Context finalContext = enableSyncRestProxy(getNotNullContext(context))
                .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final StringIndexType finalStringIndexType = StringIndexType.UTF16CODE_UNIT;
            final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
            final String displayName = options.getDisplayName();

            final CustomEntitiesLROTask task = new CustomEntitiesLROTask().setParameters(
                new CustomEntitiesTaskParameters()
                    .setStringIndexType(finalStringIndexType)
                    .setProjectName(projectName)
                    .setDeploymentName(deploymentName)
                    .setLoggingOptOut(finalLoggingOptOut));

            return SyncPoller.createPoller(
                DEFAULT_POLL_INTERVAL,
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    activationOperationSync(documents, task, displayName, finalContext).apply(cxt)),
                pollingOperationTextJobSync(operationId -> service.jobStatusWithResponse(operationId,
                    finalIncludeStatistics, null, null, finalContext)),
                cancelOperationTextJobSync(operationId -> service.cancelJobWithResponse(operationId, finalContext)),
                fetchingOperationSync(
                    operationId -> getRecognizeCustomEntitiesPagedIterable(operationId, null, null,
                        finalIncludeStatistics, finalContext)));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError((HttpResponseException) mapToHttpResponseExceptionIfExists(ex));
        }
    }

    RecognizeCustomEntitiesPagedFlux getRecognizeCustomEntitiesPagedFlux(
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        return new RecognizeCustomEntitiesPagedFlux(
            () -> (continuationToken, pageSize) ->
                getPagedResult(continuationToken, operationId, top, skip, showStats, context).flux());
    }

    RecognizeCustomEntitiesPagedIterable getRecognizeCustomEntitiesPagedIterable(
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        return new RecognizeCustomEntitiesPagedIterable(
            () -> (continuationToken, pageSize) ->
                getPagedResultSync(continuationToken, operationId, top, skip, showStats, context)
        );
    }

    Mono<PagedResponse<RecognizeCustomEntitiesResultCollection>> getPagedResult(String continuationToken,
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        try {
            if (continuationToken != null) {
                final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
                final Integer topValue = (Integer) continuationTokenMap.getOrDefault("$top", null);
                final Integer skipValue = (Integer) continuationTokenMap.getOrDefault("$skip", null);
                final Boolean showStatsValue = (Boolean) continuationTokenMap.getOrDefault(showStats, false);
                return service.jobStatusWithResponseAsync(operationId, showStatsValue, topValue, skipValue,
                    context)
                    .map(this::toCustomEntitiesPagedResponse)
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } else {
                return service.jobStatusWithResponseAsync(operationId, showStats, top, skip, context)
                    .map(this::toCustomEntitiesPagedResponse)
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            }
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    PagedResponse<RecognizeCustomEntitiesResultCollection> getPagedResultSync(String continuationToken,
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
            top = (Integer) continuationTokenMap.getOrDefault("$top", null);
            skip = (Integer) continuationTokenMap.getOrDefault("$skip", null);
            showStats = (Boolean) continuationTokenMap.getOrDefault(showStats, false);
        }
        return toCustomEntitiesPagedResponse(service.jobStatusWithResponse(operationId, showStats, top, skip, context));
    }

    private PagedResponse<RecognizeCustomEntitiesResultCollection> toCustomEntitiesPagedResponse(
        Response<AnalyzeTextJobState> response) {

        final AnalyzeTextJobState jobState = response.getValue();
        final List<AnalyzeTextLROResult> lroResults = jobState.getTasks().getItems();

        CustomEntityRecognitionLROResult customEntityLROResult = (CustomEntityRecognitionLROResult) lroResults.get(0);
        final CustomEntitiesResult customEntitiesResult = customEntityLROResult.getResults();
        final RecognizeCustomEntitiesResultCollection recognizeCustomEntitiesResultCollection =
            toRecognizeCustomEntitiesResultCollection(customEntitiesResult);
        final RequestStatistics requestStatistics = customEntitiesResult.getStatistics();
        if (requestStatistics != null) {
            final TextDocumentBatchStatistics batchStatistic = new TextDocumentBatchStatistics(
                requestStatistics.getDocumentsCount(), requestStatistics.getValidDocumentsCount(),
                requestStatistics.getErroneousDocumentsCount(), requestStatistics.getTransactionsCount()
            );
            RecognizeCustomEntitiesResultCollectionPropertiesHelper.setStatistics(
                recognizeCustomEntitiesResultCollection, batchStatistic);
        }

        final List<Error> errors = jobState.getErrors();

        if (!CoreUtils.isNullOrEmpty(errors)) {
            final TextAnalyticsException textAnalyticsException = new TextAnalyticsException(
                "Recognize custom entities operation failed", null, null);
            final IterableStream<com.azure.ai.textanalytics.models.TextAnalyticsError> textAnalyticsErrors =
                IterableStream.of(errors.stream().map(Utility::toTextAnalyticsError).collect(Collectors.toList()));
            TextAnalyticsExceptionPropertiesHelper.setErrors(textAnalyticsException, textAnalyticsErrors);
            throw LOGGER.logExceptionAsError(textAnalyticsException);
        }

        return new PagedResponseBase<Void, RecognizeCustomEntitiesResultCollection>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            Arrays.asList(recognizeCustomEntitiesResultCollection),
            jobState.getNextLink(),
            null);
    }

    // Activation operation
    private Function<PollingContext<RecognizeCustomEntitiesOperationDetail>,
        Mono<RecognizeCustomEntitiesOperationDetail>> activationOperation(
        Mono<RecognizeCustomEntitiesOperationDetail> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<RecognizeCustomEntitiesOperationDetail>, RecognizeCustomEntitiesOperationDetail>
        activationOperationSync(Iterable<TextDocumentInput> documents, AnalyzeTextLROTask task, String displayName,
            Context context) {
        return pollingContext -> {
            final ResponseBase<AnalyzeTextsSubmitJobHeaders, Void> analyzeResponse =
                service.submitJobWithResponse(
                    new AnalyzeTextJobsInput()
                        .setDisplayName(displayName)
                        .setAnalysisInput(new MultiLanguageAnalysisInput()
                            .setDocuments(toMultiLanguageInput(documents)))
                        .setTasks(Arrays.asList(task)),
                    context);
            final RecognizeCustomEntitiesOperationDetail operationDetail =
                new RecognizeCustomEntitiesOperationDetail();
            RecognizeCustomEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
            return operationDetail;
        };
    }

    // Polling operation
    private Function<PollingContext<RecognizeCustomEntitiesOperationDetail>,
        Mono<PollResponse<RecognizeCustomEntitiesOperationDetail>>> pollingOperationTextJob(
            Function<UUID, Mono<Response<AnalyzeTextJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<RecognizeCustomEntitiesOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return pollingFunction.apply(operationId)
                    .flatMap(modelResponse ->
                        Mono.just(processAnalyzeTextModelResponse(modelResponse, operationResultPollResponse)))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<RecognizeCustomEntitiesOperationDetail>,
        PollResponse<RecognizeCustomEntitiesOperationDetail>> pollingOperationTextJobSync(
        Function<UUID, Response<AnalyzeTextJobState>> pollingFunction) {
        return pollingContext -> {
            final PollResponse<RecognizeCustomEntitiesOperationDetail> operationResultPollResponse =
                pollingContext.getLatestResponse();
            final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
            return processAnalyzeTextModelResponse(pollingFunction.apply(operationId), operationResultPollResponse);
        };
    }

    // Fetching operation
    private Function<PollingContext<RecognizeCustomEntitiesOperationDetail>,
        Mono<RecognizeCustomEntitiesPagedFlux>> fetchingOperationTextJob(
            Function<UUID, Mono<RecognizeCustomEntitiesPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<RecognizeCustomEntitiesOperationDetail>,
        RecognizeCustomEntitiesPagedIterable> fetchingOperationSync(
        final Function<UUID, RecognizeCustomEntitiesPagedIterable> fetchingFunction) {
        return pollingContext -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
            return fetchingFunction.apply(resultUuid);
        };
    }

    // Cancel operation
    private BiFunction<PollingContext<RecognizeCustomEntitiesOperationDetail>,
        PollResponse<RecognizeCustomEntitiesOperationDetail>,
        Mono<RecognizeCustomEntitiesOperationDetail>> cancelOperationTextJob(
        Function<UUID, Mono<ResponseBase<AnalyzeTextsCancelJobHeaders, Void>>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            try {
                return cancelFunction.apply(resultUuid)
                    .map(cancelJobResponse -> {
                        final RecognizeCustomEntitiesOperationDetail operationResult =
                            new RecognizeCustomEntitiesOperationDetail();
                        RecognizeCustomEntitiesOperationDetailPropertiesHelper.setOperationId(operationResult,
                            parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
                        return operationResult;
                    }).onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private BiFunction<PollingContext<RecognizeCustomEntitiesOperationDetail>,
        PollResponse<RecognizeCustomEntitiesOperationDetail>, RecognizeCustomEntitiesOperationDetail>
        cancelOperationTextJobSync(Function<UUID, ResponseBase<AnalyzeTextsCancelJobHeaders, Void>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            ResponseBase<AnalyzeTextsCancelJobHeaders, Void> cancelJobResponse = cancelFunction.apply(resultUuid);
            final RecognizeCustomEntitiesOperationDetail operationResult =
                new RecognizeCustomEntitiesOperationDetail();
            RecognizeCustomEntitiesOperationDetailPropertiesHelper.setOperationId(operationResult,
                parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
            return operationResult;
        };
    }

    private PollResponse<RecognizeCustomEntitiesOperationDetail> processAnalyzeTextModelResponse(
        Response<AnalyzeTextJobState> analyzeOperationResultResponse,
        PollResponse<RecognizeCustomEntitiesOperationDetail> operationResultPollResponse) {
        LongRunningOperationStatus status;
        State state = analyzeOperationResultResponse.getValue().getStatus();
        if (NOT_STARTED.equals(state) || RUNNING.equals(state)) {
            status = LongRunningOperationStatus.IN_PROGRESS;
        } else if (SUCCEEDED.equals(state)) {
            status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (CANCELLED.equals(state)) {
            status = LongRunningOperationStatus.USER_CANCELLED;
        } else {
            status = LongRunningOperationStatus.fromString(
                analyzeOperationResultResponse.getValue().getStatus().toString(), true);
        }
        RecognizeCustomEntitiesOperationDetailPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getDisplayName());
        RecognizeCustomEntitiesOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getCreatedDateTime());
        RecognizeCustomEntitiesOperationDetailPropertiesHelper.setLastModifiedAt(
            operationResultPollResponse.getValue(), analyzeOperationResultResponse.getValue().getLastUpdatedDateTime());
        RecognizeCustomEntitiesOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getExpirationDateTime());
        return new PollResponse<>(status, operationResultPollResponse.getValue());
    }

    private RecognizeCustomEntitiesOptions getNotNullRecognizeCustomEntitiesOptions(
        RecognizeCustomEntitiesOptions options) {
        return options == null ? new RecognizeCustomEntitiesOptions() : options;
    }
}
