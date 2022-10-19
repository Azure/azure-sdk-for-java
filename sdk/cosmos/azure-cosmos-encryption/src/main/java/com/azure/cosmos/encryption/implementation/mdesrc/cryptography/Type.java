/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Class used to hold information about the type of a serializer.
 *
 */
public class Type {
    private final String id;
    private final int size;
    private final int precision;
    private final int scale;
    private final String codepage;

    /**
     * Initializes Type object.
     *
     * @param id
     *        ID of the serializer, can be found in SqlSerializerFactory
     * @param size
     *        The maximum size of value.
     * @param precision
     *        The maximum number of digits.
     * @param scale
     *        The number of decimal places.
     * @param codepage
     *        The code page to represent the value.
     */
    public Type(String id, int size, int precision, int scale, String codepage) {
        this.id = id;
        this.size = size;
        this.precision = precision;
        this.scale = scale;
        this.codepage = codepage;
    }

    /**
     * Getter for ID.
     *
     * @return The type of serializer to get or create.
     */
    public String getId() {
        return id;
    }

    /**
     * Getter for Size.
     *
     * @return The maximum size of value.
     */
    public int getSize() {
        return size;
    }

    /**
     * Getter for precision.
     *
     * @return The maximum number of digits.
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Getter for scale.
     *
     * @return The number of decimal places.
     */
    public int getScale() {
        return scale;
    }

    /**
     * Getter for codepage.
     *
     * @return The code page to represent the value.
     */
    public String getCodepage() {
        return codepage;
    }

}
