// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeActionsOperationDetailPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeActionsResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeSentimentActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.AnalyzeTextsImpl;
import com.azure.ai.textanalytics.implementation.ExtractKeyPhrasesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.MultiLabelClassificationActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeCustomEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizeLinkedEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.RecognizePiiEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.SingleLabelClassificationActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsActionResultPropertiesHelper;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeBatchInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeJobState;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobState;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextJobsInput;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROResult;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextLROTask;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesLROTask;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesResult;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesTask;
import com.azure.ai.textanalytics.implementation.models.CustomEntitiesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.CustomEntityRecognitionLROResult;
import com.azure.ai.textanalytics.implementation.models.CustomLabelClassificationResult;
import com.azure.ai.textanalytics.implementation.models.CustomMultiClassificationTask;
import com.azure.ai.textanalytics.implementation.models.CustomMultiLabelClassificationLROResult;
import com.azure.ai.textanalytics.implementation.models.CustomMultiLabelClassificationLROTask;
import com.azure.ai.textanalytics.implementation.models.CustomMultiLabelClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.CustomSingleClassificationTask;
import com.azure.ai.textanalytics.implementation.models.CustomSingleLabelClassificationLROResult;
import com.azure.ai.textanalytics.implementation.models.CustomSingleLabelClassificationLROTask;
import com.azure.ai.textanalytics.implementation.models.CustomSingleLabelClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.EntitiesLROTask;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.EntitiesTask;
import com.azure.ai.textanalytics.implementation.models.EntitiesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingLROResult;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingLROTask;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingTask;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingTaskParameters;
import com.azure.ai.textanalytics.implementation.models.EntityRecognitionLROResult;
import com.azure.ai.textanalytics.implementation.models.Error;
import com.azure.ai.textanalytics.implementation.models.HealthcareLROResult;
import com.azure.ai.textanalytics.implementation.models.HealthcareLROTask;
import com.azure.ai.textanalytics.implementation.models.HealthcareResult;
import com.azure.ai.textanalytics.implementation.models.HealthcareTaskParameters;
import com.azure.ai.textanalytics.implementation.models.JobManifestTasks;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseExtractionLROResult;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseLROTask;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseTaskParameters;
import com.azure.ai.textanalytics.implementation.models.KeyPhrasesTask;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.PiiDomain;
import com.azure.ai.textanalytics.implementation.models.PiiEntityRecognitionLROResult;
import com.azure.ai.textanalytics.implementation.models.PiiLROTask;
import com.azure.ai.textanalytics.implementation.models.PiiResult;
import com.azure.ai.textanalytics.implementation.models.PiiTask;
import com.azure.ai.textanalytics.implementation.models.PiiTaskParameters;
import com.azure.ai.textanalytics.implementation.models.SentimentAnalysisLROTask;
import com.azure.ai.textanalytics.implementation.models.SentimentAnalysisTask;
import com.azure.ai.textanalytics.implementation.models.SentimentAnalysisTaskParameters;
import com.azure.ai.textanalytics.implementation.models.SentimentLROResult;
import com.azure.ai.textanalytics.implementation.models.SentimentResponse;
import com.azure.ai.textanalytics.implementation.models.State;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasks;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityLinkingTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityRecognitionPiiTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksEntityRecognitionTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksKeyPhraseExtractionTasksItem;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksOld;
import com.azure.ai.textanalytics.implementation.models.TasksStateTasksSentimentAnalysisTasksItem;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesAction;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesActionResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentAction;
import com.azure.ai.textanalytics.models.AnalyzeSentimentActionResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesAction;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.MultiLabelClassificationAction;
import com.azure.ai.textanalytics.models.MultiLabelClassificationActionResult;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.SingleLabelClassificationAction;
import com.azure.ai.textanalytics.models.SingleLabelClassificationActionResult;
import com.azure.ai.textanalytics.models.TextAnalyticsActionResult;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
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
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.parseNextLink;
import static com.azure.ai.textanalytics.implementation.Utility.parseOperationId;
import static com.azure.ai.textanalytics.implementation.Utility.toAnalyzeHealthcareEntitiesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toAnalyzeSentimentResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toCategoriesFilter;
import static com.azure.ai.textanalytics.implementation.Utility.toExtractKeyPhrasesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeCustomEntitiesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeEntitiesResultCollectionResponse;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeLinkedEntitiesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizePiiEntitiesResultCollection;
import static com.azure.ai.textanalytics.implementation.Utility.toLabelClassificationResultCollection;
import static com.azure.ai.textanalytics.implementation.models.State.CANCELLED;
import static com.azure.ai.textanalytics.implementation.models.State.NOT_STARTED;
import static com.azure.ai.textanalytics.implementation.models.State.PARTIALLY_COMPLETED;
import static com.azure.ai.textanalytics.implementation.models.State.RUNNING;
import static com.azure.ai.textanalytics.implementation.models.State.SUCCEEDED;
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
    private final TextAnalyticsClientImpl legacyService;
    private final AnalyzeTextsImpl service;
    private static final Pattern PATTERN;
    static {
        PATTERN = Pattern.compile(REGEX_ACTION_ERROR_TARGET, Pattern.MULTILINE);
    }

    AnalyzeActionsAsyncClient(TextAnalyticsClientImpl legacyService) {
        this.legacyService = legacyService;
        this.service = null;
    }

    AnalyzeActionsAsyncClient(AnalyzeTextsImpl service) {
        this.legacyService = null;
        this.service = service;
    }

    PollerFlux<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> beginAnalyzeActions(
        Iterable<TextDocumentInput> documents, TextAnalyticsActions actions, AnalyzeActionsOptions options,
        Context context) {
        try {
            Objects.requireNonNull(actions, "'actions' cannot be null.");
            inputDocumentsValidation(documents);
            options = getNotNullAnalyzeActionsOptions(options);
            final Context finalContext = getNotNullContext(context)
                                             .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
            final boolean finalIncludeStatistics = options.isIncludeStatistics();

            if (service != null) {
                final AnalyzeTextJobsInput analyzeTextJobsInput =
                    new AnalyzeTextJobsInput()
                        .setDisplayName(actions.getDisplayName())
                        .setAnalysisInput(
                            new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents)))
                        .setTasks(getAnalyzeTextLROTasks(actions));
                return new PollerFlux<>(
                    DEFAULT_POLL_INTERVAL,
                    activationOperation(
                        service.submitJobWithResponseAsync(analyzeTextJobsInput, finalContext)
                            .map(analyzeResponse -> {
                                final AnalyzeActionsOperationDetail textAnalyticsOperationResult =
                                    new AnalyzeActionsOperationDetail();
                                AnalyzeActionsOperationDetailPropertiesHelper
                                    .setOperationId(textAnalyticsOperationResult,
                                        parseOperationId(
                                            analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                                return textAnalyticsOperationResult;
                            })),
                    pollingOperationLanguageApi(operationId -> service.jobStatusWithResponseAsync(operationId,
                        finalIncludeStatistics, null, null, finalContext)),
                    (pollingContext, pollResponse) -> Mono.just(pollingContext.getLatestResponse().getValue()),
                    fetchingOperation(
                        operationId -> Mono.just(getAnalyzeOperationFluxPage(
                            operationId, null, null, finalIncludeStatistics, finalContext)))
                );
            }

            final AnalyzeBatchInput analyzeBatchInput =
                new AnalyzeBatchInput()
                    .setAnalysisInput(new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)))
                    .setTasks(getJobManifestTasks(actions));
            analyzeBatchInput.setDisplayName(actions.getDisplayName());
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    legacyService.analyzeWithResponseAsync(analyzeBatchInput, finalContext)
                        .map(analyzeResponse -> {
                            final AnalyzeActionsOperationDetail textAnalyticsOperationResult =
                                new AnalyzeActionsOperationDetail();
                            AnalyzeActionsOperationDetailPropertiesHelper
                                .setOperationId(textAnalyticsOperationResult,
                                    parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return textAnalyticsOperationResult;
                        })),
                pollingOperation(operationId -> legacyService.analyzeStatusWithResponseAsync(operationId.toString(),
                    finalIncludeStatistics, null, null, finalContext)),
                (pollingContext, activationResponse) ->
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
            Objects.requireNonNull(actions, "'actions' cannot be null.");
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

            if (service != null) {
                return new PollerFlux<>(
                    DEFAULT_POLL_INTERVAL,
                    activationOperation(
                        service.submitJobWithResponseAsync(
                            new AnalyzeTextJobsInput()
                                .setDisplayName(actions.getDisplayName())
                                .setAnalysisInput(new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents)))
                                .setTasks(getAnalyzeTextLROTasks(actions)),
                            finalContext)
                            .map(analyzeResponse -> {
                                final AnalyzeActionsOperationDetail operationDetail =
                                    new AnalyzeActionsOperationDetail();
                                AnalyzeActionsOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                    parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                                return operationDetail;
                            })),
                    pollingOperationLanguageApi(operationId -> service.jobStatusWithResponseAsync(operationId,
                        finalIncludeStatistics, null, null, finalContext)),
                    (activationResponse, pollingContext) ->
                        Mono.error(new RuntimeException("Cancellation is not supported.")),
                    fetchingOperationIterable(
                        operationId -> Mono.just(new AnalyzeActionsResultPagedIterable(getAnalyzeOperationFluxPage(
                            operationId, null, null, finalIncludeStatistics, finalContext))))
                );
            }

            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(
                    legacyService.analyzeWithResponseAsync(analyzeBatchInput, finalContext)
                        .map(analyzeResponse -> {
                            final AnalyzeActionsOperationDetail operationDetail =
                                new AnalyzeActionsOperationDetail();
                            AnalyzeActionsOperationDetailPropertiesHelper.setOperationId(operationDetail,
                                parseOperationId(analyzeResponse.getDeserializedHeaders().getOperationLocation()));
                            return operationDetail;
                        })),
                pollingOperation(operationId -> legacyService.analyzeStatusWithResponseAsync(operationId.toString(),
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

    private List<AnalyzeTextLROTask> getAnalyzeTextLROTasks(TextAnalyticsActions actions) {
        if (actions == null) {
            return null;
        }

        final List<AnalyzeTextLROTask> tasks = new ArrayList<>();
        final Iterable<RecognizeEntitiesAction> recognizeEntitiesActions = actions.getRecognizeEntitiesActions();
        final Iterable<RecognizePiiEntitiesAction> recognizePiiEntitiesActions =
            actions.getRecognizePiiEntitiesActions();
        final Iterable<ExtractKeyPhrasesAction> extractKeyPhrasesActions = actions.getExtractKeyPhrasesActions();
        final Iterable<RecognizeLinkedEntitiesAction> recognizeLinkedEntitiesActions =
            actions.getRecognizeLinkedEntitiesActions();
        final Iterable<AnalyzeHealthcareEntitiesAction> analyzeHealthcareEntitiesActions =
            actions.getAnalyzeHealthcareEntitiesActions();
        final Iterable<AnalyzeSentimentAction> analyzeSentimentActions = actions.getAnalyzeSentimentActions();
        final Iterable<RecognizeCustomEntitiesAction> recognizeCustomEntitiesActions =
            actions.getRecognizeCustomEntitiesActions();
        final Iterable<SingleLabelClassificationAction> singleLabelClassificationActions =
            actions.getSingleLabelClassificationActions();
        final Iterable<MultiLabelClassificationAction> multiCategoryClassifyActions =
            actions.getMultiLabelClassificationActions();

        if (recognizeEntitiesActions != null) {
            recognizeEntitiesActions.forEach(action -> tasks.add(toEntitiesLROTask(action)));
        }

        if (recognizePiiEntitiesActions != null) {
            recognizePiiEntitiesActions.forEach(action -> tasks.add(toPiiLROTask(action)));
        }

        if (analyzeHealthcareEntitiesActions != null) {
            analyzeHealthcareEntitiesActions.forEach(action -> tasks.add(toHealthcareLROTask(action)));
        }

        if (extractKeyPhrasesActions != null) {
            extractKeyPhrasesActions.forEach(action -> tasks.add(toKeyPhraseLROTask(action)));
        }

        if (recognizeLinkedEntitiesActions != null) {
            recognizeLinkedEntitiesActions.forEach(action -> tasks.add(toEntityLinkingLROTask(action)));
        }

        if (analyzeSentimentActions != null) {
            analyzeSentimentActions.forEach(action -> tasks.add(toSentimentAnalysisLROTask(action)));
        }

        if (recognizeCustomEntitiesActions != null) {
            recognizeCustomEntitiesActions.forEach(action -> tasks.add(toCustomEntitiesLROTask(action)));
        }

        if (singleLabelClassificationActions != null) {
            singleLabelClassificationActions.forEach(action -> tasks.add(
                toCustomSingleLabelClassificationLROTask(action)));
        }

        if (multiCategoryClassifyActions != null) {
            multiCategoryClassifyActions.forEach(action -> tasks.add(toCustomMultiLabelClassificationLROTask(action)));
        }

        return tasks;
    }

    private JobManifestTasks getJobManifestTasks(TextAnalyticsActions actions) {
        if (actions == null) {
            return null;
        }

        final JobManifestTasks jobManifestTasks = new JobManifestTasks();
        if (actions.getRecognizeEntitiesActions() != null) {
            jobManifestTasks.setEntityRecognitionTasks(toEntitiesTasks(actions));
        }

        if (actions.getRecognizePiiEntitiesActions() != null) {
            jobManifestTasks.setEntityRecognitionPiiTasks(toPiiTasks(actions));
        }

        if (actions.getExtractKeyPhrasesActions() != null) {
            jobManifestTasks.setKeyPhraseExtractionTasks(toKeyPhrasesTasks(actions));
        }

        if (actions.getRecognizeLinkedEntitiesActions() != null) {
            jobManifestTasks.setEntityLinkingTasks(toEntityLinkingTasks(actions));
        }

        if (actions.getAnalyzeSentimentActions() != null) {
            jobManifestTasks.setSentimentAnalysisTasks(toSentimentAnalysisTasks(actions));
        }

        if (actions.getRecognizeCustomEntitiesActions() != null) {
            jobManifestTasks.setCustomEntityRecognitionTasks(toCustomEntitiesTask(actions));
        }

        if (actions.getSingleLabelClassificationActions() != null) {
            jobManifestTasks.setCustomSingleClassificationTasks(toCustomSingleClassificationTask(actions));
        }

        if (actions.getMultiLabelClassificationActions() != null) {
            jobManifestTasks.setCustomMultiClassificationTasks(toCustomMultiClassificationTask(actions));
        }

        return jobManifestTasks;
    }

    private EntitiesLROTask toEntitiesLROTask(RecognizeEntitiesAction action) {
        if (action == null) {
            return null;
        }
        final EntitiesLROTask task = new EntitiesLROTask();
        task.setParameters(getEntitiesTaskParameters(action)).setTaskName(action.getActionName());
        return task;
    }

    private List<EntitiesTask> toEntitiesTasks(TextAnalyticsActions actions) {
        final List<EntitiesTask> entitiesTasks = new ArrayList<>();
        for (RecognizeEntitiesAction action : actions.getRecognizeEntitiesActions()) {
            entitiesTasks.add(
                action == null
                    ? null
                    : new EntitiesTask()
                        .setTaskName(action.getActionName())
                        .setParameters(getEntitiesTaskParameters(action)));
        }
        return entitiesTasks;
    }

    private EntitiesTaskParameters getEntitiesTaskParameters(RecognizeEntitiesAction action) {
        return (EntitiesTaskParameters) new EntitiesTaskParameters()
                                         .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                         .setModelVersion(action.getModelVersion())
                                         .setLoggingOptOut(action.isServiceLogsDisabled());
    }

    private PiiLROTask toPiiLROTask(RecognizePiiEntitiesAction action) {
        if (action == null) {
            return null;
        }
        final PiiLROTask task = new PiiLROTask();
        task.setParameters(getPiiTaskParameters(action)).setTaskName(action.getActionName());
        return task;
    }

    private List<PiiTask> toPiiTasks(TextAnalyticsActions actions) {
        final List<PiiTask> piiTasks = new ArrayList<>();
        for (RecognizePiiEntitiesAction action : actions.getRecognizePiiEntitiesActions()) {
            piiTasks.add(
                action == null
                    ? null
                    : new PiiTask()
                          .setTaskName(action.getActionName())
                          .setParameters(getPiiTaskParameters(action)));
        }
        return piiTasks;
    }

    private PiiTaskParameters getPiiTaskParameters(RecognizePiiEntitiesAction action) {
        return (PiiTaskParameters) new PiiTaskParameters()
                                       .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                       .setDomain(PiiDomain.fromString(
                                           action.getDomainFilter() == null
                                               ? null : action.getDomainFilter().toString()))
                                       .setPiiCategories(toCategoriesFilter(action.getCategoriesFilter()))
                                       .setModelVersion(action.getModelVersion())
                                       .setLoggingOptOut(action.isServiceLogsDisabled());
    }

    private HealthcareLROTask toHealthcareLROTask(AnalyzeHealthcareEntitiesAction action) {
        if (action == null) {
            return null;
        }
        final HealthcareLROTask task = new HealthcareLROTask();
        task.setParameters(getHealthcareTaskParameters(action)).setTaskName(action.getActionName());
        return task;
    }

    private HealthcareTaskParameters getHealthcareTaskParameters(AnalyzeHealthcareEntitiesAction action) {
        return (HealthcareTaskParameters) new HealthcareTaskParameters()
                                              .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                              .setModelVersion(action.getModelVersion())
                                              .setLoggingOptOut(action.isServiceLogsDisabled());
    }

    private KeyPhraseLROTask toKeyPhraseLROTask(ExtractKeyPhrasesAction action) {
        if (action == null) {
            return null;
        }
        final KeyPhraseLROTask task = new KeyPhraseLROTask();
        task.setParameters(getKeyPhraseTaskParameters(action)).setTaskName(action.getActionName());
        return task;
    }

    private List<KeyPhrasesTask> toKeyPhrasesTasks(TextAnalyticsActions actions) {
        final List<KeyPhrasesTask> keyPhrasesTasks = new ArrayList<>();
        for (ExtractKeyPhrasesAction action : actions.getExtractKeyPhrasesActions()) {
            keyPhrasesTasks.add(
                action == null
                    ? null
                    : new KeyPhrasesTask()
                          .setTaskName(action.getActionName())
                          .setParameters(getKeyPhraseTaskParameters(action)));
        }
        return keyPhrasesTasks;
    }

    private KeyPhraseTaskParameters getKeyPhraseTaskParameters(ExtractKeyPhrasesAction action) {
        return (KeyPhraseTaskParameters) new KeyPhraseTaskParameters()
                                             .setModelVersion(action.getModelVersion())
                                             .setLoggingOptOut(action.isServiceLogsDisabled());
    }

    private EntityLinkingLROTask toEntityLinkingLROTask(RecognizeLinkedEntitiesAction action) {
        if (action == null) {
            return null;
        }
        final EntityLinkingLROTask task = new EntityLinkingLROTask();
        task.setParameters(getEntityLinkingTaskParameters(action)).setTaskName(action.getActionName());
        return task;
    }

    private List<EntityLinkingTask> toEntityLinkingTasks(TextAnalyticsActions actions) {
        final List<EntityLinkingTask> tasks = new ArrayList<>();
        for (RecognizeLinkedEntitiesAction action : actions.getRecognizeLinkedEntitiesActions()) {
            tasks.add(
                action == null
                    ? null
                    : new EntityLinkingTask()
                          .setTaskName(action.getActionName())
                          .setParameters(getEntityLinkingTaskParameters(action)));
        }
        return tasks;
    }

    private EntityLinkingTaskParameters getEntityLinkingTaskParameters(RecognizeLinkedEntitiesAction action) {
        return (EntityLinkingTaskParameters) new EntityLinkingTaskParameters()
                                                 .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                                 .setModelVersion(action.getModelVersion())
                                                 .setLoggingOptOut(action.isServiceLogsDisabled());
    }

    private SentimentAnalysisLROTask toSentimentAnalysisLROTask(AnalyzeSentimentAction action) {
        if (action == null) {
            return null;
        }
        final SentimentAnalysisLROTask task = new SentimentAnalysisLROTask();
        task.setParameters(getSentimentAnalysisTaskParameters(action)).setTaskName(action.getActionName());
        return task;
    }

    private List<SentimentAnalysisTask> toSentimentAnalysisTasks(TextAnalyticsActions actions) {
        final List<SentimentAnalysisTask> tasks = new ArrayList<>();
        for (AnalyzeSentimentAction action : actions.getAnalyzeSentimentActions()) {
            tasks.add(
                action == null
                    ? null
                    : new SentimentAnalysisTask()
                          .setTaskName(action.getActionName())
                          .setParameters(getSentimentAnalysisTaskParameters(action)));
        }
        return tasks;
    }

    private SentimentAnalysisTaskParameters getSentimentAnalysisTaskParameters(AnalyzeSentimentAction action) {
        return (SentimentAnalysisTaskParameters) new SentimentAnalysisTaskParameters()
                                                     .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                                                     .setOpinionMining(action.isIncludeOpinionMining())
                                                     .setModelVersion(action.getModelVersion())
                                                     .setLoggingOptOut(action.isServiceLogsDisabled());
    }

    private CustomEntitiesLROTask toCustomEntitiesLROTask(RecognizeCustomEntitiesAction action) {
        if (action == null) {
            return null;
        }
        final CustomEntitiesLROTask task = new CustomEntitiesLROTask();
        task.setParameters(getCustomEntitiesTaskParameters(action)).setTaskName(action.getActionName());
        return task;
    }

    private List<CustomEntitiesTask> toCustomEntitiesTask(TextAnalyticsActions actions) {
        final List<CustomEntitiesTask> tasks = new ArrayList<>();
        for (RecognizeCustomEntitiesAction action : actions.getRecognizeCustomEntitiesActions()) {
            tasks.add(
                action == null
                    ? null
                    : new CustomEntitiesTask()
                          .setTaskName(action.getActionName())
                          .setParameters(getCustomEntitiesTaskParameters(action)));
        }
        return tasks;
    }

    private CustomEntitiesTaskParameters getCustomEntitiesTaskParameters(RecognizeCustomEntitiesAction action) {
        return (CustomEntitiesTaskParameters)
                   new CustomEntitiesTaskParameters()
                       .setStringIndexType(StringIndexType.UTF16CODE_UNIT)
                       .setProjectName(action.getProjectName())
                       .setDeploymentName(action.getDeploymentName())
                       .setLoggingOptOut(action.isServiceLogsDisabled());
    }

    private CustomSingleLabelClassificationLROTask toCustomSingleLabelClassificationLROTask(
        SingleLabelClassificationAction action) {
        if (action == null) {
            return null;
        }
        final CustomSingleLabelClassificationLROTask task = new CustomSingleLabelClassificationLROTask();
        task.setParameters(getCustomSingleClassificationTaskParameters(action)).setTaskName(action.getActionName());
        return task;
    }

    private List<CustomSingleClassificationTask> toCustomSingleClassificationTask(TextAnalyticsActions actions) {
        final List<CustomSingleClassificationTask> tasks = new ArrayList<>();
        for (SingleLabelClassificationAction action : actions.getSingleLabelClassificationActions()) {
            tasks.add(
                action == null
                    ? null
                    : new CustomSingleClassificationTask()
                          .setTaskName(action.getActionName())
                          .setParameters(getCustomSingleClassificationTaskParameters(action)));
        }
        return tasks;
    }

    private CustomSingleLabelClassificationTaskParameters getCustomSingleClassificationTaskParameters(
        SingleLabelClassificationAction action) {
        return (CustomSingleLabelClassificationTaskParameters)
                   new CustomSingleLabelClassificationTaskParameters()
                       .setProjectName(action.getProjectName())
                       .setDeploymentName(action.getDeploymentName())
                       .setLoggingOptOut(action.isServiceLogsDisabled());
    }

    private CustomMultiLabelClassificationLROTask toCustomMultiLabelClassificationLROTask(
        MultiLabelClassificationAction action) {
        if (action == null) {
            return null;
        }
        final CustomMultiLabelClassificationLROTask task = new CustomMultiLabelClassificationLROTask();
        task.setParameters(getCustomMultiLabelClassificationTaskParameters(action)).setTaskName(action.getActionName());
        return task;
    }

    private List<CustomMultiClassificationTask> toCustomMultiClassificationTask(TextAnalyticsActions actions) {
        final List<CustomMultiClassificationTask> tasks = new ArrayList<>();
        for (MultiLabelClassificationAction action : actions.getMultiLabelClassificationActions()) {
            tasks.add(
                action == null
                    ? null
                    : new CustomMultiClassificationTask()
                          .setTaskName(action.getActionName())
                          .setParameters(getCustomMultiLabelClassificationTaskParameters(action)));
        }
        return tasks;
    }

    private CustomMultiLabelClassificationTaskParameters getCustomMultiLabelClassificationTaskParameters(
        MultiLabelClassificationAction action) {
        return (CustomMultiLabelClassificationTaskParameters)
                   new CustomMultiLabelClassificationTaskParameters()
                       .setProjectName(action.getProjectName())
                       .setDeploymentName(action.getDeploymentName())
                       .setLoggingOptOut(action.isServiceLogsDisabled());
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
        pollingOperation(Function<UUID, Mono<Response<AnalyzeJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeActionsOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return pollingFunction.apply(operationId)
                    .flatMap(modelResponse -> processAnalyzedModelResponse(modelResponse, operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeActionsOperationDetail>, Mono<PollResponse<AnalyzeActionsOperationDetail>>>
        pollingOperationLanguageApi(Function<UUID, Mono<Response<AnalyzeTextJobState>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<AnalyzeActionsOperationDetail> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID operationId = UUID.fromString(operationResultPollResponse.getValue().getOperationId());
                return pollingFunction.apply(operationId)
                           .flatMap(modelResponse -> processAnalyzedModelResponseLanguageApi(
                               modelResponse, operationResultPollResponse))
                           .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeActionsOperationDetail>, Mono<AnalyzeActionsResultPagedFlux>>
        fetchingOperation(Function<UUID, Mono<AnalyzeActionsResultPagedFlux>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID operationId = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(operationId);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<AnalyzeActionsOperationDetail>, Mono<AnalyzeActionsResultPagedIterable>>
        fetchingOperationIterable(Function<UUID, Mono<AnalyzeActionsResultPagedIterable>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID operationId = UUID.fromString(pollingContext.getLatestResponse().getValue().getOperationId());
                return fetchingFunction.apply(operationId);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    AnalyzeActionsResultPagedFlux getAnalyzeOperationFluxPage(UUID operationId, Integer top, Integer skip,
        boolean showStats, Context context) {
        return new AnalyzeActionsResultPagedFlux(
            () -> (continuationToken, pageSize) ->
                      getPage(continuationToken, operationId, top, skip, showStats, context).flux());
    }

    Mono<PagedResponse<AnalyzeActionsResult>> getPage(String continuationToken, UUID operationId, Integer top,
        Integer skip, boolean showStats, Context context) {
        if (continuationToken != null) {
            final Map<String, Object> continuationTokenMap = parseNextLink(continuationToken);
            final Integer topValue = (Integer) continuationTokenMap.getOrDefault("$top", null);
            final Integer skipValue = (Integer) continuationTokenMap.getOrDefault("$skip", null);
            final Boolean showStatsValue = (Boolean) continuationTokenMap.getOrDefault(showStats, false);

            if (service != null) {
                return service.jobStatusWithResponseAsync(operationId, showStatsValue, topValue, skipValue,
                    context)
                    .map(this::toAnalyzeActionsResultPagedResponseLanguageApi)
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            }
            return legacyService.analyzeStatusWithResponseAsync(operationId.toString(), showStatsValue, topValue, skipValue,
                context)
                .map(this::toAnalyzeActionsResultPagedResponse)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        } else {
            if (service != null) {
                return service.jobStatusWithResponseAsync(operationId, showStats, top, skip, context)
                    .map(this::toAnalyzeActionsResultPagedResponseLanguageApi)
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            }
            return legacyService.analyzeStatusWithResponseAsync(operationId.toString(), showStats, top, skip, context)
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

    private PagedResponse<AnalyzeActionsResult> toAnalyzeActionsResultPagedResponseLanguageApi(Response<AnalyzeTextJobState> response) {
        final AnalyzeTextJobState analyzeJobState = response.getValue();
        return new PagedResponseBase<Void, AnalyzeActionsResult>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            Arrays.asList(toAnalyzeActionsResultLanguageApi(analyzeJobState)),
            analyzeJobState.getNextLink(),
            null);
    }

    private AnalyzeActionsResult toAnalyzeActionsResult(AnalyzeJobState analyzeJobState) {
        TasksStateTasksOld tasksStateTasks = analyzeJobState.getTasks();
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

        List<RecognizeEntitiesActionResult> recognizeEntitiesActionResults = new ArrayList<>();
        List<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults = new ArrayList<>();
        List<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults = new ArrayList<>();
        List<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults = new ArrayList<>();
        List<AnalyzeSentimentActionResult> analyzeSentimentActionResults = new ArrayList<>();
        List<RecognizeCustomEntitiesActionResult> recognizeCustomEntitiesActionResults = new ArrayList<>();
        List<SingleLabelClassificationActionResult> singleLabelClassificationActionResults =
            new ArrayList<>();
        List<MultiLabelClassificationActionResult> multiLabelClassificationActionResults =
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

        final List<TextAnalyticsError> errors = analyzeJobState.getErrors();
        if (!CoreUtils.isNullOrEmpty(errors)) {
            for (TextAnalyticsError error : errors) {
                if (error != null) {
                    final String[] targetPair = parseActionErrorTarget(error.getTarget(), error.getMessage());
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
                    } else if (CUSTOM_ENTITY_RECOGNITION_TASKS.equals(taskName)) {
                        actionResult = recognizeCustomEntitiesActionResults.get(taskIndex);
                    } else if (CUSTOM_SINGLE_CLASSIFICATION_TASKS.equals(taskName)) {
                        actionResult = singleLabelClassificationActionResults.get(taskIndex);
                    } else if (CUSTOM_MULTI_CLASSIFICATION_TASKS.equals(taskName)) {
                        actionResult = multiLabelClassificationActionResults.get(taskIndex);
                    } else {
                        throw logger.logExceptionAsError(new RuntimeException(
                            "Invalid task name in target reference, " + taskName));
                    }

                    TextAnalyticsActionResultPropertiesHelper.setIsError(actionResult, true);
                    TextAnalyticsActionResultPropertiesHelper.setError(actionResult,
                        new com.azure.ai.textanalytics.models.TextAnalyticsError(
                            TextAnalyticsErrorCode.fromString(
                                error.getErrorCode() == null ? null : error.getErrorCode().toString()),
                            error.getMessage(), null));
                }
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
        AnalyzeActionsResultPropertiesHelper.setRecognizeCustomEntitiesResults(analyzeActionsResult,
            IterableStream.of(recognizeCustomEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setClassifySingleCategoryResults(analyzeActionsResult,
            IterableStream.of(singleLabelClassificationActionResults));
        AnalyzeActionsResultPropertiesHelper.setClassifyMultiCategoryResults(analyzeActionsResult,
            IterableStream.of(multiLabelClassificationActionResults));
        return analyzeActionsResult;
    }

    private AnalyzeActionsResult toAnalyzeActionsResultLanguageApi(AnalyzeTextJobState analyzeJobState) {
        final TasksStateTasks tasksStateTasks = analyzeJobState.getTasks();
        final List<AnalyzeTextLROResult> tasksResults = tasksStateTasks.getItems();

        final List<RecognizeEntitiesActionResult> recognizeEntitiesActionResults = new ArrayList<>();
        final List<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults = new ArrayList<>();
        final List<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults = new ArrayList<>();
        final List<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults = new ArrayList<>();
        final List<AnalyzeHealthcareEntitiesActionResult> analyzeHealthcareEntitiesActionResults = new ArrayList<>();
        final List<AnalyzeSentimentActionResult> analyzeSentimentActionResults = new ArrayList<>();
        final List<RecognizeCustomEntitiesActionResult> recognizeCustomEntitiesActionResults = new ArrayList<>();
        final List<SingleLabelClassificationActionResult> singleLabelClassificationActionResults = new ArrayList<>();
        final List<MultiLabelClassificationActionResult> multiLabelClassificationActionResults = new ArrayList<>();

        if (!CoreUtils.isNullOrEmpty(tasksResults)) {
            for (int i = 0; i < tasksResults.size(); i++) {
                final AnalyzeTextLROResult taskResult = tasksResults.get(i);
                if (taskResult instanceof EntityRecognitionLROResult) {
                    final EntityRecognitionLROResult entityTaskResult = (EntityRecognitionLROResult) taskResult;
                    final RecognizeEntitiesActionResult actionResult = new RecognizeEntitiesActionResult();
                    final EntitiesResult results = entityTaskResult.getResults();
                    if (results != null) {
                        RecognizeEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                            toRecognizeEntitiesResultCollectionResponse(results));
                    }
                    TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult,
                        entityTaskResult.getTaskName());
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        entityTaskResult.getLastUpdateDateTime());
                    recognizeEntitiesActionResults.add(actionResult);

                } else if (taskResult instanceof CustomEntityRecognitionLROResult) {
                    final CustomEntityRecognitionLROResult customEntityTaskResult =
                        (CustomEntityRecognitionLROResult) taskResult;
                    final RecognizeCustomEntitiesActionResult actionResult = new RecognizeCustomEntitiesActionResult();
                    final CustomEntitiesResult results = customEntityTaskResult.getResults();
                    if (results != null) {
                        RecognizeCustomEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                            toRecognizeCustomEntitiesResultCollection(results));
                    }
                    TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult,
                        customEntityTaskResult.getTaskName());
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        customEntityTaskResult.getLastUpdateDateTime());
                    recognizeCustomEntitiesActionResults.add(actionResult);
                } else if (taskResult instanceof CustomSingleLabelClassificationLROResult) {
                    final CustomSingleLabelClassificationLROResult customSingleLabelClassificationResult =
                        (CustomSingleLabelClassificationLROResult) taskResult;
                    final SingleLabelClassificationActionResult actionResult =
                        new SingleLabelClassificationActionResult();
                    final CustomLabelClassificationResult results =
                        customSingleLabelClassificationResult.getResults();
                    if (results != null) {
                        SingleLabelClassificationActionResultPropertiesHelper.setDocumentsResults(actionResult,
                            toLabelClassificationResultCollection(results));
                    }
                    TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult,
                        customSingleLabelClassificationResult.getTaskName());
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        customSingleLabelClassificationResult.getLastUpdateDateTime());
                    singleLabelClassificationActionResults.add(actionResult);
                } else if (taskResult instanceof CustomMultiLabelClassificationLROResult) {
                    final CustomMultiLabelClassificationLROResult customMultiLabelClassificationLROResult =
                        (CustomMultiLabelClassificationLROResult) taskResult;
                    final MultiLabelClassificationActionResult actionResult = new MultiLabelClassificationActionResult();
                    final CustomLabelClassificationResult results =
                        customMultiLabelClassificationLROResult.getResults();
                    if (results != null) {
                        MultiLabelClassificationActionResultPropertiesHelper.setDocumentsResults(actionResult,
                            toLabelClassificationResultCollection(results));
                    }
                    TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult,
                        customMultiLabelClassificationLROResult.getTaskName());
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        customMultiLabelClassificationLROResult.getLastUpdateDateTime());
                    multiLabelClassificationActionResults.add(actionResult);
                } else if (taskResult instanceof EntityLinkingLROResult) {
                    final EntityLinkingLROResult entityLinkingLROResult = (EntityLinkingLROResult) taskResult;
                    final RecognizeLinkedEntitiesActionResult actionResult = new RecognizeLinkedEntitiesActionResult();
                    final EntityLinkingResult results = entityLinkingLROResult.getResults();
                    if (results != null) {
                        RecognizeLinkedEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                            toRecognizeLinkedEntitiesResultCollection(results));
                    }
                    TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult,
                        entityLinkingLROResult.getTaskName());
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        entityLinkingLROResult.getLastUpdateDateTime());
                    recognizeLinkedEntitiesActionResults.add(actionResult);
                } else if (taskResult instanceof PiiEntityRecognitionLROResult) {
                    final PiiEntityRecognitionLROResult piiEntityRecognitionLROResult =
                        (PiiEntityRecognitionLROResult) taskResult;
                    final RecognizePiiEntitiesActionResult actionResult = new RecognizePiiEntitiesActionResult();
                    final PiiResult results = piiEntityRecognitionLROResult.getResults();
                    if (results != null) {
                        RecognizePiiEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                            toRecognizePiiEntitiesResultCollection(results));
                    }
                    TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult,
                        piiEntityRecognitionLROResult.getTaskName());
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        piiEntityRecognitionLROResult.getLastUpdateDateTime());
                    recognizePiiEntitiesActionResults.add(actionResult);
                } else if (taskResult instanceof HealthcareLROResult) {
                    final HealthcareLROResult healthcareLROResult = (HealthcareLROResult) taskResult;
                    final AnalyzeHealthcareEntitiesActionResult actionResult =
                        new AnalyzeHealthcareEntitiesActionResult();
                    final HealthcareResult results = healthcareLROResult.getResults();
                    if (results != null) {
                        AnalyzeHealthcareEntitiesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                            toAnalyzeHealthcareEntitiesResultCollection(results));
                    }
                    TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult,
                        healthcareLROResult.getTaskName());
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        healthcareLROResult.getLastUpdateDateTime());
                    analyzeHealthcareEntitiesActionResults.add(actionResult);
                } else if (taskResult instanceof SentimentLROResult) {
                    final SentimentLROResult sentimentLROResult = (SentimentLROResult) taskResult;
                    final AnalyzeSentimentActionResult actionResult = new AnalyzeSentimentActionResult();
                    final SentimentResponse results = sentimentLROResult.getResults();
                    if (results != null) {
                        AnalyzeSentimentActionResultPropertiesHelper.setDocumentsResults(actionResult,
                            toAnalyzeSentimentResultCollection(results));
                    }
                    TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult,
                        sentimentLROResult.getTaskName());
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        sentimentLROResult.getLastUpdateDateTime());
                    analyzeSentimentActionResults.add(actionResult);
                } else if (taskResult instanceof KeyPhraseExtractionLROResult) {
                    final KeyPhraseExtractionLROResult keyPhraseExtractionLROResult =
                        (KeyPhraseExtractionLROResult) taskResult;
                    final ExtractKeyPhrasesActionResult actionResult = new ExtractKeyPhrasesActionResult();
                    final KeyPhraseResult results = keyPhraseExtractionLROResult.getResults();
                    if (results != null) {
                        ExtractKeyPhrasesActionResultPropertiesHelper.setDocumentsResults(actionResult,
                            toExtractKeyPhrasesResultCollection(results));
                    }
                    TextAnalyticsActionResultPropertiesHelper.setActionName(actionResult,
                        keyPhraseExtractionLROResult.getTaskName());
                    TextAnalyticsActionResultPropertiesHelper.setCompletedAt(actionResult,
                        keyPhraseExtractionLROResult.getLastUpdateDateTime());
                    extractKeyPhrasesActionResults.add(actionResult);
                } else {
                    throw logger.logExceptionAsError(new RuntimeException(
                        "Invalid Long running operation task result: " + taskResult.getClass()));
                }
            }
        }

        //TODO: In Language REST API, there is no such task names. It might be a different way to parse the Error Target
        // https://github.com/Azure/azure-sdk-for-java/issues/28834
        final List<Error> errors = analyzeJobState.getErrors();
        if (!CoreUtils.isNullOrEmpty(errors)) {
            for (Error error : errors) {
                if (error != null) {
                    final String[] targetPair = parseActionErrorTarget(error.getTarget(), error.getMessage());
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
                    } else if (CUSTOM_ENTITY_RECOGNITION_TASKS.equals(taskName)) {
                        actionResult = recognizeCustomEntitiesActionResults.get(taskIndex);
                    } else if (CUSTOM_SINGLE_CLASSIFICATION_TASKS.equals(taskName)) {
                        actionResult = singleLabelClassificationActionResults.get(taskIndex);
                    } else if (CUSTOM_MULTI_CLASSIFICATION_TASKS.equals(taskName)) {
                        actionResult = multiLabelClassificationActionResults.get(taskIndex);
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
        }

        final AnalyzeActionsResult analyzeActionsResult = new AnalyzeActionsResult();
        AnalyzeActionsResultPropertiesHelper.setRecognizeEntitiesResults(analyzeActionsResult,
            IterableStream.of(recognizeEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setRecognizePiiEntitiesResults(analyzeActionsResult,
            IterableStream.of(recognizePiiEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setAnalyzeHealthcareEntitiesResults(analyzeActionsResult,
            IterableStream.of(analyzeHealthcareEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setExtractKeyPhrasesResults(analyzeActionsResult,
            IterableStream.of(extractKeyPhrasesActionResults));
        AnalyzeActionsResultPropertiesHelper.setRecognizeLinkedEntitiesResults(analyzeActionsResult,
            IterableStream.of(recognizeLinkedEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setAnalyzeSentimentResults(analyzeActionsResult,
            IterableStream.of(analyzeSentimentActionResults));
        AnalyzeActionsResultPropertiesHelper.setRecognizeCustomEntitiesResults(analyzeActionsResult,
            IterableStream.of(recognizeCustomEntitiesActionResults));
        AnalyzeActionsResultPropertiesHelper.setClassifySingleCategoryResults(analyzeActionsResult,
            IterableStream.of(singleLabelClassificationActionResults));
        AnalyzeActionsResultPropertiesHelper.setClassifyMultiCategoryResults(analyzeActionsResult,
            IterableStream.of(multiLabelClassificationActionResults));
        return analyzeActionsResult;
    }

    private Mono<PollResponse<AnalyzeActionsOperationDetail>> processAnalyzedModelResponse(
        Response<AnalyzeJobState> analyzeJobStateResponse,
        PollResponse<AnalyzeActionsOperationDetail> operationResultPollResponse) {

        LongRunningOperationStatus status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        if (analyzeJobStateResponse.getValue() != null && analyzeJobStateResponse.getValue().getStatus() != null) {
            State state = analyzeJobStateResponse.getValue().getStatus();
            if (NOT_STARTED.equals(state) || RUNNING.equals(state)) {
                status = LongRunningOperationStatus.IN_PROGRESS;
            } else if (SUCCEEDED.equals(state)) {
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            } else if (CANCELLED.equals(state)) {
                status = LongRunningOperationStatus.USER_CANCELLED;
            } else {
                status = LongRunningOperationStatus.fromString(
                    analyzeJobStateResponse.getValue().getStatus().toString(), true);
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
        final TasksStateTasksOld tasksResult = analyzeJobStateResponse.getValue().getTasks();
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

    private Mono<PollResponse<AnalyzeActionsOperationDetail>> processAnalyzedModelResponseLanguageApi(
        Response<AnalyzeTextJobState> analyzeJobStateResponse,
        PollResponse<AnalyzeActionsOperationDetail> operationResultPollResponse) {

        LongRunningOperationStatus status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        if (analyzeJobStateResponse.getValue() != null && analyzeJobStateResponse.getValue().getStatus() != null) {
            State state = analyzeJobStateResponse.getValue().getStatus();
            if (NOT_STARTED.equals(state) || RUNNING.equals(state)) {
                status = LongRunningOperationStatus.IN_PROGRESS;
            } else if (SUCCEEDED.equals(state)) {
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            } else if (CANCELLED.equals(state)) {
                status = LongRunningOperationStatus.USER_CANCELLED;
            } else if (PARTIALLY_COMPLETED.equals(state)) {
                status = LongRunningOperationStatus.fromString("partiallySucceeded", true);
            } else {
                status = LongRunningOperationStatus.fromString(
                    analyzeJobStateResponse.getValue().getStatus().toString(), true);
            }
        }
        AnalyzeActionsOperationDetailPropertiesHelper.setDisplayName(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getDisplayName());
        AnalyzeActionsOperationDetailPropertiesHelper.setCreatedAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getCreatedDateTime());
        AnalyzeActionsOperationDetailPropertiesHelper.setExpiresAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getExpirationDateTime());
        AnalyzeActionsOperationDetailPropertiesHelper.setLastModifiedAt(operationResultPollResponse.getValue(),
            analyzeJobStateResponse.getValue().getLastUpdatedDateTime());
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

    private String[] parseActionErrorTarget(String targetReference, String errorMessage) {
        if (CoreUtils.isNullOrEmpty(targetReference)) {
            if (CoreUtils.isNullOrEmpty(errorMessage)) {
                errorMessage = "Expected an error with a target field referencing an action but did not get one";
            }
            throw logger.logExceptionAsError(new RuntimeException(errorMessage));
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
