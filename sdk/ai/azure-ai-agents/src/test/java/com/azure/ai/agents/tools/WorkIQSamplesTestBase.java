// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WorkIqPreviewTool;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

abstract class WorkIQSamplesTestBase extends ClientTestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final String WORK_IQ_RESOURCE_LOCK = "work-iq";

    private static final String DEFAULT_USER_INPUT = "Use Work IQ to summarize the available enterprise context.";

    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V1)));
        return argumentsList.stream();
    }

    PromptAgentDefinition createAgentDefinition() {
        WorkIqPreviewTool workIqTool
            = new WorkIqPreviewTool(getRecordedConfig("WORK_IQ_PROJECT_CONNECTION_ID")).setName("work_iq_lookup")
                .setDescription("Use Work IQ to answer questions grounded in enterprise data.");

        return new PromptAgentDefinition(getRecordedConfig("FOUNDRY_MODEL_NAME"))
            .setInstructions("Use the available Work IQ tools to answer questions and perform tasks.")
            .setTools(Collections.singletonList(workIqTool));
    }

    String getUserInput() {
        return Configuration.getGlobalConfiguration().get("WORK_IQ_USER_INPUT", DEFAULT_USER_INPUT);
    }

    ResponseCreateParams.Builder createResponseParams() {
        return ResponseCreateParams.builder().input(getUserInput());
    }

    void assertCompletedResponse(Response response) {
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.status().isPresent());
        Assertions.assertEquals(ResponseStatus.COMPLETED, response.status().get());
        Assertions.assertFalse(response.output().isEmpty());
        Assertions.assertTrue(response.output().stream().anyMatch(item -> item.isMessage()));
    }

    private String getRecordedConfig(String name) {
        if (getTestMode() == TestMode.PLAYBACK) {
            return testResourceNamer.recordValueFromConfig(name);
        }

        String value = Configuration.getGlobalConfiguration().get(name);
        if (getTestMode() == TestMode.RECORD) {
            testResourceNamer.recordValueFromConfig(name);
        }
        return value;
    }
}
