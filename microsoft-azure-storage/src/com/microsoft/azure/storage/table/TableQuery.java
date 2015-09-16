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

package com.microsoft.azure.storage.table;

import java.util.Date;
import java.util.Formatter;
import java.util.UUID;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.UriQueryBuilder;
import com.microsoft.azure.storage.core.Utility;

/**
 * A class which represents a query against a specified table. A {@link TableQuery} instance aggregates the query
 * parameters to use when the query is executed. One of the <code>execute</code> or <code>executeSegmented</code>
 * methods of {@link CloudTableClient} must be called to execute the query. The parameters are encoded and passed to the
 * server when the table query is executed.
 * <p>
 * To create a table query with fluent syntax, the {@link #from} static factory method and the {@link #where},
 * {@link #select}, and {@link #take} mutator methods each return a reference to the object which can be chained into a
 * single expression. Use the {@link #from(Class)} static class factory method to create a
 * <code>TableQuery</code> instance that executes on the named table with entities of the specified {@link TableEntity}
 * implementing type. Use the {@link #where} method to specify a filter expression for the entities returned. Use the
 * {@link #select} method to specify the table entity properties to return. Use the {@link #take} method to limit the
 * number of entities returned by the query. Note that nothing prevents calling these methods more than once on a
 * <code>TableQuery</code>, so the values saved in the <code>TableQuery</code> will be the last encountered in order of
 * execution.
 * <p>
 * As an example, you could construct a table query using fluent syntax:
 * <p>
 * <code>TableQuery&ltTableServiceEntity> myQuery = TableQuery.from("Products", DynamicTableEntity.class)<br>
 * &nbsp&nbsp&nbsp&nbsp.where("(PartitionKey eq 'ProductsMNO') and (RowKey ge 'Napkin')")<br>
 * &nbsp&nbsp&nbsp&nbsp.take(25)<br>
 * &nbsp&nbsp&nbsp&nbsp.select(new String[] {"InventoryCount"});</code>
 * <p>
 * This example creates a query on the "Products" table for all entities where the PartitionKey value is "ProductsMNO"
 * and the RowKey value is greater than or equal to "Napkin" and requests the first 25 matching entities, selecting only
 * the common properties and the property named "InventoryCount", and returns them as {@link DynamicTableEntity}
 * objects.
 * <p>
 * Filter expressions for use with the {@link #where} method or {@link #setFilterString} method can be created using
 * fluent syntax with the overloaded {@link #generateFilterCondition} methods and {@link #combineFilters} method, using
 * the comparison operators defined in {@link QueryComparisons} and the logical operators defined in {@link Operators}.
 * Note that the first operand in a filter comparison must be a property name, and the second operand must evaluate to a
 * constant. The PartitionKey and RowKey property values are <code>String</code> types for comparison purposes.
 * <p>
 * The values that may be used in table queries are explained in more detail in the MSDN topic <a
 * href="http://msdn.microsoft.com/en-us/library/azure/dd894031.aspx">Querying Tables and Entities</a>, but note that
 * the space characters within values do not need to be URL-encoded, as this will be done when the query is executed.
 * <p>
 * The {@link TableQuery#TableQuery(Class)} constructor and {@link TableQuery#from(Class)} static factory methods
 * require a class type which implements {@link TableEntity} and contains a nullary constructor. If the query will be
 * executed using an {@link EntityResolver}, the caller may specify {@link TableServiceEntity} <code>.class</code> as
 * the class type.
 * 
 * @param <T>
 *            A class type which implements {@link TableEntity} and contains a nullary constructor. Note: when using an
 *            inner class to define the class type, mark the class as static.
 */
public class TableQuery<T extends TableEntity> {
    /**
     * A static class that maps identifiers to filter expression operators.
     */
    public static class Operators {
        /**
         * And
         */
        public static final String AND = "and";

        /**
         * Not
         */
        public static final String NOT = "not";

        /**
         * Or
         */
        public static final String OR = "or";
    }

    /**
     * A static class that maps identifiers to filter property comparison operators.
     */
    public static class QueryComparisons {
        /**
         * Equal
         */
        public static final String EQUAL = "eq";

        /**
         * Not Equal
         */
        public static final String NOT_EQUAL = "ne";

        /**
         * Greater Than
         */
        public static final String GREATER_THAN = "gt";

        /**
         * Greater Than Or Equal
         */
        public static final String GREATER_THAN_OR_EQUAL = "ge";

        /**
         * Less Than
         */
        public static final String LESS_THAN = "lt";

        /**
         * Less Than Or Equal
         */
        public static final String LESS_THAN_OR_EQUAL = "le";
    }

    /**
     * A static factory method that constructs a {@link TableQuery} instance and defines its table entity type. The
     * method returns the {@link TableQuery} instance reference, allowing additional methods to be chained to modify the
     * query.
     * <p>
     * The created {@link TableQuery} instance is specialized for table entities of the specified class type T. Callers
     * may specify {@link TableServiceEntity} <code>.class</code> as the class type parameter if no more specialized
     * type is required.
     * 
     * @param clazzType
     *            The <code>java.lang.Class</code> of the class <code>T</code> implementing the {@link TableEntity}
     *            interface that represents the table entity type for the query.
     * 
     * @return
     *         The {@link TableQuery} instance with the entity type specialization set.
     * 
     */
    public static <T extends TableEntity> TableQuery<T> from(final Class<T> clazzType) {
        return new TableQuery<T>(clazzType);
    }

    /**
     * Generates a property filter condition string for a <code>boolean</code> value. Creates a formatted string to use
     * in a filter expression that uses the specified operation to compare the property with the value, formatted as a
     * boolean, as in the following example:
     * <p>
     * <code>String condition = generateFilterCondition("BooleanProperty", QueryComparisons.EQUAL, false);</code>
     * <p>
     * This statement sets <code>condition</code> to the following value:
     * <p>
     * <code>BooleanProperty eq false</code>
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            A <code>boolean</code> which specifies the value to compare with the property.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, final boolean value) {
        return generateFilterCondition(propertyName, operation, value ? Constants.TRUE : Constants.FALSE,
                EdmType.BOOLEAN);
    }

    /**
     * Generates a property filter condition string for a <code>byte[]</code> value. Creates a formatted string to use
     * in a filter expression that uses the specified operation to compare the property with the value, formatted as a
     * binary value, as in the following example:
     * <p>
     * <code>String condition = generateFilterCondition("ByteArray", QueryComparisons.EQUAL, new byte[] {0x01, 0x0f});</code>
     * <p>
     * This statement sets <code>condition</code> to the following value:
     * <p>
     * <code>ByteArray eq X'010f'</code>
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            A <code>byte</code> array which specifies the value to compare with the property.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, final byte[] value) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        for (byte b : value) {
            formatter.format("%02x", b);
        }
        formatter.flush();
        formatter.close();

        return generateFilterCondition(propertyName, operation, sb.toString(), EdmType.BINARY);
    }

    /**
     * Generates a property filter condition string for a <code>Byte[]</code> value. Creates a formatted string to use
     * in a filter expression that uses the specified operation to compare the property with the value, formatted as a
     * binary value, as in the following example:
     * <p>
     * <code>String condition = generateFilterCondition("ByteArray", QueryComparisons.EQUAL, new Byte[] {0x01, 0xfe});</code>
     * <p>
     * This statement sets <code>condition</code> to the following value:
     * <p>
     * <code>ByteArray eq X'01fe'</code>
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            A <code>Byte</code> array which specifies the value to compare with the property.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, final Byte[] value) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        for (byte b : value) {
            formatter.format("%02x", b);
        }
        formatter.flush();
        formatter.close();

        return generateFilterCondition(propertyName, operation, sb.toString(), EdmType.BINARY);
    }

    /**
     * Generates a property filter condition string for a <code>java.util.Date</code> value. Creates a formatted string to use in
     * a filter expression that uses the specified operation to compare the property with the value, formatted as a
     * datetime value, as in the following example:
     * <p>
     * <code>String condition = generateFilterCondition("FutureDate", QueryComparisons.GREATER_THAN, new Date());</code>
     * <p>
     * This statement sets <code>condition</code> to something like the following value:
     * <p>
     * <code>FutureDate gt datetime'2013-01-31T09:00:00'</code>
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            A <code>java.util.Date</code> which specifies the value to compare with the property.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, final Date value) {
        return generateFilterCondition(propertyName, operation,
                Utility.getJavaISO8601Time(value), EdmType.DATE_TIME);
    }

    /**
     * Generates a property filter condition string for a <code>double</code> value. Creates a formatted string to use
     * in a filter expression that uses the specified operation to compare the property with the value, formatted as
     * a double value, as in the following example:
     * <p>
     * <code>String condition = generateFilterCondition("Circumference", QueryComparisons.EQUAL, 2 * 3.141592);</code>
     * <p>
     * This statement sets <code>condition</code> to the following value:
     * <p>
     * <code>Circumference eq 6.283184</code>
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            A <code>double</code> which specifies the value to compare with the property.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, final double value) {
        return generateFilterCondition(propertyName, operation, Double.toString(value), EdmType.DOUBLE);
    }

    /**
     * Generates a property filter condition string for an <code>int</code> value. Creates a formatted string to use
     * in a filter expression that uses the specified operation to compare the property with the value, formatted as
     * a numeric value, as in the following example:
     * <p>
     * <code>String condition = generateFilterCondition("Population", QueryComparisons.GREATER_THAN, 1000);</code>
     * <p>
     * This statement sets <code>condition</code> to the following value:
     * <p>
     * <code>Population gt 1000</code>
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            An <code>int</code> which specifies the value to compare with the property.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, final int value) {
        return generateFilterCondition(propertyName, operation, Integer.toString(value), EdmType.INT32);
    }

    /**
     * Generates a property filter condition string for a <code>long</code> value. Creates a formatted string to use
     * in a filter expression that uses the specified operation to compare the property with the value, formatted as
     * a numeric value, as in the following example:
     * <p>
     * <code>String condition = generateFilterCondition("StellarMass", QueryComparisons.GREATER_THAN, 7000000000L);</code>
     * <p>
     * This statement sets <code>condition</code> to the following value:
     * <p>
     * <code>StellarMass gt 7000000000</code>
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            A <code>long</code> which specifies the value to compare with the property.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, final long value) {
        return generateFilterCondition(propertyName, operation, Long.toString(value), EdmType.INT64);
    }

    /**
     * Generates a property filter condition string for a <code>String</code> value. Creates a formatted string to use
     * in a filter expression that uses the specified operation to compare the property with the value, formatted as
     * a string value, as in the following example:
     * <p>
     * <code>String condition = generateFilterCondition("Platform", QueryComparisons.EQUAL, "Azure");</code>
     * <p>
     * This statement sets <code>condition</code> to the following value:
     * <p>
     * <code>Platform eq 'Azure'</code>
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            A <code>String</code> which specifies the value to compare with the property.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, final String value) {
        return generateFilterCondition(propertyName, operation, value, EdmType.STRING);
    }

    /**
     * Generates a property filter condition string. Creates a formatted string to use in a filter expression that uses
     * the specified operation to compare the property with the value, formatted as the specified {@link EdmType}.
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            A <code>String</code> which specifies the value to compare with the property.
     * @param edmType
     *            The {@link EdmType} to format the value as.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, String value, EdmType edmType) {
        String valueOperand = null;

        if (edmType == EdmType.BOOLEAN || edmType == EdmType.DOUBLE || edmType == EdmType.INT32) {
            valueOperand = value;
        }
        else if (edmType == EdmType.INT64) {
            valueOperand = String.format("%sL", value);
        }
        else if (edmType == EdmType.DATE_TIME) {
            valueOperand = String.format("datetime'%s'", value);
        }
        else if (edmType == EdmType.GUID) {
            valueOperand = String.format("guid'%s'", value);
        }
        else if (edmType == EdmType.BINARY) {
            valueOperand = String.format("X'%s'", value);
        }
        else {
            valueOperand = String.format("'%s'", value.replace("'", "''"));
        }

        return String.format("%s %s %s", propertyName, operation, valueOperand);
    }

    /**
     * Generates a property filter condition string for a <code>UUID</code> value. Creates a formatted string to use
     * in a filter expression that uses the specified operation to compare the property with the value, formatted as
     * a UUID value, as in the following example:
     * <p>
     * <code>String condition = generateFilterCondition("Identity", QueryComparisons.EQUAL, UUID.fromString(</code>
     * <code>"c9da6455-213d-42c9-9a79-3e9149a57833"));</code>
     * <p>
     * This statement sets <code>condition</code> to the following value:
     * <p>
     * <code>Identity eq guid'c9da6455-213d-42c9-9a79-3e9149a57833'</code>
     * 
     * @param propertyName
     *            A <code>String</code> which specifies the name of the property to compare.
     * @param operation
     *            A <code>String</code> which specifies the comparison operator to use.
     * @param value
     *            A <code>UUID</code> which specifies the value to compare with the property.
     * @return
     *         A <code>String</code> which represents the formatted filter condition.
     */
    public static String generateFilterCondition(String propertyName, String operation, final UUID value) {
        return generateFilterCondition(propertyName, operation, value.toString(), EdmType.GUID);
    }

    /**
     * Creates a filter condition using the specified logical operator on two filter conditions.
     * 
     * @param filterA
     *            A <code>String</code> which specifies the first formatted filter condition.
     * @param operator
     *            A <code>String</code> which specifies <code>Operators.AND</code> or <code>Operators.OR</code>.
     * @param filterB
     *            A <code>String</code> which specifies the first formatted filter condition.
     * @return
     *         A <code>String</code> which represents the combined filter expression.
     */
    public static String combineFilters(String filterA, String operator, String filterB) {
        return String.format("(%s) %s (%s)", filterA, operator, filterB);
    }

    private Class<T> clazzType = null;
    private String sourceTableName = null;
    private String[] columns = null;
    private Integer takeCount;
    private String filterString = null;

    /**
     * Initializes an empty {@link TableQuery} instance. This table query cannot be executed without
     * setting a table entity type.
     */
    public TableQuery() {
        // empty ctor
    }

    /**
     * Initializes a {@link TableQuery} with the specified table entity type. Callers may specify
     * {@link TableServiceEntity}<code>.class</code> as the class type parameter if no more specialized type is
     * required.
     * 
     * @param clazzType
     *            The <code>java.lang.Class</code> of the class <code>T</code> that represents the table entity type for
     *            the query. Class <code>T</code> must be a type that implements {@link TableEntity} and has a nullary
     *            constructor.
     */
    public TableQuery(final Class<T> clazzType) {
        this.setClazzType(clazzType);
    }

    /**
     * Gets the class type of the table entities returned by the query.
     * 
     * @return
     *         The <code>java.lang.Class</code> of the class <code>T</code> that represents the table entity type for
     *         the query.
     */
    public Class<T> getClazzType() {
        return this.clazzType;
    }

    /**
     * Gets an array of the table entity property names specified in the table query. All properties in the table are
     * returned by default if no property names are specified with a select clause in the table query. The table entity
     * properties to return may be specified with a call to the {@link #setColumns} or {@link #select} methods with a
     * array of property names as parameter.
     * <p>
     * Note that the system properties <code>PartitionKey</code>, <code>RowKey</code>, and <code>Timestamp</code> are
     * automatically requested from the table service whether specified in the {@link TableQuery} or not.
     * 
     * @return
     *         An array of <code>String</code> objects which represents the property names of the table entity properties to
     *         return in the query.
     */
    public String[] getColumns() {
        return this.columns;
    }

    /**
     * Gets the filter expression specified in the table query. All entities in the table are returned by
     * default if no filter expression is specified in the table query. A filter for the entities to return may be
     * specified with a call to the {@link #setFilterString} or {@link #where} methods.
     * 
     * @return
     *         A <code>String</code> which represents the filter expression used in the query.
     */
    public String getFilterString() {
        return this.filterString;
    }

    /**
     * Gets the name of the source table specified in the table query.
     * 
     * @return
     *         A <code>String</code> which represents the name of the source table used in the query.
     */
    protected String getSourceTableName() {
        return this.sourceTableName;
    }

    /**
     * Gets the number of entities the query returns specified in the table query. If this value is not
     * specified in a table query, a maximum of 1,000 entries will be returned. The number of entities to return may be
     * specified with a call to the {@link #setTakeCount} or {@link #take} methods.
     * <p>
     * If the value returned by <code>getTakeCount</code> is greater than 1,000, the query will throw a
     * {@link StorageException} when executed.
     * 
     * @return
     *         An <code>Integer</code> which represents the maximum number of entities for the table query to return.
     */
    public Integer getTakeCount() {
        return this.takeCount;
    }

    /**
     * Defines the property names of the table entity properties to return when the table query is executed. The
     * <code>select</code> clause is optional on a table query, used to limit the table properties returned from the
     * server. By default, a query will return all properties from the table entity.
     * <p>
     * Note that the system properties <code>PartitionKey</code>, <code>RowKey</code>, and <code>Timestamp</code> are
     * automatically requested from the table service whether specified in the {@link TableQuery} or not.
     * 
     * @param columns
     *            An array of <code>String</code> objects which specify the property names of the table entity properties
     *            to return when the query is executed.
     * 
     * @return
     *         A reference to the {@link TableQuery} instance with the table entity properties to return set.
     */
    public TableQuery<T> select(final String[] columns) {
        this.setColumns(columns);
        return this;
    }

    /**
     * Sets the class type of the table entities returned by the query. A class type is required to execute a table
     * query.
     * <p>
     * Callers may specify {@link TableServiceEntity}<code>.class</code> as the class type parameter if no more
     * specialized type is required.
     * 
     * @param clazzType
     *            The <code>java.lang.Class</code> of the class <code>T</code> that represents the table entity type for
     *            the query. Class <code>T</code> must be a type that implements {@link TableEntity} and has a nullary
     *            constructor,
     */
    public void setClazzType(final Class<T> clazzType) {
        Utility.assertNotNull("class type", clazzType);
        Utility.checkNullaryCtor(clazzType);
        this.clazzType = clazzType;
    }

    /**
     * Sets the property names of the table entity properties to return when the table query is executed. By default, a
     * query will return all properties from the table entity.
     * <p>
     * Note that the system properties <code>PartitionKey</code>, <code>RowKey</code>, and <code>Timestamp</code> are
     * automatically requested from the table service whether specified in the {@link TableQuery} or not.
     * 
     * @param columns
     *            An array of <code>String</code> objects which specify the property names of the table entity properties
     *            to return when the query is executed.
     */
    public void setColumns(final String[] columns) {
        this.columns = columns;
    }

    /**
     * Sets the filter expression to use in the table query. A filter expression is optional; by default a table query
     * will return all entities in the table.
     * <p>
     * Filter expressions for use with the {@link #setFilterString} method can be created using fluent syntax with the
     * overloaded {@link #generateFilterCondition} methods and {@link #combineFilters} method, using the comparison
     * operators defined in {@link QueryComparisons} and the logical operators defined in {@link Operators}. Note that
     * the first operand in a filter comparison must be a property name, and the second operand must evaluate to a
     * constant. The PartitionKey and RowKey property values are <code>String</code> types for comparison purposes. For
     * example, to query all entities with a PartitionKey value of "AccessLogs" on table query <code>myQuery</code>:
     * <p>
     * <code>&nbsp&nbsp&nbsp&nbspmyQuery.setFilterString("PartitionKey eq 'AccessLogs'");</code>
     * <p>
     * The values that may be used in table queries are explained in more detail in the MSDN topic
     * 
     * <a href="http://msdn.microsoft.com/en-us/library/azure/dd894031.aspx">Querying Tables and Entities</a>, but note
     * that the space characters within values do not need to be URL-encoded, as this will be done when the query is
     * executed.
     * <p>
     * Note that no more than 15 discrete comparisons are permitted within a filter string.
     * 
     * @param filterString
     *            A <code>String</code> which represents the filter expression to use in the query.
     */
    public void setFilterString(final String filterString) {
        Utility.assertNotNullOrEmpty("filterString", filterString);
        this.filterString = filterString;
    }

    /**
     * Sets the name of the source table for the table query. A table query must have a source table to be executed.
     * 
     * @param sourceTableName
     *            A <code>String</code> which specifies the name of the source table to use in the query.
     */
    protected void setSourceTableName(final String sourceTableName) {
        Utility.assertNotNullOrEmpty("tableName", sourceTableName);
        this.sourceTableName = sourceTableName;
    }

    /**
     * Sets the upper bound for the number of entities the query returns. If this value is not specified in a table
     * query, by default a maximum of 1,000 entries will be returned.
     * <p>
     * If the value specified for the <code>takeCount</code> parameter is greater than 1,000, the query will throw a
     * {@link StorageException} when executed.
     * 
     * @param takeCount
     *        An <code>Integer</code> which represents the maximum number of entities for the table query to return.
     */
    public void setTakeCount(final Integer takeCount) {
        if (takeCount != null && takeCount <= 0) {
            throw new IllegalArgumentException(SR.TAKE_COUNT_ZERO_OR_NEGATIVE);
        }

        this.takeCount = takeCount;
    }

    /**
     * Defines the upper bound for the number of entities the query returns. If this value is not specified in a table
     * query, by default a maximum of 1,000 entries will be returned.
     * <p>
     * If the value specified for the <code>take</code> parameter is greater than 1,000, the query will throw a
     * {@link StorageException} when executed.
     * 
     * @param take
     *        An <code>Integer</code> which represents the maximum number of entities for the table query to return.
     * 
     * @return
     *         A reference to the {@link TableQuery} instance with the number of entities to return set.
     */
    public TableQuery<T> take(final Integer take) {
        if (take != null) {
            this.setTakeCount(take);
        }
        return this;
    }

    /**
     * Defines a filter expression for the table query. Only entities that satisfy the specified filter expression will
     * be returned by the query. Setting a filter expression is optional; by default, all entities in the table are
     * returned if no filter expression is specified in the table query.
     * <p>
     * Filter expressions for use with the {@link #where} method can be created using fluent syntax with the overloaded
     * {@link #generateFilterCondition} methods and {@link #combineFilters} method, using the comparison operators
     * defined in {@link QueryComparisons} and the logical operators defined in {@link Operators}. Note that the first
     * operand in a filter comparison must be a property name, and the second operand must evaluate to a constant. The
     * PartitionKey and RowKey property values are <code>String</code> types for comparison purposes. For example, to
     * query all entities with a PartitionKey value of "AccessLogs" on table query <code>myQuery</code>:
     * <p>
     * <code>&nbsp&nbsp&nbsp&nbspmyQuery = myQuery.where("PartitionKey eq 'AccessLogs'");</code>
     * <p>
     * The values that may be used in table queries are explained in more detail in the MSDN topic
     * 
     * <a href="http://msdn.microsoft.com/en-us/library/azure/dd894031.aspx">Querying Tables and Entities</a>, but note
     * that the space characters within values do not need to be URL-encoded, as this will be done when the query is
     * executed.
     * <p>
     * Note that no more than 15 discrete comparisons are permitted within a filter string.
     * 
     * @param filter
     *            A <code>String</code> which specifies the filter expression to apply to the table query.
     * @return
     *         A reference to the {@link TableQuery} instance with the filter on entities to return set.
     */
    public TableQuery<T> where(final String filter) {
        this.setFilterString(filter);
        return this;
    }

    /**
     * Reserved for internal use. Creates a {@link UriQueryBuilder} object representing the table query.
     * 
     * @return A {@link UriQueryBuilder} object representing the table query.
     * 
     * @throws StorageException
     *             if an error occurs in adding or encoding the query parameters.
     */
    protected UriQueryBuilder generateQueryBuilder() throws StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();
        if (!Utility.isNullOrEmpty(this.filterString)) {
            builder.add(TableConstants.FILTER, this.filterString);
        }

        if (this.takeCount != null) {
            builder.add(TableConstants.TOP, this.takeCount.toString());
        }

        if (this.columns != null && this.columns.length > 0) {
            final StringBuilder colBuilder = new StringBuilder();

            boolean foundRk = false;
            boolean foundPk = false;
            boolean roundTs = false;

            for (int m = 0; m < this.columns.length; m++) {
                if (TableConstants.ROW_KEY.equals(this.columns[m])) {
                    foundRk = true;
                }
                else if (TableConstants.PARTITION_KEY.equals(this.columns[m])) {
                    foundPk = true;
                }
                else if (TableConstants.TIMESTAMP.equals(this.columns[m])) {
                    roundTs = true;
                }

                colBuilder.append(this.columns[m]);
                if (m < this.columns.length - 1) {
                    colBuilder.append(",");
                }
            }

            if (!foundPk) {
                colBuilder.append(",");
                colBuilder.append(TableConstants.PARTITION_KEY);
            }

            if (!foundRk) {
                colBuilder.append(",");
                colBuilder.append(TableConstants.ROW_KEY);
            }

            if (!roundTs) {
                colBuilder.append(",");
                colBuilder.append(TableConstants.TIMESTAMP);
            }

            builder.add(TableConstants.SELECT, colBuilder.toString());
        }

        return builder;
    }
}
