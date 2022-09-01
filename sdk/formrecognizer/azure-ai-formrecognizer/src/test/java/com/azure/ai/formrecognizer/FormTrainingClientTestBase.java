// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.ai.formrecognizer.training.models.AccountProperties;
import com.azure.ai.formrecognizer.training.models.CopyAuthorization;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelStatus;
import com.azure.ai.formrecognizer.training.models.TrainingDocumentInfo;
import com.azure.ai.formrecognizer.training.models.TrainingStatus;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.ai.formrecognizer.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_RECEIPT_URL;
import static com.azure.ai.formrecognizer.TestUtils.ONE_NANO_DURATION;
import static com.azure.ai.formrecognizer.TestUtils.TEST_DATA_PNG;
import static com.azure.ai.formrecognizer.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class FormTrainingClientTestBase extends TestBase {
    private static final String RESOURCE_ID = "FORM_RECOGNIZER_TARGET_RESOURCE_ID";
    private static final String RESOURCE_REGION = "FORM_RECOGNIZER_TARGET_RESOURCE_REGION";
    private static final String LOCAL_FILE_PATH = "src/test/resources/sample_files/Test/";

    static final String AZURE_FORM_RECOGNIZER_API_KEY = "AZURE_FORM_RECOGNIZER_API_KEY";
    static final String AZURE_FORM_RECOGNIZER_ENDPOINT = "AZURE_FORM_RECOGNIZER_ENDPOINT";
    static final String FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL =
        "FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL";
    static final String FORM_RECOGNIZER_MULTIPAGE_TRAINING_BLOB_CONTAINER_SAS_URL =
        "FORM_RECOGNIZER_MULTIPAGE_TRAINING_BLOB_CONTAINER_SAS_URL";
    static final String FORM_RECOGNIZER_SELECTION_MARK_BLOB_CONTAINER_SAS_URL =
        "FORM_RECOGNIZER_SELECTION_MARK_BLOB_CONTAINER_SAS_URL";
    static final String PREFIX_SUBFOLDER = "subfolder";
    static final String INVALID_PREFIX_FILE_NAME = "XXXXX";

    // Error Code
    static final String COPY_REQUEST_INVALID_TARGET_RESOURCE_REGION_ERROR_CODE = "1002";
    static final String INVALID_MODEL_STATUS_ERROR_CODE = "2012";
    static final String NO_VALID_BLOB_FOUND_ERROR_CODE = "2014";

    Duration durationTestMode;

    /**
     * Use duration of nearly zero value for PLAYBACK test mode, otherwise, use default duration value for LIVE mode.
     */
    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = ONE_NANO_DURATION;
        } else {
            durationTestMode = DEFAULT_POLL_INTERVAL;
        }
    }

    static void validateCopyAuthorizationResult(String expectedResourceId, String expectedResourceRegion,
        CopyAuthorization actualResult) {
        assertNotNull(actualResult.getModelId());
        assertNotNull(actualResult.getAccessToken());
        assertNotNull(actualResult.getExpiresOn());
        assertEquals(expectedResourceRegion, actualResult.getResourceRegion());
        assertEquals(expectedResourceId, actualResult.getResourceId());
    }

    FormTrainingClientBuilder getFormTrainingClientBuilder(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        FormTrainingClientBuilder builder = new FormTrainingClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder;
    }

    private static void validateTrainingDocumentsData(List<TrainingDocumentInfo> actualTrainingDocuments) {
        actualTrainingDocuments.forEach(actualTrainingDocument -> {
            Assertions.assertNotNull(actualTrainingDocument.getName());
            Assertions.assertNotNull(actualTrainingDocument.getModelId());
            Assertions.assertNotNull(actualTrainingDocument.getPageCount());
            Assertions.assertEquals(TrainingStatus.SUCCEEDED, actualTrainingDocument.getStatus());
            Assertions.assertNotNull(actualTrainingDocument.getErrors());
            Assertions.assertEquals(0, actualTrainingDocument.getErrors().size());
        });
    }

    static void validateAccountProperties(AccountProperties actualAccountProperties) {
        assertNotNull(actualAccountProperties.getCustomModelLimit());
        assertNotNull(actualAccountProperties.getCustomModelCount());
    }

    void validateCustomModelData(CustomFormModel actualCustomModel, boolean isLabeled, boolean isComposed) {
        assertEquals(CustomFormModelStatus.READY, actualCustomModel.getModelStatus());
        assertNotNull(actualCustomModel.getTrainingStartedOn());
        assertNotNull(actualCustomModel.getTrainingCompletedOn());
        assertNotNull(actualCustomModel.getModelId());
        assertNotNull(actualCustomModel.getCustomModelProperties());
        if (!isComposed) {
            assertFalse(actualCustomModel.getCustomModelProperties().isComposed());
        }

        actualCustomModel.getSubmodels().forEach(customFormSubmodel -> {
            Assertions.assertNotNull(customFormSubmodel.getFormType());
            Assertions.assertNotNull(customFormSubmodel.getModelId());

            customFormSubmodel.getFields().forEach((label, customFormModelField) -> {
                Assertions.assertNotNull(customFormModelField.getName());
                if (isLabeled) {
                    Assertions.assertNotNull(customFormModelField.getAccuracy());
                } else if (!isComposed) {
                    Assertions.assertNotNull(customFormModelField.getLabel());
                }
            });
        });
        validateTrainingDocumentsData(actualCustomModel.getTrainingDocuments());
    }

    @Test
    abstract void getFormRecognizerClientAndValidate(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void getCustomModelNullModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void getCustomModelLabeled(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void getCustomModelUnlabeled(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void getCustomModelInvalidModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void getCustomModelWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void validGetAccountProperties(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void validGetAccountPropertiesWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void deleteModelInvalidModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void deleteModelValidModelIdWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void listCustomModels(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingNullInput(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginCopy(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginCopyInvalidRegion(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void copyAuthorization(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingInvalidModelStatus(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingWithTrainingLabelsForJPGTrainingSet(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingWithoutTrainingLabelsForJPGTrainingSet(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingWithTrainingLabelsForMultiPagePDFTrainingSet(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingWithoutTrainingLabelsForMultiPagePDFTrainingSet(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingWithoutTrainingLabelsExcludeSubfolderWithPrefixName(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingWithoutTrainingLabelsIncludeSubfolderWithPrefixName(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingWithoutTrainingLabelsIncludeSubfolderWithNonExistPrefixName(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingWithoutTrainingLabelsExcludeSubfolderWithNonExistPrefixName(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    void getCustomModelInvalidModelIdRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_MODEL_ID);
    }

    void beginTrainingLabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingFilesContainerUrl(), true);
    }

    void beginTrainingUnlabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingFilesContainerUrl(), false);
    }

    void beginCopyRunner(BiConsumer<String, String> testRunner) {
        testRunner.accept(getTargetResourceId(), getTargetResourceRegion());
    }

    void beginCopyInvalidRegionRunner(BiConsumer<String, String> testRunner) {
        testRunner.accept(getTargetResourceId(), "RESOURCE_REGION");
    }

    void beginCopyIncorrectRegionRunner(BiConsumer<String, String> testRunner) {
        testRunner.accept(getTargetResourceId(), "westus");
    }

    void beginTrainingInvalidModelStatusRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(INVALID_RECEIPT_URL, false);
    }

    void beginTrainingMultipageRunner(Consumer<String> testRunner) {
        testRunner.accept(getMultipageTrainingFilesUrl());
    }

    /**
     * Get the target resource Identifier based on the test running mode.
     *
     * @return the target resource Identifier
     */
    String getTargetResourceId() {
        return interceptorManager.isPlaybackMode() ? "resourceIdInPlayback"
            : Configuration.getGlobalConfiguration().get(RESOURCE_ID);
    }


    /**
     * Get the target resource region based on the test running mode.
     *
     * @return the target resource region
     */
    String getTargetResourceRegion() {
        return interceptorManager.isPlaybackMode() ? "resourceRegionInPlayback"
            : Configuration.getGlobalConfiguration().get(RESOURCE_REGION);
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_ENDPOINT);
    }

    void blankPdfDataRunner(BiConsumer<InputStream, Long> testRunner) {
        final long fileLength = new File(LOCAL_FILE_PATH + BLANK_PDF).length();

        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream(TEST_DATA_PNG.getBytes(StandardCharsets.UTF_8)), fileLength);
        } else {
            try {
                testRunner.accept(new FileInputStream(LOCAL_FILE_PATH + BLANK_PDF), fileLength);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Local file not found.", e);
            }
        }
    }

    private String getTrainingFilesContainerUrl() {
        return interceptorManager.isPlaybackMode()
            ? "https://isPlaybackmode"
            : Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL);
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    private String getMultipageTrainingFilesUrl() {
        return interceptorManager.isPlaybackMode()
            ? "https://isPlaybackmode"
            : Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_MULTIPAGE_TRAINING_BLOB_CONTAINER_SAS_URL);
    }
}
