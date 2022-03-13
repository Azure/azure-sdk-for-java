// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsExceptionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.CancelHealthJobResponse;
import com.azure.ai.textanalytics.implementation.models.HealthcareJobState;
import com.azure.ai.textanalytics.implementation.models.HealthcareResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.implementation.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedFlux;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedIterable;
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
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.parseOperationId;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeHealthcareEntitiesResults;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

class AnalyzeHealthcareEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(AnalyzeHealthcareEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create an {@link AnalyzeHealthcareEntityAsyncClient} that sends requests to the Text Analytics service's
     * healthcare LRO endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    AnalyzeHealthcareEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
        beginAnalyzeHealthcareEntities(Iterable<TextDocumentInput> documents, AnalyzeHealthcareEntitiesOptions options,
            Context context) {
        try {
            inputDocumentsValidation(documents);
            options = getNotNullAnalyzeHealthcareEntitiesOptions(options);
            final Context finalContext = getNotNullContext(context)
                                             .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    service.healthWithResponseAsync(
                        new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                        options.getModelVersion(),
                        StringIndexType.UTF16CODE_UNIT,
                        options.isServiceLogsDisabled(),
                        finalContext)
                        .map(healthResponse -> {
                            final AnalyzeHealthcareEntitiesOperationDetail operationDetail =
                                new AnalyzeHealthcareEntitiesOperationDetail();
                            AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(healthResponse.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })),
                pollingOperation(operationId -> service.healthStatusWithResponseAsync(operationId,
                    null, null, finalIncludeStatistics, finalContext)),
                cancelOperation(operationId -> service.cancelHealthJobWithResponseAsync(operationId, finalContext)),
                fetchingOperation(operationId -> Mono.just(getHealthcareEntitiesPagedFlux(operationId,
                    null, null, finalIncludeStatistics, finalContext)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
        beginAnalyzeHealthcarePagedIterable(Iterable<TextDocumentInput> documents,
            AnalyzeHealthcareEntitiesOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            options = getNotNullAnalyzeHealthcareEntitiesOptions(options);
            final Context finalContext = getNotNullContext(context)
                                             .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    service.healthWithResponseAsync(
                        new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                        options.getModelVersion(),
                        StringIndexType.UTF16CODE_UNIT,
                        options.isServiceLogsDisabled(),
                        finalContext)
                        .map(healthResponse -> {
                            final AnalyzeHealthcareEntitiesOperationDetail operationDetail =
                                new AnalyzeHealthcareEntitiesOperationDetail();
                            AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(healthResponse.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })),
                pollingOperation(operationId -> service.healthStatusWithResponseAsync(operationId, null,
                    null, finalIncludeStatistics, finalContext)),
                cancelOperation(operationId -> service.cancelHealthJobWithResponseAsync(operationId, finalContext)),
                fetchingOperationIterable(operationId -> Mono.just(new AnalyzeHealthcareEntitiesPagedIterable(
                    getHealthcareEntitiesPagedFlux(operationId, null, null, finalIncludeStatistics,
                        finalContext))))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    AnalyzeHealthcareEntitiesPagedFlux getHealthcareEntitiesPagedFlux(
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        return new AnalyzeHealthcareEntitiesPagedFlux(
            () -> (continuationToken, pageSize) ->
                      getPagedResult(continuationToken, operationId, top, skip, showStats, context).flux());
    }

    Mono<PagedResponse<AnalyzeHealthcareEntitiesResultCollection>> getPagedResult(String continuationToken,
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        try {
            if (continuationToken != null) {
                final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
                final Integer topValue = (Integer) continuationTokenMap.getOrDefault("$top", null);
                final Integer skipValue = (Integer) continuationTokenMap.getOrDefault("$skip", null);
                final Boolean showStatsValue = (Boolean) continuationTokenMap.getOrDefault(showStats, false);
                return service.healthStatusWithResponseAsync(operationId, topValue, skipValue, showStatsValue, context)
                           .map(this::toTextAnalyticsPagedResponse)
                           .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } else {
                return service.healthStatusWithResponseAsync(operationId, top, skip, showStats, context)
                           .map(this::toTextAnalyticsPagedResponse)
                           .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private PagedResponse<AnalyzeHealthcareEntitiesResultCollection> toTextAnalyticsPagedResponse(
        Response<HealthcareJobState> response) {
        final HealthcareJobState healthcareJobState = response.getValue();
        final HealthcareResult healthcareResult = healthcareJobState.getResults();
        final IterableStream<AnalyzeHealthcareEntitiesResult> recognizeHealthcareEntitiesResults
            = toRecognizeHealthcareEntitiesResults(healthcareResult);
        final AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection =
            new AnalyzeHealthcareEntitiesResultCollection(recognizeHealthcareEntitiesResults);
        AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper.setModelVersion(
            analyzeHealthcareEntitiesResultCollection, healthcareResult.getModelVersion());
        final RequestStatistics requestStatistics = healthcareResult.getStatistics();
        if (requestStatistics != null) {
            final TextDocumentBatchStatistics batchStatistic = new TextDocumentBatchStatistics(
                requestStatistics.getDocumentsCount(), requestStatistics.getValidDocumentsCount(),
                requestStatistics.getErroneousDocumentsCount(), requestStatistics.getTransactionsCount()
            );
            AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper.setStatistics(
                analyzeHealthcareEntitiesResultCollection, batchStatistic);
        }

        final List<TextAnalyticsError> errors = healthcareJobState.getErrors();

        if (!CoreUtils.isNullOrEmpty(errors)) {
            final TextAnalyticsException textAnalyticsException = new TextAnalyticsException(
                "Analyze healthcare operation failed", null, null);
            final IterableStream<com.azure.ai.textanalytics.models.TextAnalyticsError> textAnalyticsErrors =
                IterableStream.of(errors.stream().map(Utility::toTextAnalyticsError).collect(Collectors.toList()));
            TextAnalyticsExceptionPropertiesHelper.setErrors(textAnalyticsException, textAnalyticsErrors);
            throw logger.logExceptionAsError(textAnalyticsException);
        }

        return new PagedResponseBase<Void, AnalyzeHealthcareEntitiesResultCollection>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            Arrays.asList(analyzeHealthcareEntitiesResultCollection),
            healthcareJobState.getNextLink(),
            null);
    }

    // Activation operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
                        Mono<AnalyzeHealthcareEntitiesOperationDetail>> activationOperation(
                            Mono<AnalyzeHealthcareEntitiesOperationDetail> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    // Polling operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
                        Mono<PollResponse<AnalyzeHealthcareEntitiesOperationDetail>>>
        pollingOperation(Function<UUID, Mono<Response<HealthcareJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeHealthcareEntitiesOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID resultUuid = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return pollingFunction.apply(resultUuid)
                    .flatMap(modelResponse -> processAnalyzeModelResponse(modelResponse, operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    // Fetching operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
                        Mono<AnalyzeHealthcareEntitiesPagedFlux>>
        fetchingOperation(Function<UUID, Mono<AnalyzeHealthcareEntitiesPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    // Cancel operation
    private BiFunction<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
                          PollResponse<AnalyzeHealthcareEntitiesOperationDetail>,
                          Mono<AnalyzeHealthcareEntitiesOperationDetail>> cancelOperation(
                              Function<UUID, Mono<CancelHealthJobResponse>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            try {
                return cancelFunction.apply(resultUuid)
                    .map(cancelHealthJobResponse -> {
                        final AnalyzeHealthcareEntitiesOperationDetail operationResult =
                            new AnalyzeHealthcareEntitiesOperationDetail();
                        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationResult,
                            parseOperationId(cancelHealthJobResponse.getDeserializedHeaders().getOperationLocation()));
                        return operationResult;
                    }).onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    // Fetching iterable operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        Mono<AnalyzeHealthcareEntitiesPagedIterable>> fetchingOperationIterable(
            final Function<UUID, Mono<AnalyzeHealthcareEntitiesPagedIterable>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PollResponse<AnalyzeHealthcareEntitiesOperationDetail>> processAnalyzeModelResponse(
        Response<HealthcareJobState> analyzeOperationResultResponse,
        PollResponse<AnalyzeHealthcareEntitiesOperationDetail> operationResultPollResponse) {
        LongRunningOperationStatus status;
        switch (analyzeOperationResultResponse.getValue().getStatus()) {
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
                    analyzeOperationResultResponse.getValue().getStatus().toString(), true);
                break;
        }

        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getCreatedDateTime());
        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setLastModifiedAt(
            operationResultPollResponse.getValue(), analyzeOperationResultResponse.getValue().getLastUpdateDateTime());
        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getExpirationDateTime());
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }

    private AnalyzeHealthcareEntitiesOptions getNotNullAnalyzeHealthcareEntitiesOptions(
        AnalyzeHealthcareEntitiesOptions options) {
        return options == null ? new AnalyzeHealthcareEntitiesOptions() : options;
    }
}
