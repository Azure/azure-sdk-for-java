/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.models;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the response to a request for a list of entities in the storage account returned from a Table Service REST
 * API Query Tables operation. This is returned by calls to implementations of
 * {@link TableContract#queryEntities(String)} and {@link TableContract#queryEntities(String, QueryEntitiesOptions)}.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179421.aspx">Query Entities</a> documentation
 * on MSDN for details of the underlying Table Service REST API operation.
 */
public class QueryEntitiesResult {
    private String nextPartitionKey;
    private String nextRowKey;
    private List<Entity> entities = new ArrayList<Entity>();

    /**
     * Gets the list of entities returned in the server response to the query entities request.
     * 
     * @return
     *         A {@link java.util.List} of {@link Entity} instances representing the entities returned in the server
     *         response.
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * Reserved for internal use. Sets the list of entities returned in the server response to the query entities
     * request.
     * <p>
     * This method is invoked by the API to set the value from the Table Service REST API operation response returned by
     * the server.
     * 
     * @param entities
     *            A {@link java.util.List} of {@link Entity} instances representing the entities returned in the server
     *            response.
     */
    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    /**
     * Gets the next partition key continuation token to resume the query entities request with, if any is returned.
     * <p>
     * A query against the Table service may return a maximum of 1,000 items at one time and may execute for a maximum
     * of five seconds. If the result set contains more than 1,000 items, if the query did not complete within five
     * seconds, or if the query crosses the partition boundary, the response includes values which provide the client
     * with continuation tokens to use in order to resume the query at the next item in the result set.
     * <p>
     * Use the {@link QueryEntitiesResult#getNextPartitionKey()} and {@link QueryEntitiesResult#getNextRowKey()} methods
     * on the result of a query entities request to determine if there are more results to retrieve. If so, set the
     * values returned on a {@link QueryEntitiesOptions} instance with the {@link #setNextPartitionKey(String)} and
     * {@link #setNextRowKey(String)} methods, and resume the query with another call to
     * {@link TableContract#queryEntities(String, QueryEntitiesOptions)} to get the next set of results.
     * <p>
     * It is possible for a query to return no results but to still return a continuation token.
     * 
     * @return
     *         A {@link String} containing the next partition key continuation token to use to resume the query entities
     *         request with, or <code>null</code> if no token was returned.
     */
    public String getNextPartitionKey() {
        return nextPartitionKey;
    }

    /**
     * Reserved for internal use. Sets the next partition key continuation token to resume a query entities request with
     * from the <code>x-ms-continuation-NextPartitionKey</code> header returned in the response from the server.
     * <p>
     * A query against the Table service may return a maximum of 1,000 items at one time and may execute for a maximum
     * of five seconds. If the result set contains more than 1,000 items, if the query did not complete within five
     * seconds, or if the query crosses the partition boundary, the response includes values which provide the client
     * with continuation tokens to use in order to resume the query at the next item in the result set.
     * <p>
     * This method is invoked by the API to set the value from the Table Service REST API operation response returned by
     * the server.
     * 
     * @param nextPartitionKey
     *            A {@link String} containing the next partition key continuation token to use to resume a query
     *            entities request with.
     */
    public void setNextPartitionKey(String nextPartitionKey) {
        this.nextPartitionKey = nextPartitionKey;
    }

    /**
     * Gets the next row key continuation token to resume the query tables request with, if any is returned.
     * <p>
     * A query against the Table service may return a maximum of 1,000 items at one time and may execute for a maximum
     * of five seconds. If the result set contains more than 1,000 items, if the query did not complete within five
     * seconds, or if the query crosses the partition boundary, the response includes values which provide the client
     * with continuation tokens to use in order to resume the query at the next item in the result set.
     * <p>
     * Use the {@link QueryEntitiesResult#getNextPartitionKey()} and {@link QueryEntitiesResult#getNextRowKey()} methods
     * on the result of a query entities request to determine if there are more results to retrieve. If so, set the
     * values returned on a {@link QueryEntitiesOptions} instance with the {@link #setNextPartitionKey(String)} and
     * {@link #setNextRowKey(String)} methods, and resume the query with another call to
     * {@link TableContract#queryEntities(String, QueryEntitiesOptions)} to get the next set of results.
     * <p>
     * It is possible for a query to return no results but to still return a continuation token.
     * 
     * @return
     *         A {@link String} containing the next row key continuation token to use to resume the query tables
     *         request with, or <code>null</code> if no token was returned.
     */
    public String getNextRowKey() {
        return nextRowKey;
    }

    /**
     * Reserved for internal use. Sets the next row key continuation token to resume a query entities request with from
     * the <code>x-ms-continuation-NextRowKey</code> header returned in the response from the server.
     * <p>
     * A query against the Table service may return a maximum of 1,000 items at one time and may execute for a maximum
     * of five seconds. If the result set contains more than 1,000 items, if the query did not complete within five
     * seconds, or if the query crosses the partition boundary, the response includes values which provide the client
     * with continuation tokens to use in order to resume the query at the next item in the result set.
     * <p>
     * This method is invoked by the API to set the value from the Table Service REST API operation response returned by
     * the server.
     * 
     * @param nextRowKey
     *            A {@link String} containing the next row key continuation token to use to resume a query entities
     *            request with.
     */
    public void setNextRowKey(String nextRowKey) {
        this.nextRowKey = nextRowKey;
    }
}
