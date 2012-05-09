/**
 * Copyright 2012 Microsoft Corporation
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
 * Represents the options that may be set on a {@link TableContract#queryEntities(String, QueryEntitiesOptions)
 * queryEntities} request. These options include the next partition key and next row key continuation tokens to use to
 * resume the query entities request from, a collection of the property names to include in the entities returned in the
 * server response, a filter to limit results to entities with certain property values, and a top count to limit the
 * response to that number of the first matching results.
 */
public class QueryEntitiesOptions extends TableServiceOptions {

    private List<String> selectFields = new ArrayList<String>();
    private String from;
    private Filter filter;
    private List<String> orderByFields = new ArrayList<String>();
    private Integer top;

    public String nextPartitionKey;
    public String nextRowKey;

    /**
     * Gets the next partition key continuation token set in this {@link QueryEntitiesOptions} instance.
     * 
     * @return
     *         A {@link String} containing the next partition key continuation token to use to resume a query entities
     *         request with.
     */
    public String getNextPartitionKey() {
        return nextPartitionKey;
    }

    /**
     * Sets the next partition key continuation token to resume a query entities request with.
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
     * <p>
     * This value only affects calls made on methods where this {@link QueryEntitiesOptions} instance is passed as a
     * parameter.
     * 
     * @param nextPartitionKey
     *            A {@link String} containing the next partition key continuation token to use to resume a query
     *            entities request with.
     * @return
     *         A reference to this {@link QueryEntitiesOptions} instance.
     */
    public QueryEntitiesOptions setNextPartitionKey(String nextPartitionKey) {
        this.nextPartitionKey = nextPartitionKey;
        return this;
    }

    /**
     * Gets the next row key continuation token set in this {@link QueryEntitiesOptions} instance.
     * 
     * @return
     *         A {@link String} containing the next row key continuation token to use to resume a query entities request
     *         with.
     */
    public String getNextRowKey() {
        return nextRowKey;
    }

    /**
     * Sets the next row key continuation token to resume a query entities request with.
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
     * <p>
     * This value only affects calls made on methods where this {@link QueryEntitiesOptions} instance is passed as a
     * parameter.
     * 
     * @param nextRowKey
     *            A {@link String} containing the next row key continuation token to use to resume a query
     *            entities request with.
     * @return
     *         A reference to this {@link QueryEntitiesOptions} instance.
     */
    public QueryEntitiesOptions setNextRowKey(String nextRowKey) {
        this.nextRowKey = nextRowKey;
        return this;
    }

    /**
     * Gets the collection of properties to include in the entities returned in the server response set in this
     * {@link QueryEntitiesOptions} instance.
     * 
     * @return
     *         The {@link java.util.List} of {@link String} property names to include in the entities returned in the
     *         server response.
     */
    public List<String> getSelectFields() {
        return selectFields;
    }

    /**
     * Sets the collection of properties to include in the entities returned in the server response.
     * 
     * @param selectFields
     *            A {@link java.util.List} of {@link String} property names to include in the entities returned in the
     *            server response.
     * @return
     *         A reference to this {@link QueryEntitiesOptions} instance.
     */
    public QueryEntitiesOptions setSelectFields(List<String> selectFields) {
        this.selectFields = selectFields;
        return this;
    }

    /**
     * Adds a property name to the collection of properties to include in the entities returned in the server response.
     * 
     * @param selectField
     *            A {@link String} property name to include in the entities returned in the server response.
     * @return
     *         A reference to this {@link QueryEntitiesOptions} instance.
     */
    public QueryEntitiesOptions addSelectField(String selectField) {
        this.selectFields.add(selectField);
        return this;
    }

    /**
     * Reserved for future use. Gets the from option value set in this {@link QueryEntitiesOptions} instance.
     * 
     * @return
     *         A {@link String} containing the from option value set.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Reserved for future use. Sets the from option value.
     * 
     * @param from
     *            A {@link String} containing the from option value to set.
     * @return
     *         A reference to this {@link QueryEntitiesOptions} instance.
     */
    public QueryEntitiesOptions setFrom(String from) {
        this.from = from;
        return this;
    }

    /**
     * Gets the table query filter set in this {@link QueryEntitiesOptions} instance.
     * 
     * @return
     *         A {@link Filter} containing the table query filter to apply on the server to limit the entities returned
     *         in the response.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Sets the table query filter to apply on the server to limit the entities returned in the response.
     * 
     * @param filter
     *            A {@link Filter} containing the table query filter to apply on the server to limit the entities
     *            returned in the response.
     * @return
     *         A reference to this {@link QueryEntitiesOptions} instance.
     */
    public QueryEntitiesOptions setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Reserved for future use. Gets the list of property names to use to order the results in the server response set
     * in this {@link QueryEntitiesOptions} instance.
     * 
     * @return
     *         The {@link java.util.List} of {@link String} property names to use to order the results in the server
     *         response.
     */
    public List<String> getOrderByFields() {
        return orderByFields;
    }

    /**
     * Reserved for future use. Sets the list of property names to use to order the results in the server response.
     * 
     * @param orderByFields
     *            A {@link java.util.List} of {@link String} property names to use to order the results in the server
     *            response.
     * @return
     *         A reference to this {@link QueryEntitiesOptions} instance.
     */
    public QueryEntitiesOptions setOrderByFields(List<String> orderByFields) {
        this.orderByFields = orderByFields;
        return this;
    }

    /**
     * Reserved for future use. Adds a property name to the list of property names to use to order the results in the
     * server response.
     * 
     * @param orderByField
     *            A {@link String} containing a property name to add to the list to use to order the results in the
     *            server response.
     * @return
     *         A reference to this {@link QueryEntitiesOptions} instance.
     */
    public QueryEntitiesOptions addOrderByField(String orderByField) {
        this.orderByFields.add(orderByField);
        return this;
    }

    /**
     * Gets the number of entities to return in the server response set in this {@link QueryEntitiesOptions} instance.
     * 
     * @return
     *         The number of entities to return in the server response.
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Sets the number of entities to return in the server response. The first results in order by partition key and row
     * key will be returned.
     * 
     * @param top
     *            The number of entities to return in the server response.
     * @return
     *         A reference to this {@link QueryEntitiesOptions} instance.
     */
    public QueryEntitiesOptions setTop(Integer top) {
        this.top = top;
        return this;
    }
}
