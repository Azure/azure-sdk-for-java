// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.*;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions;
import com.azure.digitaltwins.core.models.ModelData;
import com.azure.digitaltwins.core.util.ListModelOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client for interacting synchronously with an Azure Digital Twins instance.
 *
 * <p>
 * This client is instantiated through {@link DigitalTwinsClientBuilder}.
 * </p>
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
     * Gets the {@link HttpPipeline} that this client is configured to use for all service requests. This pipeline can
     * be customized while building this client through {@link DigitalTwinsClientBuilder#httpPipeline(HttpPipeline)}.
     *
     * @return The {@link HttpPipeline} that this client uses for all service requests.
     */
    public HttpPipeline getHttpPipeline() {
        return digitalTwinsAsyncClient.getHttpPipeline();
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

    // TODO These are temporary implementations for sample purposes. This should be spruced up/replaced once this API is actually designed.
    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json relationship to be created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the application/json relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> createRelationshipWithResponse(String digitalTwinId, String relationshipId, String relationship, Context context) {
        return digitalTwinsAsyncClient.createRelationshipWithResponse(digitalTwinId, relationshipId, relationship, context).block();
    }

    /**
     * Gets all the relationships on a digital twin filtered by the relationship name, by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipName The name of a relationship to filter to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of application/json relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listRelationships(String digitalTwinId, String relationshipName, Context context) {
        return new PagedIterable<>(digitalTwinsAsyncClient.listRelationships(digitalTwinId, relationshipName, context));
    }

    //==================================================================================================================================================
    // Models APIs
    //==================================================================================================================================================

    /**
     * Creates one or many models.
     * @param models The list of models to create. Each string corresponds to exactly one model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of created models and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ModelData> createModels(List<String> models, Context context) {
        return new PagedIterable<>(digitalTwinsAsyncClient.createModels(models, context));
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     * @param modelId The Id of the model.
     * @return The ModelData
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ModelData getModel(String modelId) {
        return digitalTwinsAsyncClient.getModel(modelId).block();
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     * @param modelId The Id of the model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The ModelData and the http response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ModelData> getModelWithResponse(String modelId, Context context) {
        return digitalTwinsAsyncClient.getModelWithResponse(modelId, context).block();
    }

    /**
     * Gets the list of models by iterating through a collection.
     * @param listModelOptions The options to follow when listing the models. For example, the page size hint can be specified.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of ModelData and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ModelData> listModels(ListModelOptions listModelOptions, Context context) {
        return new PagedIterable<>(
            digitalTwinsAsyncClient.listModels(listModelOptions, context));
    }

    /**
     * Gets the list of models by iterating through a collection.
     * @return A {@link PagedFlux} of ModelData and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ModelData> listModels() {
        return new PagedIterable<>(digitalTwinsAsyncClient.listModels());
    }

    /**
     * Deletes a model.
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @return Void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void deleteModel(String modelId) {
        return digitalTwinsAsyncClient.deleteModel(modelId).block();
    }

    /**
     * Deletes a model.
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The http response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteModelWithResponse(String modelId, Context context) {
        return digitalTwinsAsyncClient.deleteModelWithResponse(modelId, context).block();
    }

    //TODO: Decommission Model APIs (waiting for Abhipsa's change to come in)
}
