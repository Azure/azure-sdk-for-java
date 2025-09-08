// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

/** Utilities to check the Translator Custom endpoint. */
final class CustomEndpointUtils {
    private static final String PLATFORM_HOST = "cognitiveservices";

    /**
     * Checks the endpoint and decides whether it is platform host.
     *
     * @param endpoint Endpoint to check
     * @return True - Endpoint is pointing to platform. False otherwise.
     */
    public static boolean isPlatformHost(String endpoint) {
        return endpoint != null && endpoint.contains(PLATFORM_HOST);
    }
}
