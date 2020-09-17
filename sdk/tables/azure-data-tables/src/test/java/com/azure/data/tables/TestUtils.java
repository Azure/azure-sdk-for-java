// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

/**
 * Common test utilities.
 */
public final class TestUtils {
    /**
     * Gets the connection string for running tests.
     *
     * @param isPlaybackMode {@code true} if the code is not running against a live service. false otherwise.
     *
     * @return The corresponding connection string.
     */
    public static String getConnectionString(boolean isPlaybackMode) {
        return isPlaybackMode
            ? "DefaultEndpointsProtocol=https;AccountName=dummyAccount;AccountKey=xyzDummy;EndpointSuffix=core.windows.net"
            : System.getenv("AZURE_TABLES_CONNECTION_STRING");
    }

    /**
     * Private constructor so this class cannot be instantiated.
     */
    private TestUtils() {
    }
}
