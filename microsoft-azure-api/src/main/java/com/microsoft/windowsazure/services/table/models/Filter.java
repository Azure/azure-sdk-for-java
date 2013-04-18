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
 * Represents a table query filter expression, which can be used as an option in a
 * {@link TableContract#queryEntities(String, QueryEntitiesOptions)} request.
 * <p>
 * A filter expression is built of operands consisting of property names, constant values, pre-computed query strings,
 * or other filter expressions. Filter expressions may be combined with unary and binary operators to create complex
 * expressions.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894031.aspx">Querying Tables and Entities</a>
 * topic in MSDN for more information on creating table query filters.
 */
public class Filter {
    /**
     * A static factory method that creates a boolean filter expression that is the logical 'not' of the
     * <em>operand</em> parameter.
     * 
     * @param operand
     *            A {@link Filter} instance containing a boolean filter expression.
     * @return
     *         A {@link UnaryFilter} instance containing a filter expression that is the logical 'not' of the
     *         <em>operand</em> parameter.
     */
    public static UnaryFilter not(Filter operand) {
        return new UnaryFilter("not", operand);
    }

    /**
     * A static factory method that creates a boolean filter expression that is the logical 'and' of the <em>left</em>
     * and <em>right</em> parameters.
     * 
     * @param left
     *            A {@link Filter} instance containing a boolean filter expression.
     * @param right
     *            A {@link Filter} instance containing a boolean filter expression.
     * @return
     *         A {@link BinaryFilter} instance containing a filter expression that is the logical 'and' of the
     *         <em>left</em> and <em>right</em> parameters.
     */
    public static BinaryFilter and(Filter left, Filter right) {
        return new BinaryFilter(left, "and", right);
    }

    /**
     * A static factory method that creates a boolean filter expression that is the logical 'or' of the <em>left</em>
     * and <em>right</em> parameters.
     * 
     * @param left
     *            A {@link Filter} instance containing a boolean filter expression.
     * @param right
     *            A {@link Filter} instance containing a boolean filter expression.
     * @return
     *         A {@link BinaryFilter} instance containing a filter expression that is the logical 'or' of the
     *         <em>left</em> and <em>right</em> parameters.
     */
    public static BinaryFilter or(Filter left, Filter right) {
        return new BinaryFilter(left, "or", right);
    }

    /**
     * A static factory method that creates a boolean filter expression that expresses whether the <em>left</em> and
     * <em>right</em> parameters are equal.
     * <p>
     * Use this method to create a filter that compares the content of a property with a constant value.
     * <p>
     * The value must be of the same type as the property for the comparison operation to return valid results. Note
     * that it is not possible to compare a property to a dynamic value; one side of the expression must be a constant.
     * 
     * @param left
     *            A {@link Filter} instance containing a filter expression.
     * @param right
     *            A {@link Filter} instance containing a filter expression.
     * @return
     *         A {@link BinaryFilter} instance containing a filter expression expresses whether the <em>left</em> and
     *         <em>right</em> parameters are equal.
     */
    public static BinaryFilter eq(Filter left, Filter right) {
        return new BinaryFilter(left, "eq", right);
    }

    /**
     * A static factory method that creates a boolean filter expression that expresses whether the <em>left</em> and
     * <em>right</em> parameters are not equal.
     * <p>
     * Use this method to create a filter that compares the content of a property with a constant value.
     * <p>
     * The value must be of the same type as the property for the comparison operation to return valid results. Note
     * that it is not possible to compare a property to a dynamic value; one side of the expression must be a constant.
     * 
     * @param left
     *            A {@link Filter} instance containing a filter expression.
     * @param right
     *            A {@link Filter} instance containing a filter expression.
     * @return
     *         A {@link BinaryFilter} instance containing a filter expression expresses whether the <em>left</em> and
     *         <em>right</em> parameters are not equal.
     */
    public static BinaryFilter ne(Filter left, Filter right) {
        return new BinaryFilter(left, "ne", right);
    }

    /**
     * A static factory method that creates a boolean filter expression that expresses whether the value of the
     * <em>left</em> parameter is greater than or equal to the value of the <em>right</em> parameter.
     * <p>
     * Use this method to create a filter that compares the content of a property with a constant value.
     * <p>
     * The value must be of the same type as the property for the comparison operation to return valid results. Note
     * that it is not possible to compare a property to a dynamic value; one side of the expression must be a constant.
     * 
     * @param left
     *            A {@link Filter} instance containing a filter expression.
     * @param right
     *            A {@link Filter} instance containing a filter expression.
     * @return
     *         A {@link BinaryFilter} instance containing a filter expression expresses whether the value of the
     *         <em>left</em> parameter is greater than or equal to the value of the <em>right</em> parameter.
     */
    public static BinaryFilter ge(Filter left, Filter right) {
        return new BinaryFilter(left, "ge", right);
    }

    /**
     * A static factory method that creates a boolean filter expression that expresses whether the value of the
     * <em>left</em> parameter is greater than the value of the <em>right</em> parameter.
     * <p>
     * Use this method to create a filter that compares the content of a property with a constant value.
     * <p>
     * The value must be of the same type as the property for the comparison operation to return valid results. Note
     * that it is not possible to compare a property to a dynamic value; one side of the expression must be a constant.
     * 
     * @param left
     *            A {@link Filter} instance containing a filter expression.
     * @param right
     *            A {@link Filter} instance containing a filter expression.
     * @return
     *         A {@link BinaryFilter} instance containing a filter expression expresses whether the value of the
     *         <em>left</em> parameter is greater than the value of the <em>right</em> parameter.
     */
    public static BinaryFilter gt(Filter left, Filter right) {
        return new BinaryFilter(left, "gt", right);
    }

    /**
     * A static factory method that creates a boolean filter expression that expresses whether the value of the
     * <em>left</em> parameter is less than the value of the <em>right</em> parameter.
     * <p>
     * Use this method to create a filter that compares the content of a property with a constant value.
     * <p>
     * The value must be of the same type as the property for the comparison operation to return valid results. Note
     * that it is not possible to compare a property to a dynamic value; one side of the expression must be a constant.
     * 
     * @param left
     *            A {@link Filter} instance containing a filter expression.
     * @param right
     *            A {@link Filter} instance containing a filter expression.
     * @return
     *         A {@link BinaryFilter} instance containing a filter expression expresses whether the value of the
     *         <em>left</em> parameter is less than the value of the <em>right</em> parameter.
     */
    public static BinaryFilter lt(Filter left, Filter right) {
        return new BinaryFilter(left, "lt", right);
    }

    /**
     * A static factory method that creates a boolean filter expression that expresses whether the value of the
     * <em>left</em> parameter is less than or equal to the value of the <em>right</em> parameter.
     * <p>
     * Use this method to create a filter that compares the content of a property with a constant value.
     * <p>
     * The value must be of the same type as the property for the comparison operation to return valid results. Note
     * that it is not possible to compare a property to a dynamic value; one side of the expression must be a constant.
     * 
     * @param left
     *            A {@link Filter} instance containing a filter expression.
     * @param right
     *            A {@link Filter} instance containing a filter expression.
     * @return
     *         A {@link BinaryFilter} instance containing a filter expression expresses whether the value of the
     *         <em>left</em> parameter is less than or equal to the value of the <em>right</em> parameter.
     */
    public static BinaryFilter le(Filter left, Filter right) {
        return new BinaryFilter(left, "le", right);
    }

    /**
     * A static factory method that creates a constant value to use as an operand in a {@link BinaryFilter} expression.
     * Case is significant in comparison expressions with constants of type {@link String}.
     * 
     * @param value
     *            An <code>Object</code> reference to a constant value of a supported type, or <code>null</code>.
     * @return
     *         A {@link ConstantFilter} instance containing the constant value for use in a filter expression.
     */
    public static ConstantFilter constant(Object value) {
        return new ConstantFilter(value);
    }

    /**
     * A static factory method that creates a property name value to use as an operand in a {@link BinaryFilter}
     * expression. When the filter is evaluated, the content of the named property in the entity is used as the operand.
     * <p>
     * Note that case is significant for the <strong>PartitionKey</strong> and <strong>RowKey</strong> property names.
     * 
     * @param value
     *            A {@link String} containing the name of a property.
     * @return
     *         A {@link ConstantFilter} instance containing the constant value for use in a filter expression.
     */
    public static PropertyNameFilter propertyName(String value) {
        return new PropertyNameFilter(value);
    }

    /**
     * A static factory method that creates a table query filter string from the contents of the <em>value</em>
     * parameter.
     * 
     * @param value
     *            A {@link String} containing a table query filter string.
     * 
     * @return
     *         A {@link QueryStringFilter} instance containing the table query filter string.
     */
    public static QueryStringFilter queryString(String value) {
        return new QueryStringFilter(value);
    }
}
