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

import java.util.List;

import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the response to a request for a list of tables in the storage account returned
 * from a Table Service REST API Query Tables operation. This is returned by calls to implementations of
 * {@link TableContract#queryTables()} and {@link TableContract#queryTables(QueryTablesOptions)}.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179405.aspx">Query Tables</a> documentation
 * on MSDN for details of the underlying Table Service REST API operation.
 */
public class QueryTablesResult {
    private String nextTableName;
    private List<TableEntry> tables;

    /**
     * Gets the next table name continuation token to resume the query tables request with, if any is returned.
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
     * 
     * @return
     *         A {@link String} containing the next table name continuation token to use to resume the query tables
     *         request with, or <code>null</code> if no token was returned.
     */
    public String getNextTableName() {
        return nextTableName;
    }

    /**
     * Reserved for internal use. Sets the next table name continuation token to resume a query tables request with from
     * the <strong>x-ms-continuation-NextTableName</strong> header returned in the response from the server.
     * <p>
     * A query against the Table service may return a maximum of 1,000 items at one time and may execute for a maximum
     * of five seconds. If the result set contains more than 1,000 items, if the query did not complete within five
     * seconds, or if the query crosses the partition boundary, the response includes values which provide the client
     * with continuation tokens to use in order to resume the query at the next item in the result set.
     * <p>
     * This method is invoked by the API to set the value from the Table Service REST API operation response returned by
     * the server.
     * 
     * @param nextTableName
     *            A {@link String} containing the next table name continuation token to use to resume a query tables
     *            request with.
     * @return
     *         A reference to this {@link QueryTablesOptions} instance.
     */
    public void setNextTableName(String nextTableName) {
        this.nextTableName = nextTableName;
    }

    /**
     * Gets the list of table entries returned in the server response.
     * 
     * @return
     *         A {@link List} of {@link TableEntry} instances containing the table entries returned in the response.
     */
    public List<TableEntry> getTables() {
        return tables;
    }

    /**
     * Reserved for internal use. Sets the list of table entries from each <strong>TableName</strong> entity in the
     * properties of the <strong>entry</strong> entities returned in the body of the server response.
     * <p>
     * This method is invoked by the API to set the value from the Table Service REST API operation response returned by
     * the server.
     * 
     * @param tables
     *            A {@link List} of {@link TableEntry} instances containing the table entries returned in the response.
     */
    public void setTables(List<TableEntry> tables) {
        this.tables = tables;
    }
}
