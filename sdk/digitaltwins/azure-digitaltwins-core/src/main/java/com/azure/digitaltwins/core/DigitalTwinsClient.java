// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.models.*;

import java.util.List;

/**
 * This class provides a client for interacting synchronously with an Azure Digital Twins instance.
 * This client is instantiated through {@link DigitalTwinsClientBuilder}.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.digitaltwins.core.syncClient.instantiation}
 *
 * <p>
 * This client allows for management of digital twins, their components, and their relationships. It also allows for managing
 * the digital twin models and event routes tied to your Azure Digital Twins instance.
 * </p>
 */
@ServiceClient(builder = DigitalTwinsClientBuilder.class)
public final class DigitalTwinsClient {
    private final DigitalTwinsAsyncClient digitalTwinsAsyncClient;

    DigitalTwinsClient(DigitalTwinsAsyncClient digitalTwinsAsyncClient) {
        this.digitalTwinsAsyncClient = digitalTwinsAsyncClient;
    }

    /**
     * Gets the Azure Digital Twins service API version that this client is configured to use for all service requests.
     * Unless configured while building this client through {@link DigitalTwinsClientBuilder#serviceVersion(DigitalTwinsServiceVersion)},
     * this value will be equal to the latest service API version supported by this client.
     *
     * @return The Azure Digital Twins service API version.
     */
    public DigitalTwinsServiceVersion getServiceVersion() {
        return this.digitalTwinsAsyncClient.getServiceVersion();
    }

    //region Digital twin APIs

    /**
     * Creates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicDigitalTwin} can be provided as the input parameter:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createDigitalTwins#String-Object-Class#String}
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @return The deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T createDigitalTwin(String digitalTwinId, T digitalTwin, Class<T> clazz)
    {
        return createDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, Context.NONE).getValue();
    }

    /**
     * Creates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object type such as {@link BasicDigitalTwin} can be provided as the input parameter:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createDigitalTwinsWithResponse#String-Object-Class-Context#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createDigitalTwins#String-Object-Class#String}
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Response<T> createDigitalTwinWithResponse(String digitalTwinId, T digitalTwin, Class<T> clazz, Context context)
    {
        return digitalTwinsAsyncClient.createDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, context).block();
    }

    /**
     * Gets a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>
     * A Strongly typed object type such as {@link BasicDigitalTwin} can be provided as an input parameter for {@code clazz}
     * to indicate what type is used to deserialize the response.
     * </p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getDigitalTwin#String-Class#BasicDigitalTwin}
     *
     * <p>Alternatively String can be used to get the response in a json string format.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getDigitalTwin#String-Class#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @return The deserialized application/json object representing the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T getDigitalTwin(String digitalTwinId, Class<T> clazz)
    {
        return getDigitalTwinWithResponse(digitalTwinId, clazz, Context.NONE).getValue();
    }

    /**
     * Gets a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>
     * A Strongly typed object type such as {@link BasicDigitalTwin} can be provided as an input parameter for {@code clazz}
     * to indicate what type is used to deserialize the response.
     * </p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getDigitalTwinWithResponse#String-Class-Context#BasicDigitalTwin}
     *
     * <p>Alternatively String can be used to get the response in a json string format.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getDigitalTwinWithResponse#String-Class-Context#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> DigitalTwinsResponse<T> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz, Context context)
    {
        return digitalTwinsAsyncClient.getDigitalTwinWithResponse(digitalTwinId, clazz, context).block();
    }

    /**
     * Updates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update digital twin by providing list of intended patch operations.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.updateDigitalTwin#String-List}
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link UpdateOperationUtility}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateDigitalTwin(String digitalTwinId, List<Object> digitalTwinUpdateOperations)
    {
        updateDigitalTwinWithResponse(digitalTwinId, digitalTwinUpdateOperations, new UpdateDigitalTwinRequestOptions(), Context.NONE);
    }

    /**
     * Updates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update digital twin by providing list of intended patch operations.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.updateDigitalTwinWithResponse#String-List-Options-Context}
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional settings for this request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwinsResponse<Void> updateDigitalTwinWithResponse(String digitalTwinId, List<Object> digitalTwinUpdateOperations, UpdateDigitalTwinRequestOptions options, Context context)
    {
        return digitalTwinsAsyncClient.updateDigitalTwinWithResponse(digitalTwinId, digitalTwinUpdateOperations, options, context).block();
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.deleteDigitalTwin#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDigitalTwin(String digitalTwinId)
    {
        deleteDigitalTwinWithResponse(digitalTwinId, new DeleteDigitalTwinRequestOptions(), Context.NONE);
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.deleteDigitalTwinWithResponse#String-Options-Context}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param options The optional settings for this request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The Http response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDigitalTwinWithResponse(String digitalTwinId, DeleteDigitalTwinRequestOptions options, Context context)
    {
        return digitalTwinsAsyncClient.deleteDigitalTwinWithResponse(digitalTwinId, options, context).block();
    }

    //endregion Digital twin APIs

    //region Relationship APIs

    /**
     * Creates a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createRelationship#String-String-Object-Class#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createRelationship#String-String-Object-Class#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json object representing the relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @return The relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T createRelationship(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz) {
        return createRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz, Context.NONE).getValue();
    }

    /**
     * Creates a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createRelationshipWithResponse#String-String-Object-Class-Context#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createRelationshipWithResponse#String-String-Object-Class-Context#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json object representing the relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing the relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> DigitalTwinsResponse<T> createRelationshipWithResponse(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz, Context context) {
        return digitalTwinsAsyncClient.createRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz, context).block();
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getRelationship#String#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getRelationship#String#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @param clazz The model class to deserialize the relationship into.
     * @param <T> The generic type to deserialize the relationship into.
     * @return The deserialized relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T getRelationship(String digitalTwinId, String relationshipId, Class<T> clazz) {
        return getRelationshipWithResponse(digitalTwinId, relationshipId, clazz, Context.NONE).getValue();
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getRelationshipWithResponse#String-String-Class-Context#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getRelationshipWithResponse#String-String-Class-Context#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @param clazz The model class to deserialize the relationship into.
     * @param <T> The generic type to deserialize the relationship into.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing the deserialized relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> DigitalTwinsResponse<T> getRelationshipWithResponse(String digitalTwinId, String relationshipId, Class<T> clazz, Context context) {
        return digitalTwinsAsyncClient.getRelationshipWithResponse(digitalTwinId, relationshipId, clazz, context).block();
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.updateRelationship#String-String-List}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link UpdateOperationUtility}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateRelationship(String digitalTwinId, String relationshipId, List<Object> relationshipUpdateOperations) {
        updateRelationshipWithResponse(digitalTwinId, relationshipId, relationshipUpdateOperations, new UpdateRelationshipRequestOptions(), Context.NONE);
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.updateRelationshipWithResponse#String-String-List-Options-Context}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional settings for this request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwinsResponse<Void> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, List<Object> relationshipUpdateOperations, UpdateRelationshipRequestOptions options, Context context) {
        return digitalTwinsAsyncClient.updateRelationshipWithResponse(digitalTwinId, relationshipId, relationshipUpdateOperations, options, context).block();
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.deleteRelationship#String-String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRelationship(String digitalTwinId, String relationshipId) {
        deleteRelationshipWithResponse(digitalTwinId, relationshipId, new DeleteRelationshipRequestOptions(), Context.NONE);
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.deleteRelationshipWithResponse#String-String-Options-Context}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional settings for this request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, DeleteRelationshipRequestOptions options, Context context) {
        return digitalTwinsAsyncClient.deleteRelationshipWithResponse(digitalTwinId, relationshipId, options, context).block();
    }

    /**
     * List the relationships that have a given digital twin as the source.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listRelationships#String-Class#BasicRelationship#IterateByItem}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listRelationships#String-Class#String#IterateByItem}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param clazz The model class to deserialize each relationship into. Since a digital twin might have relationships
     *              that conform to different models, it is advisable to convert them to a generic model like {@link BasicRelationship}.
     * @param <T> The generic type to deserialize each relationship into.
     * @return A {@link PagedIterable} of relationships belonging to the specified digital twin.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedIterable<T> listRelationships(String digitalTwinId, Class<T> clazz) {
        return listRelationships(digitalTwinId, null, clazz, Context.NONE);
    }

    /**
     * List the relationships that have a given digital twin as the source and that have the given relationship name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listRelationships#String-String-Class-Context#BasicRelationship#IterateByItem}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listRelationships#String-String-Class-Context#String#IterateByItem}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipName The name of a relationship to filter to.
     * @param clazz The model class to deserialize each relationship into. Since a digital twin might have relationships
     *              that conform to different models, it is advisable to convert them to a generic model like {@link BasicRelationship}.
     * @param <T> The generic type to deserialize each relationship into.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of relationships belonging to the specified digital twin.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedIterable<T> listRelationships(String digitalTwinId, String relationshipName, Class<T> clazz, Context context) {
        return new PagedIterable<>(digitalTwinsAsyncClient.listRelationships(digitalTwinId, relationshipName, clazz, context));
    }

    /**
     * List the relationships that have a given digital twin as the target.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listIncomingRelationships#String}
     *
     * @param digitalTwinId The Id of the target digital twin.
     * @return A {@link PagedIterable} of application/json strings representing the relationships directed towards the
     * specified digital twin.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IncomingRelationship> listIncomingRelationships(String digitalTwinId) {
        return listIncomingRelationships(digitalTwinId, Context.NONE);
    }

    /**
     * List the relationships that have a given digital twin as the target.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listIncomingRelationships#String-Context}
     *
     * @param digitalTwinId The Id of the target digital twin.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of application/json strings representing the relationships directed towards the
     * specified digital twin.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IncomingRelationship> listIncomingRelationships(String digitalTwinId, Context context) {
        return new PagedIterable<>(digitalTwinsAsyncClient.listIncomingRelationships(digitalTwinId, context));
    }

    //endregion Relationship APIs

    //region Model APIs

    /**
     * Creates one or many models.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createModels#Iterable}
     *
     * @param models The list of models to create. Each string corresponds to exactly one model.
     * @return A List of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Iterable<DigitalTwinsModelData> createModels(Iterable<String> models) {
        return createModelsWithResponse(models, Context.NONE).getValue();
    }

    /**
     * Creates one or many models.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createModelsWithResponse#Iterable}
     *
     * @param models The list of models to create. Each string corresponds to exactly one model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the list of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<Iterable<DigitalTwinsModelData>> createModelsWithResponse(Iterable<String> models, Context context) {
        return digitalTwinsAsyncClient.createModelsWithResponse(models, context).block();
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getModel#String}
     *
     * @param modelId The Id of the model.
     * @return A {@link DigitalTwinsModelData} instance that contains the model and its metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwinsModelData getModel(String modelId) {

        return getModelWithResponse(modelId, Context.NONE).getValue();
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getModelWithResponse#String}
     *
     * @param modelId The Id of the model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing a {@link DigitalTwinsModelData} instance that contains the model and its metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DigitalTwinsModelData> getModelWithResponse(String modelId, Context context) {
        return digitalTwinsAsyncClient.getModelWithResponse(modelId, context).block();
    }

    /**
     * List all of the models in this digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listModels}
     *
     * @return A {@link PagedFlux} of {@link DigitalTwinsModelData} that enumerates all the models.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DigitalTwinsModelData> listModels() {
        return new PagedIterable<>(digitalTwinsAsyncClient.listModels());
    }

    /**
     * List the models in this digital twins instance based on some options.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listModels#Options}
     *
     * @param modelsListOptions The options to follow when listing the models.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing the retrieved {@link DigitalTwinsModelData} instances.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DigitalTwinsModelData> listModels(ModelsListOptions modelsListOptions, Context context) {
        return new PagedIterable<>(
            digitalTwinsAsyncClient.listModels(modelsListOptions, context));
    }

    /**
     * Deletes a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.deleteModel#String}
     *
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteModel(String modelId) {
        deleteModelWithResponse(modelId, Context.NONE);
    }

    /**
     * Deletes a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.deleteModelWithResponse#String}
     *
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteModelWithResponse(String modelId, Context context) {
        return digitalTwinsAsyncClient.deleteModelWithResponse(modelId, context).block();
    }

    /**
     * Decommissions a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.decommissionModel#String}
     *
     * @param modelId The Id of the model to decommission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void decommissionModel(String modelId) {
        decommissionModelWithResponse(modelId, Context.NONE);
    }

    /**
     * Decommissions a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.decommissionModelWithResponse#String}
     *
     * @param modelId The Id of the model to decommission.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> decommissionModelWithResponse(String modelId, Context context) {
        return digitalTwinsAsyncClient.decommissionModelWithResponse(modelId, context).block();
    }

    //endregion Model APIs

    //region Component APIs

    /**
     * Get a component of a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getComponent#String-String-Class}
     *
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentPath The path of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize the application/json component into.
     * @return The deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T getComponent(String digitalTwinId, String componentPath, Class<T> clazz) {
        return getComponentWithResponse(digitalTwinId, componentPath, clazz, Context.NONE).getValue();
    }

    /**
     * Get a component of a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getComponentWithResponse#String-String-Class-Context}
     *
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentPath The path of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize the application/json component into.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> DigitalTwinsResponse<T> getComponentWithResponse(String digitalTwinId, String componentPath, Class<T> clazz, Context context) {
        return digitalTwinsAsyncClient.getComponentWithResponse(digitalTwinId, componentPath, clazz, context).block();
    }

    /**
     * Patch a component on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.updateComponent#String-String-List}
     *
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentPath The path of the component on the digital twin.
     * @param componentUpdateOperations The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link UpdateOperationUtility}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateComponent(String digitalTwinId, String componentPath, List<Object> componentUpdateOperations) {
        updateComponentWithResponse(digitalTwinId, componentPath, componentUpdateOperations, new UpdateComponentRequestOptions(), Context.NONE);
    }

    /**
     * Patch a component on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.updateComponentWithResponse#String-String-List-Options-Context}
     *
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentPath The path of the component on the digital twin.
     * @param componentUpdateOperations The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional parameters for this request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwinsResponse<Void> updateComponentWithResponse(String digitalTwinId, String componentPath, List<Object> componentUpdateOperations, UpdateComponentRequestOptions options, Context context) {
        return digitalTwinsAsyncClient.updateComponentWithResponse(digitalTwinId, componentPath, componentUpdateOperations, options, context).block();
    }

    //endregion Component APIs

    //region Query APIs

    /**
     * Query digital twins.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicDigitalTwin} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.query#String#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.query#String#String}
     *
     * @param query The query string, in SQL-like syntax.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     *              have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
     * @param <T> The generic type to deserialize each queried digital twin into.
     * @return A {@link PagedIterable} of deserialized digital twins.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedIterable<T> query(String query, Class<T> clazz) {
        return query(query, clazz, Context.NONE);
    }

    /**
     * Query digital twins.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicDigitalTwin} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.query#String-Context#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.query#String-Context#String}
     *
     * @param query The query string, in SQL-like syntax.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     *              have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
     * @param <T> The generic type to deserialize each queried digital twin into.
     * @return A {@link PagedIterable} of deserialized digital twins.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedIterable<T> query(String query, Class<T> clazz, Context context) {
        return new PagedIterable<>(digitalTwinsAsyncClient.query(query, clazz, context));
    }

    //endregion Query APIs

    //region Event Route APIs
    /**
     * Create an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createEventRoute#String-EventRoute}
     *
     * @param eventRouteId The id of the event route to create.
     * @param eventRoute The event route to create.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createEventRoute(String eventRouteId, EventRoute eventRoute) {
        createEventRouteWithResponse(eventRouteId, eventRoute, Context.NONE);
    }

    /**
     * Create an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.createEventRouteWithResponse#String-EventRoute-Context}
     *
     * @param eventRouteId The id of the event route to create.
     * @param eventRoute The event route to create.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createEventRouteWithResponse(String eventRouteId, EventRoute eventRoute, Context context) {
        return this.digitalTwinsAsyncClient.createEventRouteWithResponse(eventRouteId, eventRoute, context).block();
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getEventRoute#String}
     *
     * @param eventRouteId The Id of the event route to get.
     * @return The retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventRoute getEventRoute(String eventRouteId) {
        return getEventRouteWithResponse(eventRouteId, Context.NONE).getValue();
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.getEventRouteWithResponse#String-Context}
     *
     * @param eventRouteId The Id of the event route to get.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EventRoute> getEventRouteWithResponse(String eventRouteId, Context context) {
        return this.digitalTwinsAsyncClient.getEventRouteWithResponse(eventRouteId, context).block();
    }

    /**
     * Delete an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.deleteEventRoute#String}
     *
     * @param eventRouteId The Id of the event route to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEventRoute(String eventRouteId)
    {
        deleteEventRouteWithResponse(eventRouteId, Context.NONE);
    }

    /**
     * Delete an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.deleteEventRouteWithResponse#String-Context}
     *
     * @param eventRouteId The Id of the event route to delete.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing no parsed value.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEventRouteWithResponse(String eventRouteId, Context context)
    {
        return this.digitalTwinsAsyncClient.deleteEventRouteWithResponse(eventRouteId, context).block();
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listEventRoutes}
     *
     * @return A {@link PagedIterable} containing all the event routes that exist in your digital twins instance.
     * This PagedIterable may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<EventRoute> listEventRoutes() {
        return listEventRoutes(new EventRoutesListOptions(), Context.NONE);
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.listEventRoutes#Options-Context}
     *
     * @param options The optional parameters to use when listing event routes. See {@link EventRoutesListOptions} for more details
     * on what optional parameters can be set.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing all the event routes that exist in your digital twins instance.
     * This PagedIterable may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<EventRoute> listEventRoutes(EventRoutesListOptions options, Context context) {
        return new PagedIterable<>(this.digitalTwinsAsyncClient.listEventRoutes(options, context));
    }

    //endregion Event Route APIs

    //region Telemetry APIs

    /**
     * Publishes telemetry from a digital twin
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.publishTelemetry#String-Object#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.publishTelemetry#String-Object#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void publishTelemetry(String digitalTwinId, Object payload) {
        PublishTelemetryRequestOptions publishTelemetryRequestOptions = new PublishTelemetryRequestOptions();
        publishTelemetryWithResponse(digitalTwinId, payload, publishTelemetryRequestOptions, Context.NONE);
    }

    /**
     * Publishes telemetry from a digital twin
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.publishTelemetryWithResponse#String-Object-Options-Context#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.publishTelemetryWithResponse#String-Object-Options-Context#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param publishTelemetryRequestOptions The additional information to be used when processing a telemetry request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> publishTelemetryWithResponse(String digitalTwinId, Object payload, PublishTelemetryRequestOptions publishTelemetryRequestOptions, Context context) {
        return digitalTwinsAsyncClient.publishTelemetryWithResponse(digitalTwinId, payload, publishTelemetryRequestOptions, context).block();
    }

    /**
     * Publishes telemetry from a digital twin's component
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.publishComponentTelemetry#String-String-Object#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.publishComponentTelemetry#String-String-Object#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void publishComponentTelemetry(String digitalTwinId, String componentName, Object payload) {
        PublishTelemetryRequestOptions publishTelemetryRequestOptions = new PublishTelemetryRequestOptions();
        publishComponentTelemetryWithResponse(digitalTwinId, componentName, payload, publishTelemetryRequestOptions, Context.NONE);
    }

    /**
     * Publishes telemetry from a digital twin's component
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.publishComponentTelemetryWithResponse#String-String-Object-Options-Context#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.syncClient.publishComponentTelemetryWithResponse#String-String-Object-Options-Context#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param publishTelemetryRequestOptions The additional information to be used when processing a telemetry request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName, Object payload, PublishTelemetryRequestOptions publishTelemetryRequestOptions, Context context) {
        return digitalTwinsAsyncClient.publishComponentTelemetryWithResponse(digitalTwinId, componentName, payload, publishTelemetryRequestOptions, context).block();
    }

    //endregion TelemetryAPIs
}
