// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeTextsImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsExceptionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobState;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobsInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROResult;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsCancelJobHeaders;
import com.azure.ai.textanalytics.implementation.models.CancelHealthJobHeaders;
import com.azure.ai.textanalytics.implementation.models.Error;
import com.azure.ai.textanalytics.implementation.models.FhirVersion;
import com.azure.ai.textanalytics.implementation.models.HealthcareDocumentType;
import com.azure.ai.textanalytics.implementation.models.HealthcareJobState;
import com.azure.ai.textanalytics.implementation.models.HealthcareLROResult;
import com.azure.ai.textanalytics.implementation.models.HealthcareLROTask;
import com.azure.ai.textanalytics.implementation.models.HealthcareResult;
import com.azure.ai.textanalytics.implementation.models.HealthcareTaskParameters;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.State;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedFlux;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedIterable;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
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
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.parseOperationId;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toAnalyzeHealthcareEntitiesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toFhirVersion;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.models.State.CANCELLED;
import static com.azure.ai.textanalytics.implementation.models.State.NOT_STARTED;
import static com.azure.ai.textanalytics.implementation.models.State.RUNNING;
import static com.azure.ai.textanalytics.implementation.models.State.SUCCEEDED;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

class AnalyzeHealthcareEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(AnalyzeHealthcareEntityAsyncClient.class);
    private final TextAnalyticsClientImpl legacyService;
    private final AnalyzeTextsImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    AnalyzeHealthcareEntityAsyncClient(TextAnalyticsClientImpl legacyService,
                                       TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = legacyService;
        this.service = null;
        this.serviceVersion = serviceVersion;
    }

    AnalyzeHealthcareEntityAsyncClient(AnalyzeTextsImpl service, TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = null;
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
        beginAnalyzeHealthcareEntities(Iterable<TextDocumentInput> documents, AnalyzeHealthcareEntitiesOptions options,
            Context context) {
        try {
            throwIfTargetServiceVersionFound(this.serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_0),
                getUnsupportedServiceApiVersionMessage("beginAnalyzeHealthcareEntities", serviceVersion,
                    TextAnalyticsServiceVersion.V3_1));
            throwIfCallingNotAvailableFeatureInOptions(options);
            inputDocumentsValidation(documents);
            options = getNotNullAnalyzeHealthcareEntitiesOptions(options);
            final Context finalContext = getNotNullContext(context)
                                             .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final StringIndexType finalStringIndexType = StringIndexType.UTF16CODE_UNIT;
            final String finalModelVersion = options.getModelVersion();
            final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
            final FhirVersion finalFhirVersion = toFhirVersion(options.getFhirVersion());
            final HealthcareDocumentType finalDocumentType = options.getDocumentType() == null ? null
                : HealthcareDocumentType.fromString(options.getDocumentType().toString());

            if (service != null) {
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
                                    new HealthcareLROTask().setParameters(
                                        new HealthcareTaskParameters()
                                            .setDocumentType(finalDocumentType)
                                            .setFhirVersion(finalFhirVersion)
                                            .setStringIndexType(finalStringIndexType)
                                            .setModelVersion(finalModelVersion)
                                            .setLoggingOptOut(finalLoggingOptOut)))),
                            finalContext)
                            .map(healthResponse -> {
                                final AnalyzeHealthcareEntitiesOperationDetail operationDetail =
                                    new AnalyzeHealthcareEntitiesOperationDetail();
                                AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                    parseOperationId(healthResponse.getDeserializedHeaders().getOperationLocation()));
                                return operationDetail;
                            })),
                    pollingOperationTextJob(
                        operationId -> service.jobStatusWithResponseAsync(operationId,
                            finalIncludeStatistics, null, null, finalContext)),
                    cancelOperationTextJob(
                        operationId -> service.cancelJobWithResponseAsync(operationId, finalContext)),
                    fetchingOperationTextJob(
                        operationId -> Mono.just(getHealthcareEntitiesPagedFlux(operationId, null, null,
                            finalIncludeStatistics, finalContext)))
                );
            }

            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    legacyService.healthWithResponseAsync(
                        new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                        finalModelVersion,
                        finalStringIndexType,
                        finalLoggingOptOut,
                        finalContext)
                        .map(healthResponse -> {
                            final AnalyzeHealthcareEntitiesOperationDetail operationDetail =
                                new AnalyzeHealthcareEntitiesOperationDetail();
                            AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(healthResponse.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })),
                pollingOperation(operationId -> legacyService.healthStatusWithResponseAsync(operationId,
                    null, null, finalIncludeStatistics, finalContext)),
                cancelOperation(operationId -> legacyService.cancelHealthJobWithResponseAsync(operationId, finalContext)),
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
            throwIfTargetServiceVersionFound(this.serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_0),
                getUnsupportedServiceApiVersionMessage("beginAnalyzeHealthcareEntities", serviceVersion,
                    TextAnalyticsServiceVersion.V3_1));
            throwIfCallingNotAvailableFeatureInOptions(options);
            inputDocumentsValidation(documents);
            options = getNotNullAnalyzeHealthcareEntitiesOptions(options);
            final Context finalContext = getNotNullContext(context)
                                             .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final StringIndexType finalStringIndexType = StringIndexType.UTF16CODE_UNIT;
            final String finalModelVersion = options.getModelVersion();
            final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
            final FhirVersion finalFhirVersion = toFhirVersion(options.getFhirVersion());
            final HealthcareDocumentType finalDocumentType = options.getDocumentType() == null ? null
                : HealthcareDocumentType.fromString(options.getDocumentType().toString());

            if (service != null) {
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
                                    new HealthcareLROTask().setParameters(
                                        new HealthcareTaskParameters()
                                            .setFhirVersion(finalFhirVersion)
                                            .setDocumentType(finalDocumentType)
                                            .setStringIndexType(finalStringIndexType)
                                            .setModelVersion(finalModelVersion)
                                            .setLoggingOptOut(finalLoggingOptOut)))),
                            finalContext)
                            .map(healthResponse -> {
                                final AnalyzeHealthcareEntitiesOperationDetail operationDetail =
                                    new AnalyzeHealthcareEntitiesOperationDetail();
                                AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                    parseOperationId(healthResponse.getDeserializedHeaders().getOperationLocation()));
                                return operationDetail;
                            })),
                    pollingOperationTextJob(
                        operationId -> service.jobStatusWithResponseAsync(operationId,
                            finalIncludeStatistics, null, null, finalContext)),
                    cancelOperationTextJob(
                        operationId -> service.cancelJobWithResponseAsync(operationId, finalContext)),
                    fetchingOperationIterable(
                        operationId -> Mono.just(new AnalyzeHealthcareEntitiesPagedIterable(
                            getHealthcareEntitiesPagedFlux(operationId, null, null,
                            finalIncludeStatistics, finalContext))))
                );
            }

            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    legacyService.healthWithResponseAsync(
                        new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                        finalModelVersion,
                        finalStringIndexType,
                        finalLoggingOptOut,
                        finalContext)
                        .map(healthResponse -> {
                            final AnalyzeHealthcareEntitiesOperationDetail operationDetail =
                                new AnalyzeHealthcareEntitiesOperationDetail();
                            AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(healthResponse.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })),
                pollingOperation(operationId -> legacyService.healthStatusWithResponseAsync(operationId, null,
                    null, finalIncludeStatistics, finalContext)),
                cancelOperation(operationId -> legacyService.cancelHealthJobWithResponseAsync(operationId, finalContext)),
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
                if (service != null) {
                    return service.jobStatusWithResponseAsync(operationId, showStatsValue, topValue, skipValue,
                        context)
                               .map(this::toHealthcarePagedResponse)
                               .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
                }

                return legacyService.healthStatusWithResponseAsync(operationId, topValue, skipValue, showStatsValue, context)
                           .map(this::toTextAnalyticsPagedResponse)
                           .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } else {
                if (service != null) {
                    return service.jobStatusWithResponseAsync(operationId, showStats, top, skip, context)
                               .map(this::toHealthcarePagedResponse)
                               .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
                }
                return legacyService.healthStatusWithResponseAsync(operationId, top, skip, showStats, context)
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
        final AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection =
            toAnalyzeHealthcareEntitiesResultCollection(healthcareResult);
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

        final List<Error> errors = healthcareJobState.getErrors();

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

    private PagedResponse<AnalyzeHealthcareEntitiesResultCollection> toHealthcarePagedResponse(
        Response<AnalyzeTextJobState> response) {

        final AnalyzeTextJobState jobState = response.getValue();
        final List<AnalyzeTextLROResult> lroResults = jobState.getTasks().getItems();

        HealthcareLROResult healthcareLROResult = (HealthcareLROResult) lroResults.get(0);
        final HealthcareResult healthcareResult = healthcareLROResult.getResults();
        final AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection =
            toAnalyzeHealthcareEntitiesResultCollection(healthcareResult);
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

        final List<Error> errors = jobState.getErrors();

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
            jobState.getNextLink(),
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

    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
                        Mono<PollResponse<AnalyzeHealthcareEntitiesOperationDetail>>>
        pollingOperationTextJob(Function<UUID, Mono<Response<AnalyzeTextJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeHealthcareEntitiesOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return pollingFunction.apply(operationId)
                           .flatMap(modelResponse ->
                                        processAnalyzeTextModelResponse(modelResponse, operationResultPollResponse))
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

    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
                        Mono<AnalyzeHealthcareEntitiesPagedFlux>>
        fetchingOperationTextJob(Function<UUID, Mono<AnalyzeHealthcareEntitiesPagedFlux>> fetchingFunction) {
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
                              Function<UUID, Mono<ResponseBase<CancelHealthJobHeaders, Void>>> cancelFunction) {
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

    private BiFunction<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
                          PollResponse<AnalyzeHealthcareEntitiesOperationDetail>,
                          Mono<AnalyzeHealthcareEntitiesOperationDetail>> cancelOperationTextJob(
        Function<UUID, Mono<ResponseBase<AnalyzeTextsCancelJobHeaders, Void>>> cancelFunction) {
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

        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getCreatedDateTime());
        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setLastModifiedAt(
            operationResultPollResponse.getValue(), analyzeOperationResultResponse.getValue().getLastUpdateDateTime());
        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getExpirationDateTime());
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }

    private Mono<PollResponse<AnalyzeHealthcareEntitiesOperationDetail>> processAnalyzeTextModelResponse(
        Response<AnalyzeTextJobState> analyzeOperationResultResponse,
        PollResponse<AnalyzeHealthcareEntitiesOperationDetail> operationResultPollResponse) {
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
        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getDisplayName());
        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getCreatedDateTime());
        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setLastModifiedAt(
            operationResultPollResponse.getValue(), analyzeOperationResultResponse.getValue().getLastUpdatedDateTime());
        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getExpirationDateTime());
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }

    private AnalyzeHealthcareEntitiesOptions getNotNullAnalyzeHealthcareEntitiesOptions(
        AnalyzeHealthcareEntitiesOptions options) {
        return options == null ? new AnalyzeHealthcareEntitiesOptions() : options;
    }

    private void throwIfCallingNotAvailableFeatureInOptions(AnalyzeHealthcareEntitiesOptions options) {
        if (options != null && options.getDisplayName() != null) {
            throwIfTargetServiceVersionFound(serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_1),
                getUnsupportedServiceApiVersionMessage("AnalyzeHealthcareEntitiesOptions.displayName",
                    serviceVersion, TextAnalyticsServiceVersion.V2022_05_01));
        }
    }
}
