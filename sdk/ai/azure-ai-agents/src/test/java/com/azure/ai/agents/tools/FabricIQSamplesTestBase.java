// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.models.FabricIqPreviewTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
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

abstract class FabricIQSamplesTestBase extends ClientTestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final String FABRIC_IQ_RESOURCE_LOCK = "fabric-iq";

    private static final String DEFAULT_USER_INPUT = "Tell me weather history in London, Ohio";

    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V1)));
        return argumentsList.stream();
    }

    PromptAgentDefinition createAgentDefinition() {
        FabricIqPreviewTool fabricIqTool
            = new FabricIqPreviewTool(getRecordedConfig("FABRIC_IQ_PROJECT_CONNECTION_ID")).setRequireApproval("never");

        return new PromptAgentDefinition(getRecordedConfig("FOUNDRY_MODEL_NAME"))
            .setInstructions("Use the available Fabric IQ tools to answer questions and perform tasks.")
            .setTools(Collections.singletonList(fabricIqTool));
    }

    String getUserInput() {
        return Configuration.getGlobalConfiguration().get("FABRIC_IQ_USER_INPUT", DEFAULT_USER_INPUT);
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
