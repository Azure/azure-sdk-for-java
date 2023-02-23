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
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROTask;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsCancelJobHeaders;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsSubmitJobHeaders;
import com.azure.ai.textanalytics.implementation.models.CancelHealthJobHeaders;
import com.azure.ai.textanalytics.implementation.models.Error;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.FhirVersion;
import com.azure.ai.textanalytics.implementation.models.HealthHeaders;
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
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExists;
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

class AnalyzeHealthcareEntityUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(AnalyzeHealthcareEntityUtilClient.class);
    private final TextAnalyticsClientImpl legacyService;
    private final AnalyzeTextsImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    AnalyzeHealthcareEntityUtilClient(TextAnalyticsClientImpl legacyService,
        TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = legacyService;
        this.service = null;
        this.serviceVersion = serviceVersion;
    }

    AnalyzeHealthcareEntityUtilClient(AnalyzeTextsImpl service, TextAnalyticsServiceVersion serviceVersion) {
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
            final Context finalContext = getNotNullContext(context);
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

    SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
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
            final Context finalContext = enableSyncRestProxy(getNotNullContext(context));
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final StringIndexType finalStringIndexType = StringIndexType.UTF16CODE_UNIT;
            final String finalModelVersion = options.getModelVersion();
            final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
            final FhirVersion finalFhirVersion = toFhirVersion(options.getFhirVersion());
            final HealthcareDocumentType finalDocumentType = options.getDocumentType() == null ? null
                : HealthcareDocumentType.fromString(options.getDocumentType().toString());

            if (service != null) {
                final String displayName = options.getDisplayName();
                final HealthcareLROTask task = new HealthcareLROTask().setParameters(
                    new HealthcareTaskParameters()
                        .setFhirVersion(finalFhirVersion)
                        .setDocumentType(finalDocumentType)
                        .setStringIndexType(finalStringIndexType)
                        .setModelVersion(finalModelVersion)
                        .setLoggingOptOut(finalLoggingOptOut));

                return SyncPoller.createPoller(
                    DEFAULT_POLL_INTERVAL,
                    cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                        activationOperationLanguageApiSync(documents, task, displayName, finalContext).apply(cxt)),
                    pollingOperationLanguageApiSync(
                        operationId -> service.jobStatusWithResponse(operationId,
                            finalIncludeStatistics, null, null, finalContext)),
                    cancelOperationLanguageApiSync(
                        operationId -> service.cancelJobWithResponse(operationId, finalContext)),
                    fetchingOperationIterable(
                        operationId -> getHealthcareEntitiesPagedIterable(operationId, null, null,
                            finalIncludeStatistics, finalContext))
                );
            }
            return SyncPoller.createPoller(
                DEFAULT_POLL_INTERVAL,
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    activationOperationLegacyApiSync(documents, finalModelVersion, finalStringIndexType,
                        finalLoggingOptOut, finalContext).apply(cxt)),
                pollingOperationLegacyApiSync(
                    operationId -> legacyService.healthStatusWithResponseSync(operationId, null,
                        null, finalIncludeStatistics, finalContext)),
                cancelOperationLegacyApiSync(operationId ->
                    legacyService.cancelHealthJobWithResponseSync(operationId, finalContext)),
                fetchingOperationIterable(
                    operationId -> getHealthcareEntitiesPagedIterable(operationId, null, null,
                        finalIncludeStatistics, finalContext))
            );
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    AnalyzeHealthcareEntitiesPagedFlux getHealthcareEntitiesPagedFlux(
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        return new AnalyzeHealthcareEntitiesPagedFlux(
            () -> (continuationToken, pageSize) ->
                getPagedResult(continuationToken, operationId, top, skip, showStats, context).flux());
    }

    AnalyzeHealthcareEntitiesPagedIterable getHealthcareEntitiesPagedIterable(
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        return new AnalyzeHealthcareEntitiesPagedIterable(
            () -> (continuationToken, pageSize) ->
                getPagedResultSync(continuationToken, operationId, top, skip, showStats, context));
    }

    Mono<PagedResponse<AnalyzeHealthcareEntitiesResultCollection>> getPagedResult(String continuationToken,
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        try {
            if (continuationToken != null) {
                final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
                top = getTopContinuesToken(continuationTokenMap);
                skip = getSkipContinuesToken(continuationTokenMap);
                showStats = getShowStatsContinuesToken(continuationTokenMap);
            }
            return service != null
                ? service.jobStatusWithResponseAsync(operationId, showStats, top, skip, context)
                .map(this::toHealthcarePagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)
                : legacyService.healthStatusWithResponseAsync(operationId, top, skip, showStats, context)
                .map(this::toTextAnalyticsPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    PagedResponse<AnalyzeHealthcareEntitiesResultCollection> getPagedResultSync(String continuationToken,
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
            top = getTopContinuesToken(continuationTokenMap);
            skip = getSkipContinuesToken(continuationTokenMap);
            showStats = getShowStatsContinuesToken(continuationTokenMap);
        }
        return service != null
            ? toHealthcarePagedResponse(service.jobStatusWithResponse(
            operationId, showStats, top, skip, context))
            : toTextAnalyticsPagedResponse(legacyService.healthStatusWithResponseSync(
            operationId, top, skip, showStats, context));
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
            throw LOGGER.logExceptionAsError(textAnalyticsException);
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
            throw LOGGER.logExceptionAsError(textAnalyticsException);
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
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        AnalyzeHealthcareEntitiesOperationDetail> activationOperationLanguageApiSync(
        Iterable<TextDocumentInput> documents, AnalyzeTextLROTask task, String displayName,
        Context context) {
        return pollingContext -> {
            try {
                final ResponseBase<AnalyzeTextsSubmitJobHeaders, Void> analyzeResponse =
                    service.submitJobWithResponse(
                        new AnalyzeTextJobsInput()
                            .setDisplayName(displayName)
                            .setAnalysisInput(new MultiLanguageAnalysisInput()
                                .setDocuments(toMultiLanguageInput(documents)))
                            .setTasks(Arrays.asList(task)),
                        context);
                final AnalyzeHealthcareEntitiesOperationDetail operationDetail =
                    new AnalyzeHealthcareEntitiesOperationDetail();
                AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                    parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                return operationDetail;
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError(ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        AnalyzeHealthcareEntitiesOperationDetail> activationOperationLegacyApiSync(
        Iterable<TextDocumentInput> documents, String modelVersion, StringIndexType stringIndexType,
        boolean loggingOptOut, Context context) {
        return pollingContext -> {
            final ResponseBase<HealthHeaders, Void> analyzeResponse = legacyService.healthWithResponseSync(
                new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                modelVersion,
                stringIndexType,
                loggingOptOut,
                context);
            final AnalyzeHealthcareEntitiesOperationDetail operationDetail =
                new AnalyzeHealthcareEntitiesOperationDetail();
            AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationDetail,
                parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
            return operationDetail;
        };
    }

    // Polling operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        Mono<PollResponse<AnalyzeHealthcareEntitiesOperationDetail>>> pollingOperation(
            Function<UUID, Mono<Response<HealthcareJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeHealthcareEntitiesOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID resultUuid = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return pollingFunction.apply(resultUuid)
                    .flatMap(modelResponse -> Mono.just(
                        processHealthcareJobResponseLegacyApi(modelResponse, operationResultPollResponse)))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        Mono<PollResponse<AnalyzeHealthcareEntitiesOperationDetail>>> pollingOperationTextJob(
            Function<UUID, Mono<Response<AnalyzeTextJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeHealthcareEntitiesOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return pollingFunction.apply(operationId)
                    .flatMap(modelResponse ->
                        Mono.just(processHealthcareJobResponseLanguageApi(modelResponse, operationResultPollResponse)))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        PollResponse<AnalyzeHealthcareEntitiesOperationDetail>> pollingOperationLanguageApiSync(
            Function<UUID, Response<AnalyzeTextJobState>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeHealthcareEntitiesOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return processHealthcareJobResponseLanguageApi(pollingFunction.apply(operationId),
                    operationResultPollResponse);
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError((RuntimeException) mapToHttpResponseExceptionIfExists(ex));
            }
        };
    }

    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        PollResponse<AnalyzeHealthcareEntitiesOperationDetail>> pollingOperationLegacyApiSync(
        Function<UUID, Response<HealthcareJobState>> pollingFunction) {
        return pollingContext -> {
            final PollResponse<AnalyzeHealthcareEntitiesOperationDetail> operationResultPollResponse =
                pollingContext.getLatestResponse();
            final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
            return processHealthcareJobResponseLegacyApi(pollingFunction.apply(operationId),
                operationResultPollResponse);
        };
    }

    // Fetching operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>, Mono<AnalyzeHealthcareEntitiesPagedFlux>>
        fetchingOperation(Function<UUID, Mono<AnalyzeHealthcareEntitiesPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>, Mono<AnalyzeHealthcareEntitiesPagedFlux>>
        fetchingOperationTextJob(Function<UUID, Mono<AnalyzeHealthcareEntitiesPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    // Cancel operation
    private BiFunction<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        PollResponse<AnalyzeHealthcareEntitiesOperationDetail>, Mono<AnalyzeHealthcareEntitiesOperationDetail>>
        cancelOperation(Function<UUID, Mono<ResponseBase<CancelHealthJobHeaders, Void>>> cancelFunction) {
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
                return monoError(LOGGER, ex);
            }
        };
    }

    private BiFunction<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        PollResponse<AnalyzeHealthcareEntitiesOperationDetail>, Mono<AnalyzeHealthcareEntitiesOperationDetail>>
        cancelOperationTextJob(Function<UUID, Mono<ResponseBase<AnalyzeTextsCancelJobHeaders, Void>>> cancelFunction) {
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
                return monoError(LOGGER, ex);
            }
        };
    }

    private BiFunction<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        PollResponse<AnalyzeHealthcareEntitiesOperationDetail>, AnalyzeHealthcareEntitiesOperationDetail>
        cancelOperationLegacyApiSync(Function<UUID, ResponseBase<CancelHealthJobHeaders, Void>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            final ResponseBase<CancelHealthJobHeaders, Void> cancelResponse =
                cancelFunction.apply(resultUuid);
            final AnalyzeHealthcareEntitiesOperationDetail operationResult =
                new AnalyzeHealthcareEntitiesOperationDetail();
            AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationResult,
                parseOperationId(cancelResponse.getDeserializedHeaders().getOperationLocation()));
            return operationResult;
        };
    }


    private BiFunction<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        PollResponse<AnalyzeHealthcareEntitiesOperationDetail>, AnalyzeHealthcareEntitiesOperationDetail>
        cancelOperationLanguageApiSync(
            Function<UUID, ResponseBase<AnalyzeTextsCancelJobHeaders, Void>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            try {
                final ResponseBase<AnalyzeTextsCancelJobHeaders, Void> cancelResponse =
                    cancelFunction.apply(resultUuid);
                final AnalyzeHealthcareEntitiesOperationDetail operationResult =
                    new AnalyzeHealthcareEntitiesOperationDetail();
                AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setOperationId(operationResult,
                    parseOperationId(cancelResponse.getDeserializedHeaders().getOperationLocation()));
                return operationResult;
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError((RuntimeException) mapToHttpResponseExceptionIfExists(ex));
            }
        };
    }

    // Fetching iterable operation
    private Function<PollingContext<AnalyzeHealthcareEntitiesOperationDetail>,
        AnalyzeHealthcareEntitiesPagedIterable> fetchingOperationIterable(
            final Function<UUID, AnalyzeHealthcareEntitiesPagedIterable> fetchingFunction) {
        return pollingContext -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
            return fetchingFunction.apply(resultUuid);
        };
    }

    private PollResponse<AnalyzeHealthcareEntitiesOperationDetail> processHealthcareJobResponseLegacyApi(
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
        return new PollResponse<>(status, operationResultPollResponse.getValue());
    }

    private PollResponse<AnalyzeHealthcareEntitiesOperationDetail> processHealthcareJobResponseLanguageApi(
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
        return new PollResponse<>(status, operationResultPollResponse.getValue());
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
