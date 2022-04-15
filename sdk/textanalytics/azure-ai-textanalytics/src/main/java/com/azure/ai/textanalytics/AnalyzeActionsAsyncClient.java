// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeActionsOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeTextsImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeBatchInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeJobState;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesTask;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.CustomMultiClassificationTask;
import com.azure.ai.textanalytics.implementation.models.CustomMultiClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.CustomSingleClassificationTask;
import com.azure.ai.textanalytics.implementation.models.CustomSingleClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.CustomTaskParameters;
import com.azure.ai.textanalytics.implementation.models.EntitiesTask;
import com.azure.ai.textanalytics.implementation.models.EntitiesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingTask;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingTaskParameters;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationSortingCriteria;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationTask;
import com.azure.ai.textanalytics.implementation.models.ExtractiveSummarizationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.JobManifestTasks;
import com.azure.ai.textanalytics.implementation.models.KeyPhrasesTask;
import com.azure.ai.textanalytics.implementation.models.KeyPhrasesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.PiiDomain;
import com.azure.ai.textanalytics.implementation.models.PiiTask;
import com.azure.ai.textanalytics.implementation.models.PiiTaskParameters;
import com.azure.ai.textanalytics.implementation.models.PreBuiltTaskParameters;
import com.azure.ai.textanalytics.implementation.models.SentimentAnalysisTask;
import com.azure.ai.textanalytics.implementation.models.SentimentAnalysisTaskParameters;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.implementation.models.TaskParameters;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasks;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentAction;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesAction;
import com.azure.ai.textanalytics.models.ExtractSummaryAction;
import com.azure.ai.textanalytics.models.MultiCategoryClassifyAction;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesAction;
import com.azure.ai.textanalytics.models.SingleCategoryClassifyAction;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedFlux;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.parseOperationId;
import static com.azure.ai.textanalytics.implementation.Utility.toCategoriesFilter;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
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
    private TextAnalyticsClientImpl service;
    private AnalyzeTextsImpl languageAsyncApiService;
    private static final Pattern PATTERN;
    static {
        PATTERN = Pattern.compile(REGEX_ACTION_ERROR_TARGET, Pattern.MULTILINE);
    }

    AnalyzeActionsAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    AnalyzeActionsAsyncClient(AnalyzeTextsImpl service) {
        this.languageAsyncApiService = service;
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
        if (actions == null) {
            return null;
        }

        final JobManifestTasks jobManifestTasks = new JobManifestTasks();
        if (actions.getRecognizeEntitiesActions() != null) {
            jobManifestTasks.setEntityRecognitionTasks(toEntitiesTask(actions));
        }

        if (actions.getRecognizePiiEntitiesActions() != null) {
            jobManifestTasks.setEntityRecognitionPiiTasks(toPiiTask(actions));
        }

        if (actions.getExtractKeyPhrasesActions() != null) {
            jobManifestTasks.setKeyPhraseExtractionTasks(toKeyPhrasesTask(actions));
        }

        if (actions.getRecognizeLinkedEntitiesActions() != null) {
            jobManifestTasks.setEntityLinkingTasks(toEntityLinkingTask(actions));
        }

        if (actions.getAnalyzeSentimentActions() != null) {
            jobManifestTasks.setSentimentAnalysisTasks(toSentimentAnalysisTask(actions));
        }

        if (actions.getExtractSummaryActions() != null) {
            jobManifestTasks.setExtractiveSummarizationTasks(toExtractiveSummarizationTask(actions));
        }

        if (actions.getRecognizeCustomEntitiesActions() != null) {
            jobManifestTasks.setCustomEntityRecognitionTasks(toCustomEntitiesTask(actions));
        }

        if (actions.getSingleCategoryClassifyActions() != null) {
            jobManifestTasks.setCustomSingleClassificationTasks(toCustomSingleClassificationTask(actions));
        }

        if (actions.getMultiCategoryClassifyActions() != null) {
            jobManifestTasks.setCustomMultiClassificationTasks(toCustomMultiClassificationTask(actions));
        }

        return jobManifestTasks;
    }

    private List<EntitiesTask> toEntitiesTask(TextAnalyticsActions actions) {
        final List<EntitiesTask> entitiesTasks = new ArrayList<>();
        for (RecognizeEntitiesAction action : actions.getRecognizeEntitiesActions()) {
            if (action == null) {
                entitiesTasks.add(null);
            } else {
                final PreBuiltTaskParameters preBuiltTaskParameters =
                    (PreBuiltTaskParameters) new TaskParameters().setLoggingOptOut(action.isServiceLogsDisabled());
                final EntitiesTaskParameters taskParameters =
                    (EntitiesTaskParameters) preBuiltTaskParameters.setModelVersion(action.getModelVersion());
                entitiesTasks.add(
                    new EntitiesTask()
                        .setTaskName(action.getActionName())
                        .setParameters(taskParameters.setStringIndexType(StringIndexType.UTF16CODE_UNIT)));
            }
        }
        return entitiesTasks;
    }

    private List<PiiTask> toPiiTask(TextAnalyticsActions actions) {
        final List<PiiTask> piiTasks = new ArrayList<>();
        for (RecognizePiiEntitiesAction action : actions.getRecognizePiiEntitiesActions()) {
            if (action == null) {
                piiTasks.add(null);
            } else {
                final PreBuiltTaskParameters preBuiltTaskParameters =
                    (PreBuiltTaskParameters) new TaskParameters().setLoggingOptOut(action.isServiceLogsDisabled());
                final PiiTaskParameters taskParameters =
                    (PiiTaskParameters) preBuiltTaskParameters.setModelVersion(action.getModelVersion());
                piiTasks.add(
                    new PiiTask()
                        .setTaskName(action.getActionName())
                        .setParameters(
                            taskParameters
                                .setDomain(PiiDomain.fromString(
                                    action.getDomainFilter() == null ? null
                                        : action.getDomainFilter().toString()))
                                .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                .setPiiCategories(toCategoriesFilter(action.getCategoriesFilter()))));
            }
        }
        return piiTasks;
    }

    private List<KeyPhrasesTask> toKeyPhrasesTask(TextAnalyticsActions actions) {
        final List<KeyPhrasesTask> keyPhrasesTasks = new ArrayList<>();
        for (ExtractKeyPhrasesAction action : actions.getExtractKeyPhrasesActions()) {
            if (action == null) {
                keyPhrasesTasks.add(null);
            } else {
                keyPhrasesTasks.add(
                    new KeyPhrasesTask()
                        .setTaskName(action.getActionName())
                        .setParameters(
                            new KeyPhrasesTaskParameters()
                                .setModelVersion(action.getModelVersion())
                                .setLoggingOptOut(action.isServiceLogsDisabled())));
            }
        }
        return keyPhrasesTasks;
    }

    private List<EntityLinkingTask> toEntityLinkingTask(TextAnalyticsActions actions) {
        final List<EntityLinkingTask> entityLinkingTasks = new ArrayList<>();
        for (RecognizeLinkedEntitiesAction action : actions.getRecognizeLinkedEntitiesActions()) {
            if (action == null) {
                entityLinkingTasks.add(null);
            } else {
                final PreBuiltTaskParameters preBuiltTaskParameters =
                    (PreBuiltTaskParameters) new TaskParameters().setLoggingOptOut(action.isServiceLogsDisabled());
                final EntityLinkingTaskParameters taskParameters =
                    (EntityLinkingTaskParameters) preBuiltTaskParameters.setModelVersion(action.getModelVersion());
                entityLinkingTasks.add(
                    new EntityLinkingTask()
                        .setTaskName(action.getActionName())
                        .setParameters(taskParameters.setStringIndexType(StringIndexType.UTF16CODE_UNIT)));
            }
        }
        return entityLinkingTasks;
    }

    private List<SentimentAnalysisTask> toSentimentAnalysisTask(TextAnalyticsActions actions) {
        final List<SentimentAnalysisTask> sentimentAnalysisTasks = new ArrayList<>();
        for (AnalyzeSentimentAction action : actions.getAnalyzeSentimentActions()) {
            if (action == null) {
                sentimentAnalysisTasks.add(null);
            } else {
                final PreBuiltTaskParameters preBuiltTaskParameters =
                    (PreBuiltTaskParameters) new TaskParameters().setLoggingOptOut(action.isServiceLogsDisabled());
                final SentimentAnalysisTaskParameters taskParameters =
                    (SentimentAnalysisTaskParameters) preBuiltTaskParameters.setModelVersion(action.getModelVersion());
                sentimentAnalysisTasks.add(
                    new SentimentAnalysisTask()
                        .setTaskName(action.getActionName())
                        .setParameters(taskParameters.setStringIndexType(StringIndexType.UTF16CODE_UNIT)));
            }
        }
        return sentimentAnalysisTasks;
    }

    private List<ExtractiveSummarizationTask> toExtractiveSummarizationTask(TextAnalyticsActions actions) {
        final List<ExtractiveSummarizationTask> extractiveSummarizationTasks = new ArrayList<>();
        for (ExtractSummaryAction action : actions.getExtractSummaryActions()) {
            if (action == null) {
                extractiveSummarizationTasks.add(null);
            } else {
                // TODO: it might break older API version but might not because the json structure should be the same.
                final PreBuiltTaskParameters preBuiltTaskParameters =
                    (PreBuiltTaskParameters) new TaskParameters().setLoggingOptOut(action.isServiceLogsDisabled());
                final ExtractiveSummarizationTaskParameters taskParameters =
                    (ExtractiveSummarizationTaskParameters) preBuiltTaskParameters.setModelVersion(
                        action.getModelVersion());
                extractiveSummarizationTasks.add(
                    new ExtractiveSummarizationTask()
                        .setTaskName(action.getActionName())
                        .setParameters(
                            taskParameters
                                .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                .setSentenceCount(action.getMaxSentenceCount())
                                .setSortBy(action.getOrderBy() == null ? null
                                               : ExtractiveSummarizationSortingCriteria.fromString(
                                    action.getOrderBy().toString()))));
            }
        }
        return extractiveSummarizationTasks;
    }

    private List<CustomEntitiesTask> toCustomEntitiesTask(TextAnalyticsActions actions) {
        final List<CustomEntitiesTask> customEntitiesTasks = new ArrayList<>();
        for (RecognizeCustomEntitiesAction action : actions.getRecognizeCustomEntitiesActions()) {
            if (action == null) {
                customEntitiesTasks.add(null);
            } else {
                final CustomTaskParameters customTaskParameters =
                    (CustomTaskParameters) new TaskParameters().setLoggingOptOut(action.isServiceLogsDisabled());
                final CustomEntitiesTaskParameters taskParameters =
                    (CustomEntitiesTaskParameters) customTaskParameters
                                                       .setProjectName(action.getProjectName())
                                                       .setDeploymentName(action.getDeploymentName());
                customEntitiesTasks.add(
                    new CustomEntitiesTask()
                        .setTaskName(action.getActionName())
                        .setParameters(taskParameters.setStringIndexType(StringIndexType.UTF16CODE_UNIT)));
            }
        }
        return customEntitiesTasks;
    }

    private List<CustomSingleClassificationTask> toCustomSingleClassificationTask(TextAnalyticsActions actions) {
        final List<CustomSingleClassificationTask> customSingleClassificationTask = new ArrayList<>();
        for (SingleCategoryClassifyAction action : actions.getSingleCategoryClassifyActions()) {
            if (action == null) {
                customSingleClassificationTask.add(null);
            } else {
                customSingleClassificationTask.add(
                    new CustomSingleClassificationTask()
                        .setTaskName(action.getActionName())
                        .setParameters(
                            new CustomSingleClassificationTaskParameters()
                                .setProjectName(action.getProjectName())
                                .setDeploymentName(action.getDeploymentName())
                                .setLoggingOptOut(action.isServiceLogsDisabled())));
            }
        }
        return customSingleClassificationTask;
    }

    private List<CustomMultiClassificationTask> toCustomMultiClassificationTask(TextAnalyticsActions actions) {
        final List<CustomMultiClassificationTask> customMultiClassificationTask = new ArrayList<>();
        for (MultiCategoryClassifyAction action : actions.getMultiCategoryClassifyActions()) {
            if (action == null) {
                customMultiClassificationTask.add(null);
            } else {
                customMultiClassificationTask.add(
                    new CustomMultiClassificationTask()
                        .setTaskName(action.getActionName())
                        .setParameters(
                            new CustomMultiClassificationTaskParameters()
                                .setProjectName(action.getProjectName())
                                .setDeploymentName(action.getDeploymentName())
                                .setLoggingOptOut(action.isServiceLogsDisabled())));
            }
        }
        return customMultiClassificationTask;
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
            null,
//            Arrays.asList(toAnalyzeActionsResult(analyzeJobState)),
            analyzeJobState.getNextLink(),
            null);
    }

//    private AnalyzeActionsResult toAnalyzeActionsResult(AnalyzeJobState analyzeJobState) {
//        TasksStateTasks tasksStateTasks = analyzeJobState.getTasks();
//
//        for (AnalyzeTextLROResult result : tasksStateTasks.getItems()) {
//            result.
//        }
//        final List<TasksStateTasksEntityRecognitionPiiTasksItem> piiTasksItems =
//            tasksStateTasks.getEntityRecognitionPiiTasks();
//        final List<TasksStateTasksEntityRecognitionTasksItem> entityRecognitionTasksItems =
//            tasksStateTasks.getEntityRecognitionTasks();
//        final List<TasksStateTasksKeyPhraseExtractionTasksItem> keyPhraseExtractionTasks =
//            tasksStateTasks.getKeyPhraseExtractionTasks();
//        final List<TasksStateTasksEntityLinkingTasksItem> linkedEntityRecognitionTasksItems =
//            tasksStateTasks.getEntityLinkingTasks();
//        final List<TasksStateTasksSentimentAnalysisTasksItem> sentimentAnalysisTasksItems =
//            tasksStateTasks.getSentimentAnalysisTasks();
//        final List<TasksStateTasksExtractiveSummarizationTasksItem> extractiveSummarizationTasksItems =
//            tasksStateTasks.getExtractiveSummarizationTasks();
//        final List<TasksStateTasksCustomEntityRecognitionTasksItem> customEntityRecognitionTasksItems =
//            tasksStateTasks.getCustomEntityRecognitionTasks();
//        final List<TasksStateTasksCustomSingleClassificationTasksItem> customSingleClassificationTasksItems =
//            tasksStateTasks.getCustomSingleClassificationTasks();
//        final List<TasksStateTasksCustomMultiClassificationTasksItem> customMultiClassificationTasksItems =
//            tasksStateTasks.getCustomMultiClassificationTasks();
//
//        List<RecognizeEntitiesActionResult> recognizeEntitiesActionResults = new ArrayList<>();
//        List<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults = new ArrayList<>();
//        List<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults = new ArrayList<>();
//        List<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults = new ArrayList<>();
//        List<AnalyzeSentimentActionResult> analyzeSentimentActionResults = new ArrayList<>();
//        List<ExtractSummaryActionResult> extractSummaryActionResults = new ArrayList<>();
//        List<RecognizeCustomEntitiesActionResult> recognizeCustomEntitiesActionResults = new ArrayList<>();
//        List<SingleCategoryClassifyActionResult> singleCategoryClassifyActionResults =
//            new ArrayList<>();
//        List<MultiCategoryClassifyActionResult> multiCategoryClassifyActionResults =
//            new ArrayList<>();
//
//        if (!CoreUtils.isNullOrEmpty(entityRecognitionTasksItems)) {
//            for (int i = 0; i < entityRecognitionTasksItems.size(); i++) {
//                final TasksStateTasksEntityRecognitionTasksItem taskItem = entityRecognitionTasksItems.get(i);
//                final RecognizeEntitiesActionResult actionResult = new RecognizeEntitiesActionResult();
//                final EntitiesResult results = taskItem.getResults();
//                if (results != null) {
//                    RecognizeEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
//                        toRecognizeEntitiesResultCollectionResponse(results));
//                }
//                TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult, taskItem.getTaskName());
//                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
//                    taskItem.getLastUpdateDateTime());
//                recognizeEntitiesActionResults.add(actionResult);
//            }
//        }
//
//        if (!CoreUtils.isNullOrEmpty(piiTasksItems)) {
//            for (int i = 0; i < piiTasksItems.size(); i++) {
//                final TasksStateTasksEntityRecognitionPiiTasksItem taskItem = piiTasksItems.get(i);
//                final RecognizePiiEntitiesActionResult actionResult = new RecognizePiiEntitiesActionResult();
//                final PiiResult results = taskItem.getResults();
//                if (results != null) {
//                    RecognizePiiEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
//                        toRecognizePiiEntitiesResultCollection(results));
//                }
//                TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult, taskItem.getTaskName());
//                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
//                    taskItem.getLastUpdateDateTime());
//                recognizePiiEntitiesActionResults.add(actionResult);
//            }
//        }
//
//        if (!CoreUtils.isNullOrEmpty(keyPhraseExtractionTasks)) {
//            for (int i = 0; i < keyPhraseExtractionTasks.size(); i++) {
//                final TasksStateTasksKeyPhraseExtractionTasksItem taskItem = keyPhraseExtractionTasks.get(i);
//                final ExtractKeyPhrasesActionResult actionResult = new ExtractKeyPhrasesActionResult();
//                final KeyPhraseResult results = taskItem.getResults();
//                if (results != null) {
//                    ExtractKeyPhrasesActionResultPropertiesHelper.setDocumentsResults(actionResult,
//                        toExtractKeyPhrasesResultCollection(results));
//                }
//                TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult, taskItem.getTaskName());
//                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
//                    taskItem.getLastUpdateDateTime());
//                extractKeyPhrasesActionResults.add(actionResult);
//            }
//        }
//
//        if (!CoreUtils.isNullOrEmpty(linkedEntityRecognitionTasksItems)) {
//            for (int i = 0; i < linkedEntityRecognitionTasksItems.size(); i++) {
//                final TasksStateTasksEntityLinkingTasksItem taskItem = linkedEntityRecognitionTasksItems.get(i);
//                final RecognizeLinkedEntitiesActionResult actionResult = new RecognizeLinkedEntitiesActionResult();
//                final EntityLinkingResult results = taskItem.getResults();
//                if (results != null) {
//                    RecognizeLinkedEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
//                        toRecognizeLinkedEntitiesResultCollection(results));
//                }
//                TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult, taskItem.getTaskName());
//                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
//                    taskItem.getLastUpdateDateTime());
//                recognizeLinkedEntitiesActionResults.add(actionResult);
//            }
//        }
//
//        if (!CoreUtils.isNullOrEmpty(sentimentAnalysisTasksItems)) {
//            for (int i = 0; i < sentimentAnalysisTasksItems.size(); i++) {
//                final TasksStateTasksSentimentAnalysisTasksItem taskItem = sentimentAnalysisTasksItems.get(i);
//                final AnalyzeSentimentActionResult actionResult = new AnalyzeSentimentActionResult();
//                final SentimentResponse results = taskItem.getResults();
//                if (results != null) {
//                    AnalyzeSentimentActionResultPropertiesHelper.setDocumentsResults(actionResult,
//                        toAnalyzeSentimentResultCollection(results));
//                }
//                TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult, taskItem.getTaskName());
//                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
//                    taskItem.getLastUpdateDateTime());
//                analyzeSentimentActionResults.add(actionResult);
//            }
//        }
//
//        if (!CoreUtils.isNullOrEmpty(extractiveSummarizationTasksItems)) {
//            for (int i = 0; i < extractiveSummarizationTasksItems.size(); i++) {
//                final TasksStateTasksExtractiveSummarizationTasksItem taskItem =
//                    extractiveSummarizationTasksItems.get(i);
//                final ExtractSummaryActionResult actionResult = new ExtractSummaryActionResult();
//                final ExtractiveSummarizationResult results = taskItem.getResults();
//                if (results != null) {
//                    ExtractSummaryActionResultPropertiesHelper.setDocumentsResults(actionResult,
//                        toExtractSummaryResultCollection(results));
//                }
//                TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult, taskItem.getTaskName());
//                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
//                    taskItem.getLastUpdateDateTime());
//                extractSummaryActionResults.add(actionResult);
//            }
//        }
//
//        if (!CoreUtils.isNullOrEmpty(customEntityRecognitionTasksItems)) {
//            for (int i = 0; i < customEntityRecognitionTasksItems.size(); i++) {
//                final TasksStateTasksCustomEntityRecognitionTasksItem taskItem =
//                    customEntityRecognitionTasksItems.get(i);
//                final RecognizeCustomEntitiesActionResult actionResult = new RecognizeCustomEntitiesActionResult();
//                final CustomEntitiesResult results = taskItem.getResults();
//                if (results != null) {
//                    RecognizeCustomEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
//                        toRecognizeCustomEntitiesResultCollection(results));
//                }
//                TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult, taskItem.getTaskName());
//                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
//                    taskItem.getLastUpdateDateTime());
//                recognizeCustomEntitiesActionResults.add(actionResult);
//            }
//        }
//
//        if (!CoreUtils.isNullOrEmpty(customSingleClassificationTasksItems)) {
//            for (int i = 0; i < customSingleClassificationTasksItems.size(); i++) {
//                final TasksStateTasksCustomSingleClassificationTasksItem taskItem =
//                    customSingleClassificationTasksItems.get(i);
//                final SingleCategoryClassifyActionResult actionResult =
//                    new SingleCategoryClassifyActionResult();
//                final CustomSingleClassificationResult results = taskItem.getResults();
//                if (results != null) {
//                    SingleCategoryClassifyActionResultPropertiesHelper.setDocumentsResults(actionResult,
//                        toSingleCategoryClassifyResultCollection(results));
//                }
//                TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult, taskItem.getTaskName());
//                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
//                    taskItem.getLastUpdateDateTime());
//                singleCategoryClassifyActionResults.add(actionResult);
//            }
//        }
//
//        if (!CoreUtils.isNullOrEmpty(customMultiClassificationTasksItems)) {
//            for (int i = 0; i < customMultiClassificationTasksItems.size(); i++) {
//                final TasksStateTasksCustomMultiClassificationTasksItem taskItem =
//                    customMultiClassificationTasksItems.get(i);
//                final MultiCategoryClassifyActionResult actionResult =
//                    new MultiCategoryClassifyActionResult();
//                final CustomMultiClassificationResult results = taskItem.getResults();
//                if (results != null) {
//                    MultiCategoryClassifyActionResultPropertiesHelper.setDocumentsResults(actionResult,
//                        toMultiCategoryClassifyResultCollection(results));
//                }
//                TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult, taskItem.getTaskName());
//                TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
//                    taskItem.getLastUpdateDateTime());
//                multiCategoryClassifyActionResults.add(actionResult);
//            }
//        }
//
//        final List<TextAnalyticsError> errors = analyzeJobState.getErrors();
//        if (!CoreUtils.isNullOrEmpty(errors)) {
//            for (TextAnalyticsError error : errors) {
//                final String[] targetPair = parseActionErrorTarget(error.getTarget());
//                final String taskName = targetPair[0];
//                final Integer taskIndex = Integer.valueOf(targetPair[1]);
//                final TextAnalyticsActionResult actionResult;
//                if (ENTITY_RECOGNITION_TASKS.equals(taskName)) {
//                    actionResult = recognizeEntitiesActionResults.get(taskIndex);
//                } else if (ENTITY_RECOGNITION_PII_TASKS.equals(taskName)) {
//                    actionResult = recognizePiiEntitiesActionResults.get(taskIndex);
//                } else if (KEY_PHRASE_EXTRACTION_TASKS.equals(taskName)) {
//                    actionResult = extractKeyPhrasesActionResults.get(taskIndex);
//                } else if (ENTITY_LINKING_TASKS.equals(taskName)) {
//                    actionResult = recognizeLinkedEntitiesActionResults.get(taskIndex);
//                } else if (SENTIMENT_ANALYSIS_TASKS.equals(taskName)) {
//                    actionResult = analyzeSentimentActionResults.get(taskIndex);
//                } else if (EXTRACTIVE_SUMMARIZATION_TASKS.equals(taskName)) {
//                    actionResult = extractSummaryActionResults.get(taskIndex);
//                } else if (CUSTOM_ENTITY_RECOGNITION_TASKS.equals(taskName)) {
//                    actionResult = recognizeCustomEntitiesActionResults.get(taskIndex);
//                } else if (CUSTOM_SINGLE_CLASSIFICATION_TASKS.equals(taskName)) {
//                    actionResult = singleCategoryClassifyActionResults.get(taskIndex);
//                } else if (CUSTOM_MULTI_CLASSIFICATION_TASKS.equals(taskName)) {
//                    actionResult = multiCategoryClassifyActionResults.get(taskIndex);
//                } else {
//                    throw logger.logExceptionAsError(new RuntimeException(
//                        "Invalid task name in target reference, " + taskName));
//                }
//
//                TextAnalyticsActionResultPropertiesHelper.setIsError(actionResult, true);
//                TextAnalyticsActionResultPropertiesHelper.setError(actionResult,
//                    new com.azure.ai.textanalytics.models.TextAnalyticsError(
//                        TextAnalyticsErrorCode.fromString(
//                            error.getCode() == null ? null : error.getCode().toString()),
//                        error.getMessage(), null));
//            }
//        }
//
//        final AnalyzeActionsResult analyzeActionsResult = new AnalyzeActionsResult();
//        AnalyzeActionsResultPropertiesHelper.setRecognizeEntitiesResults(analyzeActionsResult,
//            IterableStream.of(recognizeEntitiesActionResults));
//        AnalyzeActionsResultPropertiesHelper.setRecognizePiiEntitiesResults(analyzeActionsResult,
//            IterableStream.of(recognizePiiEntitiesActionResults));
//        AnalyzeActionsResultPropertiesHelper.setExtractKeyPhrasesResults(analyzeActionsResult,
//            IterableStream.of(extractKeyPhrasesActionResults));
//        AnalyzeActionsResultPropertiesHelper.setRecognizeLinkedEntitiesResults(analyzeActionsResult,
//            IterableStream.of(recognizeLinkedEntitiesActionResults));
//        AnalyzeActionsResultPropertiesHelper.setAnalyzeSentimentResults(analyzeActionsResult,
//            IterableStream.of(analyzeSentimentActionResults));
//        AnalyzeActionsResultPropertiesHelper.setExtractSummaryResults(analyzeActionsResult,
//            IterableStream.of(extractSummaryActionResults));
//        AnalyzeActionsResultPropertiesHelper.setRecognizeCustomEntitiesResults(analyzeActionsResult,
//            IterableStream.of(recognizeCustomEntitiesActionResults));
//        AnalyzeActionsResultPropertiesHelper.setClassifySingleCategoryResults(analyzeActionsResult,
//            IterableStream.of(singleCategoryClassifyActionResults));
//        AnalyzeActionsResultPropertiesHelper.setClassifyMultiCategoryResults(analyzeActionsResult,
//            IterableStream.of(multiCategoryClassifyActionResults));
//        return analyzeActionsResult;
//    }

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
