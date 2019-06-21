/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Paths;
import reactor.core.publisher.Mono;

public class CosmosUserDefinedFunction extends CosmosResource {

    private CosmosContainer container;

    CosmosUserDefinedFunction(String id, CosmosContainer container) {
        super(id);
        this.container = container;
    }

    /**
     * READ a user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the read user defined
     * function.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response for the read user defined function or an error.
     */
    public Mono<CosmosUserDefinedFunctionResponse> read(RequestOptions options) {
        return container.getDatabase().getDocClientWrapper().readUserDefinedFunction(getLink(), options)
                .map(response -> new CosmosUserDefinedFunctionResponse(response, container)).single();
    }

    /**
     * Replaces a cosmos user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced user
     * defined function.
     * In case of failure the {@link Mono} will error.
     *
     * @param udfSettings the cosmos user defined function settings.
     * @param options     the request options.
     * @return an {@link Mono} containing the single resource response with the replaced cosmos user defined function
     * or an error.
     */
    public Mono<CosmosUserDefinedFunctionResponse> replace(CosmosUserDefinedFunctionSettings udfSettings,
                                                           RequestOptions options) {
        return container.getDatabase()
                .getDocClientWrapper()
                .replaceUserDefinedFunction(new UserDefinedFunction(udfSettings.toJson())
                        , options)
                .map(response -> new CosmosUserDefinedFunctionResponse(response, container))
                .single();
    }

    /**
     * Deletes a cosmos user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted user defined function.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response for the deleted cosmos user defined function or
     * an error.
     */
    public Mono<CosmosResponse> delete(CosmosRequestOptions options) {
        return container.getDatabase()
                .getDocClientWrapper()
                .deleteUserDefinedFunction(this.getLink(), options.toRequestOptions())
                .map(response -> new CosmosResponse(response.getResource()))
                .single();
    }

    @Override
    protected String URIPathSegment() {
        return Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT;
    }

    @Override
    protected String parentLink() {
        return container.getLink();
    }
}
