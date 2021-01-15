// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.management.provider;

/**
 * The IdentifierProvider to help generate distinct values.
 */
public interface IdentifierProvider {
    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the max length for the random generated name
     * @return the random name
     */
    String getRandomName(String prefix, int maxLen);

    /**
     * Gets a random UUID.
     *
     * @return the UUID string.
     */
    String getRandomUuid();
}
