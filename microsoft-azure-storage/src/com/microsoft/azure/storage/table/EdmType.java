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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.core.SR;

/**
 * A enumeration used to represent the primitive types of the Entity Data Model (EDM) in the Open Data Protocol (OData).
 * The EDM is the underlying abstract data model used by OData services. The {@link EdmType} enumeration includes a
 * {@link #parse(String)} method to convert EDM data type names to the enumeration type, and overrides the
 * {@link #toString()} method to produce an EDM data type name.
 * <p>
 * For more information about OData, see the <a href="http://www.odata.org/">Open Data Protocol</a> website.
 * <p>
 * For an overview of the available EDM primitive data types and names, see the <a
 * href="http://www.odata.org/developers/protocols/overview#AbstractTypeSystem">Primitive Data Types</a> section of the
 * <a href="http://www.odata.org/developers/protocols/overview">OData Protocol Overview</a>.
 * <p>
 * The Abstract Type System used to define the primitive types supported by OData is defined in detail in <a
 * href="http://msdn.microsoft.com/en-us/library/dd541474.aspx">[MC-CSDL] (section 2.2.1).
 */
public enum EdmType {
    /**
     * <strong>Null</strong> Represents the absence of a value
     */
    NULL,

    /**
     * <strong>Edm.Binary</strong> Represents fixed- or variable-length binary data
     */
    BINARY,

    /**
     * <strong>Edm.Boolean</strong> Represents the mathematical concept of binary-valued logic
     */
    BOOLEAN,

    /**
     * <strong>Edm.Byte</strong> Represents a unsigned 8-bit integer value
     */
    BYTE,

    /**
     * <strong>Edm.DateTime</strong> Represents date and time with values ranging from 12:00:00 midnight, January 1,
     * 1753 A.D. through 11:59:59 P.M, December 9999 A.D.
     */
    DATE_TIME,

    /**
     * <strong>Edm.DateTimeOffset</strong> Represents date and time as an Offset in minutes from GMT, with values
     * ranging from 12:00:00 midnight, January 1, 1753 A.D. through 11:59:59 P.M, December 9999 A.D
     */
    DATE_TIME_OFFSET,

    /**
     * <strong>Edm.Decimal</strong> Represents numeric values with fixed precision and scale. This type can describe a
     * numeric value ranging from negative 10^255 + 1 to positive 10^255 -1
     */
    DECIMAL,

    /**
     * <strong>Edm.Double</strong> Represents a floating point number with 15 digits precision that can represent values
     * with approximate range of +/- 2.23e -308 through +/- 1.79e +308
     */
    DOUBLE,

    /**
     * <strong>Edm.Single</strong> Represents a floating point number with 7 digits precision that can represent values
     * with approximate range of +/- 1.18e -38 through +/- 3.40e +38
     */
    SINGLE,

    /**
     * <strong>Edm.Guid</strong> Represents a 16-byte (128-bit) unique identifier value
     */
    GUID,

    /**
     * <strong>Edm.Int16</strong> Represents a signed 16-bit integer value
     */
    INT16,

    /**
     * <strong>Edm.Int32</strong> Represents a signed 32-bit integer value
     */
    INT32,

    /**
     * <strong>Edm.Int64</strong> Represents a signed 64-bit integer value
     */
    INT64,

    /**
     * <strong>Edm.SByte</strong> Represents a signed 8-bit integer value
     */
    SBYTE,

    /**
     * <strong>Edm.String</strong> Represents fixed- or variable-length character data
     */
    STRING,

    /**
     * <strong>Edm.Time</strong> Represents the time of day with values ranging from 0:00:00.x to 23:59:59.y, where x
     * and y depend upon the precision
     */
    TIME;

    private static final Set<EdmType> UNANNOTATED = Collections.unmodifiableSet(EnumSet.of(BOOLEAN, DOUBLE, INT32,
            STRING));

    protected final boolean mustAnnotateType() {
        return !UNANNOTATED.contains(this);
    }

    /**
     * Parses an EDM data type name and return the matching {@link EdmType} enumeration value. A <code>null</code> or
     * empty value parameter is matched as {@link EdmType#STRING}. Note that only the subset of EDM data types
     * supported in Microsoft Azure Table storage is parsed, consisting of {@link #BINARY}, {@link #BOOLEAN},
     * {@link #BYTE} , {@link #DATE_TIME}, {@link #DOUBLE}, {@link #GUID}, {@link #INT32}, {@link #INT64}, and
     * {@link #STRING}. Any
     * other type will cause an {@link IllegalArgumentException} to be thrown.
     * 
     * @param value
     *            A <code>String</code> containing the name of an EDM data type.
     * @return
     *         The {@link EdmType} enumeration value matching the specified EDM data type.
     *         
     * @throws IllegalArgumentException
     *             if an EDM data type not supported in Microsoft Azure Table storage is passed as an argument.
     * 
     */
    public static EdmType parse(final String value) {
        if (value == null || value.length() == 0) {
            return EdmType.STRING;
        }
        else if (value.equals(ODataConstants.EDMTYPE_DATETIME)) {
            return EdmType.DATE_TIME;
        }
        else if (value.equals(ODataConstants.EDMTYPE_INT32)) {
            return EdmType.INT32;
        }
        else if (value.equals(ODataConstants.EDMTYPE_BOOLEAN)) {
            return EdmType.BOOLEAN;
        }
        else if (value.equals(ODataConstants.EDMTYPE_DOUBLE)) {
            return EdmType.DOUBLE;
        }
        else if (value.equals(ODataConstants.EDMTYPE_INT64)) {
            return EdmType.INT64;
        }
        else if (value.equals(ODataConstants.EDMTYPE_GUID)) {
            return EdmType.GUID;
        }
        else if (value.equals(ODataConstants.EDMTYPE_BINARY)) {
            return EdmType.BINARY;
        }

        throw new IllegalArgumentException(String.format(SR.INVALID_EDMTYPE_VALUE, value));
    }

    /**
     * Returns the name of the EDM data type corresponding to the enumeration value.
     * 
     * @return
     *         A <code>String</code> containing the name of the EDM data type.
     */
    @Override
    public String toString() {
        if (this == EdmType.BINARY) {
            return ODataConstants.EDMTYPE_BINARY;
        }
        else if (this == EdmType.STRING) {
            return ODataConstants.EDMTYPE_STRING;
        }
        else if (this == EdmType.BOOLEAN) {
            return ODataConstants.EDMTYPE_BOOLEAN;
        }
        else if (this == EdmType.DOUBLE) {
            return ODataConstants.EDMTYPE_DOUBLE;
        }
        else if (this == EdmType.GUID) {
            return ODataConstants.EDMTYPE_GUID;
        }
        else if (this == EdmType.INT32) {
            return ODataConstants.EDMTYPE_INT32;
        }
        else if (this == EdmType.INT64) {
            return ODataConstants.EDMTYPE_INT64;
        }
        else if (this == EdmType.DATE_TIME) {
            return ODataConstants.EDMTYPE_DATETIME;
        }
        else {
            // VNext : Update here if we add to supported edmtypes in the future.
            return Constants.EMPTY_STRING;
        }
    }
}
