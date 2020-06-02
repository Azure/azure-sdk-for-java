// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.UserDefinedFunction;
import com.azure.cosmos.models.CosmosUserDefinedFunctionResponse;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

/**
 * The type Cosmos async user defined function.
 */
public class CosmosAsyncUserDefinedFunction {

    @SuppressWarnings("EnforceFinalFields")
    private final CosmosAsyncContainer container;
    private String id;

    CosmosAsyncUserDefinedFunction(String id, CosmosAsyncContainer container) {
        this.id = id;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosAsyncUserDefinedFunction}
     *
     * @return the id of the {@link CosmosAsyncUserDefinedFunction}
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncUserDefinedFunction}
     *
     * @param id the id of the {@link CosmosAsyncUserDefinedFunction}
     * @return the same {@link CosmosAsyncUserDefinedFunction} that had the id set
     */
    CosmosAsyncUserDefinedFunction setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Read a user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the read user defined
     * function.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the read user defined function or an error.
     */
    public Mono<CosmosUserDefinedFunctionResponse> read() {
        return container.getDatabase().getDocClientWrapper().readUserDefinedFunction(getLink(), null)
                        .map(response -> ModelBridgeInternal.createCosmosUserDefinedFunctionResponse(response)).single();
    }

    /**
     * Replaces a cosmos user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced user
     * defined function.
     * In case of failure the {@link Mono} will error.
     *
     * @param udfSettings the cosmos user defined function properties.
     * @return an {@link Mono} containing the single resource response with the replaced cosmos user defined function
     * or an error.
     */
    public Mono<CosmosUserDefinedFunctionResponse> replace(CosmosUserDefinedFunctionProperties udfSettings) {
        return container.getDatabase()
                   .getDocClientWrapper()
                   .replaceUserDefinedFunction(new UserDefinedFunction(ModelBridgeInternal.toJsonFromJsonSerializable(
                       ModelBridgeInternal.getResource(udfSettings))), null)
                   .map(response -> ModelBridgeInternal.createCosmosUserDefinedFunctionResponse(response))
                   .single();
    }

    /**
     * Deletes a cosmos user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted user
     * defined function.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the deleted cosmos user defined function or
     * an error.
     */
    public Mono<CosmosUserDefinedFunctionResponse> delete() {
        return container.getDatabase()
                   .getDocClientWrapper()
                   .deleteUserDefinedFunction(this.getLink(), null)
                   .map(response -> ModelBridgeInternal.createCosmosUserDefinedFunctionResponse(response))
                   .single();
    }

    String getURIPathSegment() {
        return Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT;
    }

    String getParentLink() {
        return container.getLink();
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(getParentLink());
        builder.append("/");
        builder.append(getURIPathSegment());
        builder.append("/");
        builder.append(getId());
        return builder.toString();
    }
}
