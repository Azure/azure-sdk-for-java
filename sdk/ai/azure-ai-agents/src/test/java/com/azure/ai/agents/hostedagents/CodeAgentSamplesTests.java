// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.BetaAgentsAsyncClient;
import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.hostedagents.utils.CodeAgentSampleUtils;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

@Disabled("Direct code deployment is not enabled for the current test subscription.")
public class CodeAgentSamplesTests extends ClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V1)));
        return argumentsList.stream();
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void codeAgentSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) throws Exception {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        BetaAgentsClient betaAgentsClient = builder.beta().buildBetaAgentsClient();
        String agentName = CodeAgentSampleUtils.SAMPLE_AGENT_NAME + "-test";

        try {
            agentsClient.deleteAgent(agentName);
        } catch (ResourceNotFoundException ignored) {
            // The sample agent does not already exist.
        }

        try {
            BinaryData codeZip = CodeAgentSampleUtils.createCodeZip();
            String codeZipSha256 = CodeAgentSampleUtils.sha256(codeZip);

            AgentVersionDetails version = betaAgentsClient.createAgentVersionFromCode(agentName, codeZipSha256,
                CodeAgentSampleUtils.createAgentVersionFromCodeContent(codeZip));
            Assertions.assertNotNull(version);
            Assertions.assertEquals(agentName, version.getName());
            Assertions.assertNotNull(version.getVersion());

            BinaryData downloadedCode = betaAgentsClient.downloadAgentCode(agentName, null);
            Assertions.assertNotNull(downloadedCode);
            Assertions.assertTrue(downloadedCode.toBytes().length > 0);

            Path downloadPath = Files.createTempFile(agentName + "-", ".zip");
            Files.write(downloadPath, downloadedCode.toBytes());
            Assertions.assertTrue(Files.size(downloadPath) > 0);

            AgentVersionDetails newVersion = betaAgentsClient.createAgentVersionFromCode(agentName, codeZipSha256,
                CodeAgentSampleUtils.createAgentVersionFromCodeContent(codeZip));
            Assertions.assertNotNull(newVersion);
            Assertions.assertEquals(agentName, newVersion.getName());
            Assertions.assertNotNull(newVersion.getVersion());
        } finally {
            try {
                agentsClient.deleteAgent(agentName);
            } catch (ResourceNotFoundException ignored) {
                // The sample agent may not have been created.
            }
        }
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void codeAgentAsyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) throws Exception {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        BetaAgentsAsyncClient betaAgentsAsyncClient = builder.beta().buildBetaAgentsAsyncClient();
        String agentName = CodeAgentSampleUtils.SAMPLE_AGENT_NAME + "-async-test";
        BinaryData codeZip = CodeAgentSampleUtils.createCodeZip();
        String codeZipSha256 = CodeAgentSampleUtils.sha256(codeZip);

        Mono<Void> testFlow = agentsAsyncClient.deleteAgent(agentName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(betaAgentsAsyncClient.createAgentVersionFromCode(agentName, codeZipSha256,
                CodeAgentSampleUtils.createAgentVersionFromCodeContent(codeZip)))
            .flatMap(version -> {
                Assertions.assertNotNull(version);
                Assertions.assertEquals(agentName, version.getName());
                Assertions.assertNotNull(version.getVersion());

                return betaAgentsAsyncClient.downloadAgentCode(agentName, null);
            })
            .flatMap(downloadedCode -> {
                Assertions.assertNotNull(downloadedCode);
                Assertions.assertTrue(downloadedCode.toBytes().length > 0);
                return betaAgentsAsyncClient.createAgentVersionFromCode(agentName, codeZipSha256,
                    CodeAgentSampleUtils.createAgentVersionFromCodeContent(codeZip));
            })
            .doOnNext(newVersion -> {
                Assertions.assertNotNull(newVersion);
                Assertions.assertEquals(agentName, newVersion.getName());
                Assertions.assertNotNull(newVersion.getVersion());
            })
            .then(agentsAsyncClient.deleteAgent(agentName));

        StepVerifier.create(testFlow).verifyComplete();
    }
}
