// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

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

public class FabricIQSamplesTests extends ClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V1)));
        return argumentsList.stream();
    }

    @Disabled("Requires FABRIC_IQ_PROJECT_CONNECTION_ID and FOUNDRY_MODEL_NAME.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void fabricIqSyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        Assertions.fail("Enable after providing FABRIC_IQ_PROJECT_CONNECTION_ID and FOUNDRY_MODEL_NAME.");
    }

    @Disabled("Requires FABRIC_IQ_PROJECT_CONNECTION_ID and FOUNDRY_MODEL_NAME.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void fabricIqAsyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        Assertions.fail("Enable after providing FABRIC_IQ_PROJECT_CONNECTION_ID and FOUNDRY_MODEL_NAME.");
    }
}
