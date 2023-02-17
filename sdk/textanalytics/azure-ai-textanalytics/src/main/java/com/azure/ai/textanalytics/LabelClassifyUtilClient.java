// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeTextsImpl;
import com.azure.ai.textanalytics.implementation.ClassifyDocumentOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ClassifyDocumentResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsExceptionPropertiesHelper;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobState;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobsInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROResult;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROTask;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsCancelJobHeaders;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextsSubmitJobHeaders;
import com.azure.ai.textanalytics.implementation.models.CustomLabelClassificationResult;
import com.azure.ai.textanalytics.implementation.models.CustomMultiLabelClassificationLROResult;
import com.azure.ai.textanalytics.implementation.models.CustomMultiLabelClassificationLROTask;
import com.azure.ai.textanalytics.implementation.models.CustomMultiLabelClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.CustomSingleLabelClassificationLROResult;
import com.azure.ai.textanalytics.implementation.models.CustomSingleLabelClassificationLROTask;
import com.azure.ai.textanalytics.implementation.models.CustomSingleLabelClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.Error;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.State;
import com.azure.ai.textanalytics.models.ClassifyDocumentOperationDetail;
import com.azure.ai.textanalytics.models.MultiLabelClassifyOptions;
import com.azure.ai.textanalytics.models.SingleLabelClassifyOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ClassifyDocumentPagedFlux;
import com.azure.ai.textanalytics.util.ClassifyDocumentPagedIterable;
import com.azure.ai.textanalytics.util.ClassifyDocumentResultCollection;
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
import static com.azure.ai.textanalytics.implementation.Utility.toLabelClassificationResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.models.State.CANCELLED;
import static com.azure.ai.textanalytics.implementation.models.State.NOT_STARTED;
import static com.azure.ai.textanalytics.implementation.models.State.RUNNING;
import static com.azure.ai.textanalytics.implementation.models.State.SUCCEEDED;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

class LabelClassifyUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(LabelClassifyUtilClient.class);
    private final AnalyzeTextsImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    LabelClassifyUtilClient(AnalyzeTextsImpl service, TextAnalyticsServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    PollerFlux<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> singleLabelClassify(
        Iterable<TextDocumentInput> documents, String projectName, String deploymentName,
        SingleLabelClassifyOptions options, Context context) {
        try {
            throwIfTargetServiceVersionFound(this.serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1),
                getUnsupportedServiceApiVersionMessage("beginSingleLabelClassify", serviceVersion,
                    TextAnalyticsServiceVersion.V2022_05_01));
            inputDocumentsValidation(documents);
            options = getNotNullSingleLabelClassifyOptions(options);
            final Context finalContext = getNotNullContext(context)
                .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
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
                                new CustomSingleLabelClassificationLROTask().setParameters(
                                    new CustomSingleLabelClassificationTaskParameters()
                                        .setProjectName(projectName)
                                        .setDeploymentName(deploymentName)
                                        .setLoggingOptOut(finalLoggingOptOut)))),
                        finalContext)
                        .map(responseBase -> {
                            final ClassifyDocumentOperationDetail operationDetail =
                                new ClassifyDocumentOperationDetail();
                            ClassifyDocumentOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(responseBase.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })
                ),
                pollingOperationTextJob(
                    operationId -> service.jobStatusWithResponseAsync(operationId,
                        finalIncludeStatistics, null, null, finalContext)),
                cancelOperationTextJob(
                    operationId -> service.cancelJobWithResponseAsync(operationId, finalContext)),
                fetchingOperationTextJob(
                    operationId -> Mono.just(getClassifyDocumentPagedFlux(operationId, null, null,
                        finalIncludeStatistics, finalContext)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable> singleLabelClassifyPagedIterable(
        Iterable<TextDocumentInput> documents, String projectName, String deploymentName,
        SingleLabelClassifyOptions options, Context context) {
        try {
            throwIfTargetServiceVersionFound(this.serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1),
                getUnsupportedServiceApiVersionMessage("beginSingleLabelClassify", serviceVersion,
                    TextAnalyticsServiceVersion.V2022_05_01));
            inputDocumentsValidation(documents);
            options = getNotNullSingleLabelClassifyOptions(options);
            final Context finalContext = enableSyncRestProxy(getNotNullContext(context))
                .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
            final String displayName = options.getDisplayName();
            final CustomSingleLabelClassificationLROTask task =
                new CustomSingleLabelClassificationLROTask().setParameters(
                    new CustomSingleLabelClassificationTaskParameters()
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
                    operationId -> getClassifyDocumentPagedIterable(operationId, null, null,
                        finalIncludeStatistics, finalContext))
            );
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    PollerFlux<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> multiLabelClassify(
        Iterable<TextDocumentInput> documents, String projectName, String deploymentName,
        MultiLabelClassifyOptions options, Context context) {
        try {
            throwIfTargetServiceVersionFound(this.serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1),
                getUnsupportedServiceApiVersionMessage("beginMultiLabelClassify", serviceVersion,
                    TextAnalyticsServiceVersion.V2022_05_01));
            inputDocumentsValidation(documents);
            options = getNotNullMultiLabelClassifyOptions(options);
            final Context finalContext = getNotNullContext(context)
                .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
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
                                new CustomMultiLabelClassificationLROTask().setParameters(
                                    new CustomMultiLabelClassificationTaskParameters()
                                        .setProjectName(projectName)
                                        .setDeploymentName(deploymentName)
                                        .setLoggingOptOut(finalLoggingOptOut)))),
                        finalContext)
                        .map(responseBase -> {
                            final ClassifyDocumentOperationDetail operationDetail =
                                new ClassifyDocumentOperationDetail();
                            ClassifyDocumentOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(responseBase.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })
                ),
                pollingOperationTextJob(
                    operationId -> service.jobStatusWithResponseAsync(operationId,
                        finalIncludeStatistics, null, null, finalContext)),
                cancelOperationTextJob(
                    operationId -> service.cancelJobWithResponseAsync(operationId, finalContext)),
                fetchingOperationTextJob(
                    operationId -> Mono.just(getClassifyDocumentPagedFlux(operationId, null, null,
                        finalIncludeStatistics, finalContext)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable> multiLabelClassifyPagedIterable(
        Iterable<TextDocumentInput> documents, String projectName, String deploymentName,
        MultiLabelClassifyOptions options, Context context) {
        try {
            throwIfTargetServiceVersionFound(this.serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1),
                getUnsupportedServiceApiVersionMessage("beginMultiLabelClassify", serviceVersion,
                    TextAnalyticsServiceVersion.V2022_05_01));
            inputDocumentsValidation(documents);
            options = getNotNullMultiLabelClassifyOptions(options);
            final Context finalContext = enableSyncRestProxy(getNotNullContext(context))
                .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
            final String displayName = options.getDisplayName();
            final CustomMultiLabelClassificationLROTask task = new CustomMultiLabelClassificationLROTask()
                .setParameters(
                    new CustomMultiLabelClassificationTaskParameters()
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
                    operationId -> getClassifyDocumentPagedIterable(operationId, null, null,
                        finalIncludeStatistics, finalContext)));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    ClassifyDocumentPagedFlux getClassifyDocumentPagedFlux(
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        return new ClassifyDocumentPagedFlux(
            () -> (continuationToken, pageSize) ->
                getPagedResult(continuationToken, operationId, top, skip, showStats, context).flux());
    }

    ClassifyDocumentPagedIterable getClassifyDocumentPagedIterable(
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        return new ClassifyDocumentPagedIterable(
            () -> (continuationToken, pageSize) ->
                getPagedResultSync(continuationToken, operationId, top, skip, showStats, context));
    }

    Mono<PagedResponse<ClassifyDocumentResultCollection>> getPagedResult(String continuationToken,
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        try {
            if (continuationToken != null) {
                final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
                top = getTopContinuesToken(continuationTokenMap);
                skip = getSkipContinuesToken(continuationTokenMap);
                showStats = getShowStatsContinuesToken(continuationTokenMap);
            }
            return service.jobStatusWithResponseAsync(operationId, showStats, top, skip, context)
                    .map(this::toClassifyDocumentResultCollectionPagedResponse)
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    PagedResponse<ClassifyDocumentResultCollection> getPagedResultSync(String continuationToken,
        UUID operationId, Integer top, Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
            top = getTopContinuesToken(continuationTokenMap);
            skip = getSkipContinuesToken(continuationTokenMap);
            showStats = getShowStatsContinuesToken(continuationTokenMap);
        }
        return toClassifyDocumentResultCollectionPagedResponse(service.jobStatusWithResponse(
            operationId, showStats, top, skip, context));
    }

    private PagedResponse<ClassifyDocumentResultCollection> toClassifyDocumentResultCollectionPagedResponse(
        Response<AnalyzeTextJobState> response) {

        final AnalyzeTextJobState jobState = response.getValue();
        final List<AnalyzeTextLROResult> lroResults = jobState.getTasks().getItems();

        final CustomLabelClassificationResult customLabelClassificationResult;
        final AnalyzeTextLROResult lroResult = lroResults.get(0);
        if (lroResult instanceof CustomSingleLabelClassificationLROResult) {
            CustomSingleLabelClassificationLROResult customSingleLabelClassificationLROResult =
                (CustomSingleLabelClassificationLROResult) lroResults.get(0);
            customLabelClassificationResult = customSingleLabelClassificationLROResult.getResults();
        } else if (lroResult instanceof CustomMultiLabelClassificationLROResult) {
            CustomMultiLabelClassificationLROResult customMultiLabelClassificationLROResult =
                (CustomMultiLabelClassificationLROResult) lroResults.get(0);
            customLabelClassificationResult = customMultiLabelClassificationLROResult.getResults();
        } else {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Invalid class type returned: " + lroResult.getClass().getName()));
        }

        final ClassifyDocumentResultCollection classifyDocumentResultCollection =
            toLabelClassificationResultCollection(customLabelClassificationResult);
        final RequestStatistics requestStatistics = customLabelClassificationResult.getStatistics();
        if (requestStatistics != null) {
            final TextDocumentBatchStatistics batchStatistic = new TextDocumentBatchStatistics(
                requestStatistics.getDocumentsCount(), requestStatistics.getValidDocumentsCount(),
                requestStatistics.getErroneousDocumentsCount(), requestStatistics.getTransactionsCount());
            ClassifyDocumentResultCollectionPropertiesHelper.setStatistics(
                classifyDocumentResultCollection, batchStatistic);
        }

        final List<Error> errors = jobState.getErrors();

        if (!CoreUtils.isNullOrEmpty(errors)) {
            final TextAnalyticsException textAnalyticsException = new TextAnalyticsException(
                "Classify label operation failed", null, null);
            final IterableStream<TextAnalyticsError> textAnalyticsErrors =
                IterableStream.of(errors.stream().map(Utility::toTextAnalyticsError).collect(Collectors.toList()));
            TextAnalyticsExceptionPropertiesHelper.setErrors(textAnalyticsException, textAnalyticsErrors);
            throw LOGGER.logExceptionAsError(textAnalyticsException);
        }

        return new PagedResponseBase<Void, ClassifyDocumentResultCollection>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            Arrays.asList(classifyDocumentResultCollection),
            jobState.getNextLink(),
            null);
    }

    // Activation operation
    private Function<PollingContext<ClassifyDocumentOperationDetail>,
        Mono<ClassifyDocumentOperationDetail>> activationOperation(
        Mono<ClassifyDocumentOperationDetail> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<ClassifyDocumentOperationDetail>, ClassifyDocumentOperationDetail>
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
            final ClassifyDocumentOperationDetail operationDetail = new ClassifyDocumentOperationDetail();
            ClassifyDocumentOperationDetailPropertiesHelper.setOperationId(operationDetail,
                parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
            return operationDetail;
        };
    }

    // Polling operation
    private Function<PollingContext<ClassifyDocumentOperationDetail>,
        Mono<PollResponse<ClassifyDocumentOperationDetail>>> pollingOperationTextJob(
        Function<UUID, Mono<Response<AnalyzeTextJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<ClassifyDocumentOperationDetail> operationResultPollResponse =
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

    private Function<PollingContext<ClassifyDocumentOperationDetail>,
        PollResponse<ClassifyDocumentOperationDetail>> pollingOperationTextJobSync(
        Function<UUID, Response<AnalyzeTextJobState>> pollingFunction) {
        return pollingContext -> {
            final PollResponse<ClassifyDocumentOperationDetail> operationResultPollResponse =
                pollingContext.getLatestResponse();
            final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
            return processAnalyzeTextModelResponse(pollingFunction.apply(operationId), operationResultPollResponse);
        };
    }

    // Fetching operation
    private Function<PollingContext<ClassifyDocumentOperationDetail>,
        Mono<ClassifyDocumentPagedFlux>> fetchingOperationTextJob(
        Function<UUID, Mono<ClassifyDocumentPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<ClassifyDocumentOperationDetail>,
        ClassifyDocumentPagedIterable> fetchingOperationSync(
        final Function<UUID, ClassifyDocumentPagedIterable> fetchingFunction) {
        return pollingContext -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
            return fetchingFunction.apply(resultUuid);
        };
    }

    // Cancel operation
    private BiFunction<PollingContext<ClassifyDocumentOperationDetail>,
        PollResponse<ClassifyDocumentOperationDetail>,
        Mono<ClassifyDocumentOperationDetail>> cancelOperationTextJob(
        Function<UUID, Mono<ResponseBase<AnalyzeTextsCancelJobHeaders, Void>>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            try {
                return cancelFunction.apply(resultUuid)
                    .map(cancelJobResponse -> {
                        final ClassifyDocumentOperationDetail operationResult = new ClassifyDocumentOperationDetail();
                        ClassifyDocumentOperationDetailPropertiesHelper.setOperationId(operationResult,
                            parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
                        return operationResult;
                    }).onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private BiFunction<PollingContext<ClassifyDocumentOperationDetail>,
        PollResponse<ClassifyDocumentOperationDetail>,
        ClassifyDocumentOperationDetail> cancelOperationTextJobSync(
        Function<UUID, ResponseBase<AnalyzeTextsCancelJobHeaders, Void>> cancelFunction) {
        return (activationResponse, pollingContext) -> {
            final UUID resultUuid = UUID.fromString(pollingContext.getValue().getOperationId());
            ResponseBase<AnalyzeTextsCancelJobHeaders, Void> cancelJobResponse = cancelFunction.apply(resultUuid);
            final ClassifyDocumentOperationDetail operationResult = new ClassifyDocumentOperationDetail();
            ClassifyDocumentOperationDetailPropertiesHelper.setOperationId(operationResult,
                parseOperationId(cancelJobResponse.getDeserializedHeaders().getOperationLocation()));
            return operationResult;
        };
    }

    private PollResponse<ClassifyDocumentOperationDetail> processAnalyzeTextModelResponse(
        Response<AnalyzeTextJobState> analyzeOperationResultResponse,
        PollResponse<ClassifyDocumentOperationDetail> operationResultPollResponse) {
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
        ClassifyDocumentOperationDetailPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getDisplayName());
        ClassifyDocumentOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getCreatedDateTime());
        ClassifyDocumentOperationDetailPropertiesHelper.setLastModifiedAt(
            operationResultPollResponse.getValue(), analyzeOperationResultResponse.getValue().getLastUpdatedDateTime());
        ClassifyDocumentOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeOperationResultResponse.getValue().getExpirationDateTime());
        return new PollResponse<>(status, operationResultPollResponse.getValue());
    }

    private SingleLabelClassifyOptions getNotNullSingleLabelClassifyOptions(SingleLabelClassifyOptions options) {
        return options == null ? new SingleLabelClassifyOptions() : options;
    }

    private MultiLabelClassifyOptions getNotNullMultiLabelClassifyOptions(MultiLabelClassifyOptions options) {
        return options == null ? new MultiLabelClassifyOptions() : options;
    }
}
