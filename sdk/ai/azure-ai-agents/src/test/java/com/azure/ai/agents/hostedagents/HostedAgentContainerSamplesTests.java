// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

public class HostedAgentContainerSamplesTests extends ClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V1)));
        return argumentsList.stream();
    }

    @Disabled("Requires FOUNDRY_AGENT_CONTAINER_IMAGE: a prebuilt, pushed hosted-agent container image URI.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void sessionsSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        Assertions
            .fail("Enable after providing FOUNDRY_AGENT_CONTAINER_IMAGE and recording hosted-agent session flow.");
    }

    @Disabled("Requires FOUNDRY_AGENT_CONTAINER_IMAGE: a prebuilt, pushed hosted-agent container image URI.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void sessionFilesSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        Assertions.fail("Enable after providing FOUNDRY_AGENT_CONTAINER_IMAGE and recording session file flow.");
    }

    @Disabled("Requires FOUNDRY_AGENT_CONTAINER_IMAGE: a prebuilt, pushed hosted-agent container image URI.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void agentEndpointSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        Assertions.fail("Enable after providing FOUNDRY_AGENT_CONTAINER_IMAGE and recording agent endpoint flow.");
    }

    @Disabled("Requires FOUNDRY_AGENT_CONTAINER_IMAGE: a prebuilt, pushed hosted-agent container image URI.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void sessionLogStreamSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        Assertions.fail("Enable after providing FOUNDRY_AGENT_CONTAINER_IMAGE and recording session log stream flow.");
    }
}
