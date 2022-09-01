// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesAction;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.MultiLabelClassifyAction;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesAction;
import com.azure.ai.textanalytics.models.SingleLabelClassifyAction;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static com.azure.ai.textanalytics.TestUtils.VALID_HTTPS_LOCALHOST;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Text Analytics client side validation
 */
public class ClientSideValidationUnitTests {
    static TextAnalyticsClient clientV30;
    static TextAnalyticsClient clientV31;
    static TextAnalyticsAsyncClient asyncClientV30;
    static TextAnalyticsAsyncClient asyncClientV31;
    static List<String> dummyDocument = Arrays.asList("A tree", "Be good");
    static final String DISABLE_SERVICE_LOGS_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("TextAnalyticsRequestOptions.disableServiceLogs",
            TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1);
    static final String RECOGNIZE_PII_ENTITIES_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("recognizePiiEntitiesBatch", TextAnalyticsServiceVersion.V3_0,
            TextAnalyticsServiceVersion.V3_1);
    static final String OPINION_MINING_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("AnalyzeSentimentOptions.includeOpinionMining",
            TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1);
    static final String ANALYZE_ACTIONS_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("beginAnalyzeActions", TextAnalyticsServiceVersion.V3_0,
            TextAnalyticsServiceVersion.V3_1);
    static final String HEALTHCARE_ENTITIES_ACTION_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("AnalyzeHealthcareEntitiesAction", TextAnalyticsServiceVersion.V3_1,
            TextAnalyticsServiceVersion.V2022_05_01);
    static final String CUSTOM_ENTITIES_ACTION_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("RecognizeCustomEntitiesAction", TextAnalyticsServiceVersion.V3_1,
            TextAnalyticsServiceVersion.V2022_05_01);
    static final String SINGLE_LABEL_ACTION_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("SingleLabelClassifyAction", TextAnalyticsServiceVersion.V3_1,
            TextAnalyticsServiceVersion.V2022_05_01);
    static final String MULTI_LABEL_ACTION_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("MultiLabelClassifyAction", TextAnalyticsServiceVersion.V3_1,
            TextAnalyticsServiceVersion.V2022_05_01);
    static final String ANALYZE_HEALTHCARE_ENTITIES_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("beginAnalyzeHealthcareEntities",
            TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1);
    static final String HEALTHCARE_ENTITIES_DISPLAY_NAME_ERROR_MESSAGE =
        getUnsupportedServiceApiVersionMessage("AnalyzeHealthcareEntitiesOptions.displayName",
            TextAnalyticsServiceVersion.V3_1, TextAnalyticsServiceVersion.V2022_05_01);
    static final String RECOGNIZE_CUSTOM_ENTITIES_ERROR_MESSAGE_30 =
        getUnsupportedServiceApiVersionMessage("beginRecognizeCustomEntities",
            TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V2022_05_01);
    static final String RECOGNIZE_CUSTOM_ENTITIES_ERROR_MESSAGE_31 =
        getUnsupportedServiceApiVersionMessage("beginRecognizeCustomEntities",
            TextAnalyticsServiceVersion.V3_1, TextAnalyticsServiceVersion.V2022_05_01);
    static final String SINGLE_LABEL_CLASSIFY_ERROR_MESSAGE_30 =
        getUnsupportedServiceApiVersionMessage("beginSingleLabelClassify", TextAnalyticsServiceVersion.V3_0,
            TextAnalyticsServiceVersion.V2022_05_01);
    static final String SINGLE_LABEL_CLASSIFY_ERROR_MESSAGE_31 =
        getUnsupportedServiceApiVersionMessage("beginSingleLabelClassify", TextAnalyticsServiceVersion.V3_1,
            TextAnalyticsServiceVersion.V2022_05_01);
    static final String MULTI_LABEL_CLASSIFY_ERROR_MESSAGE_30 =
        getUnsupportedServiceApiVersionMessage("beginMultiLabelClassify", TextAnalyticsServiceVersion.V3_0,
            TextAnalyticsServiceVersion.V2022_05_01);
    static final String MULTI_LABEL_CLASSIFY_ERROR_MESSAGE_31 =
        getUnsupportedServiceApiVersionMessage("beginMultiLabelClassify", TextAnalyticsServiceVersion.V3_1,
            TextAnalyticsServiceVersion.V2022_05_01);
    static final String PROJECT_NAME = "project-name";
    static final String DEPLOYMENT_NAME = "deployment-name";
    static final String LANGUAGE_EN = "en";

    @BeforeAll
    protected static void beforeTest() {
        TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder()
            .endpoint(VALID_HTTPS_LOCALHOST)
            .credential(new AzureKeyCredential("fakeKey"));
        clientV30 = builder.serviceVersion(TextAnalyticsServiceVersion.V3_0).buildClient();
        asyncClientV30 = builder.serviceVersion(TextAnalyticsServiceVersion.V3_0).buildAsyncClient();
        clientV31 = builder.serviceVersion(TextAnalyticsServiceVersion.V3_1).buildClient();
        asyncClientV31 = builder.serviceVersion(TextAnalyticsServiceVersion.V3_1).buildAsyncClient();
    }

    @AfterAll
    protected static void afterTest() {
        clientV30 = null;
        asyncClientV30 = null;
        clientV31 = null;
        asyncClientV31 = null;
    }

    @Test
    public void detectLanguageClientSideValidation() {
        TextAnalyticsRequestOptions enableServiceLogsOption = new TextAnalyticsRequestOptions().setServiceLogsDisabled(true);
        // Async
        StepVerifier.create(asyncClientV30.detectLanguageBatch(dummyDocument, null, enableServiceLogsOption))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(DISABLE_SERVICE_LOGS_ERROR_MESSAGE, exception.getMessage());
            });

        // Sync
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> clientV30.detectLanguageBatch(dummyDocument, null, enableServiceLogsOption));
        assertEquals(DISABLE_SERVICE_LOGS_ERROR_MESSAGE, exception.getMessage());
    }


    @Test
    public void recognizeEntitiesClientSideValidation() {
        TextAnalyticsRequestOptions enableServiceLogsOption = new TextAnalyticsRequestOptions().setServiceLogsDisabled(true);

        // Async
        StepVerifier.create(asyncClientV30.recognizeEntitiesBatch(dummyDocument, null, enableServiceLogsOption))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertTrue(DISABLE_SERVICE_LOGS_ERROR_MESSAGE.equals(exception.getMessage()));
            });

        // Sync
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> clientV30.recognizeEntitiesBatch(dummyDocument, null, enableServiceLogsOption));
        assertEquals(DISABLE_SERVICE_LOGS_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    public void recognizePiiEntitiesClientSideValidation() {
        // Async
        StepVerifier.create(asyncClientV30.recognizePiiEntitiesBatch(dummyDocument, null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertTrue(RECOGNIZE_PII_ENTITIES_ERROR_MESSAGE.equals(exception.getMessage()));
            });

        // Sync
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> clientV30.recognizePiiEntitiesBatch(dummyDocument, null, null));
        assertEquals(RECOGNIZE_PII_ENTITIES_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    public void recognizeLinkedEntitiesClientSideValidation() {
        TextAnalyticsRequestOptions enableServiceLogsOption = new TextAnalyticsRequestOptions().setServiceLogsDisabled(true);

        // Async
        StepVerifier.create(asyncClientV30.recognizeLinkedEntitiesBatch(dummyDocument, null, enableServiceLogsOption))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(DISABLE_SERVICE_LOGS_ERROR_MESSAGE, exception.getMessage());
            });

        // Sync
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> clientV30.recognizeLinkedEntitiesBatch(dummyDocument, null, enableServiceLogsOption));
        assertEquals(DISABLE_SERVICE_LOGS_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    public void extractKeyPhrasesClientSideValidation() {
        TextAnalyticsRequestOptions enableServiceLogsOption = new TextAnalyticsRequestOptions().setServiceLogsDisabled(true);

        // Async
        StepVerifier.create(asyncClientV30.extractKeyPhrasesBatch(dummyDocument, null, enableServiceLogsOption))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(DISABLE_SERVICE_LOGS_ERROR_MESSAGE, exception.getMessage());
            });

        // Sync
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> clientV30.extractKeyPhrasesBatch(dummyDocument, null, enableServiceLogsOption));
        assertEquals(DISABLE_SERVICE_LOGS_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    public void analyzeSentimentClientSideValidation() {
        AnalyzeSentimentOptions enableServiceLogsOption = new AnalyzeSentimentOptions().setServiceLogsDisabled(true);

        // Async
        StepVerifier.create(asyncClientV30.analyzeSentimentBatch(dummyDocument, null, enableServiceLogsOption))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(DISABLE_SERVICE_LOGS_ERROR_MESSAGE, exception.getMessage());
            });

        AnalyzeSentimentOptions includeOpinionMiningOption  = new AnalyzeSentimentOptions().setIncludeOpinionMining(true);
        StepVerifier.create(asyncClientV30.analyzeSentimentBatch(dummyDocument, null, includeOpinionMiningOption))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(OPINION_MINING_ERROR_MESSAGE, exception.getMessage());
            });

        // Sync
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> clientV30.analyzeSentimentBatch(dummyDocument, null, enableServiceLogsOption));
        assertEquals(DISABLE_SERVICE_LOGS_ERROR_MESSAGE, exception.getMessage());

        IllegalStateException includeOpinionMiningException = assertThrows(IllegalStateException.class,
            () -> clientV30.analyzeSentimentBatch(dummyDocument, null, includeOpinionMiningOption));
        assertEquals(OPINION_MINING_ERROR_MESSAGE, includeOpinionMiningException.getMessage());
    }

    @Test
    public void analyzeActionsClientSideValidation() {
        TextAnalyticsActions actions = new TextAnalyticsActions();
        // Async
        // beginAnalyzeActions is only supported in 3.1 and newer
        StepVerifier.create(asyncClientV30.beginAnalyzeActions(dummyDocument, actions, LANGUAGE_EN, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(ANALYZE_ACTIONS_ERROR_MESSAGE, exception.getMessage());
            });
        // AnalyzeHealthcareEntitiesAction is only supported in 2022-05-01 and newer
        TextAnalyticsActions healthcareEntitiesActions =
            new TextAnalyticsActions()
                .setAnalyzeHealthcareEntitiesActions(new AnalyzeHealthcareEntitiesAction());
        StepVerifier.create(asyncClientV31.beginAnalyzeActions(dummyDocument, healthcareEntitiesActions, LANGUAGE_EN,
                null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(HEALTHCARE_ENTITIES_ACTION_ERROR_MESSAGE, exception.getMessage());
            });
        // RecognizeCustomEntitiesAction is only supported in 2022-05-01 and newer
        TextAnalyticsActions customEntitiesActions =
            new TextAnalyticsActions()
                .setRecognizeCustomEntitiesActions(new RecognizeCustomEntitiesAction(PROJECT_NAME, DEPLOYMENT_NAME));
        StepVerifier.create(asyncClientV31.beginAnalyzeActions(dummyDocument, customEntitiesActions, LANGUAGE_EN,
                null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(CUSTOM_ENTITIES_ACTION_ERROR_MESSAGE, exception.getMessage());
            });
        // SingleLabelClassifyAction is only supported in 2022-05-01 and newer
        TextAnalyticsActions singleLabelClassifyActions =
            new TextAnalyticsActions()
                .setSingleLabelClassifyActions(new SingleLabelClassifyAction(PROJECT_NAME, DEPLOYMENT_NAME));
        StepVerifier.create(asyncClientV31.beginAnalyzeActions(dummyDocument, singleLabelClassifyActions, LANGUAGE_EN,
                null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(SINGLE_LABEL_ACTION_ERROR_MESSAGE, exception.getMessage());
            });
        // MultiLabelClassifyAction is only supported in 2022-05-01 and newer
        TextAnalyticsActions multiLabelClassifyActions =
            new TextAnalyticsActions()
                .setMultiLabelClassifyActions(new MultiLabelClassifyAction(PROJECT_NAME, DEPLOYMENT_NAME));
        StepVerifier.create(asyncClientV31.beginAnalyzeActions(dummyDocument, multiLabelClassifyActions, LANGUAGE_EN,
                null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(MULTI_LABEL_ACTION_ERROR_MESSAGE, exception.getMessage());
            });

        // Sync
        // beginAnalyzeActions is only supported in 3.1 and newer
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> clientV30.beginAnalyzeActions(dummyDocument, actions, LANGUAGE_EN, null));
        assertEquals(ANALYZE_ACTIONS_ERROR_MESSAGE, exception.getMessage());
        // AnalyzeHealthcareEntitiesAction is only supported in 2022-05-01 and newer
        IllegalStateException healthcareEntitiesActionsException = assertThrows(IllegalStateException.class,
            () -> clientV31.beginAnalyzeActions(dummyDocument, healthcareEntitiesActions, LANGUAGE_EN, null));
        assertEquals(HEALTHCARE_ENTITIES_ACTION_ERROR_MESSAGE, healthcareEntitiesActionsException.getMessage());
        // RecognizeCustomEntitiesAction is only supported in 2022-05-01 and newer
        IllegalStateException customEntitiesActionsException = assertThrows(IllegalStateException.class,
            () -> clientV31.beginAnalyzeActions(dummyDocument, customEntitiesActions, LANGUAGE_EN, null));
        assertEquals(CUSTOM_ENTITIES_ACTION_ERROR_MESSAGE, customEntitiesActionsException.getMessage());
        // SingleLabelClassifyAction is only supported in 2022-05-01 and newer
        IllegalStateException singleLabelClassifyActionsException = assertThrows(IllegalStateException.class,
            () -> clientV31.beginAnalyzeActions(dummyDocument, singleLabelClassifyActions, LANGUAGE_EN, null));
        assertEquals(SINGLE_LABEL_ACTION_ERROR_MESSAGE, singleLabelClassifyActionsException.getMessage());
        // MultiLabelClassifyAction is only supported in 2022-05-01 and newer
        IllegalStateException multiLabelClassifyActionsException = assertThrows(IllegalStateException.class,
            () -> clientV31.beginAnalyzeActions(dummyDocument, multiLabelClassifyActions, LANGUAGE_EN, null));
        assertEquals(MULTI_LABEL_ACTION_ERROR_MESSAGE, multiLabelClassifyActionsException.getMessage());
    }

    @Test
    public void analyzeHealthcareEntitiesClientSideValidation() {
        // Async
        StepVerifier.create(asyncClientV30.beginAnalyzeHealthcareEntities(dummyDocument, null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(ANALYZE_HEALTHCARE_ENTITIES_ERROR_MESSAGE, exception.getMessage());
            });
        AnalyzeHealthcareEntitiesOptions displayNameOptions = new AnalyzeHealthcareEntitiesOptions()
            .setDisplayName("operationName");
        StepVerifier.create(asyncClientV31.beginAnalyzeHealthcareEntities(dummyDocument, null, displayNameOptions))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(HEALTHCARE_ENTITIES_DISPLAY_NAME_ERROR_MESSAGE, exception.getMessage());
            });

        // Sync
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> clientV30.beginAnalyzeHealthcareEntities(dummyDocument, null, null));
        assertEquals(ANALYZE_HEALTHCARE_ENTITIES_ERROR_MESSAGE, exception.getMessage());
        IllegalStateException displayNameException = assertThrows(IllegalStateException.class,
            () -> clientV31.beginAnalyzeHealthcareEntities(dummyDocument, null, displayNameOptions));
        assertEquals(HEALTHCARE_ENTITIES_DISPLAY_NAME_ERROR_MESSAGE, displayNameException.getMessage());
    }

    @Test
    public void recognizeCustomEntitiesClientSideValidation() {
        // Async
        StepVerifier.create(asyncClientV30.beginRecognizeCustomEntities(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME,
                null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(RECOGNIZE_CUSTOM_ENTITIES_ERROR_MESSAGE_30, exception.getMessage());
            });
        StepVerifier.create(asyncClientV31.beginRecognizeCustomEntities(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME,
                null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(RECOGNIZE_CUSTOM_ENTITIES_ERROR_MESSAGE_31, exception.getMessage());
            });
        // Sync
        IllegalStateException exception30 = assertThrows(IllegalStateException.class,
            () -> clientV30.beginRecognizeCustomEntities(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME, null, null));
        assertEquals(RECOGNIZE_CUSTOM_ENTITIES_ERROR_MESSAGE_30, exception30.getMessage());

        IllegalStateException exception31 = assertThrows(IllegalStateException.class,
            () -> clientV31.beginRecognizeCustomEntities(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME, null, null));
        assertEquals(RECOGNIZE_CUSTOM_ENTITIES_ERROR_MESSAGE_31, exception31.getMessage());
    }

    @Test
    public void singleLabelClassificationClientSideValidation() {
        // Async
        StepVerifier.create(asyncClientV30.beginSingleLabelClassify(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME,
                null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(SINGLE_LABEL_CLASSIFY_ERROR_MESSAGE_30, exception.getMessage());
            });
        StepVerifier.create(asyncClientV31.beginSingleLabelClassify(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME,
                null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(SINGLE_LABEL_CLASSIFY_ERROR_MESSAGE_31, exception.getMessage());
            });

        // Sync
        IllegalStateException exception30 = assertThrows(IllegalStateException.class,
            () -> clientV30.beginSingleLabelClassify(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME, null, null));
        assertEquals(SINGLE_LABEL_CLASSIFY_ERROR_MESSAGE_30, exception30.getMessage());

        IllegalStateException exception31 = assertThrows(IllegalStateException.class,
            () -> clientV31.beginSingleLabelClassify(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME, null, null));
        assertEquals(SINGLE_LABEL_CLASSIFY_ERROR_MESSAGE_31, exception31.getMessage());
    }

    @Test
    public void multiLabelClassificationClientSideValidation() {
        // Async
        StepVerifier.create(asyncClientV30.beginMultiLabelClassify(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME,
                null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(MULTI_LABEL_CLASSIFY_ERROR_MESSAGE_30, exception.getMessage());
            });
        StepVerifier.create(asyncClientV31.beginMultiLabelClassify(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME,
                null, null))
            .verifyErrorSatisfies(exception -> {
                assertEquals(IllegalStateException.class, exception.getClass());
                assertEquals(MULTI_LABEL_CLASSIFY_ERROR_MESSAGE_31, exception.getMessage());
            });

        // Sync
        IllegalStateException exception30 = assertThrows(IllegalStateException.class,
            () -> clientV30.beginMultiLabelClassify(dummyDocument, PROJECT_NAME, DEPLOYMENT_NAME, null, null));
        assertEquals(MULTI_LABEL_CLASSIFY_ERROR_MESSAGE_30, exception30.getMessage());

        IllegalStateException exception31 = assertThrows(IllegalStateException.class,
            () -> clientV31.beginMultiLabelClassify(dummyDocument, PROJECT_NAME,  DEPLOYMENT_NAME, null, null));
        assertEquals(MULTI_LABEL_CLASSIFY_ERROR_MESSAGE_31, exception31.getMessage());
    }


    @Test
    public void test() {
        for (int i = 0; i < 5; i++) {
            try {
                throwIfTargetServiceVersionFound(TextAnalyticsServiceVersion.V3_0,
                    Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_0),
                    "a message");
            } catch (IllegalStateException ex) {
                System.out.println("i = " + i);
            }
        }
    }
}
