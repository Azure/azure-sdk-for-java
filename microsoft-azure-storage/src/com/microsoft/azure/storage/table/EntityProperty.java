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

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * A class which represents a single typed property value in a table entity. An {@link EntityProperty} stores the data
 * type as an {@link EdmType}. The value, which may be <code>null</code> for object types, but not for primitive types,
 * is serialized and stored as a <code>String</code>.
 * <p>
 * {@link EntityProperty} provides overloaded constructors and overloads of the <code>setValue</code> method for
 * supported value types. Each overloaded constructor or <code>setValue</code> method sets the {@link EdmType} and
 * serializes the value appropriately based on the parameter type.
 * <p>
 * Use one of the <code>getValueAs</code><em>Type</em> methods to deserialize an {@link EntityProperty} as the
 * appropriate Java type. The method will throw a {@link ParseException} or {@link IllegalArgumentException} if the
 * {@link EntityProperty} cannot be deserialized as the Java type.
 */
public final class EntityProperty {
    private String value;
    private Class<?> type;
    private EdmType edmType = EdmType.NULL;
    
    /**
     * Flag that specifies whether the client should look to correct Date values stored on a {@link TableEntity}
     * that may have been written using versions of this library prior to 2.0.0.
     * See <a href="http://go.microsoft.com/fwlink/?LinkId=523753">here</a> for more details.
     */
    private boolean dateBackwardCompatibility = false;

    /**
     * Reserved for internal use. Constructs an {@link EntityProperty} instance from a <code>Object</code> value type,
     * and verifies that the value can be interpreted as the specified data type.
     * 
     * @param value
     *            The <code>Object</code> to convert to a string and store.
     */
    protected EntityProperty(final String value, final Class<?> type) {
        this.type = type;
        this.value = value;
        if (type.equals(byte[].class)) {
            this.getValueAsByteArray();
            this.edmType = EdmType.BINARY;
        }
        else if (type.equals(Byte[].class)) {
            this.getValueAsByteObjectArray();
            this.edmType = EdmType.BINARY;
        }
        else if (type.equals(String.class)) {
            this.edmType = EdmType.STRING;
        }
        else if (type.equals(boolean.class)) {
            this.getValueAsBoolean();
            this.edmType = EdmType.BOOLEAN;
        }
        else if (type.equals(Boolean.class)) {
            this.getValueAsBooleanObject();
            this.edmType = EdmType.BOOLEAN;
        }
        else if (type.equals(Date.class)) {
            this.getValueAsDate();
            this.edmType = EdmType.DATE_TIME;
        }
        else if (type.equals(double.class)) {
            this.getValueAsDouble();
            this.edmType = EdmType.DOUBLE;
        }
        else if (type.equals(Double.class)) {
            this.getValueAsDoubleObject();
            this.edmType = EdmType.DOUBLE;
        }
        else if (type.equals(UUID.class)) {
            this.getValueAsUUID();
            this.edmType = EdmType.GUID;
        }
        else if (type.equals(int.class)) {
            this.getValueAsInteger();
            this.edmType = EdmType.INT32;
        }
        else if (type.equals(Integer.class)) {
            this.getValueAsIntegerObject();
            this.edmType = EdmType.INT32;
        }
        else if (type.equals(long.class)) {
            this.getValueAsLong();
            this.edmType = EdmType.INT64;
        }
        else if (type.equals(Long.class)) {
            this.getValueAsLongObject();
            this.edmType = EdmType.INT64;
        }
        else {
            throw new IllegalArgumentException(String.format(SR.TYPE_NOT_SUPPORTED, type.toString()));
        }
    }

    /**
     * Reserved for internal use. Constructs an {@link EntityProperty} instance from a <code>Object</code> value and a
     * data type, and verifies that the value can be interpreted as the specified data type.
     * 
     * @param value
     *            The <code>Object</code> to convert to a string and store.
     * @param edmType
     *            The <code>Class<?></code> type of the value to construct.
     */
    protected EntityProperty(final Object value, final Class<?> type) {
        if (type.equals(byte[].class)) {
            setValue((byte[]) value);
            this.type = type;
        }
        else if (type.equals(Byte[].class)) {
            setValue((Byte[]) value);
            this.type = type;
        }
        else if (type.equals(String.class)) {
            setValue((String) value);
            this.type = type;
        }
        else if (type.equals(boolean.class)) {
            setValue(((Boolean) value).booleanValue());
            this.type = type;
        }
        else if (type.equals(Boolean.class)) {
            setValue((Boolean) value);
            this.type = type;
        }
        else if (type.equals(double.class)) {
            setValue(((Double) value).doubleValue());
            this.type = type;
        }
        else if (type.equals(Double.class)) {
            setValue((Double) value);
            this.type = type;
        }
        else if (type.equals(UUID.class)) {
            setValue((UUID) value);
            this.type = type;
        }
        else if (type.equals(int.class)) {
            setValue(((Integer) value).intValue());
            this.type = type;
        }
        else if (type.equals(Integer.class)) {
            setValue((Integer) value);
            this.type = type;
        }
        else if (type.equals(long.class)) {
            setValue(((Long) value).longValue());
            this.type = type;
        }
        else if (type.equals(Long.class)) {
            setValue((Long) value);
            this.type = type;
        }
        else if (type.equals(Date.class)) {
            setValue((Date) value);
            this.type = type;
        }
        else {
            throw new IllegalArgumentException(String.format(SR.TYPE_NOT_SUPPORTED, type.toString()));
        }
    }

    /**
     * Reserved for internal use. Constructs an {@link EntityProperty} instance from a <code>String</code> value and a
     * data type, and verifies that the value can be interpreted as the specified data type.
     * 
     * @param value
     *            The <code>String</code> representation of the value to construct.
     * @param edmType
     *            The {@link EdmType} data type of the value to construct.
     */
    protected EntityProperty(final String value, final EdmType edmType) {
        this.edmType = edmType;
        this.value = value;

        // validate data is encoded correctly
        if (edmType == EdmType.STRING) {
            this.type = String.class;
            return;
        }
        else if (edmType == EdmType.BINARY) {
            this.getValueAsByteArray();
            this.type = Byte[].class;
        }
        else if (edmType == EdmType.BOOLEAN) {
            this.getValueAsBoolean();
            this.type = Boolean.class;
        }
        else if (edmType == EdmType.DOUBLE) {
            this.getValueAsDouble();
            this.type = Double.class;
        }
        else if (edmType == EdmType.GUID) {
            this.getValueAsUUID();
            this.type = UUID.class;
        }
        else if (edmType == EdmType.INT32) {
            this.getValueAsInteger();
            this.type = Integer.class;
        }
        else if (edmType == EdmType.INT64) {
            this.getValueAsLong();
            this.type = Long.class;
        }
        else if (edmType == EdmType.DATE_TIME) {
            this.getValueAsDate();
            this.type = Date.class;
        }
        else {
            // these are overwritten when this is called from the table parser with a more informative message
            if (edmType == null) {
                throw new IllegalArgumentException(SR.EDMTYPE_WAS_NULL);
            }
            throw new IllegalArgumentException(String.format(SR.INVALID_EDMTYPE_VALUE, edmType.toString()));
        }
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>boolean</code> value.
     * 
     * @param value
     *            The <code>boolean</code> value of the entity property to set.
     */
    public EntityProperty(final boolean value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>Boolean</code> value.
     * 
     * @param value
     *            The <code>Boolean</code> value of the entity property to set.
     */
    public EntityProperty(final Boolean value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>byte[]</code> value.
     * 
     * @param value
     *            The <code>byte[]</code> value of the entity property to set.
     */
    public EntityProperty(final byte[] value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>Byte[]</code>.
     * 
     * @param value
     *            The <code>Byte[]</code> to set as the entity property value.
     */
    public EntityProperty(final Byte[] value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>java.util.Date</code> value.
     * 
     * @param value
     *            The <code>java.util.Date</code> to set as the entity property value.
     */
    public EntityProperty(final Date value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>double</code> value.
     * 
     * @param value
     *            The <code>double</code> value of the entity property to set.
     */
    public EntityProperty(final double value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>Double</code> value.
     * 
     * @param value
     *            The <code>Double</code> value of the entity property to set.
     */
    public EntityProperty(final Double value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from an <code>int</code> value.
     * 
     * @param value
     *            The <code>int</code> value of the entity property to set.
     */
    public EntityProperty(final int value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from an <code>Integer</code> value.
     * 
     * @param value
     *            The <code>Integer</code> value of the entity property to set.
     */
    public EntityProperty(final Integer value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>long</code> value.
     * 
     * @param value
     *            The <code>long</code> value of the entity property to set.
     */
    public EntityProperty(final long value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>Long</code> value.
     * 
     * @param value
     *            The <code>Long</code> value of the entity property to set.
     */
    public EntityProperty(final Long value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>String</code> value.
     * 
     * @param value
     *            The <code>String</code> to set as the entity property value.
     */
    public EntityProperty(final String value) {
        this.setValue(value);
    }

    /**
     * Constructs an {@link EntityProperty} instance from a <code>java.util.UUID</code> value.
     * 
     * @param value
     *            The <code>java.util.UUID</code> to set as the entity property value.
     */
    public EntityProperty(final UUID value) {
        this.setValue(value);
    }

    /**
     * Gets the {@link EdmType} storage data type for the {@link EntityProperty}.
     * 
     * @return
     *         The {@link EdmType} enumeration value for the data type of the {@link EntityProperty}.
     */
    public EdmType getEdmType() {
        return this.edmType;
    }

    /**
     * Gets a flag indicating that the {@link EntityProperty} value is <code>null</code>.
     * 
     * @return
     *         A <code>boolean</code> flag indicating that the {@link EntityProperty} value is <code>null</code>.
     */
    public boolean getIsNull() {
        return this.value == null;
    }

    /**
     * Gets the class type of the {@link EntityProperty}.
     * 
     * @return
     *         The <code>Class<?></code> of the {@link EntityProperty}.
     */
    public Class<?> getType() {
        return this.type;
    }
    
    /**
     * Gets the value of this {@link EntityProperty} as a <code>boolean</code>.
     * 
     * @return
     *         A <code>boolean</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             If the value is <code>null</code> or cannot be parsed as a <code>Boolean</code>.
     */
    public boolean getValueAsBoolean() {
        if (this.getIsNull()) {
            throw new IllegalArgumentException(SR.ENTITY_PROPERTY_CANNOT_BE_NULL_FOR_PRIMITIVES);
        }
        return Boolean.parseBoolean(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>Boolean</code>.
     * 
     * @return
     *         A <code>Boolean</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             If the value is <code>null</code> or cannot be parsed as a <code>Boolean</code>.
     */
    public Boolean getValueAsBooleanObject() {
        if (this.getIsNull()) {
            return null;
        }
        return Boolean.parseBoolean(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>byte</code> array.
     * 
     * @return
     *         A <code>byte[]</code> representation of the {@link EntityProperty} value, or <code>null</code>.
     */
    public byte[] getValueAsByteArray() {
        return this.getIsNull() ? null : Base64.decode(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>Byte</code> array.
     * 
     * @return
     *         A <code>Byte[]</code> representation of the {@link EntityProperty} value, or <code>null</code>.
     */
    public Byte[] getValueAsByteObjectArray() {
        return this.getIsNull() ? null : Base64.decodeAsByteObjectArray(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>java.util.Date</code>.
     * 
     * @return
     *         A <code>java.util.Date</code> representation of the {@link EntityProperty} value, or <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *             If the value is not <code>null</code> and cannot be parsed as a <code>java.util.Date</code>.
     */
    public Date getValueAsDate() {
        return this.getIsNull() ? null : Utility.parseDate(this.value, this.dateBackwardCompatibility);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>double</code>.
     * 
     * @return
     *         A <code>double</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             If the value is <code>null</code> or cannot be parsed as a <code>double</code>.
     */
    public double getValueAsDouble() {
        if (this.getIsNull()) {
            throw new IllegalArgumentException(SR.ENTITY_PROPERTY_CANNOT_BE_NULL_FOR_PRIMITIVES);
        }
        else if (this.value.equals("Infinity") || this.value.equals("INF")) {
            return Double.POSITIVE_INFINITY;
        }
        else if (this.value.equals("-Infinity") || this.value.equals("-INF")) {
            return Double.NEGATIVE_INFINITY;
        }
        else if (this.value.equals("NaN")) {
            return Double.NaN;
        }
        else {
            return Double.parseDouble(this.value);
        }
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>double</code>.
     * 
     * @return
     *         A <code>double</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             If the value is <code>null</code> or cannot be parsed as a <code>double</code>.
     */
    public Double getValueAsDoubleObject() {
        if (this.getIsNull()) {
            return null;
        }
        else if (this.value.equals("Infinity") || this.value.equals("INF")) {
            return Double.POSITIVE_INFINITY;
        }
        else if (this.value.equals("-Infinity") || this.value.equals("-INF")) {
            return Double.NEGATIVE_INFINITY;
        }
        else if (this.value.equals("NaN")) {
            return Double.NaN;
        }
        else {
            return Double.parseDouble(this.value);
        }
    }

    /**
     * Gets the value of this {@link EntityProperty} as an <code>int</code>.
     * 
     * @return
     *         An <code>int</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             If the value is <code>null</code> or cannot be parsed as an <code>int</code>.
     */
    public int getValueAsInteger() {
        if (this.getIsNull()) {
            throw new IllegalArgumentException(SR.ENTITY_PROPERTY_CANNOT_BE_NULL_FOR_PRIMITIVES);
        }
        return Integer.parseInt(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as an <code>Integer</code>.
     * 
     * @return
     *         An <code>Integer</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             If the value is <code>null</code> or cannot be parsed as an <code>int</code>.
     */
    public Integer getValueAsIntegerObject() {
        return this.getIsNull() ? null : Integer.parseInt(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>long</code>.
     * 
     * @return
     *         A <code>long</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             If the value is <code>null</code> or cannot be parsed as a <code>long</code>.
     */
    public long getValueAsLong() {
        if (this.getIsNull()) {
            throw new IllegalArgumentException(SR.ENTITY_PROPERTY_CANNOT_BE_NULL_FOR_PRIMITIVES);
        }
        return Long.parseLong(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>Long</code>.
     * 
     * @return
     *         A <code>long</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             If the value is <code>null</code> or cannot be parsed as a <code>long</code>.
     */
    public Long getValueAsLongObject() {
        return this.getIsNull() ? null : Long.parseLong(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>String</code>.
     * 
     * @return
     *         A <code>String</code> representation of the {@link EntityProperty} value, or <code>null</code>.
     */
    public String getValueAsString() {
        return this.getIsNull() ? null : this.value;
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>java.util.UUID</code>.
     * 
     * @return
     *         A <code>java.util.UUID</code> representation of the {@link EntityProperty} value, or <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *             If the value cannot be parsed as a <code>java.util.UUID</code>.
     */
    public UUID getValueAsUUID() {
        return this.getIsNull() ? null : UUID.fromString(this.value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>boolean</code> value.
     * 
     * @param value
     *            The <code>boolean</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final boolean value) {
        this.edmType = EdmType.BOOLEAN;
        this.type = boolean.class;
        this.value = value ? Constants.TRUE : Constants.FALSE;
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>Boolean</code> value.
     * 
     * @param value
     *            The <code>Boolean</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final Boolean value) {
        this.edmType = EdmType.BOOLEAN;
        this.type = Boolean.class;
        if (value == null) {
            this.value = null;
        }
        else {
            this.value = value ? Constants.TRUE : Constants.FALSE;
        }
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>byte[]</code> value.
     * 
     * @param value
     *            The <code>byte[]</code> value to set as the {@link EntityProperty} value. This value may be
     *            <code>null</code>.
     */
    public synchronized final void setValue(final byte[] value) {
        this.edmType = EdmType.BINARY;
        this.type = byte[].class;
        this.value = value == null ? null : Base64.encode(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>Byte[]</code> value.
     * 
     * @param value
     *            The <code>Byte[]</code> value to set as the {@link EntityProperty} value. This value may be
     *            <code>null</code>.
     */
    public synchronized final void setValue(final Byte[] value) {
        this.edmType = EdmType.BINARY;
        this.type = Byte[].class;
        this.value = value == null ? null : Base64.encode(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>java.util.Date</code> value.
     * 
     * @param value
     *            The <code>java.util.Date</code> value to set as the {@link EntityProperty} value. This value may be
     *            <code>null</code>.
     */
    public synchronized final void setValue(final Date value) {
        this.edmType = EdmType.DATE_TIME;
        this.type = Date.class;
        this.value = value == null ? null : Utility.getJavaISO8601Time(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>double</code> value.
     * 
     * @param value
     *            The <code>double</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final double value) {
        this.edmType = EdmType.DOUBLE;
        this.type = double.class;
        this.value = Double.toString(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>Double</code> value.
     * 
     * @param value
     *            The <code>Double</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final Double value) {
        this.edmType = EdmType.DOUBLE;
        this.type = Double.class;
        this.value = value == null ? null : Double.toString(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>int</code> value.
     * 
     * @param value
     *            The <code>int</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final int value) {
        this.edmType = EdmType.INT32;
        this.type = int.class;
        this.value = Integer.toString(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>Integer</code> value.
     * 
     * @param value
     *            The <code>Integer</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final Integer value) {
        this.edmType = EdmType.INT32;
        this.type = Integer.class;
        this.value = value == null ? null : Integer.toString(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>long</code> value.
     * 
     * @param value
     *            The <code>long</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final long value) {
        this.edmType = EdmType.INT64;
        this.type = long.class;
        this.value = Long.toString(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>Long</code> value.
     * 
     * @param value
     *            The <code>Long</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final Long value) {
        this.edmType = EdmType.INT64;
        this.type = Long.class;
        this.value = value == null ? null : Long.toString(value);
    }

    /**
     * Sets this {@link EntityProperty} using the <code>String</code> value.
     * 
     * @param value
     *            The <code>String</code> value to set as the {@link EntityProperty} value. This value may be
     *            <code>null</code>.
     */
    public synchronized final void setValue(final String value) {
        this.edmType = EdmType.STRING;
        this.type = String.class;
        this.value = value;   
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>java.util.UUID</code> value.
     * 
     * @param value
     *            The <code>java.util.UUID</code> value to set as the {@link EntityProperty} value.
     *            This value may be <code>null</code>.
     */
    public synchronized final void setValue(final UUID value) {
        this.edmType = EdmType.GUID;
        this.type = UUID.class;
        this.value = value == null ? null : value.toString();
    }

    /**
     * Sets whether the client should look to correct Date values stored on a {@link TableEntity}
     * that may have been written using versions of this library prior to 2.0.0.
     * <p>
     * {@link #dateBackwardCompatibility} is by default <code>false</code>, indicating a post 2.0.0 version or
     * mixed-platform usage.
     * <p>
     * See <a href="http://go.microsoft.com/fwlink/?LinkId=523753">here</a> for more details.
     * 
     * @param dateBackwardCompatibility
     *        <code>true</code> to enable <code>dateBackwardCompatibility</code>; otherwise, <code>false</code>
     */
    void setDateBackwardCompatibility(boolean dateBackwardCompatibility) {
        this.dateBackwardCompatibility = dateBackwardCompatibility;
    }
}
