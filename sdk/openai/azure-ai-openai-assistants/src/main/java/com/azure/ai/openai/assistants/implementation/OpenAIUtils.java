// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.AssistantsServiceVersion;
import com.azure.core.http.rest.RequestOptions;

/**
 * Utility class to be used by the SDK internally.
 */
public class OpenAIUtils {
    /**
     * This is the endpoint that non-azure OpenAI supports. Currently, it has only v1 version.
     */
    private static final String OPEN_AI_ENDPOINT = "https://api.openai.com/v1";

    /**
     * Get the endpoint for OpenAI service.
     */
    public static String getOpenAIEndpoint() {
        return OPEN_AI_ENDPOINT;
    }

    /**
     * OpenAI service can be used by either not setting the endpoint or by setting the endpoint to start with
     * "https://api.openai.com/v1"
     */
    public static boolean useAzureOpenAIService(String endpoint) {
        return endpoint != null && !endpoint.startsWith(OPEN_AI_ENDPOINT);
    }

    /**
     * Add the version query parameter to the request options if the service is not Azure OpenAI service.
     */
    public static void addAzureVersionToRequestOptions(String endpoint, RequestOptions requestOptions,
                                                          AssistantsServiceVersion serviceVersion) {
        if (useAzureOpenAIService(endpoint)) {
            requestOptions.addQueryParam("api-version", serviceVersion.getVersion());
        }
    }
}
