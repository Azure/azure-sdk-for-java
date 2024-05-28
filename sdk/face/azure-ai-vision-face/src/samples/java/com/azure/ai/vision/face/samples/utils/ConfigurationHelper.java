// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples.utils;

import com.azure.core.util.Configuration;

/**
 * This is a helper class of frequently used methods for sample codes.
 */
public class ConfigurationHelper {
    private static final String DEFAULT_ENDPOINT = "";
    private static final String DEFAULT_ACCOUNT_KEY = "";
    private  static final String CONFIGURATION_NAME_AZURE_FACE_API_ENDPOINT = "AZURE_FACE_API_ENDPOINT";
    private static final String CONFIGURATION_NAME_AZURE_FACE_API_ACCOUNT_KEY = "AZURE_FACE_API_ACCOUNT_KEY";

    public static String getEndpoint() {
        return getConfiguration(CONFIGURATION_NAME_AZURE_FACE_API_ENDPOINT, DEFAULT_ENDPOINT);
    }

    public static String getAccountKey() {
        return getConfiguration(CONFIGURATION_NAME_AZURE_FACE_API_ACCOUNT_KEY, DEFAULT_ACCOUNT_KEY);
    }

    private static <T> T getConfiguration(String name, T defaultValue) {
        return Configuration.getGlobalConfiguration().get(name, defaultValue);
    }
}
