// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeTextsImpl;
import com.azure.ai.textanalytics.implementation.ExtractiveSummaryOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ExtractiveSummaryResultCollectionPropertiesHelper;
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
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.State;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.models.ExtractiveSummaryOperationDetail;
import com.azure.ai.textanalytics.models.ExtractiveSummaryOptions;
import com.azure.ai.textanalytics.models.ExtractiveSummarySentencesOrder;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractiveSummaryPagedFlux;
import com.azure.ai.textanalytics.util.ExtractiveSummaryPagedIterable;
import com.azure.ai.textanalytics.util.ExtractiveSummaryResultCollection;
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
import static com.azure.ai.textanalytics.implementation.Utility.toExtractiveSummaryResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.models.State.CANCELLED;
import static com.azure.ai.textanalytics.implementation.models.State.NOT_STARTED;
import static com.azure.ai.textanalytics.implementation.models.State.RUNNING;
import static com.azure.ai.textanalytics.implementation.models.State.SUCCEEDED;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Helper class for managing extractive summarization endpoints.
 */
class ExtractiveSummaryUtilClient {
    private final AnalyzeTextsImpl service;
    private final TextAnalyticsServiceVersion serviceVersion;

    private static final ClientLogger LOGGER = new ClientLogger(ExtractiveSummaryUtilClient.class);

    ExtractiveSummaryUtilClient(AnalyzeTextsImpl service, TextAnalyticsServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    PollerFlux<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedFlux> extractiveSummaryAsync(
        Iterable<TextDocumentInput> documents, ExtractiveSummaryOptions options, Context context) {
        try {
            checkUnsupportedServiceVersionForExtractiveSummary();
            inputDocumentsValidation(documents);
            options = getNotNullExtractiveSummaryOptions(options);
            final Context finalContext = getNotNullContext(context);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final ExtractiveSummarySentencesOrder orderBy = options.getOrderBy();
            return new PollerFlux<>(DEFAULT_POLL_INTERVAL,
                activationOperation(service.submitJobWithResponseAsync(new AnalyzeTextJobsInput()
                    .setDisplayName(options.getDisplayName())
                    .setAnalysisInput(new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(Arrays.asList(new ExtractiveSummarizationLROTask().setParameters(
                        new ExtractiveSummarizationTaskParameters().setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                            .setSortBy(
                                orderBy == null ? null : ExtractiveSummarySentencesOrder.fromString(orderBy.toString()))
                            .setSentenceCount(options.getMaxSentenceCount())
                            .setModelVersion(options.getModelVersion())
                            .setLoggingOptOut(options.isServiceLogsDisabled())))),
                    finalContext).map(responseBase -> {
                        final ExtractiveSummaryOperationDetail operationDetail = new ExtractiveSummaryOperationDetail();
                        ExtractiveSummaryOperationDetailPropertiesHelper.setOperationId(operationDetail,
                            parseOperationId(responseBase.getDeserializedHeaders().getOperationLocation()));
                        return operationDetail;
                    })),
                pollingOperation(operationId -> service.jobStatusWithResponseAsync(operationId, finalIncludeStatistics,
                    null, null, finalContext)),
                cancelOperation(operationId -> service.cancelJobWithResponseAsync(operationId, finalContext)),
                fetchingOperation(operationId -> Mono.just(
                    getExtractiveSummaryPagedFlux(operationId, null, null, finalIncludeStatistics, finalContext))));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    SyncPoller<ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedIterable> extractiveSummaryPagedIterable(
        Iterable<TextDocumentInput> documents, ExtractiveSummaryOptions options, Context context) {
        try {
            checkUnsupportedServiceVersionForExtractiveSummary();
            inputDocumentsValidation(documents);
            options = getNotNullExtractiveSummaryOptions(options);
            final Context finalContext = enableSyncRestProxy(getNotNullContext(context));
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final String displayName = options.getDisplayName();
            final ExtractiveSummarySentencesOrder orderBy = options.getOrderBy();

            final ExtractiveSummarizationLROTask task = new ExtractiveSummarizationLROTask().setParameters(
                new ExtractiveSummarizationTaskParameters().setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                    .setSortBy(orderBy == null ? null : ExtractiveSummarySentencesOrder.fromString(orderBy.toString()))
                    .setSentenceCount(options.getMaxSentenceCount())
                    .setModelVersion(options.getModelVersion())
                    .setLoggingOptOut(options.isServiceLogsDisabled()));
            return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    activationOperationSync(documents, task, displayName, finalContext).apply(cxt)),
                pollingOperationSync(operationId -> service.jobStatusWithResponse(operationId, finalIncludeStatistics,
                    null, null, finalContext)),
                cancelOperationSync(operationId -> service.cancelJobWithResponse(operationId, finalContext)),
                fetchingOperationIterable(operationId -> getExtractiveSummaryPagedIterable(operationId, null, null,
                    finalIncludeStatistics, finalContext)));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    ExtractiveSummaryPagedFlux getExtractiveSummaryPagedFlux(UUID operationId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new ExtractiveSummaryPagedFlux(() -> (continuationToken,
            pageSize) -> getPagedResult(continuationToken, operationId, top, skip, showStats, context).flux());
    }

    ExtractiveSummaryPagedIterable getExtractiveSummaryPagedIterable(UUID operationId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new ExtractiveSummaryPagedIterable(() -> (continuationToken,
            pageSize) -> getPagedResultSync(continuationToken, operationId, top, skip, showStats, context));
    }

    Mono<PagedResponse<ExtractiveSummaryResultCollection>> getPagedResult(String continuationToken, UUID operationId,
        Integer top, Integer skip, boolean showStats, Context context) {
        try {
            if (continuationToken != null) {
                final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
                top = getTopContinuesToken(continuationTokenMap);
                skip = getSkipContinuesToken(continuationTokenMap);
                showStats = getShowStatsContinuesToken(continuationTokenMap);
            }
            return service.jobStatusWithResponseAsync(operationId, showStats, top, skip, context)
                .map(this::toExtractiveSummaryResultCollectionPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    PagedResponse<ExtractiveSummaryResultCollection> getPagedResultSync(String continuationToken, UUID operationId,
        Integer top, Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
            top = getTopContinuesToken(continuationTokenMap);
            skip = getSkipContinuesToken(continuationTokenMap);
            showStats = getShowStatsContinuesToken(continuationTokenMap);
        }
        return toExtractiveSummaryResultCollectionPagedResponse(
            service.jobStatusWithResponse(operationId, showStats, top, skip, context));
    }

    private PagedResponse<ExtractiveSummaryResultCollection>
        toExtractiveSummaryResultCollectionPagedResponse(Response<AnalyzeTextJobState> response) {

        final AnalyzeTextJobState jobState = response.getValue();
        final List<AnalyzeTextLROResult> lroResults = jobState.getTasks().getItems();

        final ExtractiveSummarizationResult extractiveSummarizationResult;
        final AnalyzeTextLROResult lroResult = lroResults.get(0);
        if (lroResult instanceof ExtractiveSummarizationLROResult) {
            ExtractiveSummarizationLROResult extractiveSummarizationLROResult
                = (ExtractiveSummarizationLROResult) lroResults.get(0);
            extractiveSummarizationResult = extractiveSummarizationLROResult.getResults();
        } else {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Invalid class type returned: " + lroResult.getClass().getName()));
        }

        final ExtractiveSummaryResultCollection extractiveSummaryResultCollection
            = toExtractiveSummaryResultCollection(extractiveSummarizationResult);
        final RequestStatistics requestStatistics = extractiveSummarizationResult.getStatistics();
        if (requestStatistics != null) {
            final TextDocumentBatchStatistics batchStatistic = new TextDocumentBatchStatistics(
                requestStatistics.getDocumentsCount(), requestStatistics.getValidDocumentsCount(),
                requestStatistics.getErroneousDocumentsCount(), requestStatistics.getTransactionsCount());
            ExtractiveSummaryResultCollectionPropertiesHelper.setStatistics(extractiveSummaryResultCollection,
                batchStatistic);
        }

        final List<Error> errors = jobState.getErrors();

        if (!CoreUtils.isNullOrEmpty(errors)) {
            final TextAnalyticsException textAnalyticsException
                = new TextAnalyticsException("Extract summary operation failed", null, null);
            final IterableStream<TextAnalyticsError> textAnalyticsErrors
                = IterableStream.of(errors.stream().map(Utility::toTextAnalyticsError).collect(Collectors.toList()));
            TextAnalyticsExceptionPropertiesHelper.setErrors(textAnalyticsException, textAnalyticsErrors);
            throw LOGGER.logExceptionAsError(textAnalyticsException);
        }

        return new PagedResponseBase<Void, ExtractiveSummaryResultCollection>(response.getRequest(),
            response.getStatusCode(), response.getHeaders(), Arrays.asList(extractiveSummaryResultCollection),
            jobState.getNextLink(), null);
    }

    // Activation operation
    private Function<PollingContext<ExtractiveSummaryOperationDetail>, Mono<ExtractiveSummaryOperationDetail>>
        activationOperation(Mono<ExtractiveSummaryOperationDetail> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<ExtractiveSummaryOperationDetail>, ExtractiveSummaryOperationDetail>
        activationOperationSync(Iterable<TextDocumentInput> documents, AnalyzeTextLROTask task, String displayName,
            Context context) {
        return pollingContext -> {
            final ResponseBase<AnalyzeTextsSubmitJobHeaders, Void> analyzeResponse
                = service.submitJobWithResponse(new AnalyzeTextJobsInput().setDisplayName(displayName)
                    .setAnalysisInput(new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(Arrays.asList(task)), context);
            final ExtractiveSummaryOperationDetail operationDetail = new ExtractiveSummaryOperationDetail();
            ExtractiveSummaryOperationDetailPropertiesHelper.setOperationId(operationDetail,
                parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
            return operationDetail;
        };
    }

    // Polling operation
    private
        Function<PollingContext<ExtractiveSummaryOperationDetail>, Mono<PollResponse<ExtractiveSummaryOperationDetail>>>
        pollingOperation(Function<UUID, Mono<Response<AnalyzeTextJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<ExtractiveSummaryOperationDetail> operationResultPollResponse
                    = pollingContext.getLatestResponse();
                final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return pollingFunction.apply(operationId)
                    .flatMap(modelResponse -> Mono
                        .just(processAnalyzeTextModelResponse(modelResponse, operationResultPollResponse)))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<ExtractiveSummaryOperationDetail>, PollResponse<ExtractiveSummaryOperationDetail>>
        pollingOperationSync(Function<UUID, Response<AnalyzeTextJobState>> pollingFunction) {
        return pollingContext -> {
            final PollResponse<ExtractiveSummaryOperationDetail> operationResultPollResponse
                = pollingContext.getLatestResponse();
            final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
            return processAnalyzeTextModelResponse(pollingFunction.apply(operationId), operationResultPollResponse);
        };
    }

    // Fetching operation
    private Function<PollingContext<ExtractiveSummaryOperationDetail>, Mono<ExtractiveSummaryPagedFlux>>
        fetchingOperation(Function<UUID, Mono<ExtractiveSummaryPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<ExtractiveSummaryOperationDetail>, ExtractiveSummaryPagedIterable>
        fetchingOperationIterable(final Function<UUID, ExtractiveSummaryPagedIterable> fetchingFunction) {
        return pollingContext -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
            return fetchingFunction.apply(resultUuid);
        };
    }

    // Cancel operation
    private
        BiFunction<PollingContext<ExtractiveSummaryOperationDetail>, PollResponse<ExtractiveSummaryOperationDetail>, Mono<ExtractiveSummaryOperationDetail>>
        cancelOperation(Function<UUID, Mono<ResponseBase<AnalyzeTextsCancelJobHeaders, Void>>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            try {
                return cancelFunction.apply(resultUuid).map(cancelJobResponse -> {
                    final ExtractiveSummaryOperationDetail operationResult = new ExtractiveSummaryOperationDetail();
                    ExtractiveSummaryOperationDetailPropertiesHelper.setOperationId(operationResult,
                        parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
                    return operationResult;
                }).onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private
        BiFunction<PollingContext<ExtractiveSummaryOperationDetail>, PollResponse<ExtractiveSummaryOperationDetail>, ExtractiveSummaryOperationDetail>
        cancelOperationSync(Function<UUID, ResponseBase<AnalyzeTextsCancelJobHeaders, Void>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            ResponseBase<AnalyzeTextsCancelJobHeaders, Void> cancelJobResponse = cancelFunction.apply(resultUuid);
            final ExtractiveSummaryOperationDetail operationResult = new ExtractiveSummaryOperationDetail();
            ExtractiveSummaryOperationDetailPropertiesHelper.setOperationId(operationResult,
                parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
            return operationResult;
        };
    }

    private PollResponse<ExtractiveSummaryOperationDetail> processAnalyzeTextModelResponse(
        Response<AnalyzeTextJobState> analyzeOperationResultResponse,
        PollResponse<ExtractiveSummaryOperationDetail> operationResultPollResponse) {
        LongRunningOperationStatus status;
        State state = analyzeOperationResultResponse.getValue().getStatus();
        if (NOT_STARTED.equals(state) || RUNNING.equals(state)) {
            status = LongRunningOperationStatus.IN_PROGRESS;
        } else if (SUCCEEDED.equals(state)) {
            status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (CANCELLED.equals(state)) {
            status = LongRunningOperationStatus.USER_CANCELLED;
        } else {
            status = LongRunningOperationStatus
                .fromString(analyzeOperationResultResponse.getValue().getStatus().toString(), true);
        }
        ExtractiveSummaryOperationDetailPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getDisplayName());
        ExtractiveSummaryOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getCreatedDateTime());
        ExtractiveSummaryOperationDetailPropertiesHelper.setLastModifiedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getLastUpdatedDateTime());
        ExtractiveSummaryOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getExpirationDateTime());
        return new PollResponse<>(status, operationResultPollResponse.getValue());
    }

    private ExtractiveSummaryOptions getNotNullExtractiveSummaryOptions(ExtractiveSummaryOptions options) {
        return options == null ? new ExtractiveSummaryOptions() : options;
    }

    private void checkUnsupportedServiceVersionForExtractiveSummary() {
        throwIfTargetServiceVersionFound(this.serviceVersion,
            Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1,
                TextAnalyticsServiceVersion.V2022_05_01),
            getUnsupportedServiceApiVersionMessage("Extractive Summarization", serviceVersion,
                TextAnalyticsServiceVersion.V2023_04_01));
    }
}
