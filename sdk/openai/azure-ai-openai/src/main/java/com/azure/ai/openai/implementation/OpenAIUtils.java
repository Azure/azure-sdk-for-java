// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.core.http.rest.RequestOptions;

/**
 * Utility class to be used by the SDK internally.
 */
public final class OpenAIUtils {
    private OpenAIUtils() {
    }

    /**
     * This is the endpoint that non-azure OpenAI supports. Currently, it has only v1 version.
     */
    private static final String OPEN_AI_ENDPOINT = "https://api.openai.com";

    /**
     * Add the version query parameter to the request options if the service is not Azure OpenAI service.
     */
    public static void addAzureVersionToRequestOptions(String endpoint, RequestOptions requestOptions,
                                                       OpenAIServiceVersion serviceVersion) {
        if (useAzureOpenAIService(endpoint)) {
            requestOptions.addQueryParam("api-version", serviceVersion.getVersion());
        }
    }

    /**
     * OpenAI service can be used by either not setting the endpoint or by setting the endpoint to start with
     * 'https://api.openai.com'.
     */
    public static boolean useAzureOpenAIService(String endpoint) {
        return endpoint != null && !endpoint.startsWith(OPEN_AI_ENDPOINT);
    }
}
