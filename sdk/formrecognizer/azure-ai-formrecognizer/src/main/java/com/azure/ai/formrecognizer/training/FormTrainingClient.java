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
 * This class provides a synchronous client that contains model management the operations that apply
 * to Azure Form Recognizer.
 * Operations allowed by the client are creating, training of custom models, deleting models, listing models and getting
 * subscription account information.
 *
 * <p><strong>Instantiating a synchronous Form Training Client</strong></p>
 * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.initialization}
 *
 * @see FormTrainingClientBuilder
 * @see FormTrainingClient
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.beginTraining#string-boolean}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.beginTraining#string-boolean-TrainingOptions-Context}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.getCustomModel#string}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.getCustomModelWithResponse#string-Context}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.getAccountProperties}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.getAccountPropertiesWithResponse#Context}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.deleteModel#string}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.deleteModelWithResponse#string-Context}
     *
     * @param modelId The UUID string format model identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing containing status code and HTTP headers.
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.listCustomModels}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.listCustomModels#Context}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginCopyModel#string-copyAuthorization}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.beginCopyModel#string-copyAuthorization-Duration-Context}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.getCopyAuthorization#string-string}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.getCopyAuthorizationWithResponse#string-string-Context}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.beginCreateComposedModel#list}
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
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.beginCreateComposedModel#list-CreateComposedModelOptions-Context}
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
