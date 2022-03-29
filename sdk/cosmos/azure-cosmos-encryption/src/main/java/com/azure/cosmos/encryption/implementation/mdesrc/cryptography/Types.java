/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Defines the constants that are used to identify the SQL types that are specific to Microsoft SQL Server.
 *
 * This class is never instantiated.
 */
final class Types {
    private Types() {
        // not reached
    }

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type DATETIMEOFFSET.
     */
    public static final int DATETIMEOFFSET = -155;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type STRUCTURED.
     */
    public static final int STRUCTURED = -153;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type DATETIME.
     */
    public static final int DATETIME = -151;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type SMALLDATETIME.
     */
    public static final int SMALLDATETIME = -150;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type MONEY.
     */
    public static final int MONEY = -148;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type SMALLMONEY.
     */
    public static final int SMALLMONEY = -146;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type GUID.
     */
    public static final int GUID = -145;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type SQL_VARIANT.
     */
    public static final int SQL_VARIANT = -156;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type GEOMETRY.
     */
    public static final int GEOMETRY = -157;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the
     * Microsoft SQL type GEOGRAPHY.
     */
    public static final int GEOGRAPHY = -158;
}
