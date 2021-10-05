// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.models.JsonPatchDocument;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.models.CreateOrReplaceDigitalTwinOptions;
import com.azure.digitaltwins.core.models.CreateOrReplaceRelationshipOptions;
import com.azure.digitaltwins.core.models.DeleteDigitalTwinOptions;
import com.azure.digitaltwins.core.models.DeleteRelationshipOptions;
import com.azure.digitaltwins.core.models.DigitalTwinsEventRoute;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.digitaltwins.core.models.DigitalTwinsResponse;
import com.azure.digitaltwins.core.models.IncomingRelationship;
import com.azure.digitaltwins.core.models.ListDigitalTwinsEventRoutesOptions;
import com.azure.digitaltwins.core.models.ListModelsOptions;
import com.azure.digitaltwins.core.models.PublishComponentTelemetryOptions;
import com.azure.digitaltwins.core.models.PublishTelemetryOptions;
import com.azure.digitaltwins.core.models.QueryOptions;
import com.azure.digitaltwins.core.models.UpdateComponentOptions;
import com.azure.digitaltwins.core.models.UpdateDigitalTwinOptions;
import com.azure.digitaltwins.core.models.UpdateRelationshipOptions;

/**
 * This class provides a client for interacting synchronously with an Azure Digital Twins instance.
 * This client is instantiated through {@link DigitalTwinsClientBuilder}.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.instantiation}
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
     * Creates a digital twin. If the provided digital twin Id is already in use, then this will attempt
     * to replace the existing digital twin with the provided digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicDigitalTwin} can be provided as the input parameter:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createDigitalTwins#String-Object-Class#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @return The deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T createOrReplaceDigitalTwin(String digitalTwinId, T digitalTwin, Class<T> clazz) {
        return createOrReplaceDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, null, Context.NONE).getValue();
    }

    /**
     * Creates a digital twin. If the provided digital twin Id is already in use, then this will attempt
     * to replace the existing digital twin with the provided digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object type such as {@link BasicDigitalTwin} can be provided as the input parameter:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createDigitalTwinsWithResponse#String-Object-Class-Options-Context#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createDigitalTwins#String-Object-Class#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Response<T> createOrReplaceDigitalTwinWithResponse(String digitalTwinId, T digitalTwin, Class<T> clazz, CreateOrReplaceDigitalTwinOptions options, Context context) {
        return digitalTwinsAsyncClient.createOrReplaceDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, options, context).block();
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getDigitalTwin#String-Class#BasicDigitalTwin}
     *
     * <p>Alternatively String can be used to get the response in a json string format.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getDigitalTwin#String-Class#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @return The deserialized application/json object representing the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T getDigitalTwin(String digitalTwinId, Class<T> clazz) {
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getDigitalTwinWithResponse#String-Class-Options-Context#BasicDigitalTwin}
     *
     * <p>Alternatively String can be used to get the response in a json string format.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getDigitalTwinWithResponse#String-Class-Options-Context#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> DigitalTwinsResponse<T> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz, Context context) {
        return digitalTwinsAsyncClient.getDigitalTwinWithResponse(digitalTwinId, clazz, context).block();
    }

    /**
     * Updates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update digital twin by providing list of intended patch operations.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.updateDigitalTwin#String-JsonPatchDocument}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param jsonPatch The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link JsonPatchDocument}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateDigitalTwin(String digitalTwinId, JsonPatchDocument jsonPatch) {
        updateDigitalTwinWithResponse(digitalTwinId, jsonPatch, null, Context.NONE);
    }

    /**
     * Updates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update digital twin by providing list of intended patch operations.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.updateDigitalTwinWithResponse#String-JsonPatchDocument-UpdateDigitalTwinOptions-Context}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param jsonPatch The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link JsonPatchDocument}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse}. This response object includes an HTTP header that gives you the updated
     * ETag for this resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwinsResponse<Void> updateDigitalTwinWithResponse(String digitalTwinId, JsonPatchDocument jsonPatch, UpdateDigitalTwinOptions options, Context context) {
        return digitalTwinsAsyncClient.updateDigitalTwinWithResponse(digitalTwinId, jsonPatch, options, context).block();
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.deleteDigitalTwin#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDigitalTwin(String digitalTwinId) {
        deleteDigitalTwinWithResponse(digitalTwinId, null, Context.NONE);
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.deleteDigitalTwinWithResponse#String-DeleteDigitalTwinOptions-Context}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The Http response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDigitalTwinWithResponse(String digitalTwinId, DeleteDigitalTwinOptions options, Context context) {
        return digitalTwinsAsyncClient.deleteDigitalTwinWithResponse(digitalTwinId, options, context).block();
    }

    //endregion Digital twin APIs

    //region Relationship APIs

    /**
     * Creates a relationship on a digital twin. If the provided relationship Id is already in use, then this will
     * attempt to replace the existing relationship with the provided relationship.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createOrReplaceRelationship#String-String-Object-Class#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createOrReplaceRelationship#String-String-Object-Class#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json object representing the relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @return The relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T createOrReplaceRelationship(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz) {
        return createOrReplaceRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz, null, Context.NONE).getValue();
    }

    /**
     * Creates a relationship on a digital twin. If the provided relationship Id is already in use, then this will
     * attempt to replace the existing relationship with the provided relationship.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options-Context#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options-Context#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json object representing the relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing the relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> DigitalTwinsResponse<T> createOrReplaceRelationshipWithResponse(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz, CreateOrReplaceRelationshipOptions options, Context context) {
        return digitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz, options, context).block();
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getRelationship#String#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getRelationship#String#String}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getRelationshipWithResponse#String-String-Class-Options-Context#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getRelationshipWithResponse#String-String-Class-Options-Context#String}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.updateRelationship#String-String-JsonPatchDocument}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link JsonPatchDocument}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateRelationship(String digitalTwinId, String relationshipId, JsonPatchDocument jsonPatch) {
        updateRelationshipWithResponse(digitalTwinId, relationshipId, jsonPatch, null, Context.NONE);
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.updateRelationshipWithResponse#String-String-JsonPatchDocument-UpdateRelationshipOptions-Context}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link JsonPatchDocument}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing no parsed payload object. This response object includes an
     * HTTP header that gives you the updated ETag for this resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwinsResponse<Void> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, JsonPatchDocument jsonPatch, UpdateRelationshipOptions options, Context context) {
        return digitalTwinsAsyncClient.updateRelationshipWithResponse(digitalTwinId, relationshipId, jsonPatch, options, context).block();
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.deleteRelationship#String-String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRelationship(String digitalTwinId, String relationshipId) {
        deleteRelationshipWithResponse(digitalTwinId, relationshipId, null, Context.NONE);
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.deleteRelationshipWithResponse#String-String-DeleteRelationshipOptions-Context}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, DeleteRelationshipOptions options, Context context) {
        return digitalTwinsAsyncClient.deleteRelationshipWithResponse(digitalTwinId, relationshipId, options, context).block();
    }

    /**
     * List the relationships that have a given digital twin as the source.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listRelationships#String-Class#BasicRelationship#IterateByItem}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listRelationships#String-Class#String#IterateByItem}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listRelationships#String-String-Class-Options-Context#BasicRelationship#IterateByItem}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listRelationships#String-String-Class-Options-Context#String#IterateByItem}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listIncomingRelationships#String}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listIncomingRelationships#String-Context}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createModels#Iterable}
     *
     * @param dtdlModels The list of models to create. Each string corresponds to exactly one model.
     * @return A List of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Iterable<DigitalTwinsModelData> createModels(Iterable<String> dtdlModels) {
        return createModelsWithResponse(dtdlModels, Context.NONE).getValue();
    }

    /**
     * Creates one or many models.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createModelsWithResponse#Iterable-Context}
     *
     * @param dtdlModels The list of models to create. Each string corresponds to exactly one model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the list of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<Iterable<DigitalTwinsModelData>> createModelsWithResponse(Iterable<String> dtdlModels, Context context) {
        return digitalTwinsAsyncClient.createModelsWithResponse(dtdlModels, context).block();
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getModel#String}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getModelWithResponse#String-Context}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listModels}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listModels#ListModelsOptions-Context}
     *
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing the retrieved {@link DigitalTwinsModelData} instances.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DigitalTwinsModelData> listModels(ListModelsOptions options, Context context) {
        return new PagedIterable<>(
            digitalTwinsAsyncClient.listModels(options, context));
    }

    /**
     * Deletes a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.deleteModel#String}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.deleteModelWithResponse#String-Context}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.decommissionModel#String}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.decommissionModelWithResponse#String-Context}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getComponent#String-String-Class}
     *
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentName The name of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize the application/json component into.
     * @return The deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T getComponent(String digitalTwinId, String componentName, Class<T> clazz) {
        return getComponentWithResponse(digitalTwinId, componentName, clazz, Context.NONE).getValue();
    }

    /**
     * Get a component of a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getComponentWithResponse#String-String-Class-Context}
     *
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentName The name of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize the application/json component into.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> DigitalTwinsResponse<T> getComponentWithResponse(String digitalTwinId, String componentName, Class<T> clazz, Context context) {
        return digitalTwinsAsyncClient.getComponentWithResponse(digitalTwinId, componentName, clazz, context).block();
    }

    /**
     * Patch a component on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.updateComponent#String-String-JsonPatchDocument}
     *
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentName The name of the component on the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link JsonPatchDocument}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateComponent(String digitalTwinId, String componentName, JsonPatchDocument jsonPatch) {
        updateComponentWithResponse(digitalTwinId, componentName, jsonPatch, null, Context.NONE);
    }

    /**
     * Patch a component on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.updateComponentWithResponse#String-String-JsonPatchDocument-UpdateComponentOptions-Context}
     *
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentName The name of the component on the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link JsonPatchDocument}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link DigitalTwinsResponse} containing no parsed payload object. This response object includes an HTTP
     * header that gives you the updated ETag for this resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwinsResponse<Void> updateComponentWithResponse(String digitalTwinId, String componentName, JsonPatchDocument jsonPatch, UpdateComponentOptions options, Context context) {
        return digitalTwinsAsyncClient.updateComponentWithResponse(digitalTwinId, componentName, jsonPatch, options, context).block();
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.query#String#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.query#String#String}
     *
     * Note that there may be a delay between before changes in your instance are reflected in queries.
     * For more details on query limitations, see
     * <a href="https://docs.microsoft.com/azure/digital-twins/how-to-query-graph#query-limitations">Query limitations</a>
     *
     * @param query The query string, in SQL-like syntax.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     *              have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
     * @param <T> The generic type to deserialize each queried digital twin into.
     * @return A {@link PagedIterable} of deserialized digital twins.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedIterable<T> query(String query, Class<T> clazz) {
        return query(query, clazz, null, Context.NONE);
    }

    /**
     * Query digital twins.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicDigitalTwin} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.query#String-Options-Context#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.query#String-Options-Context#String}
     *
     * Note that there may be a delay between before changes in your instance are reflected in queries.
     * For more details on query limitations, see
     * <a href="https://docs.microsoft.com/azure/digital-twins/how-to-query-graph#query-limitations">Query limitations</a>
     *
     * @param query The query string, in SQL-like syntax.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     *              have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
     * @param <T> The generic type to deserialize each queried digital twin into.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of deserialized digital twins.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedIterable<T> query(String query, Class<T> clazz, QueryOptions options, Context context) {
        return new PagedIterable<>(digitalTwinsAsyncClient.query(query, clazz, options, context));
    }

    //endregion Query APIs

    //region Event Route APIs
    /**
     * Create an event route. If the provided eventRouteId is already in use, then this will attempt to replace the
     * existing event route with the provided event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createOrReplaceEventRoute#String-DigitalTwinsEventRoute}
     *
     * @param eventRouteId The id of the event route to create.
     * @param eventRoute The event route to create.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createOrReplaceEventRoute(String eventRouteId, DigitalTwinsEventRoute eventRoute) {
        createOrReplaceEventRouteWithResponse(eventRouteId, eventRoute, Context.NONE);
    }

    /**
     * Create an event route. If the provided eventRouteId is already in use, then this will attempt to replace the
     * existing event route with the provided event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.createOrReplaceEventRouteWithResponse#String-DigitalTwinsEventRoute-Context}
     *
     * @param eventRouteId The id of the event route to create.
     * @param eventRoute The event route to create.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createOrReplaceEventRouteWithResponse(String eventRouteId, DigitalTwinsEventRoute eventRoute, Context context) {
        return this.digitalTwinsAsyncClient.createOrReplaceEventRouteWithResponse(eventRouteId, eventRoute, context).block();
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getEventRoute#String}
     *
     * @param eventRouteId The Id of the event route to get.
     * @return The retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DigitalTwinsEventRoute getEventRoute(String eventRouteId) {
        return getEventRouteWithResponse(eventRouteId, Context.NONE).getValue();
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.getEventRouteWithResponse#String-Context}
     *
     * @param eventRouteId The Id of the event route to get.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DigitalTwinsEventRoute> getEventRouteWithResponse(String eventRouteId, Context context) {
        return this.digitalTwinsAsyncClient.getEventRouteWithResponse(eventRouteId, context).block();
    }

    /**
     * Delete an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.deleteEventRoute#String}
     *
     * @param eventRouteId The Id of the event route to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEventRoute(String eventRouteId) {
        deleteEventRouteWithResponse(eventRouteId, Context.NONE);
    }

    /**
     * Delete an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.deleteEventRouteWithResponse#String-Context}
     *
     * @param eventRouteId The Id of the event route to delete.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing no parsed value.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEventRouteWithResponse(String eventRouteId, Context context) {
        return this.digitalTwinsAsyncClient.deleteEventRouteWithResponse(eventRouteId, context).block();
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listEventRoutes}
     *
     * @return A {@link PagedIterable} containing all the event routes that exist in your digital twins instance.
     * This PagedIterable may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DigitalTwinsEventRoute> listEventRoutes() {
        return listEventRoutes(null, Context.NONE);
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.listEventRoutes#ListDigitalTwinsEventRoutesOptions-Context}
     *
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing all the event routes that exist in your digital twins instance.
     * This PagedIterable may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DigitalTwinsEventRoute> listEventRoutes(ListDigitalTwinsEventRoutesOptions options, Context context) {
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.publishTelemetry#String-String-Object#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.publishTelemetry#String-String-Object#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link DigitalTwinsEventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void publishTelemetry(String digitalTwinId, String messageId, Object payload) {
        publishTelemetryWithResponse(digitalTwinId, messageId, payload, null, Context.NONE);
    }

    /**
     * Publishes telemetry from a digital twin
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.publishTelemetryWithResponse#String-String-Object-Options-Context#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.publishTelemetryWithResponse#String-String-Object-Options-Context#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link DigitalTwinsEventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> publishTelemetryWithResponse(String digitalTwinId, String messageId, Object payload, PublishTelemetryOptions options, Context context) {
        return digitalTwinsAsyncClient.publishTelemetryWithResponse(digitalTwinId, messageId, payload, options, context).block();
    }

    /**
     * Publishes telemetry from a digital twin's component
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.publishComponentTelemetry#String-String-String-Object#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.publishComponentTelemetry#String-String-String-Object#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link DigitalTwinsEventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void publishComponentTelemetry(String digitalTwinId, String componentName, String messageId, Object payload) {
        publishComponentTelemetryWithResponse(digitalTwinId, componentName, messageId, payload, null, Context.NONE);
    }

    /**
     * Publishes telemetry from a digital twin's component
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options-Context#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options-Context#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link DigitalTwinsEventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName, String messageId, Object payload, PublishComponentTelemetryOptions options, Context context) {
        return digitalTwinsAsyncClient.publishComponentTelemetryWithResponse(digitalTwinId, componentName, messageId, payload, options, context).block();
    }

    //endregion TelemetryAPIs
}
