// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training;

import com.azure.ai.formrecognizer.FormRecognizerClient;
import com.azure.ai.formrecognizer.FormRecognizerClientBuilder;
import com.azure.ai.formrecognizer.implementation.models.ModelStatus;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.training.models.AccountProperties;
import com.azure.ai.formrecognizer.training.models.CopyAuthorization;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;
import java.util.List;

/**
 * <p>This class provides a synchronous client to connect to the Form Recognizer Azure Cognitive Service.</p>
 * <p>This client provides synchronous methods to:</p>
 *
 * <ol>
 *     <li>Train a custom model: Train a custom model to analyze and extract data from forms and documents specific to
 *     your business using the {@link com.azure.ai.formrecognizer.training.FormTrainingClient#beginTraining(String, boolean) beginTraining}
 *     method.</li>
 *     <li>Copy custom model: Copy a custom Form Recognizer model to a target Form Recognizer resource using the
 *     {@link com.azure.ai.formrecognizer.training.FormTrainingClient#beginCopyModel(String, CopyAuthorization) beginCopyModel}
 *     method.</li>
 *     <li>List custom models: Get information about all custom models using the
 *     {@link com.azure.ai.formrecognizer.training.FormTrainingClient#getCustomModel(String) getCustomModel} and
 *     {@link FormTrainingClient#listCustomModels() listCustomModels} methods respectively.</li>
 *     <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *     operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
 *
 * <p><strong>Note: </strong>This client only supports
 * {@link com.azure.ai.formrecognizer.FormRecognizerServiceVersion#V2_1} and lower.
 * Recommended to use a newer service version,
 * {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient} and
 * {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient}.</p>
 *
 * <p><strong>Refer to the
 * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/migration-guide.md">Migration guide</a> to use API versions 2022-08-31 and up.</strong></p>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Form Recognizer.
 * {@link com.azure.ai.formrecognizer.training.FormTrainingClient} is the synchronous service client and
 * {@link com.azure.ai.formrecognizer.training.FormTrainingAsyncClient} is the asynchronous service client. The examples
 * shown in this document use a credential object named DefaultAzureCredential for authentication, which is appropriate
 * for most scenarios, including local development and production environments. Additionally, we recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a FormTrainingClient with DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.ai.formrecognizer.training.FormTrainingClient}, using the `DefaultAzureCredentialBuilder` to
 * configure it.</p>
 *
 * <!-- src_embed readme-sample-createFormTrainingClientWithAAD -->
 * <pre>
 * FormTrainingClient client = new FormTrainingClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createFormTrainingClientWithAAD  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.core.credential.AzureKeyCredential AzureKeyCredential} for client creation.</p>
 *
 * <!-- src_embed readme-sample-createFormTrainingClient -->
 * <pre>
 * FormTrainingClient formTrainingClient = new FormTrainingClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createFormTrainingClient  -->
 *
 * @see com.azure.ai.formrecognizer.training
 * @see FormTrainingClientBuilder
 * @see FormTrainingAsyncClient
 */
@ServiceClient(builder = FormTrainingClientBuilder.class)
public final class FormTrainingClient {

    private final FormTrainingAsyncClient client;

    /**
     * Create a {@link FormTrainingClient} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link FormTrainingClientBuilder#pipeline http pipeline}.
     *
     * @param formTrainingAsyncClient The {@link FormTrainingAsyncClient} that the client routes its request through.
     */
    FormTrainingClient(FormTrainingAsyncClient formTrainingAsyncClient) {
        this.client = formTrainingAsyncClient;
    }

    /**
     * Creates a new {@link FormRecognizerClient} object. The new {@link FormTrainingClient}
     * uses the same request policy pipeline as the {@link FormTrainingClient}.
     *
     * @return A new {@link FormRecognizerClient} object.
     */
    public FormRecognizerClient getFormRecognizerClient() {
        return new FormRecognizerClientBuilder().endpoint(client.getEndpoint()).pipeline(client.getHttpPipeline())
            .buildClient();
    }

    /**
     * Create and train a custom model.
     * <p>Models are trained using documents that are of the following content
     * type - 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff'.
     * Other type of content is ignored.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginTraining#string-boolean -->
     * <pre>
     * String trainingFilesUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * boolean useTrainingLabels = true;
     * CustomFormModel customFormModel =
     *     formTrainingClient.beginTraining&#40;trainingFilesUrl, useTrainingLabels&#41;.getFinalResult&#40;&#41;;
     * System.out.printf&#40;&quot;Model Id: %s%n&quot;, customFormModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Status: %s%n&quot;, customFormModel.getModelStatus&#40;&#41;&#41;;
     * customFormModel.getSubmodels&#40;&#41;
     *     .forEach&#40;customFormSubmodel -&gt; customFormSubmodel.getFields&#40;&#41;
     *         .forEach&#40;&#40;key, customFormModelField&#41; -&gt;
     *             System.out.printf&#40;&quot;Form Type: %s Field Text: %s Field Accuracy: %f%n&quot;,
     *                 key, customFormModelField.getName&#40;&#41;, customFormModelField.getAccuracy&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginTraining#string-boolean -->
     *
     * @param trainingFilesUrl an externally accessible Azure storage blob container Uri (preferably a Shared Access
     * Signature Uri).
     * For instructions on setting up forms for training in an Azure Storage Blob Container, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data">here</a>.
     *
     * @param useTrainingLabels boolean to specify the use of labeled files for training the model.
     *
     * @return A {@link SyncPoller} that polls the training model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link CustomFormModel custom form model}.
     * @throws FormRecognizerException If training fails and model with {@link ModelStatus#INVALID} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, CustomFormModel> beginTraining(String trainingFilesUrl,
        boolean useTrainingLabels) {
        return beginTraining(trainingFilesUrl, useTrainingLabels, null, Context.NONE);
    }

    /**
     * Create and train a custom model.
     * Models are trained using documents that are of the following content type - 'application/pdf',
     * 'image/jpeg', 'image/png', 'image/tiff'.
     * Other type of content is ignored.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginTraining#string-boolean-Options-Context -->
     * <pre>
     * String trainingFilesUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * TrainingFileFilter trainingFileFilter = new TrainingFileFilter&#40;&#41;.setSubfoldersIncluded&#40;false&#41;.setPrefix&#40;&quot;Invoice&quot;&#41;;
     * boolean useTrainingLabels = true;
     *
     * CustomFormModel customFormModel = formTrainingClient.beginTraining&#40;trainingFilesUrl, useTrainingLabels,
     *     new TrainingOptions&#40;&#41;
     *         .setTrainingFileFilter&#40;trainingFileFilter&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;, Context.NONE&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model Id: %s%n&quot;, customFormModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Status: %s%n&quot;, customFormModel.getModelStatus&#40;&#41;&#41;;
     * customFormModel.getSubmodels&#40;&#41;
     *     .forEach&#40;customFormSubmodel -&gt; customFormSubmodel.getFields&#40;&#41;
     *         .forEach&#40;&#40;key, customFormModelField&#41; -&gt;
     *             System.out.printf&#40;&quot;Form Type: %s Field Text: %s Field Accuracy: %f%n&quot;,
     *                 key, customFormModelField.getName&#40;&#41;, customFormModelField.getAccuracy&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginTraining#string-boolean-Options-Context -->
     *
     * @param trainingFilesUrl an externally accessible Azure storage blob container Uri (preferably a
     * Shared Access Signature Uri).
     * For instructions on setting up forms for training in an Azure Storage Blob Container, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data">here</a>.
     *
     * @param useTrainingLabels boolean to specify the use of labeled files for training the model.
     * @param trainingOptions The additional configurable {@link TrainingOptions options}
     * that may be passed when training a model.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the training model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link CustomFormModel custom form model}.
     * @throws FormRecognizerException If training fails and model with {@link ModelStatus#INVALID} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, CustomFormModel> beginTraining(String trainingFilesUrl,
        boolean useTrainingLabels,
        TrainingOptions trainingOptions, Context context) {
        return client.beginTraining(trainingFilesUrl, useTrainingLabels,
            trainingOptions, context).getSyncPoller();
    }

    /**
     * Get detailed information for a specified custom model id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getCustomModel#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * CustomFormModel customFormModel = formTrainingClient.getCustomModel&#40;modelId&#41;;
     * System.out.printf&#40;&quot;Model Id: %s%n&quot;, customFormModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Status: %s%n&quot;, customFormModel.getModelStatus&#40;&#41;&#41;;
     * customFormModel.getSubmodels&#40;&#41;
     *     .forEach&#40;customFormSubmodel -&gt; customFormSubmodel.getFields&#40;&#41;
     *         .forEach&#40;&#40;key, customFormModelField&#41; -&gt;
     *             System.out.printf&#40;&quot;Form Type: %s Field Text: %s Field Accuracy: %f%n&quot;,
     *                 key, customFormModelField.getName&#40;&#41;, customFormModelField.getAccuracy&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getCustomModel#string -->
     *
     * @param modelId The UUID string format model identifier.
     *
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CustomFormModel getCustomModel(String modelId) {
        return getCustomModelWithResponse(modelId, Context.NONE).getValue();
    }

    /**
     * Get detailed information for a specified custom model id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getCustomModelWithResponse#string-Context -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * Response&lt;CustomFormModel&gt; response = formTrainingClient.getCustomModelWithResponse&#40;modelId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * CustomFormModel customFormModel = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Model Id: %s%n&quot;, customFormModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Status: %s%n&quot;, customFormModel.getModelStatus&#40;&#41;&#41;;
     * customFormModel.getSubmodels&#40;&#41;
     *     .forEach&#40;customFormSubmodel -&gt; customFormSubmodel.getFields&#40;&#41;
     *         .forEach&#40;&#40;key, customFormModelField&#41; -&gt;
     *             System.out.printf&#40;&quot;Field: %s Field Text: %s Field Accuracy: %f%n&quot;,
     *                 key, customFormModelField.getName&#40;&#41;, customFormModelField.getAccuracy&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getCustomModelWithResponse#string-Context -->
     *
     * @param modelId The UUID string format model identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CustomFormModel> getCustomModelWithResponse(String modelId, Context context) {
        return client.getCustomModelWithResponse(modelId, context).block();
    }

    /**
     * Get account information of the form recognizer account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getAccountProperties -->
     * <pre>
     * AccountProperties accountProperties = formTrainingClient.getAccountProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Max number of models that can be trained for this account: %d%n&quot;,
     *     accountProperties.getCustomModelLimit&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Current count of trained custom models: %d%n&quot;, accountProperties.getCustomModelCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getAccountProperties -->
     *
     * @return The requested account information of the form recognizer account.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccountProperties getAccountProperties() {
        return getAccountPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get account information of the form recognizer account with an Http response and a
     * specified {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getAccountPropertiesWithResponse#Context -->
     * <pre>
     * Response&lt;AccountProperties&gt; response = formTrainingClient.getAccountPropertiesWithResponse&#40;Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * AccountProperties accountProperties = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Max number of models that can be trained for this account: %s%n&quot;,
     *     accountProperties.getCustomModelLimit&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Current count of trained custom models: %d%n&quot;, accountProperties.getCustomModelCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getAccountPropertiesWithResponse#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The requested account information of the form recognizer account.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccountProperties> getAccountPropertiesWithResponse(Context context) {
        return client.getAccountPropertiesWithResponse(context).block();
    }

    /**
     * Deletes the specified custom model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.deleteModel#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * formTrainingClient.deleteModel&#40;modelId&#41;;
     * System.out.printf&#40;&quot;Model Id: %s is deleted.%n&quot;, modelId&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.deleteModel#string -->
     *
     * @param modelId The UUID string format model identifier.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteModel(String modelId) {
        deleteModelWithResponse(modelId, Context.NONE);
    }

    /**
     * Deletes the specified custom model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.deleteModelWithResponse#string-Context -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * Response&lt;Void&gt; response = formTrainingClient.deleteModelWithResponse&#40;modelId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Id: %s is deleted.%n&quot;, modelId&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.deleteModelWithResponse#string-Context -->
     *
     * @param modelId The UUID string format model identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing  status code and HTTP headers.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteModelWithResponse(String modelId, Context context) {
        return client.deleteModelWithResponse(modelId, context).block();
    }

    /**
     * List information for each model on the form recognizer account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.listCustomModels -->
     * <pre>
     * formTrainingClient.listCustomModels&#40;&#41;
     *     .forEach&#40;customModel -&gt;
     *         System.out.printf&#40;&quot;Model Id: %s, Model status: %s, Training started on: %s, Training completed on: %s.%n&quot;,
     *             customModel.getModelId&#40;&#41;,
     *             customModel.getStatus&#40;&#41;,
     *             customModel.getTrainingStartedOn&#40;&#41;,
     *             customModel.getTrainingCompletedOn&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.listCustomModels -->
     *
     * @return {@link PagedIterable} of {@link CustomFormModelInfo} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CustomFormModelInfo> listCustomModels() {
        return new PagedIterable<>(client.listCustomModels(Context.NONE));
    }

    /**
     * List information for each model on the form recognizer account with an Http response and a specified
     * {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.listCustomModels#Context -->
     * <pre>
     * formTrainingClient.listCustomModels&#40;Context.NONE&#41;
     *     .forEach&#40;customModel -&gt;
     *         System.out.printf&#40;&quot;Model Id: %s, Model status: %s, Training started on: %s, Training completed on: %s.%n&quot;,
     *             customModel.getModelId&#40;&#41;,
     *             customModel.getStatus&#40;&#41;,
     *             customModel.getTrainingStartedOn&#40;&#41;,
     *             customModel.getTrainingCompletedOn&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.listCustomModels#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedIterable} of {@link CustomFormModelInfo} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CustomFormModelInfo> listCustomModels(Context context) {
        return new PagedIterable<>(client.listCustomModels(context));
    }

    /**
     * Copy a custom model stored in this resource (the source) to the user specified target Form Recognizer resource.
     *
     * <p>This should be called with the source Form Recognizer resource (with the model that is intended to be copied).
     * The target parameter should be supplied from the target resource's output from
     * {@link FormTrainingAsyncClient#getCopyAuthorization(String, String)} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingAsyncClient.beginCopyModel#string-copyAuthorization -->
     * <pre>
     * String resourceId = &quot;target-resource-Id&quot;;
     * String resourceRegion = &quot;target-resource-region&quot;;
     * String copyModelId = &quot;copy-model-Id&quot;;
     * formTrainingAsyncClient.getCopyAuthorization&#40;resourceId, resourceRegion&#41;
     *     .flatMapMany&#40;copyAuthorization -&gt; formTrainingAsyncClient.beginCopyModel&#40;copyModelId, copyAuthorization&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;customFormModelInfo -&gt;
     *         System.out.printf&#40;&quot;Copied model has model Id: %s, model status: %s, training started on: %s,&quot;
     *             + &quot; training completed on: %s.%n&quot;,
     *         customFormModelInfo.getModelId&#40;&#41;,
     *         customFormModelInfo.getStatus&#40;&#41;,
     *         customFormModelInfo.getTrainingStartedOn&#40;&#41;,
     *         customFormModelInfo.getTrainingCompletedOn&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingAsyncClient.beginCopyModel#string-copyAuthorization -->
     *
     * @param modelId Model identifier of the model to copy to the target Form Recognizer resource
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link FormTrainingClient#getCopyAuthorization(String, String)}
     *
     * @return A {@link SyncPoller} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, CustomFormModelInfo> beginCopyModel(String modelId,
        CopyAuthorization target) {
        return beginCopyModel(modelId, target, null, Context.NONE);
    }

    /**
     * Copy a custom model stored in this resource (the source) to the user specified target Form Recognizer resource.
     *
     * <p>This should be called with the source Form Recognizer resource (with the model that is intended to be copied).
     * The target parameter should be supplied from the target resource's output from
     * {@link FormTrainingClient#getCopyAuthorization(String, String)} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginCopyModel#string-copyAuthorization-Duration-Context -->
     * <pre>
     * &#47;&#47; The resource to copy model to
     * String resourceId = &quot;target-resource-Id&quot;;
     * String resourceRegion = &quot;target-resource-region&quot;;
     * &#47;&#47; The Id of the model to be copied
     * String copyModelId = &quot;copy-model-Id&quot;;
     *
     * CopyAuthorization copyAuthorization = targetFormTrainingClient.getCopyAuthorization&#40;resourceId,
     *     resourceRegion&#41;;
     * formTrainingClient.beginCopyModel&#40;copyModelId, copyAuthorization, Duration.ofSeconds&#40;5&#41;, Context.NONE&#41;
     *     .waitForCompletion&#40;&#41;;
     * CustomFormModel modelCopy = targetFormTrainingClient.getCustomModel&#40;copyAuthorization.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Copied model has model Id: %s, model status: %s, was requested on: %s,&quot;
     *         + &quot; transfer completed on: %s.%n&quot;,
     *     modelCopy.getModelId&#40;&#41;,
     *     modelCopy.getModelStatus&#40;&#41;,
     *     modelCopy.getTrainingStartedOn&#40;&#41;,
     *     modelCopy.getTrainingCompletedOn&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginCopyModel#string-copyAuthorization-Duration-Context -->
     *
     * @param modelId Model identifier of the model to copy to the target Form Recognizer resource
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link FormTrainingClient#getCopyAuthorization(String, String)}
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, CustomFormModelInfo> beginCopyModel(String modelId,
        CopyAuthorization target, Duration pollInterval, Context context) {
        return client.beginCopyModel(modelId, target, pollInterval, context).getSyncPoller();
    }

    /**
     * Generate authorization for copying a custom model into the target Form Recognizer resource.
     *
     * @param resourceId Azure Resource Id of the target Form Recognizer resource where the model will be copied to.
     * This information can be found in the 'Properties' section of the Form Recognizer resource in the Azure Portal.
     * @param resourceRegion Location of the target Form Recognizer resource. A valid Azure region name supported
     * by Cognitive Services. This information can be found in the 'Keys and Endpoint' section of the Form Recognizer
     * resource in the Azure Portal.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getCopyAuthorization#string-string -->
     * <pre>
     * String resourceId = &quot;target-resource-Id&quot;;
     * String resourceRegion = &quot;target-resource-region&quot;;
     * CopyAuthorization copyAuthorization = formTrainingClient.getCopyAuthorization&#40;resourceId, resourceRegion&#41;;
     * System.out.printf&#40;&quot;Copy Authorization for model id: %s, access token: %s, expiration time: %s, &quot;
     *         + &quot;target resource Id; %s, target resource region: %s%n&quot;,
     *     copyAuthorization.getModelId&#40;&#41;,
     *     copyAuthorization.getAccessToken&#40;&#41;,
     *     copyAuthorization.getExpiresOn&#40;&#41;,
     *     copyAuthorization.getResourceId&#40;&#41;,
     *     copyAuthorization.getResourceRegion&#40;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getCopyAuthorization#string-string -->
     *
     * @return The {@link CopyAuthorization}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CopyAuthorization getCopyAuthorization(String resourceId, String resourceRegion) {
        return getCopyAuthorizationWithResponse(resourceId, resourceRegion, Context.NONE).getValue();
    }

    /**
     * Generate authorization for copying a custom model into the target Form Recognizer resource.
     * This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link FormTrainingAsyncClient#beginCopyModel(String, CopyAuthorization)}.
     *
     * @param resourceId Azure Resource Id of the target Form Recognizer resource where the model will be copied to.
     * This information can be found in the 'Properties' section of the Form Recognizer resource in the Azure Portal.
     * @param resourceRegion Location of the target Form Recognizer resource. A valid Azure region name supported by
     * Cognitive Services.This information can be found in the 'Keys and Endpoint' section of the Form Recognizer
     * resource in the Azure Portal.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getCopyAuthorizationWithResponse#string-string-Context -->
     * <pre>
     * String resourceId = &quot;target-resource-Id&quot;;
     * String resourceRegion = &quot;target-resource-region&quot;;
     * Response&lt;CopyAuthorization&gt; copyAuthorizationResponse =
     *     formTrainingClient.getCopyAuthorizationWithResponse&#40;resourceId, resourceRegion, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Copy Authorization operation returned with status: %s&quot;,
     *     copyAuthorizationResponse.getStatusCode&#40;&#41;&#41;;
     * CopyAuthorization copyAuthorization = copyAuthorizationResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Copy model id: %s, access token: %s, expiration time: %s, &quot;
     *         + &quot;target resource Id; %s, target resource region: %s%n&quot;,
     *     copyAuthorization.getModelId&#40;&#41;,
     *     copyAuthorization.getAccessToken&#40;&#41;,
     *     copyAuthorization.getExpiresOn&#40;&#41;,
     *     copyAuthorization.getResourceId&#40;&#41;,
     *     copyAuthorization.getResourceRegion&#40;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.getCopyAuthorizationWithResponse#string-string-Context -->
     *
     * @return A {@link Response} containing the {@link CopyAuthorization}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CopyAuthorization> getCopyAuthorizationWithResponse(String resourceId, String resourceRegion,
        Context context) {
        return client.getCopyAuthorizationWithResponse(resourceId, resourceRegion, context).block();
    }

    /**
     * Create a composed model from the provided list of existing models in the account.
     *
     * <p>This operations fails if the list consists of an invalid, non-existing model Id or duplicate Ids.
     * This operation is currently only supported for custom models trained using labels.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginCreateComposedModel#list -->
     * <pre>
     * String labeledModelId1 = &quot;5f21ab8d-71a6-42d8-9856-ef5985c486a8&quot;;
     * String labeledModelId2 = &quot;d7b0904c-841f-46f9-a9f4-3f2273eef7c9&quot;;
     * final CustomFormModel customFormModel
     *     = formTrainingClient.beginCreateComposedModel&#40;Arrays.asList&#40;labeledModelId1, labeledModelId2&#41;&#41;
     *     .getFinalResult&#40;&#41;;
     * System.out.printf&#40;&quot;Model Id: %s%n&quot;, customFormModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Status: %s%n&quot;, customFormModel.getModelStatus&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Is this a composed model: %s%n&quot;,
     *     customFormModel.getCustomModelProperties&#40;&#41;.isComposed&#40;&#41;&#41;;
     * customFormModel.getSubmodels&#40;&#41;
     *     .forEach&#40;customFormSubmodel -&gt; customFormSubmodel.getFields&#40;&#41;
     *         .forEach&#40;&#40;key, customFormModelField&#41; -&gt;
     *             System.out.printf&#40;&quot;Form type: %s Field Text: %s Field Accuracy: %f%n&quot;,
     *                 key, customFormModelField.getName&#40;&#41;, customFormModelField.getAccuracy&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginCreateComposedModel#list -->
     *
     * @param modelIds The list of models Ids to form the composed model.
     *
     * @return A {@link SyncPoller} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the {@link CustomFormModel composed model}.
     * @throws FormRecognizerException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code modelIds} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, CustomFormModel> beginCreateComposedModel(List<String> modelIds) {
        return beginCreateComposedModel(modelIds, null, null);
    }

    /**
     * Create a composed model from the provided list of existing models in the account.
     *
     * <p>This operations fails if the list consists of an invalid, non-existing model Id or duplicate Ids.
     * This operation is currently only supported for custom models trained using labels.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginCreateComposedModel#list-Options-Context -->
     * <pre>
     * String labeledModelId1 = &quot;5f21ab8d-71a6-42d8-9856-ef5985c486a8&quot;;
     * String labeledModelId2 = &quot;d7b0904c-841f-46f9-a9f4-3f2273eef7c9&quot;;
     * final CustomFormModel customFormModel =
     *     formTrainingClient.beginCreateComposedModel&#40;Arrays.asList&#40;labeledModelId1, labeledModelId2&#41;,
     *         new CreateComposedModelOptions&#40;&#41;
     *             .setModelName&#40;&quot;my composed model name&quot;&#41;,
     *         Context.NONE&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *         .getFinalResult&#40;&#41;;
     * System.out.printf&#40;&quot;Model Id: %s%n&quot;, customFormModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Status: %s%n&quot;, customFormModel.getModelStatus&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model display name: %s%n&quot;, customFormModel.getModelName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Is this a composed model: %s%n&quot;,
     *     customFormModel.getCustomModelProperties&#40;&#41;.isComposed&#40;&#41;&#41;;
     * customFormModel.getSubmodels&#40;&#41;
     *     .forEach&#40;customFormSubmodel -&gt; customFormSubmodel.getFields&#40;&#41;
     *         .forEach&#40;&#40;key, customFormModelField&#41; -&gt;
     *             System.out.printf&#40;&quot;Form type: %s Field Text: %s Field Accuracy: %f%n&quot;,
     *                 key, customFormModelField.getName&#40;&#41;, customFormModelField.getAccuracy&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.training.FormTrainingClient.beginCreateComposedModel#list-Options-Context -->
     *
     * @param modelIds The list of models Ids to form the composed model.
     * @param createComposedModelOptions The configurable {@link CreateComposedModelOptions options} to pass when
     * creating a composed model.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the {@link CustomFormModel composed model}.
     * @throws FormRecognizerException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code modelIds} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, CustomFormModel> beginCreateComposedModel(List<String> modelIds,
        CreateComposedModelOptions createComposedModelOptions, Context context) {
        return client.beginCreateComposedModel(modelIds, createComposedModelOptions, context).getSyncPoller();
    }
}
