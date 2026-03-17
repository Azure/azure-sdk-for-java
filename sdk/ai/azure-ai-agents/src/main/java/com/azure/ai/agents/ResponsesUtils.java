package com.azure.ai.agents;

import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.ai.agents.models.AzureCreateResponseResult;
import com.openai.models.responses.Response;

public class ResponsesUtils {

    private ResponsesUtils() {
        // utility class, prevent instantiation
    }

    public static AzureCreateResponseResult getAzureFields(Response response) {
        return OpenAIJsonHelper.fromAdditionalProperties(response._additionalProperties(),
            AzureCreateResponseResult::fromJson);
    }
}
