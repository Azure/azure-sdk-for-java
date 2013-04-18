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

/**
 * Represents a table query filter string passed directly as a filter parameter in a table query request.
 * <p>
 * Use the static factory method in the {@link Filter} class to create a {@link QueryStringFilter}, rather than
 * constructing one directly.
 * <p>
 * Use this class to pass a literal query filter string without interpretation as a filter parameter. For example, if
 * you have a created a query filter expression independently, this class may be used to add it to a filter expression.
 * <p>
 * The following characters must be URL-encoded if they are to be used in a query string:
 * <ul>
 * <li>Forward slash (/)</li>
 * <li>Question mark (?)</li>
 * <li>Colon (:)</li>
 * <li>'At' symbol (@)</li>
 * <li>Ampersand (&)</li>
 * <li>Equals sign (=)</li>
 * <li>Plus sign (+)</li>
 * <li>Comma (,)</li>
 * <li>Dollar sign ($)</li>
 * </ul>
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894031.aspx">Querying Tables and Entities</a>
 * topic in MSDN for more information on creating table query filter strings.
 */
public class QueryStringFilter extends Filter {
    private final String queryString;

    /**
     * Creates a table query filter from the <em>queryString</em> parameter.
     * <p>
     * Use the static factory method in the {@link Filter} class to create a {@link QueryStringFilter}, rather than
     * constructing one directly.
     * 
     * @param queryString
     *            A {@link String} containing a table query filter to pass directly as a filter parameter in a table
     *            query request.
     */
    public QueryStringFilter(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Gets the table query filter string set in this {@link QueryStringFilter} instance.
     * 
     * @return
     *         A {@link String} containing a table query filter to pass directly as a filter parameter in a table
     *         query request.
     */
    public String getQueryString() {
        return queryString;
    }
}
