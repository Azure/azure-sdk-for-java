// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.assistants.implementation;

import com.azure.ai.assistants.AssistantsServiceVersion;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;

import java.util.Map;

/**
 * Utility class to be used by the SDK internally.
 */
public final class AssistantClientUtils {
    private AssistantClientUtils() {
    }

    /**
     * This is the endpoint that non-azure OpenAI supports. Currently, it has only v1 version.
     */
    private static final String OPEN_AI_ENDPOINT = "https://api.openai.com";

    /**
     * Get the endpoint for OpenAI service.
     */
    public static String getOpenAIEndpoint() {
        return OPEN_AI_ENDPOINT;
    }

    /**
     * OpenAI service can be used by either not setting the endpoint or by setting the endpoint to start with
     * "https://api.openai.com".
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

    /**
     * Injects a boolean field `stream` into the input JSON.
     *
     * @param inputJson The input JSON to inject the field into.
     * @param stream The value of the field to inject.
     * @return The input JSON with the field injected.
     */
    @SuppressWarnings("unchecked")
    public static BinaryData injectStreamJsonField(BinaryData inputJson, boolean stream) {
        Map<String, Object> mapJson = inputJson.toObject(Map.class);
        mapJson.put("stream", stream);
        inputJson = BinaryData.fromObject(mapJson);
        return inputJson;
    }
}
