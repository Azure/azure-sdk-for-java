// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeActionsOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeActionsResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeSentimentActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoriesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ExtractKeyPhrasesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.ExtractSummaryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeCustomEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeLinkedEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizePiiEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeBatchInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeJobState;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesResult;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesTask;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.CustomMultiClassificationResult;
import com.azure.ai.textanalytics.implementation.models.CustomMultiClassificationTask;
import com.azure.ai.textanalytics.implementation.models.CustomMultiClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.CustomSingleClassificationResult;
import com.azure.ai.textanalytics.implementation.models.CustomSingleClassificationTask;
import com.azure.ai.textanalytics.implementation.models.CustomSingleClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.EntitiesTask;
import com.azure.ai.textanalytics.implementation.models.EntitiesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingTask;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingTaskParameters;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationResult;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationTask;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationTaskParametersSortBy;
import com.azure.ai.textanalytics.implementation.models.JobManifestTasks;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.ai.textanalytics.implementation.models.KeyPhrasesTask;
import com.azure.ai.textanalytics.implementation.models.KeyPhrasesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.PiiResult;
import com.azure.ai.textanalytics.implementation.models.PiiTask;
import com.azure.ai.textanalytics.implementation.models.PiiTaskParameters;
import com.azure.ai.textanalytics.implementation.models.PiiTaskParametersDomain;
import com.azure.ai.textanalytics.implementation.models.SentimentAnalysisTask;
import com.azure.ai.textanalytics.implementation.models.SentimentAnalysisTaskParameters;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasks;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksCustomEntityRecognitionTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksCustomMultiClassificationTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksCustomSingleClassificationTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityLinkingTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityRecognitionPiiTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityRecognitionTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksExtractiveSummarizationTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksKeyPhraseExtractionTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksSentimentAnalysisTasksItem;
import com.azure.ai.textanalytics.implementation.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentActionResult;
import com.azure.ai.textanalytics.models.ClassifyCustomMultiCategoriesActionResult;
import com.azure.ai.textanalytics.models.ClassifyCustomSingleCategoryActionResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.ExtractSummaryActionResult;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.TextAnalyticsActionResult;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedFlux;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.parseOperationId;
import static com.azure.ai.textanalytics.implementation.Utility.toAnalyzeSentimentResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toCategoriesFilter;
import static com.azure.ai.textanalytics.implementation.Utility.toClassifyMultiCategoriesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toClassifySingleCategoryResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toExtractKeyPhrasesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toExtractSummaryResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeCustomEntitiesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeEntitiesResultCollectionResponse;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeLinkedEntitiesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizePiiEntitiesResultCollection;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

class AnalyzeActionsAsyncClient {
    private static final String ENTITY_RECOGNITION_TASKS = "entityRecognitionTasks";
    private static final String ENTITY_RECOGNITION_PII_TASKS = "entityRecognitionPiiTasks";
    private static final String KEY_PHRASE_EXTRACTION_TASKS = "keyPhraseExtractionTasks";
    private static final String ENTITY_LINKING_TASKS = "entityLinkingTasks";
    private static final String SENTIMENT_ANALYSIS_TASKS = "sentimentAnalysisTasks";
    private static final String EXTRACTIVE_SUMMARIZATION_TASKS = "extractiveSummarizationTasks";
    private static final String CUSTOM_ENTITY_RECOGNITION_TASKS =  "customEntityRecognitionTasks";
    private static final String CUSTOM_SINGLE_CLASSIFICATION_TASKS = "customClassificationTasks";
    private static final String CUSTOM_MULTI_CLASSIFICATION_TASKS = "customMultiClassificationTasks";

    private static final String REGEX_ACTION_ERROR_TARGET =
        String.format("#/tasks/(%s|%s|%s|%s|%s|%s|%s|%s|%s)/(\\d+)", KEY_PHRASE_EXTRACTION_TASKS,
            ENTITY_RECOGNITION_PII_TASKS, ENTITY_RECOGNITION_TASKS, ENTITY_LINKING_TASKS, SENTIMENT_ANALYSIS_TASKS,
            EXTRACTIVE_SUMMARIZATION_TASKS, CUSTOM_ENTITY_RECOGNITION_TASKS, CUSTOM_SINGLE_CLASSIFICATION_TASKS,
            CUSTOM_MULTI_CLASSIFICATION_TASKS);

    private final ClientLogger logger = new ClientLogger(AnalyzeActionsAsyncClient.class);
    private final TextAnalyticsClientImpl service;
    private static final Pattern PATTERN;
    static {
        PATTERN = Pattern.compile(REGEX_ACTION_ERROR_TARGET, Pattern.MULTILINE);
    }

    AnalyzeActionsAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    PollerFlux<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> beginAnalyzeActions(
        Iterable<TextDocumentInput> documents, TextAnalyticsActions actions, AnalyzeActionsOptions options,
        Context context) {
        try {
            inputDocumentsValidation(documents);
            options = getNotNullAnalyzeActionsOptions(options);
            final Context finalContext = getNotNullContext(context)
                                             .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final AnalyzeBatchInput analyzeBatchInput =
                new AnalyzeBatchInput()
                    .setAnalysisInput(new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(getJobManifestTasks(actions));
            analyzeBatchInput.setDisplayName(actions.getDisplayName());
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    service.analyzeWithResponseAsync(analyzeBatchInput, finalContext)
                        .map(analyzeResponse -> {
                            final AnalyzeActionsOperationDetail textAnalyticsOperationResult =
                                new AnalyzeActionsOperationDetail();
                            AnalyzeActionsOperationDetailPropertiesHelper
                                .setOperationId(textAnalyticsOperationResult,
                                    parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return textAnalyticsOperationResult;
                        })),
                pollingOperation(operationId -> service.analyzeStatusWithResponseAsync(operationId,
                    finalIncludeStatistics, null, null, finalContext)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperation(operationId -> Mono.just(getAnalyzeOperationFluxPage(
                    operationId, null, null, finalIncludeStatistics, finalContext)))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    PollerFlux<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> beginAnalyzeActionsIterable(
        Iterable<TextDocumentInput> documents, TextAnalyticsActions actions, AnalyzeActionsOptions options,
        Context context) {
        try {
            inputDocumentsValidation(documents);
            options = getNotNullAnalyzeActionsOptions(options);
            final Context finalContext = getNotNullContext(context)
                                             .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final AnalyzeBatchInput analyzeBatchInput =
                new AnalyzeBatchInput()
                    .setAnalysisInput(new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(getJobManifestTasks(actions));
            analyzeBatchInput.setDisplayName(actions.getDisplayName());
            final boolean finalIncludeStatistics = options.isIncludeStatistics();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    service.analyzeWithResponseAsync(analyzeBatchInput, finalContext)
                        .map(analyzeResponse -> {
                            final AnalyzeActionsOperationDetail operationDetail =
                                new AnalyzeActionsOperationDetail();
                            AnalyzeActionsOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })),
                pollingOperation(operationId -> service.analyzeStatusWithResponseAsync(operationId,
                    finalIncludeStatistics, null, null, finalContext)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperationIterable(
                    operationId -> Mono.just(new AnalyzeActionsResultPagedIterable(getAnalyzeOperationFluxPage(
                        operationId, null, null, finalIncludeStatistics, finalContext))))
            );
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private JobManifestTasks getJobManifestTasks(TextAnalyticsActions actions) {
        return new JobManifestTasks()
            .setEntityRecognitionTasks(actions.getRecognizeEntitiesActions() == null ? null
                : StreamSupport.stream(actions.getRecognizeEntitiesActions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final EntitiesTask entitiesTask = new EntitiesTask();
                        entitiesTask.setParameters(
                            new EntitiesTaskParameters()
                                .setModelVersion(action.getModelVersion())
                                .setLoggingOptOut(action.isServiceLogsDisabled())
                                .setStringIndexType(StringIndexType.UTF16CODE_UNIT));
                        return entitiesTask;
                    }).collect(Collectors.toList()))
            .setEntityRecognitionPiiTasks(actions.getRecognizePiiEntitiesActions() == null ? null
                : StreamSupport.stream(actions.getRecognizePiiEntitiesActions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final PiiTask piiTask = new PiiTask();
                        piiTask.setParameters(
                            new PiiTaskParameters()
                                .setModelVersion(action.getModelVersion())
                                .setLoggingOptOut(action.isServiceLogsDisabled())
                                .setDomain(PiiTaskParametersDomain.fromString(
                                    action.getDomainFilter() == null ? null
                                        : action.getDomainFilter().toString()))
                                .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                .setPiiCategories(toCategoriesFilter(action.getCategoriesFilter()))
                        );
                        return piiTask;
                    }).collect(Collectors.toList()))
            .setKeyPhraseExtractionTasks(actions.getExtractKeyPhrasesActions() == null ? null
                : StreamSupport.stream(actions.getExtractKeyPhrasesActions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final KeyPhrasesTask keyPhrasesTask = new KeyPhrasesTask();
                        keyPhrasesTask.setParameters(
                            new KeyPhrasesTaskParameters()
                                .setModelVersion(action.getModelVersion())
                                .setLoggingOptOut(action.isServiceLogsDisabled())
                        );
                        return keyPhrasesTask;
                    }).collect(Collectors.toList()))
            .setEntityLinkingTasks(actions.getRecognizeLinkedEntitiesActions() == null ? null
                : StreamSupport.stream(actions.getRecognizeLinkedEntitiesActions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final EntityLinkingTask entityLinkingTask = new EntityLinkingTask();
                        entityLinkingTask.setParameters(
                            new EntityLinkingTaskParameters()
                                .setModelVersion(action.getModelVersion())
                                .setLoggingOptOut(action.isServiceLogsDisabled())
                                .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                        );
                        return entityLinkingTask;
                    }).collect(Collectors.toList()))
            .setSentimentAnalysisTasks(actions.getAnalyzeSentimentActions() == null ? null
                : StreamSupport.stream(actions.getAnalyzeSentimentActions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final SentimentAnalysisTask sentimentAnalysisTask = new SentimentAnalysisTask();
                        sentimentAnalysisTask.setParameters(
                            new SentimentAnalysisTaskParameters()
                                .setModelVersion(action.getModelVersion())
                                .setLoggingOptOut(action.isServiceLogsDisabled())
                                .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                        );
                        return sentimentAnalysisTask;
                    }).collect(Collectors.toList()))
            .setExtractiveSummarizationTasks(actions.getExtractSummaryActions() == null ? null
                : StreamSupport.stream(actions.getExtractSummaryActions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final ExtractiveSummarizationTask extractiveSummarizationTask =
                            new ExtractiveSummarizationTask();
                        extractiveSummarizationTask.setParameters(
                            new ExtractiveSummarizationTaskParameters()
                                .setModelVersion(action.getModelVersion())
                                .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                .setLoggingOptOut(action.isServiceLogsDisabled())
                                .setSentenceCount(action.getMaxSentenceCount())
                                .setSortBy(action.getOrderBy() == null ? null
                                               : ExtractiveSummarizationTaskParametersSortBy.fromString(
                                                   action.getOrderBy().toString()))
                        );
                        return extractiveSummarizationTask;
                    }).collect(Collectors.toList()))
            .setCustomEntityRecognitionTasks(actions.getRecognizeCustomEntitiesActions() == null ? null
                : StreamSupport.stream(actions.getRecognizeCustomEntitiesActions().spliterator(), false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final CustomEntitiesTask customEntitiesTask = new CustomEntitiesTask();

                        customEntitiesTask.setParameters(
                            new CustomEntitiesTaskParameters()
                                .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                .setLoggingOptOut(action.isServiceLogsDisabled())
                                .setProjectName(action.getProjectName())
                                .setDeploymentName(action.getDeploymentName())
                        );
                        return customEntitiesTask;
                    }).collect(Collectors.toList()))
            .setCustomSingleClassificationTasks(actions.getClassifyCustomSingleCategoryActions() == null ? null
                : StreamSupport.stream(actions.getClassifyCustomSingleCategoryActions().spliterator(),
            false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final CustomSingleClassificationTask customSingleClassificationTask =
                            new CustomSingleClassificationTask();

                        customSingleClassificationTask.setParameters(
                            new CustomSingleClassificationTaskParameters()
                                .setLoggingOptOut(action.isServiceLogsDisabled())
                                .setProjectName(action.getProjectName())
                                .setDeploymentName(action.getDeploymentName())
                        );
                        return customSingleClassificationTask;
                    }).collect(Collectors.toList()))
            .setCustomMultiClassificationTasks(actions.getClassifyCustomMultiCategoriesActions() == null ? null
                : StreamSupport.stream(actions.getClassifyCustomMultiCategoriesActions().spliterator(),
                false).map(
                    action -> {
                        if (action == null) {
                            return null;
                        }
                        final CustomMultiClassificationTask customMultiClassificationTask =
                            new CustomMultiClassificationTask();

                        customMultiClassificationTask.setParameters(
                            new CustomMultiClassificationTaskParameters()
                                .setLoggingOptOut(action.isServiceLogsDisabled())
                                .setProjectName(action.getProjectName())
                                .setDeploymentName(action.getDeploymentName())
                        );
                        return customMultiClassificationTask;
                    }).collect(Collectors.toList()));
    }

    private Function<PollingContext<AnalyzeActionsOperationDetail>, Mono<AnalyzeActionsOperationDetail>>
        activationOperation(Mono<AnalyzeActionsOperationDetail> operationResult) {
        return pollingContext -> {
            try {
                return operationResult.onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeActionsOperationDetail>, Mono<PollResponse<AnalyzeActionsOperationDetail>>>
        pollingOperation(Function<String, Mono<Response<AnalyzeJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeActionsOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                final String operationId = operationResultPollResponse.getValue().getOperationId();
                return pollingFunction.apply(operationId)
                    .flatMap(modelResponse -> processAnalyzedModelResponse(modelResponse, operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeActionsOperationDetail>, Mono<AnalyzeActionsResultPagedFlux>>
        fetchingOperation(Function<String, Mono<AnalyzeActionsResultPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                final String operationId = pollingContext.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(operationId);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeActionsOperationDetail>, Mono<AnalyzeActionsResultPagedIterable>>
        fetchingOperationIterable(Function<String, Mono<AnalyzeActionsResultPagedIterable>> fetchingFunction) {
        return pollingContext -> {
            try {
                // TODO: [Service-Bug] change back to UUID after service support it.
                //  https://github.com/Azure/azure-sdk-for-java/issues/17629
//                final UUID resultUUID = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                final String operationId = pollingContext.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(operationId);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    AnalyzeActionsResultPagedFlux getAnalyzeOperationFluxPage(String operationId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new AnalyzeActionsResultPagedFlux(
            () -> (continuationToken, pageSize) ->
                      getPage(continuationToken, operationId, top, skip, showStats, context).flux());
    }

    Mono<PagedResponse<AnalyzeActionsResult>> getPage(String continuationToken, String operationId, Integer top,
        Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
            final Integer topValue = (Integer) continuationTokenMap.getOrDefault("$top", null);
            final Integer skipValue = (Integer) continuationTokenMap.getOrDefault("$skip", null);
            final Boolean showStatsValue = (Boolean) continuationTokenMap.getOrDefault(showStats, false);
            return service.analyzeStatusWithResponseAsync(operationId, showStatsValue, topValue, skipValue, context)
                .map(this::toAnalyzeActionsResultPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        } else {
            return service.analyzeStatusWithResponseAsync(operationId, showStats, top, skip, context)
                .map(this::toAnalyzeActionsResultPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        }
    }

    private PagedResponse<AnalyzeActionsResult> toAnalyzeActionsResultPagedResponse(Response<AnalyzeJobState> response) {
        final AnalyzeJobState analyzeJobState = response.getValue();
        return new PagedResponseBase<Void, AnalyzeActionsResult>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            Arrays.asList(toAnalyzeActionsResult(analyzeJobState)),
            analyzeJobState.getNextLink(),
            null);
    }

    private AnalyzeActionsResult toAnalyzeActionsResult(AnalyzeJobState analyzeJobState) {
        TasksStateTasks tasksStateTasks = analyzeJobState.getTasks();
        final List<TasksStateTasksEntityRecognitionPiiTasksItem> piiTasksItems =
            tasksStateTasks.getEntityRecognitionPiiTasks();
        final List<TasksStateTasksEntityRecognitionTasksItem> entityRecognitionTasksItems =
            tasksStateTasks.getEntityRecognitionTasks();
        final List<TasksStateTasksKeyPhraseExtractionTasksItem> keyPhraseExtractionTasks =
            tasksStateTasks.getKeyPhraseExtractionTasks();
        final List<TasksStateTasksEntityLinkingTasksItem> linkedEntityRecognitionTasksItems =
            tasksStateTasks.getEntityLinkingTasks();
        final List<TasksStateTasksSentimentAnalysisTasksItem> sentimentAnalysisTasksItems =
            tasksStateTasks.getSentimentAnalysisTasks();
        final List<TasksStateTasksExtractiveSummarizationTasksItem> extractiveSummarizationTasksItems =
            tasksStateTasks.getExtractiveSummarizationTasks();
        final List<TasksStateTasksCustomEntityRecognitionTasksItem> customEntityRecognitionTasksItems =
            tasksStateTasks.getCustomEntityRecognitionTasks();
        final List<TasksStateTasksCustomSingleClassificationTasksItem> customSingleClassificationTasksItems =
            tasksStateTasks.getCustomSingleClassificationTasks();
        final List<TasksStateTasksCustomMultiClassificationTasksItem> customMultiClassificationTasksItems =
            tasksStateTasks.getCustomMultiClassificationTasks();

        List<RecognizeEntitiesActionResult> recognizeEntitiesActionResults = new ArrayList<>();
        List<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults = new ArrayList<>();
        List<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults = new ArrayList<>();
        List<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults = new ArrayList<>();
        List<AnalyzeSentimentActionResult> analyzeSentimentActionResults = new ArrayList<>();
        List<ExtractSummaryActionResult> extractSummaryActionResults = new ArrayList<>();
        List<RecognizeCustomEntitiesActionResult> recognizeCustomEntitiesActionResults = new ArrayList<>();
        List<ClassifyCustomSingleCategoryActionResult> classifyCustomSingleCategoryActionResults =
            new ArrayList<>();
        List<ClassifyCustomMultiCategoriesActionResult> classifyCustomMultiCategoriesActionResults =
            new ArrayList<>();

        if (!CoreUtils.isNullOrEmpty(entityRecognitionTasksItems)) {
            for (int i = 0; i < entityRecognitionTasksItems.size(); i++) {
                final TasksStateTasksEntityRecognitionTasksItem taskItem = entityRecognitionTasksItems.get(i);
                final RecognizeEntitiesActionResult actionResult = new RecognizeEntitiesActionResult();
                final EntitiesResult results = taskItem.getResults();
                if (results != null) {
                    RecognizeEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                        toRecognizeEntitiesResultCollectionResponse(results));
                }
                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                    taskItem.getLastUpdateDateTime());
                recognizeEntitiesActionResults.add(actionResult);
            }
        }

        if (!CoreUtils.isNullOrEmpty(piiTasksItems)) {
            for (int i = 0; i < piiTasksItems.size(); i++) {
                final TasksStateTasksEntityRecognitionPiiTasksItem taskItem = piiTasksItems.get(i);
                final RecognizePiiEntitiesActionResult actionResult = new RecognizePiiEntitiesActionResult();
                final PiiResult results = taskItem.getResults();
                if (results != null) {
                    RecognizePiiEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                        toRecognizePiiEntitiesResultCollection(results));
                }
                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                    taskItem.getLastUpdateDateTime());
                recognizePiiEntitiesActionResults.add(actionResult);
            }
        }

        if (!CoreUtils.isNullOrEmpty(keyPhraseExtractionTasks)) {
            for (int i = 0; i < keyPhraseExtractionTasks.size(); i++) {
                final TasksStateTasksKeyPhraseExtractionTasksItem taskItem = keyPhraseExtractionTasks.get(i);
                final ExtractKeyPhrasesActionResult actionResult = new ExtractKeyPhrasesActionResult();
                final KeyPhraseResult results = taskItem.getResults();
                if (results != null) {
                    ExtractKeyPhrasesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                        toExtractKeyPhrasesResultCollection(results));
                }
                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                    taskItem.getLastUpdateDateTime());
                extractKeyPhrasesActionResults.add(actionResult);
            }
        }

        if (!CoreUtils.isNullOrEmpty(linkedEntityRecognitionTasksItems)) {
            for (int i = 0; i < linkedEntityRecognitionTasksItems.size(); i++) {
                final TasksStateTasksEntityLinkingTasksItem taskItem = linkedEntityRecognitionTasksItems.get(i);
                final RecognizeLinkedEntitiesActionResult actionResult = new RecognizeLinkedEntitiesActionResult();
                final EntityLinkingResult results = taskItem.getResults();
                if (results != null) {
                    RecognizeLinkedEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                        toRecognizeLinkedEntitiesResultCollection(results));
                }
                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                    taskItem.getLastUpdateDateTime());
                recognizeLinkedEntitiesActionResults.add(actionResult);
            }
        }

        if (!CoreUtils.isNullOrEmpty(sentimentAnalysisTasksItems)) {
            for (int i = 0; i < sentimentAnalysisTasksItems.size(); i++) {
                final TasksStateTasksSentimentAnalysisTasksItem taskItem = sentimentAnalysisTasksItems.get(i);
                final AnalyzeSentimentActionResult actionResult = new AnalyzeSentimentActionResult();
                final SentimentResponse results = taskItem.getResults();
                if (results != null) {
                    AnalyzeSentimentActionResultPropertiesHelper.setDocumentsResults(actionResult,
                        toAnalyzeSentimentResultCollection(results));
                }
                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                    taskItem.getLastUpdateDateTime());
                analyzeSentimentActionResults.add(actionResult);
            }
        }

        if (!CoreUtils.isNullOrEmpty(extractiveSummarizationTasksItems)) {
            for (int i = 0; i < extractiveSummarizationTasksItems.size(); i++) {
                final TasksStateTasksExtractiveSummarizationTasksItem taskItem =
                    extractiveSummarizationTasksItems.get(i);
                final ExtractSummaryActionResult actionResult = new ExtractSummaryActionResult();
                final ExtractiveSummarizationResult results = taskItem.getResults();
                if (results != null) {
                    ExtractSummaryActionResultPropertiesHelper.setDocumentsResults(actionResult,
                        toExtractSummaryResultCollection(results));
                }
                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                    taskItem.getLastUpdateDateTime());
                extractSummaryActionResults.add(actionResult);
            }
        }

        if (!CoreUtils.isNullOrEmpty(customEntityRecognitionTasksItems)) {
            for (int i = 0; i < customEntityRecognitionTasksItems.size(); i++) {
                final TasksStateTasksCustomEntityRecognitionTasksItem taskItem =
                    customEntityRecognitionTasksItems.get(i);
                final RecognizeCustomEntitiesActionResult actionResult = new RecognizeCustomEntitiesActionResult();
                final CustomEntitiesResult results = taskItem.getResults();
                if (results != null) {
                    RecognizeCustomEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                        toRecognizeCustomEntitiesResultCollection(results));
                }
                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                    taskItem.getLastUpdateDateTime());
                recognizeCustomEntitiesActionResults.add(actionResult);
            }
        }

        if (!CoreUtils.isNullOrEmpty(customSingleClassificationTasksItems)) {
            for (int i = 0; i < customSingleClassificationTasksItems.size(); i++) {
                final TasksStateTasksCustomSingleClassificationTasksItem taskItem =
                    customSingleClassificationTasksItems.get(i);
                final ClassifyCustomSingleCategoryActionResult actionResult =
                    new ClassifyCustomSingleCategoryActionResult();
                final CustomSingleClassificationResult results = taskItem.getResults();
                if (results != null) {
                    ClassifyCustomCategoryActionResultPropertiesHelper.setDocumentsResults(actionResult,
                        toClassifySingleCategoryResultCollection(results));
                }
                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                    taskItem.getLastUpdateDateTime());
                classifyCustomSingleCategoryActionResults.add(actionResult);
            }
        }

        if (!CoreUtils.isNullOrEmpty(customMultiClassificationTasksItems)) {
            for (int i = 0; i < customMultiClassificationTasksItems.size(); i++) {
                final TasksStateTasksCustomMultiClassificationTasksItem taskItem =
                    customMultiClassificationTasksItems.get(i);
                final ClassifyCustomMultiCategoriesActionResult actionResult =
                    new ClassifyCustomMultiCategoriesActionResult();
                final CustomMultiClassificationResult results = taskItem.getResults();
                if (results != null) {
                    ClassifyCustomCategoriesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                        toClassifyMultiCategoriesResultCollection(results));
                }
                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                    taskItem.getLastUpdateDateTime());
                classifyCustomMultiCategoriesActionResults.add(actionResult);
            }
        }

        final List<TextAnalyticsError> errors = analyzeJobState.getErrors();
        if (!CoreUtils.isNullOrEmpty(errors)) {
            for (TextAnalyticsError error : errors) {
                final String[] targetPair = parseActionErrorTarget(error.getTarget());
                final String taskName = targetPair[0];
                final Integer taskIndex = Integer.valueOf(targetPair[1]);
                final TextAnalyticsActionResult actionResult;
                if (ENTITY_RECOGNITION_TASKS.equals(taskName)) {
                    actionResult = recognizeEntitiesActionResults.get(taskIndex);
                } else if (ENTITY_RECOGNITION_PII_TASKS.equals(taskName)) {
                    actionResult = recognizePiiEntitiesActionResults.get(taskIndex);
                } else if (KEY_PHRASE_EXTRACTION_TASKS.equals(taskName)) {
                    actionResult = extractKeyPhrasesActionResults.get(taskIndex);
                } else if (ENTITY_LINKING_TASKS.equals(taskName)) {
                    actionResult = recognizeLinkedEntitiesActionResults.get(taskIndex);
                } else if (SENTIMENT_ANALYSIS_TASKS.equals(taskName)) {
                    actionResult = analyzeSentimentActionResults.get(taskIndex);
                } else if (EXTRACTIVE_SUMMARIZATION_TASKS.equals(taskName)) {
                    actionResult = extractSummaryActionResults.get(taskIndex);
                } else if (CUSTOM_ENTITY_RECOGNITION_TASKS.equals(taskName)) {
                    actionResult = recognizeCustomEntitiesActionResults.get(taskIndex);
                } else if (CUSTOM_SINGLE_CLASSIFICATION_TASKS.equals(taskName)) {
                    actionResult = classifyCustomSingleCategoryActionResults.get(taskIndex);
                } else if (CUSTOM_MULTI_CLASSIFICATION_TASKS.equals(taskName)) {
                    actionResult = classifyCustomMultiCategoriesActionResults.get(taskIndex);
                } else {
                    throw logger.logExceptionAsError(new RuntimeException(
                        "Invalid task name in target reference, " + taskName));
                }

                TextAnalyticsActionResultPropertiesHelper.setIsError(actionResult, true);
                TextAnalyticsActionResultPropertiesHelper.setError(actionResult,
                    new com.azure.ai.textanalytics.models.TextAnalyticsError(
                        TextAnalyticsErrorCode.fromString(
                            error.getCode() == null ? null : error.getCode().toString()),
                        error.getMessage(), null));
            }
        }

        final AnalyzeActionsResult analyzeActionsResult = new AnalyzeActionsResult();
        AnalyzeActionsResultPropertiesHelper.setRecognizeEntitiesResults(analyzeActionsResult,
            IterableStream.of(recognizeEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setRecognizePiiEntitiesResults(analyzeActionsResult,
            IterableStream.of(recognizePiiEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setExtractKeyPhrasesResults(analyzeActionsResult,
            IterableStream.of(extractKeyPhrasesActionResults));
        AnalyzeActionsResultPropertiesHelper.setRecognizeLinkedEntitiesResults(analyzeActionsResult,
            IterableStream.of(recognizeLinkedEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setAnalyzeSentimentResults(analyzeActionsResult,
            IterableStream.of(analyzeSentimentActionResults));
        AnalyzeActionsResultPropertiesHelper.setExtractSummaryResults(analyzeActionsResult,
            IterableStream.of(extractSummaryActionResults));
        AnalyzeActionsResultPropertiesHelper.setRecognizeCustomEntitiesResults(analyzeActionsResult,
            IterableStream.of(recognizeCustomEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setClassifySingleCategoryResults(analyzeActionsResult,
            IterableStream.of(classifyCustomSingleCategoryActionResults));
        AnalyzeActionsResultPropertiesHelper.setClassifyMultiCategoriesResults(analyzeActionsResult,
            IterableStream.of(classifyCustomMultiCategoriesActionResults));
        return analyzeActionsResult;
    }

    private Mono<PollResponse<AnalyzeActionsOperationDetail>> processAnalyzedModelResponse(
        Response<AnalyzeJobState> analyzeJobStateResponse,
        PollResponse<AnalyzeActionsOperationDetail> operationResultPollResponse) {

        LongRunningOperationStatus status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        if (analyzeJobStateResponse.getValue() != null && analyzeJobStateResponse.getValue().getStatus() != null) {
            switch (analyzeJobStateResponse.getValue().getStatus()) {
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
                        analyzeJobStateResponse.getValue().getStatus().toString(), true);
                    break;
            }
        }
        AnalyzeActionsOperationDetailPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getDisplayName());
        AnalyzeActionsOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getCreatedDateTime());
        AnalyzeActionsOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getExpirationDateTime());
        AnalyzeActionsOperationDetailPropertiesHelper.setLastModifiedAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getLastUpdateDateTime());
        final TasksStateTasks tasksResult = analyzeJobStateResponse.getValue().getTasks();
        AnalyzeActionsOperationDetailPropertiesHelper.setActionsFailed(operationResultPollResponse.getValue(),
            tasksResult.getFailed());
        AnalyzeActionsOperationDetailPropertiesHelper.setActionsInProgress(operationResultPollResponse.getValue(),
            tasksResult.getInProgress());
        AnalyzeActionsOperationDetailPropertiesHelper.setActionsSucceeded(
            operationResultPollResponse.getValue(), tasksResult.getCompleted());
        AnalyzeActionsOperationDetailPropertiesHelper.setActionsInTotal(operationResultPollResponse.getValue(),
            tasksResult.getTotal());
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }

    private Context getNotNullContext(Context context) {
        return context == null ? Context.NONE : context;
    }

    private AnalyzeActionsOptions getNotNullAnalyzeActionsOptions(AnalyzeActionsOptions options) {
        return options == null ? new AnalyzeActionsOptions() : options;
    }

    private String[] parseActionErrorTarget(String targetReference) {
        if (CoreUtils.isNullOrEmpty(targetReference)) {
            throw logger.logExceptionAsError(new RuntimeException(
                "Expected an error with a target field referencing an action but did not get one"));
        }
        // action could be failed and the target reference is "#/tasks/keyPhraseExtractionTasks/0";
        final Matcher matcher = PATTERN.matcher(targetReference);
        String[] taskNameIdPair = new String[2];
        while (matcher.find()) {
            taskNameIdPair[0] = matcher.group(1);
            taskNameIdPair[1] = matcher.group(2);
        }
        return taskNameIdPair;
    }
}
