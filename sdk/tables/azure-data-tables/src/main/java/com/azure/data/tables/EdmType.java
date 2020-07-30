// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

/**
 * A enumeration used to represent the primitive types of the Entity Data Model (EDM) in the Open Data Protocol (OData).
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

    /**
     * Returns the name of the EDM data type corresponding to the enumeration value.
     *
     * @return A <code>String</code> containing the name of the EDM data type.
     */
    @Override
    public String toString() {
        if (this == EdmType.BINARY) {
            return ODataConstants.EDMTYPE_BINARY;
        } else if (this == EdmType.STRING) {
            return ODataConstants.EDMTYPE_STRING;
        } else if (this == EdmType.BOOLEAN) {
            return ODataConstants.EDMTYPE_BOOLEAN;
        } else if (this == EdmType.DOUBLE) {
            return ODataConstants.EDMTYPE_DOUBLE;
        } else if (this == EdmType.GUID) {
            return ODataConstants.EDMTYPE_GUID;
        } else if (this == EdmType.INT32) {
            return ODataConstants.EDMTYPE_INT32;
        } else if (this == EdmType.INT64) {
            return ODataConstants.EDMTYPE_INT64;
        } else if (this == EdmType.DATE_TIME) {
            return ODataConstants.EDMTYPE_DATETIME;
        } else {
            // VNext : Update here if we add to supported edmtypes in the future.
            return ODataConstants.EDMTYPE_STRING;
        }
    }
}
