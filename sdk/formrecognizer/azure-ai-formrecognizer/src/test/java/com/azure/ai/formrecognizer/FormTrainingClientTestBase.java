// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelField;
import com.azure.ai.formrecognizer.models.CustomFormSubModel;
import com.azure.ai.formrecognizer.models.FormRecognizerError;
import com.azure.ai.formrecognizer.models.TrainingDocumentInfo;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.FormRecognizerClientBuilder.OCP_APIM_SUBSCRIPTION_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class FormTrainingClientTestBase extends TestBase {
    private static final String AZURE_FORM_RECOGNIZER_API_KEY = "AZURE_FORM_RECOGNIZER_API_KEY";
    private static final String NAME = "name";
    private static final String FORM_RECOGNIZER_PROPERTIES = "azure-ai-formrecognizer.properties";
    private static final String VERSION = "version";

    private final HttpLogOptions httpLogOptions = new HttpLogOptions();
    private final Map<String, String> properties = CoreUtils.getProperties(FORM_RECOGNIZER_PROPERTIES);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

    static void validateCustomModel(CustomFormModel expectedModel, CustomFormModel actualCustomModel) {
        assertNotNull(actualCustomModel.getModelId());
        assertEquals(expectedModel.getModelStatus(), actualCustomModel.getModelStatus());
        validateErrors(expectedModel.getModelError(), actualCustomModel.getModelError());
        assertNotNull(actualCustomModel.getCreatedOn());
        assertNotNull(actualCustomModel.getLastUpdatedOn());
        validateSubModels(expectedModel.getSubModels(), actualCustomModel.getSubModels());
        validateTrainingDocuments(expectedModel.getTrainingDocuments(), actualCustomModel.getTrainingDocuments());
    }

    static void validateAccountProperties(AccountProperties expectedAccountProperties,
        AccountProperties actualAccountProperties) {
        assertEquals(expectedAccountProperties.getLimit(), actualAccountProperties.getLimit());
        assertNotNull(actualAccountProperties.getCount());
    }

    private static void validateTrainingDocuments(List<TrainingDocumentInfo> expectedTrainingDocuments,
        List<TrainingDocumentInfo> actualTrainingDocuments) {
        List<TrainingDocumentInfo> actualTrainingList = actualTrainingDocuments.stream().collect(Collectors.toList());
        List<TrainingDocumentInfo> expectedTrainingList =
            expectedTrainingDocuments.stream().collect(Collectors.toList());
        assertEquals(expectedTrainingList.size(), actualTrainingList.size());
        for (int i = 0; i < actualTrainingList.size(); i++) {
            TrainingDocumentInfo expectedTrainingDocument = expectedTrainingList.get(i);
            TrainingDocumentInfo actualTrainingDocument = actualTrainingList.get(i);
            assertEquals(expectedTrainingDocument.getName(), actualTrainingDocument.getName());
            assertEquals(expectedTrainingDocument.getPageCount(), actualTrainingDocument.getPageCount());
            assertEquals(expectedTrainingDocument.getTrainingStatus(), actualTrainingDocument.getTrainingStatus());
            validateErrors(expectedTrainingDocument.getDocumentErrors(), actualTrainingDocument.getDocumentErrors());
        }
    }

    private static void validateErrors(List<FormRecognizerError> expectedErrors,
        List<FormRecognizerError> actualErrors) {
        if (expectedErrors != null && actualErrors != null) {
            List<FormRecognizerError> actualErrorList = actualErrors.stream().collect(Collectors.toList());
            List<FormRecognizerError> expectedErrorList = expectedErrors.stream().collect(Collectors.toList());
            assertEquals(expectedErrorList.size(), actualErrorList.size());
            for (int i = 0; i < actualErrorList.size(); i++) {
                FormRecognizerError expectedError = expectedErrorList.get(i);
                FormRecognizerError actualError = actualErrorList.get(i);
                assertEquals(expectedError.getCode(), actualError.getCode());
                assertEquals(expectedError.getMessage(), actualError.getMessage());
            }
        }
    }

    private static void validateSubModels(IterableStream<CustomFormSubModel> expectedSubModels,
        IterableStream<CustomFormSubModel> actualSubModels) {
        List<CustomFormSubModel> actualSubModelList = actualSubModels.stream().collect(Collectors.toList());
        List<CustomFormSubModel> expectedSubModelList = expectedSubModels.stream().collect(Collectors.toList());
        assertEquals(expectedSubModelList.size(), actualSubModelList.size());
        for (int i = 0; i < expectedSubModelList.size(); i++) {
            CustomFormSubModel expectedSubModel = expectedSubModelList.get(i);
            CustomFormSubModel actualSubModel = actualSubModelList.get(i);
            assertNotNull(actualSubModel.getFormType());
            assertEquals(expectedSubModel.getAccuracy(), actualSubModel.getAccuracy());
            validateModelFieldMap(expectedSubModel.getFieldMap(), actualSubModel.getFieldMap());
        }
    }

    private static void validateModelFieldMap(Map<String, CustomFormModelField> expectedFieldMap, Map<String,
        CustomFormModelField> actualFieldMap) {
        assertEquals(expectedFieldMap.size(), actualFieldMap.size());
        expectedFieldMap.entrySet().stream().allMatch(stringFieldEntry ->
            stringFieldEntry.getValue().equals(actualFieldMap.get(stringFieldEntry.getKey())));
    }

    @Test
    abstract void getCustomModelInvalidStatusModel();

    @Test
    abstract void getCustomModelNullModelId();

    @Test
    abstract void getCustomModelLabeled();

    @Test
    abstract void getCustomModelUnlabeled();

    @Test
    abstract void getCustomModelInvalidModelId();

    @Test
    abstract void getCustomModelWithResponse();

    @Test
    abstract void validGetAccountProperties();

    @Test
    abstract void validGetAccountPropertiesWithResponse();

    @Test
    abstract void deleteModelInvalidModelId();

    @Test
    abstract void deleteModelValidModelIdWithResponse();

    // void getCustomModelValidModelIdRunner(Consumer<String> testRunner) {
    //     testRunner.accept(TestUtils.VALID_MODEL_ID);
    // }

    @Test
    abstract void getModelInfos();

    // void getCustomModelWithResponseRunner(Consumer<String> testRunner) {
    //     testRunner.accept(TestUtils.LABELED_MODEL_ID);
    // }
    //
    // void getCustomModelLabeledRunner(BiConsumer<String, Boolean> testRunner) {
    //     testRunner.accept(createStorageAndGenerateSas("src/test/resources/sample_files/Train_Labeled"), true);
    // }

    @Test
    abstract void getModelInfosWithContext();

    @Test
    abstract void beginTrainingNullInput();

    @Test
    abstract void beginTrainingLabeledResult();

    @Test
    abstract void beginTrainingUnlabeledResult();

    void getCustomModelInvalidStatusModelRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_STATUS_MODEL_ID);
    }

    void getCustomModelInvalidModelIdRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_MODEL_ID);
    }

    void beginTrainingLabeledResultRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(createStorageAndGenerateSas("src/test/resources/sample_files/Train_Labeled"), true);
    }

    void beginTrainingUnlabeledResultRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(createStorageAndGenerateSas("src/test/resources/sample_files/Train"), false);
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        // TODO: #9252 AAD not supported by service
        // TokenCredential credential = null;
        AzureKeyCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new AzureKeyCredential(getApiKey());
        }

        HttpClient httpClient;
        Configuration buildConfiguration = Configuration.getGlobalConfiguration().clone();

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        if (credential != null) {
            policies.add(new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY, credential));
        }

        policies.add(new RetryPolicy());

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
        } else {
            httpClient = new NettyAsyncHttpClientBuilder().wiretap(true).build();
        }
        policies.add(interceptorManager.getRecordPolicy());

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
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
            : Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_ENDPOINT");
    }

    private String createStorageAndGenerateSas(String folderPath) {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            String accountName = Configuration.getGlobalConfiguration().get("PRIMARY_STORAGE_ACCOUNT_NAME");
            String accountKey = Configuration.getGlobalConfiguration().get("PRIMARY_STORAGE_ACCOUNT_KEY");
            StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
            String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);
            BlobServiceClient storageClient =
                new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();
            BlobContainerClient blobContainerClient =
                storageClient.getBlobContainerClient(this.testResourceNamer.randomName("testFr", 16));
            blobContainerClient.create();
            BlockBlobClient blobClient;
            File folder = new File(folderPath);
            File[] listOfFiles = folder.listFiles();
            for (File listOfFile : listOfFiles) {
                InputStream dataStream = null;
                try {
                    dataStream = new ByteArrayInputStream(Files.readAllBytes(listOfFile.toPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                blobClient = blobContainerClient.getBlobClient(listOfFile.getName()).getBlockBlobClient();
                blobClient.upload(dataStream, listOfFile.length());
            }
            BlobContainerSasPermission blob = new BlobContainerSasPermission()
                .setAddPermission(true)
                .setCreatePermission(true)
                .setReadPermission(true)
                .setListPermission(true);
            String sasToken = blobContainerClient.generateSas(
                new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1), blob)
            );
            return blobContainerClient.getBlobContainerUrl() + "?" + sasToken;
        }
    }
}
