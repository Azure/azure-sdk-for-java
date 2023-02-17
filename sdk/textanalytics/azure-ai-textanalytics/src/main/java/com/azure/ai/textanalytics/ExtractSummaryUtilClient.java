// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeTextsImpl;
import com.azure.ai.textanalytics.implementation.ExtractSummaryOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ExtractSummaryResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsExceptionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobState;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobsInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROResult;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROTask;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsCancelJobHeaders;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsSubmitJobHeaders;
import com.azure.ai.textanalytics.implementation.models.Error;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationLROResult;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationLROTask;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationResult;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationSortingCriteria;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.State;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.models.ExtractSummaryOperationDetail;
import com.azure.ai.textanalytics.models.ExtractSummaryOptions;
import com.azure.ai.textanalytics.models.SummarySentencesOrder;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractSummaryPagedFlux;
import com.azure.ai.textanalytics.util.ExtractSummaryPagedIterable;
import com.azure.ai.textanalytics.util.ExtractSummaryResultCollection;
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
import static com.azure.ai.textanalytics.implementation.Utility.getHttpResponseException;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getShowStatsContinuesToken;
import static com.azure.ai.textanalytics.implementation.Utility.getSkipContinuesToken;
import static com.azure.ai.textanalytics.implementation.Utility.getTopContinuesToken;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.parseOperationId;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toExtractSummaryResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.models.State.CANCELLED;
import static com.azure.ai.textanalytics.implementation.models.State.NOT_STARTED;
import static com.azure.ai.textanalytics.implementation.models.State.RUNNING;
import static com.azure.ai.textanalytics.implementation.models.State.SUCCEEDED;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing extractive summarization endpoints.
 */
class ExtractSummaryUtilClient {
    private final AnalyzeTextsImpl service;
    private final TextAnalyticsServiceVersion serviceVersion;

    private static final ClientLogger LOGGER = new ClientLogger(ExtractSummaryUtilClient.class);

    ExtractSummaryUtilClient(AnalyzeTextsImpl service, TextAnalyticsServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    PollerFlux<ExtractSummaryOperationDetail, ExtractSummaryPagedFlux> extractSummaryAsync(
        Iterable<TextDocumentInput> documents, ExtractSummaryOptions options, Context context) {
        try {
            checkUnsupportedServiceVersionForExtractSummary();
            inputDocumentsValidation(documents);
            options = getNotNullExtractSummaryOptions(options);
            final Context finalContext = getNotNullContext(context)
                .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final SummarySentencesOrder orderBy = options.getOrderBy();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    service.submitJobWithResponseAsync(
                        new AnalyzeTextJobsInput()
                            .setDisplayName(options.getDisplayName())
                            .setAnalysisInput(
                                new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents)))
                            .setTasks(Arrays.asList(
                                new ExtractiveSummarizationLROTask().setParameters(
                                    new ExtractiveSummarizationTaskParameters()
                                        .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                        .setSortBy(orderBy == null ? null
                                            : ExtractiveSummarizationSortingCriteria.fromString(orderBy.toString()))
                                        .setSentenceCount(options.getMaxSentenceCount())
                                        .setModelVersion(options.getModelVersion())
                                        .setLoggingOptOut(options.isServiceLogsDisabled())
                                ))),
                        finalContext)
                        .map(responseBase -> {
                            final ExtractSummaryOperationDetail operationDetail =
                                new ExtractSummaryOperationDetail();
                            ExtractSummaryOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(responseBase.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })
                ),
                pollingOperation(
                    operationId -> service.jobStatusWithResponseAsync(operationId,
                        finalIncludeStatistics, null, null, finalContext)),
                cancelOperation(
                    operationId -> service.cancelJobWithResponseAsync(operationId, finalContext)),
                fetchingOperation(
                    operationId -> Mono.just(getExtractSummaryPagedFlux(operationId, null, null,
                        finalIncludeStatistics, finalContext)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    SyncPoller<ExtractSummaryOperationDetail, ExtractSummaryPagedIterable> extractSummaryPagedIterable(
        Iterable<TextDocumentInput> documents, ExtractSummaryOptions options, Context context) {
        try {
            checkUnsupportedServiceVersionForExtractSummary();
            inputDocumentsValidation(documents);
            options = getNotNullExtractSummaryOptions(options);
            final Context finalContext = enableSyncRestProxy(getNotNullContext(context))
                .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final String displayName = options.getDisplayName();
            final SummarySentencesOrder orderBy = options.getOrderBy();

            final ExtractiveSummarizationLROTask task = new ExtractiveSummarizationLROTask().setParameters(
                new ExtractiveSummarizationTaskParameters()
                    .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                    .setSortBy(orderBy == null ? null
                        : ExtractiveSummarizationSortingCriteria.fromString(orderBy.toString()))
                    .setSentenceCount(options.getMaxSentenceCount())
                    .setModelVersion(options.getModelVersion())
                    .setLoggingOptOut(options.isServiceLogsDisabled()));
            return SyncPoller.createPoller(
                DEFAULT_POLL_INTERVAL,
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    activationOperationSync(documents, task, displayName, finalContext).apply(cxt)),
                pollingOperationSync(operationId -> service.jobStatusWithResponse(operationId,
                    finalIncludeStatistics, null, null, finalContext)),
                cancelOperationSync(operationId -> service.cancelJobWithResponse(operationId, finalContext)),
                fetchingOperationIterable(
                    operationId -> getExtractSummaryPagedIterable(operationId, null, null,
                        finalIncludeStatistics, finalContext)));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    ExtractSummaryPagedFlux getExtractSummaryPagedFlux(
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        return new ExtractSummaryPagedFlux(
            () -> (continuationToken, pageSize) ->
                getPagedResult(continuationToken, operationId, top, skip, showStats, context).flux());
    }

    ExtractSummaryPagedIterable getExtractSummaryPagedIterable(
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        return new ExtractSummaryPagedIterable(
            () -> (continuationToken, pageSize) ->
                getPagedResultSync(continuationToken, operationId, top, skip, showStats, context));
    }

    Mono<PagedResponse<ExtractSummaryResultCollection>> getPagedResult(String continuationToken,
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        try {
            if (continuationToken != null) {
                final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
                top = getTopContinuesToken(continuationTokenMap);
                skip = getSkipContinuesToken(continuationTokenMap);
                showStats = getShowStatsContinuesToken(continuationTokenMap);
            }
            return service.jobStatusWithResponseAsync(operationId, showStats, top, skip, context)
                .map(this::toExtractSummaryResultCollectionPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    PagedResponse<ExtractSummaryResultCollection> getPagedResultSync(String continuationToken,
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
            top = getTopContinuesToken(continuationTokenMap);
            skip = getSkipContinuesToken(continuationTokenMap);
            showStats = getShowStatsContinuesToken(continuationTokenMap);
        }
        return toExtractSummaryResultCollectionPagedResponse(
            service.jobStatusWithResponse(operationId, showStats, top, skip, context));
    }

    private PagedResponse<ExtractSummaryResultCollection> toExtractSummaryResultCollectionPagedResponse(
        Response<AnalyzeTextJobState> response) {

        final AnalyzeTextJobState jobState = response.getValue();
        final List<AnalyzeTextLROResult> lroResults = jobState.getTasks().getItems();

        final ExtractiveSummarizationResult extractiveSummarizationResult;
        final AnalyzeTextLROResult lroResult = lroResults.get(0);
        if (lroResult instanceof ExtractiveSummarizationLROResult) {
            ExtractiveSummarizationLROResult extractiveSummarizationLROResult =
                (ExtractiveSummarizationLROResult) lroResults.get(0);
            extractiveSummarizationResult = extractiveSummarizationLROResult.getResults();
        } else {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Invalid class type returned: " + lroResult.getClass().getName()));
        }

        final ExtractSummaryResultCollection extractSummaryResultCollection =
            toExtractSummaryResultCollection(extractiveSummarizationResult);
        final RequestStatistics requestStatistics = extractiveSummarizationResult.getStatistics();
        if (requestStatistics != null) {
            final TextDocumentBatchStatistics batchStatistic = new TextDocumentBatchStatistics(
                requestStatistics.getDocumentsCount(), requestStatistics.getValidDocumentsCount(),
                requestStatistics.getErroneousDocumentsCount(), requestStatistics.getTransactionsCount());
            ExtractSummaryResultCollectionPropertiesHelper.setStatistics(
                extractSummaryResultCollection, batchStatistic);
        }

        final List<Error> errors = jobState.getErrors();

        if (!CoreUtils.isNullOrEmpty(errors)) {
            final TextAnalyticsException textAnalyticsException = new TextAnalyticsException(
                "Extract summary operation failed", null, null);
            final IterableStream<TextAnalyticsError> textAnalyticsErrors =
                IterableStream.of(errors.stream().map(Utility::toTextAnalyticsError).collect(Collectors.toList()));
            TextAnalyticsExceptionPropertiesHelper.setErrors(textAnalyticsException, textAnalyticsErrors);
            throw LOGGER.logExceptionAsError(textAnalyticsException);
        }

        return new PagedResponseBase<Void, ExtractSummaryResultCollection>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            Arrays.asList(extractSummaryResultCollection),
            jobState.getNextLink(),
            null);
    }

    // Activation operation
    private Function<PollingContext<ExtractSummaryOperationDetail>,
        Mono<ExtractSummaryOperationDetail>> activationOperation(
        Mono<ExtractSummaryOperationDetail> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<ExtractSummaryOperationDetail>, ExtractSummaryOperationDetail>
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
            final ExtractSummaryOperationDetail operationDetail = new ExtractSummaryOperationDetail();
            ExtractSummaryOperationDetailPropertiesHelper.setOperationId(operationDetail,
                parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
            return operationDetail;
        };
    }

    // Polling operation
    private Function<PollingContext<ExtractSummaryOperationDetail>,
        Mono<PollResponse<ExtractSummaryOperationDetail>>> pollingOperation(
        Function<UUID, Mono<Response<AnalyzeTextJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<ExtractSummaryOperationDetail> operationResultPollResponse =
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

    private Function<PollingContext<ExtractSummaryOperationDetail>,
        PollResponse<ExtractSummaryOperationDetail>> pollingOperationSync(
        Function<UUID, Response<AnalyzeTextJobState>> pollingFunction) {
        return pollingContext -> {
            final PollResponse<ExtractSummaryOperationDetail> operationResultPollResponse =
                pollingContext.getLatestResponse();
            final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
            return processAnalyzeTextModelResponse(pollingFunction.apply(operationId), operationResultPollResponse);
        };
    }

    // Fetching operation
    private Function<PollingContext<ExtractSummaryOperationDetail>, Mono<ExtractSummaryPagedFlux>> fetchingOperation(
        Function<UUID, Mono<ExtractSummaryPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<ExtractSummaryOperationDetail>, ExtractSummaryPagedIterable>
        fetchingOperationIterable(final Function<UUID, ExtractSummaryPagedIterable> fetchingFunction) {
        return pollingContext -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
            return fetchingFunction.apply(resultUuid);
        };
    }

    // Cancel operation
    private BiFunction<PollingContext<ExtractSummaryOperationDetail>,
        PollResponse<ExtractSummaryOperationDetail>,
        Mono<ExtractSummaryOperationDetail>> cancelOperation(
        Function<UUID, Mono<ResponseBase<AnalyzeTextsCancelJobHeaders, Void>>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            try {
                return cancelFunction.apply(resultUuid)
                    .map(cancelJobResponse -> {
                        final ExtractSummaryOperationDetail operationResult = new ExtractSummaryOperationDetail();
                        ExtractSummaryOperationDetailPropertiesHelper.setOperationId(operationResult,
                            parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
                        return operationResult;
                    }).onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private BiFunction<PollingContext<ExtractSummaryOperationDetail>,
        PollResponse<ExtractSummaryOperationDetail>, ExtractSummaryOperationDetail> cancelOperationSync(
        Function<UUID, ResponseBase<AnalyzeTextsCancelJobHeaders, Void>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            ResponseBase<AnalyzeTextsCancelJobHeaders, Void> cancelJobResponse = cancelFunction.apply(resultUuid);
            final ExtractSummaryOperationDetail operationResult = new ExtractSummaryOperationDetail();
            ExtractSummaryOperationDetailPropertiesHelper.setOperationId(operationResult,
                parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
            return operationResult;
        };
    }

    private PollResponse<ExtractSummaryOperationDetail> processAnalyzeTextModelResponse(
        Response<AnalyzeTextJobState> analyzeOperationResultResponse,
        PollResponse<ExtractSummaryOperationDetail> operationResultPollResponse) {
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
        ExtractSummaryOperationDetailPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getDisplayName());
        ExtractSummaryOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getCreatedDateTime());
        ExtractSummaryOperationDetailPropertiesHelper.setLastModifiedAt(
            operationResultPollResponse.getValue(), analyzeOperationResultResponse.getValue().getLastUpdatedDateTime());
        ExtractSummaryOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getExpirationDateTime());
        return new PollResponse<>(status, operationResultPollResponse.getValue());
    }

    private ExtractSummaryOptions getNotNullExtractSummaryOptions(ExtractSummaryOptions options) {
        return options == null ? new ExtractSummaryOptions() : options;
    }

    private void checkUnsupportedServiceVersionForExtractSummary() {
        throwIfTargetServiceVersionFound(this.serviceVersion,
            Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1,
                TextAnalyticsServiceVersion.V2022_05_01),
            getUnsupportedServiceApiVersionMessage("Extractive Summarization", serviceVersion,
                TextAnalyticsServiceVersion.V2022_10_01_PREVIEW));
    }
}
