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
 * Represents a property name used as a filter parameter in a table query request.
 * <p>
 * Use the static factory method in the {@link Filter} class to create a {@link PropertyNameFilter}, rather than
 * constructing one directly.
 * <p>
 * Use this class to pass a property name as a filter parameter. When the filter is evaluated, the content of the named
 * property in the entity is used in the filter expression.
 * <p>
 * Note that case is significant for the <strong>PartitionKey</strong> and <strong>RowKey</strong> property names in a
 * filter.
 * <p>
 * A {@link PropertyNameFilter} may be combined in a {@link BinaryFilter} with a comparison operator and a
 * {@link ConstantFilter} to limit query results to properties with values that match the {@link ConstantFilter} value.
 * The table service does not support wildcard queries, but you can perform prefix matching by using comparison
 * operators on the desired prefix created as a {@link String} in a {@link ConstantFilter} instance.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894031.aspx">Querying Tables and Entities</a>
 * topic in MSDN for more information on creating table query filter strings.
 */
public class PropertyNameFilter extends Filter {
    private final String propertyName;

    /**
     * Creates a property name table query filter from the <em>propertyName</em> parameter.
     * <p>
     * Use the static factory method in the {@link Filter} class to create a {@link PropertyNameFilter}, rather than
     * constructing one directly.
     * 
     * @param propertyName
     *            A {@link String} containing the property name to use as a filter parameter in a table query request.
     */
    public PropertyNameFilter(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Gets the property name set in this {@link PropertyNameFilter} instance.
     * 
     * @return
     *         The {@link String} containing the property name to use as a filter parameter in a table query request.
     */
    public String getPropertyName() {
        return propertyName;
    }

}
