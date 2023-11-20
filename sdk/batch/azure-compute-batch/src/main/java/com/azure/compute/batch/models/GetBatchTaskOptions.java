// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import com.azure.core.http.RequestConditions;
import java.util.List;

/**
 * Optional parameters for getting information about a Batch Task.
 */
public class GetBatchTaskOptions extends BatchBaseOptions {
    private List<String> expand;
    private RequestConditions requestConditions;
    private List<String> select;

    /**
     * Gets the OData $expand clause.
     *
     * <p>The $expand clause specifies related entities or complex properties to include in the response.
     *
     * @return The OData $expand clause.
     */
    public List<String> getExpand() {
        return expand;
    }

    /**
     * Sets the OData $expand clause.
     *
     * <p>The $expand clause specifies related entities or complex properties to include in the response.
     *
     * @param expand The OData $expand clause.
     * @return The {@link GetBatchTaskOptions} object itself, allowing for method chaining.
     */
    public GetBatchTaskOptions setExpand(List<String> expand) {
        this.expand = expand;
        return this;
    }

     /**
     * Gets the HTTP options for conditional requests based on modification time.
     *
     * <p>The request conditions allow you to specify conditions that must be met for the request to be processed.
     *
     * @return The HTTP options for conditional requests.
     */
    public RequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the HTTP options for conditional requests based on modification time.
     *
     * <p>The request conditions allow you to specify conditions that must be met for the request to be processed.
     *
     * @param requestConditions The HTTP options for conditional requests.
     * @return The {@link GetBatchTaskOptions} object itself, allowing for method chaining.
     */
    public GetBatchTaskOptions setRequestConditions(RequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the OData $select clause.
     *
     * The $select clause specifies which properties should be included in the response.
     *
     * @return The OData $select clause.
     */
    public List<String> getSelect() {
        return select;
    }

    /**
     * Sets the OData $select clause.
     *
     * The $select clause specifies which properties should be included in the response.
     *
     * @param select The OData $select clause.
     * @return The {@link GetBatchTaskOptions} object itself, allowing for method chaining.
     */
    public GetBatchTaskOptions setSelect(List<String> select) {
        this.select = select;
        return this;
    }

}
