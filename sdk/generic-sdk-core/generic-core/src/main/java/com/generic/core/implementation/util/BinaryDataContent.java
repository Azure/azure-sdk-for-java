// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.util;

import com.generic.core.models.BinaryData;

import java.io.InputStream;

/**
 * An abstract internal representation of the content stored in {@link BinaryData}.
 */
public abstract class BinaryDataContent {
    public static final int STREAM_READ_SIZE = 8192;

    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    static final String TOO_LARGE_FOR_BYTE_ARRAY
        = "The content length is too large for a byte array. Content length is: ";

    /**
     * Gets the length of the {@link BinaryDataContent} if it can be calculated.
     * <p>
     * If the content length isn't able to be calculated null will be returned.
     *
     * @return The length of the {@link BinaryDataContent} if it can be calculated, otherwise null.
     */
    public abstract Long getLength();

    /**
     * Returns a byte array representation of this {@link BinaryDataContent}.
     *
     * @return A byte array representing this {@link BinaryDataContent}.
     */
    public abstract byte[] toBytes();

    /**
     * Returns an {@link InputStream} representation of this {@link BinaryDataContent}.
     *
     * @return An {@link InputStream} representing the {@link BinaryDataContent}.
     */
    public abstract InputStream toStream();

    /**
     * Gets the {@link BinaryDataContent} content type.
     *
     * @return The {@link BinaryDataContent} content type.
     */
    public abstract BinaryDataContentType getContentType();
}
