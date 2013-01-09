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

import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the options that may be set on a {@link TableContract#queryTables(QueryTablesOptions) queryTables}
 * request. These options include a filter to limit results to tables with certain properties, the next table name
 * continuation token to use to resume the query tables request from, and a prefix string to match table names with.
 */
public class QueryTablesOptions extends TableServiceOptions {
    private Filter filter;
    private String nextTableName;
    private String prefix;

    /**
     * Gets the filter to use to limit table entries returned set in this {@link TableServiceOptions} instance.
     * 
     * @return
     *         A {@link Filter} instance containing the filter options to use.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Sets the filter to use to limit table entries returned by the request to those that match the filter properties.
     * <p>
     * This value only affects calls made on methods where this {@link QueryTablesOptions} instance is passed as a
     * parameter.
     * 
     * @param filter
     *            A {@link Filter} instance containing the filter options to use.
     * @return
     *         A reference to this {@link QueryTablesOptions} instance.
     */
    public QueryTablesOptions setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Gets the next table name continuation token set in this {@link TableServiceOptions} instance.
     * 
     * @return
     *         A {@link String} containing the next table name continuation token to use to resume a query tables
     *         request with.
     */
    public String getNextTableName() {
        return nextTableName;
    }

    /**
     * Sets the next table name continuation token to resume a query tables request with.
     * <p>
     * A query against the Table service may return a maximum of 1,000 items at one time and may execute for a maximum
     * of five seconds. If the result set contains more than 1,000 items, if the query did not complete within five
     * seconds, or if the query crosses the partition boundary, the response includes values which provide the client
     * with continuation tokens to use in order to resume the query at the next item in the result set.
     * <p>
     * Use the {@link QueryTablesResult#getNextTableName()} method on the result of a query tables request to determine
     * if there are more results to retrieve. If so, pass the value returned as the <em>nextTableName</em> parameter to
     * this method to set the next table name continuation token option, and resume the query with another call to
     * {@link TableContract#queryTables(QueryTablesOptions)} to get the next set of results.
     * <p>
     * It is possible for a query to return no results but to still return a continuation token.
     * <p>
     * This value only affects calls made on methods where this {@link QueryTablesOptions} instance is passed as a
     * parameter.
     * 
     * @param nextTableName
     *            A {@link String} containing the next table name continuation token to use to resume a query tables
     *            request with.
     * @return
     *         A reference to this {@link QueryTablesOptions} instance.
     */
    public QueryTablesOptions setNextTableName(String nextTableName) {
        this.nextTableName = nextTableName;
        return this;
    }

    /**
     * Gets the prefix filter associated with this {@link QueryTablesOptions} instance. This value is used to return
     * only tables from the storage account with names that begin with the prefix in the response to a query tables
     * request.
     * 
     * @return
     *         A {@link String} containing the prefix used to filter the table names returned, if any.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the optional table name prefix filter value to use in a request. If this value is set, the server will
     * return only table entries with names that match the prefix value in the response.
     * <p>
     * The <em>prefix</em> value only affects calls made on methods where this {@link QueryTablesOptions} instance is
     * passed as a parameter.
     * 
     * @param prefix
     *            A {@link String} containing a prefix to use to filter the table names returned.
     */
    public QueryTablesOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }
}
