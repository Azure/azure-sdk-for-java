// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;

/**
 * This class provides a synchronous client that contains model management the operations that apply
 * to Azure Form Recognizer.
 * Operations allowed by the client are, to creating, training of custom models, delete models, list models and get
 * subscription account information.
 *
 * <p><strong>Instantiating a synchronous Form Training Client</strong></p>
 * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.initialization}
 *
 * @see FormRecognizerClientBuilder
 * @see FormRecognizerClient
 */
@ServiceClient(builder = FormRecognizerClientBuilder.class)
public class FormTrainingClient {

    private final FormTrainingAsyncClient client;

    /**
     * Create a {@link FormTrainingClient} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link FormRecognizerClientBuilder#pipeline http pipeline}.
     *
     * @param formTrainingAsyncClient The {@link FormRecognizerAsyncClient} that the client routes its request through.
     */
    FormTrainingClient(FormTrainingAsyncClient formTrainingAsyncClient) {
        this.client = formTrainingAsyncClient;
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public FormRecognizerServiceVersion getServiceVersion() {
        return client.getServiceVersion();
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
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.beginTraining#string-boolean}
     *
     * @param fileSourceUrl source URL parameter that is either an externally accessible
     * Azure storage blob container Uri (preferably a Shared Access Signature Uri).
     * @param useLabelFile Boolean to specify the use of labeled files for training the model.
     *
     * @return A {@link SyncPoller} that polls the training model operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<OperationResult, CustomFormModel> beginTraining(String fileSourceUrl, boolean useLabelFile) {
        return beginTraining(fileSourceUrl, useLabelFile, false, null, null);
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
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.beginTraining#string-boolean-boolean-string-Duration}
     *
     * @param fileSourceUrl source URL parameter that is either an externally accessible Azure storage
     * blob container Uri (preferably a Shared Access Signature Uri).
     * @param useLabelFile Boolean to specify the use of labeled files for training the model.
     * @param includeSubFolders to indicate if sub folders within the set of prefix folders will
     * also need to be included when searching for content to be preprocessed.
     * @param filePrefix A case-sensitive prefix string to filter documents in the source path
     * for training. For example, when using a Azure storage blob Uri, use the prefix to restrict
     * sub folders for training.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract receipt operation until it
     * has completed, has failed, or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<OperationResult, CustomFormModel> beginTraining(String fileSourceUrl, boolean useLabelFile,
        boolean includeSubFolders, String filePrefix, Duration pollInterval) {
        return client.beginTraining(fileSourceUrl, useLabelFile, includeSubFolders,
            filePrefix, pollInterval).getSyncPoller();
    }

    /**
     * Get detailed information for a specified custom model id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.getCustomModel#string}
     *
     * @param modelId The UUID string format model identifier.
     *
     * @return The detailed information for the specified model.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CustomFormModel getCustomModel(String modelId) {
        return getCustomModelWithResponse(modelId, Context.NONE).getValue();
    }

    /**
     * Get detailed information for a specified custom model id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.getCustomModelWithResponse#string-Context}
     *
     * @param modelId The UUID string format model identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The detailed information for the specified model.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CustomFormModel> getCustomModelWithResponse(String modelId, Context context) {
        return client.getCustomModelWithResponse(modelId, context).block();
    }

    /**
     * Get account information for all custom models.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.getAccountProperties}
     *
     * @return The account information.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccountProperties getAccountProperties() {
        return getAccountPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get account information for all custom models.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.getAccountPropertiesWithResponse#Context}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The account information.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccountProperties> getAccountPropertiesWithResponse(Context context) {
        return client.getAccountPropertiesWithResponse(context).block();
    }

    /**
     * Deletes the specified custom model.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.deleteModel#string}
     *
     * @param modelId The UUID string format model identifier.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteModel(String modelId) {
        deleteModelWithResponse(modelId, Context.NONE);
    }

    /**
     * Deletes the specified custom model.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.deleteModelWithResponse#string-Context}
     *
     * @param modelId The UUID string format model identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteModelWithResponse(String modelId, Context context) {
        return client.deleteModelWithResponse(modelId, context).block();
    }

    /**
     * List information for all models.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.getModelInfos}
     *
     * @return {@link PagedIterable} of {@link CustomFormModelInfo} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CustomFormModelInfo> getModelInfos() {
        return new PagedIterable<>(client.getModelInfos(Context.NONE));
    }

    /**
     * List information for all models with taking {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormTrainingClient.getModelInfos#Context}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of {@link CustomFormModelInfo} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CustomFormModelInfo> getModelInfos(Context context) {
        return new PagedIterable<>(client.getModelInfos(context));
    }
}
