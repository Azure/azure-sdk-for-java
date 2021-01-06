// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesOperationResultPropertiesHelper;
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
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.parseModelId;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeHealthcareEntitiesResults;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

class AnalyzeHealthcareAsyncClient {
    private final ClientLogger logger = new ClientLogger(AnalyzeHealthcareAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create an {@link AnalyzeHealthcareAsyncClient} that sends requests to the Text Analytics services's healthcare
     * LRO endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    AnalyzeHealthcareAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    PollerFlux<AnalyzeHealthcareEntitiesOperationResult, PagedFlux<AnalyzeHealthcareEntitiesResultCollection>>
        beginAnalyzeHealthcare(Iterable<TextDocumentInput> documents, AnalyzeHealthcareEntitiesOptions options,
            Context context) {
        try {
            inputDocumentsValidation(documents);
            if (options == null) {
                options = new AnalyzeHealthcareEntitiesOptions();
            }
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL, // TODO: after poller has the poll interval, change it back to it.
                activationOperation(service.healthWithResponseAsync(
                    new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                    options.getModelVersion(),
                    StringIndexType.UTF16CODE_UNIT, // Currently StringIndexType is not explored, we use it internally
                    context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                    .map(healthResponse -> {
                        final AnalyzeHealthcareEntitiesOperationResult operationResult =
                            new AnalyzeHealthcareEntitiesOperationResult();
                        AnalyzeHealthcareEntitiesOperationResultPropertiesHelper.setOperationId(operationResult,
                            parseModelId(healthResponse.getDeserializedHeaders().getOperationLocation()));
                        return operationResult;
                    })),
                pollingOperation(healthcareTaskId -> service.healthStatusWithResponseAsync(healthcareTaskId,
                    null, null, finalIncludeStatistics, context)),
//                (activationResponse, pollingContext) ->
//                    monoError(logger, new RuntimeException("Use the `beginCancelHealthcareJob` to cancel the job")),
                cancelOperation(healthcareTaskId -> service.cancelHealthJobWithResponseAsync(healthcareTaskId,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))),
                fetchingOperation(resultId -> Mono.just(getHealthcareFluxPage(resultId, null, null,
                    finalIncludeStatistics, context)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    PollerFlux<AnalyzeHealthcareEntitiesOperationResult, PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
        beginAnalyzeHealthcarePagedIterable(Iterable<TextDocumentInput> documents,
            AnalyzeHealthcareEntitiesOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            if (options == null) {
                options = new AnalyzeHealthcareEntitiesOptions();
            }
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL, // TODO: after poller has the poll interval, change it back to it.
                activationOperation(service.healthWithResponseAsync(
                    new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                    options.getModelVersion(),
                    StringIndexType.UTF16CODE_UNIT, // Currently StringIndexType is not explored, we use it internally
                    context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                    .map(healthResponse -> {
                        final AnalyzeHealthcareEntitiesOperationResult operationResult =
                            new AnalyzeHealthcareEntitiesOperationResult();
                        AnalyzeHealthcareEntitiesOperationResultPropertiesHelper.setOperationId(operationResult,
                            parseModelId(healthResponse.getDeserializedHeaders().getOperationLocation()));
                        return operationResult;
                    })),
                pollingOperation(healthcareTaskId -> service.healthStatusWithResponseAsync(healthcareTaskId, null,
                    null, finalIncludeStatistics, context)),
                cancelOperation(healthcareTaskId -> service.cancelHealthJobWithResponseAsync(healthcareTaskId,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))),
//                (activationResponse, pollingContext) ->
//                    monoError(logger, new RuntimeException("Use the `beginCancelHealthcareJob` to cancel the job")),
                fetchingOperationIterable(resultId -> Mono.just(new PagedIterable<>(getHealthcareFluxPage(resultId,
                    null, null, finalIncludeStatistics, context))))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    PagedFlux<AnalyzeHealthcareEntitiesResultCollection> getHealthcareFluxPage(UUID healthcareTaskId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new PagedFlux<>(
            () -> getPage(null, healthcareTaskId, top, skip, showStats, context),
            continuationToken -> getPage(continuationToken, healthcareTaskId, top, skip, showStats, context));
    }

    Mono<PagedResponse<AnalyzeHealthcareEntitiesResultCollection>> getPage(String continuationToken, UUID healthcareTaskId, Integer top,
        Integer skip, boolean showStats, Context context) {
        try {
            if (continuationToken != null) {
                final Map<String, Integer> continuationTokenMap = parseNextLink(continuationToken);
                final Integer topValue = continuationTokenMap.getOrDefault("$top", null);
                final Integer skipValue = continuationTokenMap.getOrDefault("$skip", null);
                return service.healthStatusWithResponseAsync(healthcareTaskId, topValue, skipValue, showStats, context)
                    .map(this::toTextAnalyticsPagedResponse)
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } else {
                return service.healthStatusWithResponseAsync(healthcareTaskId, top, skip, showStats, context)
                    .map(this::toTextAnalyticsPagedResponse)
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    PollerFlux<AnalyzeHealthcareEntitiesOperationResult, Void> beginCancelAnalyzeHealthcare(String healthTaskId,
        Duration pollInterval, Context context) {
        try {
            Objects.requireNonNull(healthTaskId, "'healthTaskId' is required and cannot be null.");
            return new PollerFlux<>(
                pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval,
                activationOperation(service.cancelHealthJobWithResponseAsync(UUID.fromString(healthTaskId),
                    context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE)).map(
                        healthResponse -> {
                            final AnalyzeHealthcareEntitiesOperationResult textAnalyticsOperationResult =
                                new AnalyzeHealthcareEntitiesOperationResult();
                            AnalyzeHealthcareEntitiesOperationResultPropertiesHelper.setOperationId(
                                textAnalyticsOperationResult,
                                parseModelId(healthResponse.getDeserializedHeaders().getOperationLocation()));
                            return textAnalyticsOperationResult;
                        })),
                pollingOperation(resultId -> service.healthStatusWithResponseAsync(
                    resultId, null, null, null, context)),
                (activationResponse, pollingContext) ->

                    monoError(logger, new RuntimeException("Cancellation of healthcare task cancellation is not supported.")),
                (resultId) -> Mono.empty()
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
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
//        final HealthcareTaskStatistics operationStatistics = new HealthcareTaskStatistics();
//        HealthcareTaskStatisticsPropertiesHelper.setDisplayName(operationStatistics,
//            healthcareJobState.getDisplayName());
//        HealthcareTaskStatisticsPropertiesHelper.setCreatedDateTime(operationStatistics,
//            healthcareJobState.getCreatedDateTime());
//        HealthcareTaskStatisticsPropertiesHelper.setExpirationDateTime(operationStatistics,
//            healthcareJobState.getExpirationDateTime());
//        HealthcareTaskStatisticsPropertiesHelper.setLastUpdatedDateTime(operationStatistics,
//            healthcareJobState.getLastUpdateDateTime());
        final RequestStatistics requestStatistics = healthcareResult.getStatistics();
//        if (batchStatistics != null) {
//            HealthcareTaskStatisticsPropertiesHelper.setDocumentCount(operationStatistics,
//                requestStatistics.getDocumentsCount());
//            HealthcareTaskStatisticsPropertiesHelper.setValidDocumentCount(operationStatistics,
//                requestStatistics.getValidDocumentsCount());
//            HealthcareTaskStatisticsPropertiesHelper.setInvalidDocumentCount(operationStatistics,
//                requestStatistics.getErroneousDocumentsCount());
//            HealthcareTaskStatisticsPropertiesHelper.setTransactionCount(operationStatistics,
//                requestStatistics.getTransactionsCount());
//        }
        final TextDocumentBatchStatistics batchStatistic = new TextDocumentBatchStatistics(
            requestStatistics.getDocumentsCount(), requestStatistics.getValidDocumentsCount(),
            requestStatistics.getErroneousDocumentsCount(), requestStatistics.getTransactionsCount()
        );
        AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper.setStatistics(
            analyzeHealthcareEntitiesResultCollection, batchStatistic);

        final List<TextAnalyticsError> errors = healthcareJobState.getErrors();
        if (errors != null) {
            AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper.setErrors(
                analyzeHealthcareEntitiesResultCollection,
                IterableStream.of(errors.stream().map(
                    error -> toTextAnalyticsError(error)).collect(Collectors.toList())));
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
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationResult>, Mono<AnalyzeHealthcareEntitiesOperationResult>>
        activationOperation(Mono<AnalyzeHealthcareEntitiesOperationResult> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    // Polling operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationResult>, Mono<PollResponse<AnalyzeHealthcareEntitiesOperationResult>>>
        pollingOperation(Function<UUID, Mono<Response<HealthcareJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeHealthcareEntitiesOperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID resultUuid = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return pollingFunction.apply(resultUuid)
                    .flatMap(modelResponse -> processAnalyzeModelResponse(modelResponse, operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    // Fetching operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationResult>,
                        Mono<PagedFlux<AnalyzeHealthcareEntitiesResultCollection>>>
        fetchingOperation(Function<UUID, Mono<PagedFlux<AnalyzeHealthcareEntitiesResultCollection>>> fetchingFunction) {
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
    private BiFunction<PollingContext<AnalyzeHealthcareEntitiesOperationResult>,
                          PollResponse<AnalyzeHealthcareEntitiesOperationResult>,
                          Mono<AnalyzeHealthcareEntitiesOperationResult>> cancelOperation(
                              Function<UUID, Mono<CancelHealthJobResponse>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            try {
                return cancelFunction.apply(resultUuid)
                    .map(cancelHealthJobResponse -> {

                        final AnalyzeHealthcareEntitiesOperationResult operationResult =
                            new AnalyzeHealthcareEntitiesOperationResult();
                        AnalyzeHealthcareEntitiesOperationResultPropertiesHelper.setOperationId(operationResult,
                            parseModelId(cancelHealthJobResponse.getDeserializedHeaders().getOperationLocation()));
                        return operationResult;
                    }).onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    // Fetching iterable operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationResult>,
                        Mono<PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>>
        fetchingOperationIterable(
        final Function<UUID, Mono<PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PollResponse<AnalyzeHealthcareEntitiesOperationResult>> processAnalyzeModelResponse(
        Response<HealthcareJobState> analyzeOperationResultResponse,
        PollResponse<AnalyzeHealthcareEntitiesOperationResult> operationResultPollResponse) {
        LongRunningOperationStatus status;
        switch (analyzeOperationResultResponse.getValue().getStatus()) {
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
            case FAILED:
                final TextAnalyticsException exception = new TextAnalyticsException("Analyze operation failed",
                    null, null);
                TextAnalyticsExceptionPropertiesHelper.setErrors(exception,
                    IterableStream.of(analyzeOperationResultResponse.getValue().getErrors().stream()
                        .map(error -> new com.azure.ai.textanalytics.models.TextAnalyticsError(
                                TextAnalyticsErrorCode.fromString(error.getCode().toString()),
                                error.getMessage(), null)).collect(Collectors.toList())));
                throw logger.logExceptionAsError(exception);
            case CANCELLED:
                status = LongRunningOperationStatus.USER_CANCELLED;
                break;
            case PARTIALLY_COMPLETED:
                status = LongRunningOperationStatus.fromString("PARTIALLY_COMPLETED", false);
                break;
            default:
                status = LongRunningOperationStatus.fromString(
                    analyzeOperationResultResponse.getValue().getStatus().toString(), true);
                break;
        }

        AnalyzeHealthcareEntitiesOperationResultPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getCreatedDateTime());
        AnalyzeHealthcareEntitiesOperationResultPropertiesHelper.setUpdatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getLastUpdateDateTime());
        AnalyzeHealthcareEntitiesOperationResultPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getExpirationDateTime());
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }
}
