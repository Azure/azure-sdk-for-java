// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AbstractiveSummaryOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AbstractiveSummaryResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeTextsImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsExceptionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AbstractiveSummarizationLROResult;
import com.azure.ai.textanalytics.implementation.models.AbstractiveSummarizationLROTask;
import com.azure.ai.textanalytics.implementation.models.AbstractiveSummarizationResult;
import com.azure.ai.textanalytics.implementation.models.AbstractiveSummarizationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobState;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobsInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROResult;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROTask;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsCancelJobHeaders;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsSubmitJobHeaders;
import com.azure.ai.textanalytics.implementation.models.Error;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.State;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.models.AbstractiveSummaryOperationDetail;
import com.azure.ai.textanalytics.models.AbstractiveSummaryOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AbstractiveSummaryPagedFlux;
import com.azure.ai.textanalytics.util.AbstractiveSummaryPagedIterable;
import com.azure.ai.textanalytics.util.AbstractiveSummaryResultCollection;
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
import static com.azure.ai.textanalytics.implementation.Utility.toAbstractiveSummaryResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.models.State.CANCELLED;
import static com.azure.ai.textanalytics.implementation.models.State.NOT_STARTED;
import static com.azure.ai.textanalytics.implementation.models.State.RUNNING;
import static com.azure.ai.textanalytics.implementation.models.State.SUCCEEDED;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Helper class for managing abstractive summarization endpoints.
 */
class AbstractiveSummaryUtilClient {
    private final AnalyzeTextsImpl service;
    private final TextAnalyticsServiceVersion serviceVersion;

    private static final ClientLogger LOGGER = new ClientLogger(AbstractiveSummaryUtilClient.class);

    AbstractiveSummaryUtilClient(AnalyzeTextsImpl service, TextAnalyticsServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    PollerFlux<AbstractiveSummaryOperationDetail, AbstractiveSummaryPagedFlux> abstractiveSummaryAsync(
        Iterable<TextDocumentInput> documents, AbstractiveSummaryOptions options, Context context) {
        try {
            checkUnsupportedServiceVersionForAbstractiveSummary();
            inputDocumentsValidation(documents);
            options = getNotNullAbstractiveSummaryOptions(options);
            final Context finalContext = getNotNullContext(context);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();

            return new PollerFlux<>(DEFAULT_POLL_INTERVAL,
                activationOperation(service.submitJobWithResponseAsync(new AnalyzeTextJobsInput()
                    .setDisplayName(options.getDisplayName())
                    .setAnalysisInput(new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(Arrays.asList(new AbstractiveSummarizationLROTask().setParameters(
                        new AbstractiveSummarizationTaskParameters().setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                            .setSentenceCount(options.getSentenceCount())
                            .setModelVersion(options.getModelVersion())
                            .setLoggingOptOut(options.isServiceLogsDisabled())))),
                    finalContext).map(responseBase -> {
                        final AbstractiveSummaryOperationDetail operationDetail
                            = new AbstractiveSummaryOperationDetail();
                        AbstractiveSummaryOperationDetailPropertiesHelper.setOperationId(operationDetail,
                            parseOperationId(responseBase.getDeserializedHeaders().getOperationLocation()));
                        return operationDetail;
                    })),
                pollingOperation(operationId -> service.jobStatusWithResponseAsync(operationId, finalIncludeStatistics,
                    null, null, finalContext)),
                cancelOperation(operationId -> service.cancelJobWithResponseAsync(operationId, finalContext)),
                fetchingOperation(operationId -> Mono.just(
                    getAbstractiveSummaryPagedFlux(operationId, null, null, finalIncludeStatistics, finalContext))));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    SyncPoller<AbstractiveSummaryOperationDetail, AbstractiveSummaryPagedIterable> abstractiveSummaryPagedIterable(
        Iterable<TextDocumentInput> documents, AbstractiveSummaryOptions options, Context context) {
        try {
            checkUnsupportedServiceVersionForAbstractiveSummary();
            inputDocumentsValidation(documents);
            options = getNotNullAbstractiveSummaryOptions(options);
            final Context finalContext = enableSyncRestProxy(getNotNullContext(context));
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final String displayName = options.getDisplayName();

            final AbstractiveSummarizationLROTask task = new AbstractiveSummarizationLROTask().setParameters(
                new AbstractiveSummarizationTaskParameters().setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                    .setSentenceCount(options.getSentenceCount())
                    .setModelVersion(options.getModelVersion())
                    .setLoggingOptOut(options.isServiceLogsDisabled()));
            return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    activationOperationSync(documents, task, displayName, finalContext).apply(cxt)),
                pollingOperationSync(operationId -> service.jobStatusWithResponse(operationId, finalIncludeStatistics,
                    null, null, finalContext)),
                cancelOperationSync(operationId -> service.cancelJobWithResponse(operationId, finalContext)),
                fetchingOperationIterable(operationId -> getAbstractiveSummaryPagedIterable(operationId, null, null,
                    finalIncludeStatistics, finalContext)));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    AbstractiveSummaryPagedFlux getAbstractiveSummaryPagedFlux(UUID operationId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new AbstractiveSummaryPagedFlux(() -> (continuationToken,
            pageSize) -> getPagedResult(continuationToken, operationId, top, skip, showStats, context).flux());
    }

    AbstractiveSummaryPagedIterable getAbstractiveSummaryPagedIterable(UUID operationId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new AbstractiveSummaryPagedIterable(() -> (continuationToken,
            pageSize) -> getPagedResultSync(continuationToken, operationId, top, skip, showStats, context));
    }

    Mono<PagedResponse<AbstractiveSummaryResultCollection>> getPagedResult(String continuationToken, UUID operationId,
        Integer top, Integer skip, boolean showStats, Context context) {
        try {
            if (continuationToken != null) {
                final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
                top = getTopContinuesToken(continuationTokenMap);
                skip = getSkipContinuesToken(continuationTokenMap);
                showStats = getShowStatsContinuesToken(continuationTokenMap);
            }
            return service.jobStatusWithResponseAsync(operationId, showStats, top, skip, context)
                .map(this::toAbstractiveSummaryResultCollectionPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    PagedResponse<AbstractiveSummaryResultCollection> getPagedResultSync(String continuationToken, UUID operationId,
        Integer top, Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
            top = getTopContinuesToken(continuationTokenMap);
            skip = getSkipContinuesToken(continuationTokenMap);
            showStats = getShowStatsContinuesToken(continuationTokenMap);
        }
        return toAbstractiveSummaryResultCollectionPagedResponse(
            service.jobStatusWithResponse(operationId, showStats, top, skip, context));
    }

    private PagedResponse<AbstractiveSummaryResultCollection>
        toAbstractiveSummaryResultCollectionPagedResponse(Response<AnalyzeTextJobState> response) {

        final AnalyzeTextJobState jobState = response.getValue();
        final List<AnalyzeTextLROResult> lroResults = jobState.getTasks().getItems();

        final AbstractiveSummarizationResult abstractiveSummarizationResult;
        final AnalyzeTextLROResult lroResult = lroResults.get(0);
        if (lroResult instanceof AbstractiveSummarizationLROResult) {
            AbstractiveSummarizationLROResult abstractiveSummarizationLROResult
                = (AbstractiveSummarizationLROResult) lroResults.get(0);
            abstractiveSummarizationResult = abstractiveSummarizationLROResult.getResults();
        } else {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Invalid class type returned: " + lroResult.getClass().getName()));
        }

        final AbstractiveSummaryResultCollection abstractiveSummaryResultCollection
            = toAbstractiveSummaryResultCollection(abstractiveSummarizationResult);
        final RequestStatistics requestStatistics = abstractiveSummarizationResult.getStatistics();
        if (requestStatistics != null) {
            final TextDocumentBatchStatistics batchStatistic = new TextDocumentBatchStatistics(
                requestStatistics.getDocumentsCount(), requestStatistics.getValidDocumentsCount(),
                requestStatistics.getErroneousDocumentsCount(), requestStatistics.getTransactionsCount());
            AbstractiveSummaryResultCollectionPropertiesHelper.setStatistics(abstractiveSummaryResultCollection,
                batchStatistic);
        }

        final List<Error> errors = jobState.getErrors();

        if (!CoreUtils.isNullOrEmpty(errors)) {
            final TextAnalyticsException textAnalyticsException
                = new TextAnalyticsException("Abstractive summary operation failed", null, null);
            final IterableStream<TextAnalyticsError> textAnalyticsErrors
                = IterableStream.of(errors.stream().map(Utility::toTextAnalyticsError).collect(Collectors.toList()));
            TextAnalyticsExceptionPropertiesHelper.setErrors(textAnalyticsException, textAnalyticsErrors);
            throw LOGGER.logExceptionAsError(textAnalyticsException);
        }

        return new PagedResponseBase<Void, AbstractiveSummaryResultCollection>(response.getRequest(),
            response.getStatusCode(), response.getHeaders(), Arrays.asList(abstractiveSummaryResultCollection),
            jobState.getNextLink(), null);
    }

    // Activation operation
    private Function<PollingContext<AbstractiveSummaryOperationDetail>, Mono<AbstractiveSummaryOperationDetail>>
        activationOperation(Mono<AbstractiveSummaryOperationDetail> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<AbstractiveSummaryOperationDetail>, AbstractiveSummaryOperationDetail>
        activationOperationSync(Iterable<TextDocumentInput> documents, AnalyzeTextLROTask task, String displayName,
            Context context) {
        return pollingContext -> {
            final ResponseBase<AnalyzeTextsSubmitJobHeaders, Void> analyzeResponse
                = service.submitJobWithResponse(new AnalyzeTextJobsInput().setDisplayName(displayName)
                    .setAnalysisInput(new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(Arrays.asList(task)), context);
            final AbstractiveSummaryOperationDetail operationDetail = new AbstractiveSummaryOperationDetail();
            AbstractiveSummaryOperationDetailPropertiesHelper.setOperationId(operationDetail,
                parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
            return operationDetail;
        };
    }

    // Polling operation
    private
        Function<PollingContext<AbstractiveSummaryOperationDetail>, Mono<PollResponse<AbstractiveSummaryOperationDetail>>>
        pollingOperation(Function<UUID, Mono<Response<AnalyzeTextJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AbstractiveSummaryOperationDetail> operationResultPollResponse
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

    private Function<PollingContext<AbstractiveSummaryOperationDetail>, PollResponse<AbstractiveSummaryOperationDetail>>
        pollingOperationSync(Function<UUID, Response<AnalyzeTextJobState>> pollingFunction) {
        return pollingContext -> {
            final PollResponse<AbstractiveSummaryOperationDetail> operationResultPollResponse
                = pollingContext.getLatestResponse();
            final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
            return processAnalyzeTextModelResponse(pollingFunction.apply(operationId), operationResultPollResponse);
        };
    }

    // Fetching operation
    private Function<PollingContext<AbstractiveSummaryOperationDetail>, Mono<AbstractiveSummaryPagedFlux>>
        fetchingOperation(Function<UUID, Mono<AbstractiveSummaryPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<AbstractiveSummaryOperationDetail>, AbstractiveSummaryPagedIterable>
        fetchingOperationIterable(final Function<UUID, AbstractiveSummaryPagedIterable> fetchingFunction) {
        return pollingContext -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
            return fetchingFunction.apply(resultUuid);
        };
    }

    // Cancel operation
    private
        BiFunction<PollingContext<AbstractiveSummaryOperationDetail>, PollResponse<AbstractiveSummaryOperationDetail>, Mono<AbstractiveSummaryOperationDetail>>
        cancelOperation(Function<UUID, Mono<ResponseBase<AnalyzeTextsCancelJobHeaders, Void>>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            try {
                return cancelFunction.apply(resultUuid).map(cancelJobResponse -> {
                    final AbstractiveSummaryOperationDetail operationResult = new AbstractiveSummaryOperationDetail();
                    AbstractiveSummaryOperationDetailPropertiesHelper.setOperationId(operationResult,
                        parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
                    return operationResult;
                }).onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private
        BiFunction<PollingContext<AbstractiveSummaryOperationDetail>, PollResponse<AbstractiveSummaryOperationDetail>, AbstractiveSummaryOperationDetail>
        cancelOperationSync(Function<UUID, ResponseBase<AnalyzeTextsCancelJobHeaders, Void>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            ResponseBase<AnalyzeTextsCancelJobHeaders, Void> cancelJobResponse = cancelFunction.apply(resultUuid);
            final AbstractiveSummaryOperationDetail operationResult = new AbstractiveSummaryOperationDetail();
            AbstractiveSummaryOperationDetailPropertiesHelper.setOperationId(operationResult,
                parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
            return operationResult;
        };
    }

    private PollResponse<AbstractiveSummaryOperationDetail> processAnalyzeTextModelResponse(
        Response<AnalyzeTextJobState> analyzeOperationResultResponse,
        PollResponse<AbstractiveSummaryOperationDetail> operationResultPollResponse) {
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
        AbstractiveSummaryOperationDetailPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getDisplayName());
        AbstractiveSummaryOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getCreatedDateTime());
        AbstractiveSummaryOperationDetailPropertiesHelper.setLastModifiedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getLastUpdatedDateTime());
        AbstractiveSummaryOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getExpirationDateTime());
        return new PollResponse<>(status, operationResultPollResponse.getValue());
    }

    private AbstractiveSummaryOptions getNotNullAbstractiveSummaryOptions(AbstractiveSummaryOptions options) {
        return options == null ? new AbstractiveSummaryOptions() : options;
    }

    private void checkUnsupportedServiceVersionForAbstractiveSummary() {
        throwIfTargetServiceVersionFound(this.serviceVersion,
            Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1,
                TextAnalyticsServiceVersion.V2022_05_01),
            getUnsupportedServiceApiVersionMessage("Abstractive Summarization", serviceVersion,
                TextAnalyticsServiceVersion.V2023_04_01));
    }
}
