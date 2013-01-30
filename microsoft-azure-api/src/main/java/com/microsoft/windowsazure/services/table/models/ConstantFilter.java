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
 * Represents a constant value used as a filter parameter in a table query request.
 * <p>
 * Use the static factory method in the {@link Filter} class to create a {@link ConstantFilter}, rather than
 * constructing one directly.
 * <p>
 * Use this class to pass a constant value as a filter parameter. The value is passed as an <code>Object</code> but must
 * be (or have an implicit conversion to) one of the following for successful serialization:
 * <ul>
 * <li><code>null</code></li>
 * <li><code>byte[]</code></li>
 * <li><code>Byte[]</code></li>
 * <li><code>Date</code></li>
 * <li><code>Long</code></li>
 * <li><code>String</code></li>
 * <li><code>UUID</code></li>
 * </ul>
 * <p>
 * A {@link PropertyNameFilter} may be combined in a {@link BinaryFilter} with a comparison operator and a
 * {@link ConstantFilter} to limit query results to properties with values that match the {@link ConstantFilter} value.
 * The table service does not support wildcard queries, but you can perform prefix matching by using comparison
 * operators on the desired prefix created as a {@link String} in a {@link ConstantFilter} instance.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894031.aspx">Querying Tables and Entities</a>
 * topic in MSDN for more information on creating table query filter strings.
 */
public class ConstantFilter extends Filter {
    private final Object value;

    /**
     * Creates a table query filter constant from the <em>value</em> parameter.
     * <p>
     * Use the static factory method in the {@link Filter} class to create a {@link ConstantFilter}, rather than
     * constructing one directly.
     * 
     * @param value
     *            An {@link Object} containing the constant value to use as a filter parameter in a table query request.
     */
    public ConstantFilter(Object value) {
        this.value = value;
    }

    /**
     * Gets the constant filter value set in this {@link ConstantFilter} instance.
     * 
     * @return
     *         The {@link Object} containing the constant value to use as a filter parameter in a table query request.
     */
    public Object getValue() {
        return value;
    }
}
