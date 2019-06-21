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

public class CosmosStoredProcedure extends CosmosResource {

    private CosmosContainer cosmosContainer;

    CosmosStoredProcedure(String id, CosmosContainer cosmosContainer) {
        super(id);
        this.cosmosContainer = cosmosContainer;
    }

    @Override
    protected String URIPathSegment() {
        return Paths.STORED_PROCEDURES_PATH_SEGMENT;
    }

    @Override
    protected String parentLink() {
        return cosmosContainer.getLink();
    }

    /**
     * READ a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the read stored
     * procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the read stored procedure or an error.
     */
    public Mono<CosmosStoredProcedureResponse> read(RequestOptions options) {
        return cosmosContainer.getDatabase().getDocClientWrapper().readStoredProcedure(getLink(), options)
                .map(response -> new CosmosStoredProcedureResponse(response, cosmosContainer)).single();
    }

    /**
     * Deletes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response for the deleted stored procedure or an error.
     */
    public Mono<CosmosResponse> delete(CosmosRequestOptions options) {
        return cosmosContainer.getDatabase()
                .getDocClientWrapper()
                .deleteStoredProcedure(getLink(), options.toRequestOptions())
                .map(response -> new CosmosResponse(response.getResource()))
                .single();
    }

    /**
     * Executes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the stored procedure response.
     * In case of failure the {@link Mono} will error.
     *
     * @param procedureParams the array of procedure parameter values.
     * @param options         the request options.
     * @return an {@link Mono} containing the single resource response with the stored procedure response or an error.
     */
    public Mono<CosmosStoredProcedureResponse> execute(Object[] procedureParams, RequestOptions options) {
        return cosmosContainer.getDatabase()
                .getDocClientWrapper()
                .executeStoredProcedure(getLink(), options, procedureParams)
                .map(response -> new CosmosStoredProcedureResponse(response, cosmosContainer))
                .single();
    }

    /**
     * Replaces a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced stored procedure.
     * In case of failure the {@link Mono} will error.
     *
     * @param storedProcedureSettings the stored procedure settings.
     * @param options                 the request options.
     * @return an {@link Mono} containing the single resource response with the replaced stored procedure or an error.
     */
    public Mono<CosmosStoredProcedureResponse> replace(CosmosStoredProcedureSettings storedProcedureSettings,
                                                       RequestOptions options) {
        return cosmosContainer.getDatabase()
                .getDocClientWrapper()
                .replaceStoredProcedure(new StoredProcedure(storedProcedureSettings.toJson()), options)
                .map(response -> new CosmosStoredProcedureResponse(response, cosmosContainer))
                .single();
    }

}
