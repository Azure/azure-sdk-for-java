// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.util;

import com.typespec.core.util.BinaryData;

/**
 * Helper class to access private values of {@link BinaryData} across package boundaries.
 */
public final class BinaryDataHelper {
    private static BinaryDataAccessor accessor;

    /**
     * Type defining the methods that access private values of {@link BinaryData}.
     */
    public interface BinaryDataAccessor {
        /**
         * Creates a new {@link BinaryData} with the given content.
         *
         * @param content The {@link BinaryDataContent}.
         * @return A new {@link BinaryData}.
         * @throws NullPointerException If {@code content} is null.
         */
        BinaryData createBinaryData(BinaryDataContent content);

        /**
         * Gets the {@link BinaryDataContent} that backs the {@link BinaryData}.
         *
         * @param binaryData The {@link BinaryData} having its content retrieved.
         * @return The {@link BinaryDataContent} that backs the {@link BinaryData}.
         */
        BinaryDataContent getContent(BinaryData binaryData);
    }

    /**
     * The method called from {@link BinaryData} to set its accessor.
     *
     * @param binaryDataAccessor The accessor.
     */
    public static void setAccessor(final BinaryDataAccessor binaryDataAccessor) {
        accessor = binaryDataAccessor;
    }

    /**
     * Creates a new {@link BinaryData} with the given content.
     *
     * @param content The {@link BinaryDataContent}.
     * @return A new {@link BinaryData}.
     * @throws NullPointerException If {@code content} is null.
     */
    public static BinaryData createBinaryData(BinaryDataContent content) {
        ensureAccessorSet();
        return accessor.createBinaryData(content);
    }

    /**
     * Gets the {@link BinaryDataContent} that backs the {@link BinaryData}.
     *
     * @param binaryData The {@link BinaryData} having its content retrieved.
     * @return The {@link BinaryDataContent} that backs the {@link BinaryData}.
     */
    public static BinaryDataContent getContent(BinaryData binaryData) {
        ensureAccessorSet();
        return accessor.getContent(binaryData);
    }

    /**
     * The success of setting up accessor depends on the order in which classes are loaded.
     * This method ensures that if accessor hasn't been set we force-load BinaryData class
     * which in turns populates the accessor.
     */
    private static void ensureAccessorSet() {
        if (accessor == null) {
            BinaryData.fromString("");
        }
    }
}
