// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.FormFieldsReport;
import com.azure.ai.formrecognizer.implementation.models.Model;
import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelField;
import com.azure.ai.formrecognizer.models.CustomFormSubModel;
import com.azure.ai.formrecognizer.models.ErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerError;
import com.azure.ai.formrecognizer.models.TrainingDocumentInfo;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.TestUtils.getSerializerAdapter;
import static com.azure.ai.formrecognizer.implementation.models.ModelStatus.READY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class FormTrainingClientTestBase extends TestBase {
    static final String FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL =
        "FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL";
    static final String FORM_RECOGNIZER_TESTING_BLOB_CONTAINER_SAS_URL =
        "FORM_RECOGNIZER_TESTING_BLOB_CONTAINER_SAS_URL";
    static final String AZURE_FORM_RECOGNIZER_API_KEY = "AZURE_FORM_RECOGNIZER_API_KEY";
    static final String NAME = "name";
    static final String FORM_RECOGNIZER_PROPERTIES = "azure-ai-formrecognizer.properties";
    static final String VERSION = "version";
    static final String AZURE_FORM_RECOGNIZER_ENDPOINT = "AZURE_FORM_RECOGNIZER_ENDPOINT";
    private final HttpLogOptions httpLogOptions = new HttpLogOptions();
    private final Map<String, String> properties = CoreUtils.getProperties(FORM_RECOGNIZER_PROPERTIES);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

    private static void validateTrainingDocumentsData(List<com.azure.ai.formrecognizer.implementation.models.TrainingDocumentInfo> expectedTrainingDocuments,
        List<TrainingDocumentInfo> actualTrainingDocuments) {
        assertEquals(expectedTrainingDocuments.size(), actualTrainingDocuments.size());
        for (int i = 0; i < actualTrainingDocuments.size(); i++) {
            com.azure.ai.formrecognizer.implementation.models.TrainingDocumentInfo expectedTrainingDocument =
                expectedTrainingDocuments.get(i);
            TrainingDocumentInfo actualTrainingDocument = actualTrainingDocuments.get(i);
            assertEquals(expectedTrainingDocument.getDocumentName(), actualTrainingDocument.getName());
            assertEquals(expectedTrainingDocument.getPages(), actualTrainingDocument.getPageCount());
            assertEquals(expectedTrainingDocument.getStatus().toString(),
                actualTrainingDocument.getTrainingStatus().toString());
            validateErrorData(expectedTrainingDocument.getErrors(), actualTrainingDocument.getDocumentErrors());
        }
    }

    private static void validateErrorData(List<ErrorInformation> expectedErrors,
        List<FormRecognizerError> actualErrors) {
        if (expectedErrors != null && actualErrors != null) {
            assertEquals(expectedErrors.size(), actualErrors.size());
            for (int i = 0; i < actualErrors.size(); i++) {
                ErrorInformation expectedError = expectedErrors.get(i);
                FormRecognizerError actualError = actualErrors.get(i);
                assertEquals(expectedError.getCode(), actualError.getCode());
                assertEquals(expectedError.getMessage(), actualError.getMessage());
            }
        }
    }

    static void validateAccountProperties(AccountProperties expectedAccountProperties,
        AccountProperties actualAccountProperties) {
        assertEquals(expectedAccountProperties.getLimit(), actualAccountProperties.getLimit());
        assertNotNull(actualAccountProperties.getCount());
    }

    /**
     * Deserialize test data from service.
     *
     * @return the deserialized raw response test data
     */
    static <T> T deserializeRawResponse(SerializerAdapter serializerAdapter, NetworkCallRecord record, Class<T> clazz) {
        try {
            return serializerAdapter.deserialize(record.getResponse().get("Body"),
                clazz, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize service response.");
        }
    }

    void validateCustomModelData(CustomFormModel actualCustomModel, boolean isLabeled) {
        Model modelRawResponse = getRawModelResponse();
        assertEquals(modelRawResponse.getModelInfo().getStatus().toString(),
            actualCustomModel.getModelStatus().toString());
        validateErrorData(modelRawResponse.getTrainResult().getErrors(), actualCustomModel.getModelError());
        assertNotNull(actualCustomModel.getCreatedOn());
        assertNotNull(actualCustomModel.getLastUpdatedOn());
        validateTrainingDocumentsData(modelRawResponse.getTrainResult().getTrainingDocuments(),
            actualCustomModel.getTrainingDocuments());
        final List<CustomFormSubModel> subModelList =
            actualCustomModel.getSubModels().stream().collect(Collectors.toList());
        if (isLabeled) {
            final List<FormFieldsReport> fields = modelRawResponse.getTrainResult().getFields();
            for (final FormFieldsReport expectedField : fields) {
                final CustomFormModelField actualFormField =
                    subModelList.get(0).getFieldMap().get(expectedField.getFieldName());
                assertEquals(expectedField.getFieldName(), actualFormField.getName());
                assertEquals(expectedField.getAccuracy(), actualFormField.getAccuracy());

            }
            assertTrue(subModelList.get(0).getFormType().startsWith("form-"));
            assertEquals(modelRawResponse.getTrainResult().getAverageModelAccuracy(),
                subModelList.get(0).getAccuracy());
        } else {
            modelRawResponse.getKeys().getClusters().forEach((clusterId, fields) -> {
                assertTrue(subModelList.get(Integer.parseInt(clusterId)).getFormType().endsWith(clusterId));
                subModelList.get(Integer.parseInt(clusterId)).getFieldMap().values().forEach(customFormModelField ->
                    assertTrue(fields.contains(customFormModelField.getLabel())));
            });
        }
    }

    /**
     * Prepare the expected test data from service raw response.
     *
     * @return the {@link Model} test data
     */
    private Model getRawModelResponse() {
        final SerializerAdapter serializerAdapter = getSerializerAdapter();
        final NetworkCallRecord networkCallRecord =
            interceptorManager.getRecordedData().findFirstAndRemoveNetworkCall(record -> {
                Model rawModelResponse = deserializeRawResponse(serializerAdapter, record, Model.class);
                return rawModelResponse != null && rawModelResponse.getModelInfo().getStatus() == READY;
            });
        interceptorManager.getRecordedData().addNetworkCall(networkCallRecord);
        return deserializeRawResponse(serializerAdapter, networkCallRecord, Model.class);
    }

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
    abstract void getModelInfos(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void getModelInfosWithContext(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingNullInput(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingLabeledResult(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void beginTrainingUnlabeledResult(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    void getCustomModelInvalidModelIdRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_MODEL_ID);
    }

    void beginTrainingLabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingSasUri(), true);
    }

    void beginTrainingUnlabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingSasUri(), false);
    }

    /**
     * Get the string of API key value based on what running mode is on.
     *
     * @return the API key string
     */
    String getApiKey() {
        return interceptorManager.isPlaybackMode() ? "apiKeyInPlayback"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_API_KEY);
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_ENDPOINT);
    }

    private String getTrainingSasUri() {
        return interceptorManager.isPlaybackMode()
            ? "https://isPlaybackmode"
            : Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL);
    }

    private String getTestingSasUri() {
        return interceptorManager.isPlaybackMode()
            ? "https://isPlaybackmode"
            : Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_TESTING_BLOB_CONTAINER_SAS_URL);
    }
}
