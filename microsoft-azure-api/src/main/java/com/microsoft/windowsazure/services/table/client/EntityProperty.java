/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.table.client;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

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
    private EdmType edmType = EdmType.NULL;
    private boolean isNull = false;

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
     * Constructs an {@link EntityProperty} instance from a <code>Date</code> value.
     * 
     * @param value
     *            The <code>Date</code> to set as the entity property value.
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
     * Constructs an {@link EntityProperty} instance from an <code>int</code> value.
     * 
     * @param value
     *            The <code>int</code> value of the entity property to set.
     */
    public EntityProperty(final int value) {
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
     * Constructs an {@link EntityProperty} instance from a <code>String</code> value.
     * 
     * @param value
     *            The <code>String</code> to set as the entity property value.
     */
    public EntityProperty(final String value) {
        this.setValue(value);
    }

    /**
     * Reserved for internal use. Constructs an {@link EntityProperty} instance from a <code>String</code> value and a
     * data type, and verifies that the value can be interpreted as the specified data type.
     * 
     * @param value
     *            The <code>String</code> representation of the value to construct.
     * @param edmType
     *            The {@link EdmType} data type of the value to construct.
     * @throws ParseException
     *             if the <code>String</code> representation of the value cannot be interpreted as the data type.
     */
    protected EntityProperty(final String value, final EdmType edmType) throws ParseException {
        this.edmType = edmType;
        this.value = value;

        // validate data is encoded correctly
        if (edmType == EdmType.STRING) {
            return;
        }
        else if (edmType == EdmType.BINARY) {
            this.getValueAsByteArray();
        }
        else if (edmType == EdmType.BOOLEAN) {
            this.getValueAsBoolean();
        }
        else if (edmType == EdmType.DOUBLE) {
            this.getValueAsDouble();
        }
        else if (edmType == EdmType.GUID) {
            this.getValueAsUUID();
        }
        else if (edmType == EdmType.INT32) {
            this.getValueAsInteger();
        }
        else if (edmType == EdmType.INT64) {
            this.getValueAsLong();
        }
        else if (edmType == EdmType.DATE_TIME) {
            this.getValueAsDate();
        }
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
     * Reserved for internal use. Constructs an {@link EntityProperty} instance as a <code>null</code> value with the
     * specified type.
     * 
     * @param type
     *            The {@link EdmType} to set as the entity property type.
     */
    protected EntityProperty(EdmType type) {
        this.value = null;
        this.edmType = type;
        this.isNull = true;
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
        return this.isNull;
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>boolean</code>.
     * 
     * @return
     *         A <code>boolean</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             if the value is <code>null</code> or cannot be parsed as a <code>boolean</code>.
     */
    public boolean getValueAsBoolean() {
        if (this.isNull) {
            throw new IllegalArgumentException("EntityProperty cannot be set to null for value types.");
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
        return this.isNull ? null : Base64.decode(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>Byte</code> array.
     * 
     * @return
     *         A <code>Byte[]</code> representation of the {@link EntityProperty} value, or <code>null</code>.
     */
    public Byte[] getValueAsByteObjectArray() {
        return this.isNull ? null : Base64.decodeAsByteObjectArray(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>Date</code>.
     * 
     * @return
     *         A <code>Date</code> representation of the {@link EntityProperty} value, or <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *             if the value is not <code>null</code> and cannot be parsed as a <code>Date</code>.
     */
    public Date getValueAsDate() {
        if (this.isNull) {
            return null;
        }

        return Utility.parseDate(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>double</code>.
     * 
     * @return
     *         A <code>double</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             if the value is <code>null</code> or cannot be parsed as a <code>double</code>.
     */
    public double getValueAsDouble() {
        if (this.isNull) {
            throw new IllegalArgumentException("EntityProperty cannot be set to null for value types.");
        }
        return Double.parseDouble(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as an <code>int</code>.
     * 
     * @return
     *         An <code>int</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             if the value is <code>null</code> or cannot be parsed as an <code>int</code>.
     */
    public int getValueAsInteger() {
        if (this.isNull) {
            throw new IllegalArgumentException("EntityProperty cannot be set to null for value types.");
        }
        return Integer.parseInt(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>long</code>.
     * 
     * @return
     *         A <code>long</code> representation of the {@link EntityProperty} value.
     * 
     * @throws IllegalArgumentException
     *             if the value is <code>null</code> or cannot be parsed as a <code>long</code>.
     */
    public long getValueAsLong() {
        if (this.isNull) {
            throw new IllegalArgumentException("EntityProperty cannot be set to null for value types.");
        }
        return Long.parseLong(this.value);
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>String</code>.
     * 
     * @return
     *         A <code>String</code> representation of the {@link EntityProperty} value, or <code>null</code>.
     */
    public String getValueAsString() {
        return this.isNull ? null : this.value;
    }

    /**
     * Gets the value of this {@link EntityProperty} as a <code>java.util.UUID</code>.
     * 
     * @return
     *         A <code>java.util.UUID</code> representation of the {@link EntityProperty} value, or <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *             if the value cannot be parsed as a <code>java.util.UUID</code>.
     */
    public UUID getValueAsUUID() {
        return this.isNull ? null : UUID.fromString(this.value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>boolean</code> value.
     * 
     * @param value
     *            The <code>boolean</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final boolean value) {
        this.edmType = EdmType.BOOLEAN;
        this.isNull = false;
        this.value = value ? Constants.TRUE : Constants.FALSE;
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
        if (value == null) {
            this.value = null;
            this.isNull = true;
            return;
        }
        else {
            this.isNull = false;
        }

        this.value = Base64.encode(value);
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
        if (value == null) {
            this.value = null;
            this.isNull = true;
            return;
        }
        else {
            this.isNull = false;
        }

        this.value = Base64.encode(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>Date</code> value.
     * 
     * @param value
     *            The <code>Date</code> value to set as the {@link EntityProperty} value. This value may be
     *            <code>null</code>.
     */
    public synchronized final void setValue(final Date value) {
        this.edmType = EdmType.DATE_TIME;

        if (value == null) {
            this.value = null;
            this.isNull = true;
            return;
        }
        else {
            this.isNull = false;
        }

        this.value = Utility.getTimeByZoneAndFormat(value, Utility.UTC_ZONE, Utility.ISO8061_LONG_PATTERN);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>double</code> value.
     * 
     * @param value
     *            The <code>double</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final double value) {
        this.edmType = EdmType.DOUBLE;
        this.isNull = false;
        this.value = Double.toString(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>int</code> value.
     * 
     * @param value
     *            The <code>int</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final int value) {
        this.edmType = EdmType.INT32;
        this.isNull = false;
        this.value = Integer.toString(value);
    }

    /**
     * Sets this {@link EntityProperty} using the serialized <code>long</code> value.
     * 
     * @param value
     *            The <code>long</code> value to set as the {@link EntityProperty} value.
     */
    public synchronized final void setValue(final long value) {
        this.edmType = EdmType.INT64;
        this.isNull = false;
        this.value = Long.toString(value);
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
        if (value == null) {
            this.value = null;
            this.isNull = true;
            return;
        }
        else {
            this.isNull = false;
        }

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
        if (value == null) {
            this.value = null;
            this.isNull = true;
            return;
        }
        else {
            this.isNull = false;
        }

        this.value = value.toString();
    }

    /**
     * Reserved for internal use. Sets the null value flag to the specified <code>boolean</code> value.
     * 
     * @param isNull
     *            The <code>boolean</code> value to set in the null value flag.
     */
    protected void setIsNull(final boolean isNull) {
        this.isNull = isNull;
    }
}
