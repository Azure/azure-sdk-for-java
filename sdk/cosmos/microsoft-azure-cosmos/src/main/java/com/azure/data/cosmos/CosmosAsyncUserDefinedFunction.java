// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.UserDefinedFunction;
import reactor.core.publisher.Mono;

public class CosmosAsyncUserDefinedFunction {

    private CosmosAsyncContainer container;
    private String id;

    CosmosAsyncUserDefinedFunction(String id, CosmosAsyncContainer container) {
        this.id = id;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosAsyncUserDefinedFunction}
     * @return the id of the {@link CosmosAsyncUserDefinedFunction}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncUserDefinedFunction}
     * @param id the id of the {@link CosmosAsyncUserDefinedFunction}
     * @return the same {@link CosmosAsyncUserDefinedFunction} that had the id set
     */
    CosmosAsyncUserDefinedFunction id(String id) {
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
    public Mono<CosmosAsyncUserDefinedFunctionResponse> read() {
        return container.getDatabase().getDocClientWrapper().readUserDefinedFunction(getLink(), null)
                .map(response -> new CosmosAsyncUserDefinedFunctionResponse(response, container)).single();
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
    public Mono<CosmosAsyncUserDefinedFunctionResponse> replace(CosmosUserDefinedFunctionProperties udfSettings) {
        return container.getDatabase()
                .getDocClientWrapper()
                .replaceUserDefinedFunction(new UserDefinedFunction(udfSettings.toJson())
                        , null)
                .map(response -> new CosmosAsyncUserDefinedFunctionResponse(response, container))
                .single();
    }

    /**
     * Deletes a cosmos user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted user defined function.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the deleted cosmos user defined function or
     * an error.
     */
    public Mono<CosmosAsyncUserDefinedFunctionResponse> delete() {
        return container.getDatabase()
                .getDocClientWrapper()
                .deleteUserDefinedFunction(this.getLink(), null)
                .map(response -> new CosmosAsyncUserDefinedFunctionResponse(response, container))
                .single();
    }

    String URIPathSegment() {
        return Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT;
    }

    String parentLink() {
        return container.getLink();
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(parentLink());
        builder.append("/");
        builder.append(URIPathSegment());
        builder.append("/");
        builder.append(id());
        return builder.toString();
    }
}
