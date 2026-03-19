// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.ai.agents.models.AzureCreateResponseResult;
import com.openai.models.responses.Response;

/**
 * Utility methods for working with Azure-specific response properties.
 */
public final class ResponsesUtils {

    private ResponsesUtils() {
        // utility class, prevent instantiation
    }

    /**
     * Extracts Azure-specific fields from a Response's additional properties.
     *
     * @param response the OpenAI response.
     * @return the Azure-specific create response result, or null if not present.
     */
    public static AzureCreateResponseResult getAzureFields(Response response) {
        return OpenAIJsonHelper.fromAdditionalProperties(response._additionalProperties(),
            AzureCreateResponseResult::fromJson);
    }
}
