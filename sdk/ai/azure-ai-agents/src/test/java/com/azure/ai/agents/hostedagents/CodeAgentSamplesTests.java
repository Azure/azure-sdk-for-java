// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.models.AgentEndpointProtocol;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeConfiguration;
import com.azure.ai.agents.models.CodeDependencyResolution;
import com.azure.ai.agents.models.CodeFileDetails;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.ProtocolVersionRecord;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.azure.ai.agents.TestUtils.getTestResourcePath;
import static com.azure.core.test.TestProxyTestBase.getHttpClients;

@Disabled("Direct code deployment is not enabled for the current test subscription.")
public class CodeAgentSamplesTests extends ClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String TEST_AGENT_NAME = "java-code-agent-test";
    private static final String TEST_DESCRIPTION
        = "Code-based hosted agent test created by the Azure AI Agents Java SDK.";
    private static final String CODE_AGENT_ASSETS_PATH = "assets/";

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
        String agentName = TEST_AGENT_NAME;

        try {
            agentsClient.deleteAgent(agentName);
        } catch (ResourceNotFoundException ignored) {
            // The sample agent does not already exist.
        }

        try {
            Path codeZipPath = createCodeZip();

            AgentVersionDetails version = agentsClient.createAgentVersionFromCode(agentName,
                createHostedAgentDefinition(), createCodeFileDetails(codeZipPath), TEST_DESCRIPTION, testMetadata());
            Assertions.assertNotNull(version);
            Assertions.assertEquals(agentName, version.getName());
            Assertions.assertNotNull(version.getVersion());

            Path downloadPath = Files.createTempDirectory(agentName + "-").resolve("code.zip");
            agentsClient.downloadAgentCodeWithResponse(agentName, downloadPath.toString(), new RequestOptions());
            Assertions.assertTrue(Files.size(downloadPath) > 0);

            AgentVersionDetails newVersion = agentsClient.createAgentVersionFromCode(agentName,
                createHostedAgentDefinition(), createCodeFileDetails(codeZipPath), TEST_DESCRIPTION, testMetadata());
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
        String agentName = TEST_AGENT_NAME + "-async";
        Path codeZipPath = createCodeZip();
        Path downloadPath = Files.createTempDirectory(agentName + "-").resolve("code.zip");

        Mono<Void> testFlow = agentsAsyncClient.deleteAgent(agentName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(agentsAsyncClient.createAgentVersionFromCode(agentName, createHostedAgentDefinition(),
                createCodeFileDetails(codeZipPath), TEST_DESCRIPTION, testMetadata()))
            .flatMap(version -> {
                Assertions.assertNotNull(version);
                Assertions.assertEquals(agentName, version.getName());
                Assertions.assertNotNull(version.getVersion());

                return agentsAsyncClient.downloadAgentCodeWithResponse(agentName, downloadPath.toString(),
                    new RequestOptions());
            })
            .then(Mono.fromCallable(() -> {
                Assertions.assertTrue(Files.size(downloadPath) > 0);
                return downloadPath;
            }))
            .flatMap(ignored -> {
                return agentsAsyncClient.createAgentVersionFromCode(agentName, createHostedAgentDefinition(),
                    createCodeFileDetails(codeZipPath), TEST_DESCRIPTION, testMetadata());
            })
            .doOnNext(newVersion -> {
                Assertions.assertNotNull(newVersion);
                Assertions.assertEquals(agentName, newVersion.getName());
                Assertions.assertNotNull(newVersion.getVersion());
            })
            .then(agentsAsyncClient.deleteAgent(agentName));

        StepVerifier.create(testFlow).verifyComplete();
    }

    private static Map<String, String> testMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("test", "code-agent");
        return metadata;
    }

    private static Path createCodeZip() throws IOException {
        Path codeZipPath = Files.createTempFile("responses-echo-agent-", ".zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(codeZipPath))) {
            addZipEntry(zipOutputStream, "main.py", getTestResourcePath(CODE_AGENT_ASSETS_PATH + "main.py"));
            addZipEntry(zipOutputStream, "requirements.txt",
                getTestResourcePath(CODE_AGENT_ASSETS_PATH + "requirements.txt"));
        }
        return codeZipPath;
    }

    private static HostedAgentDefinition createHostedAgentDefinition() {
        return new HostedAgentDefinition("0.5", "1Gi")
            .setCodeConfiguration(new CodeConfiguration("python_3_13", Arrays.asList("python", "main.py"),
                CodeDependencyResolution.REMOTE_BUILD))
            .setProtocolVersions(
                Collections.singletonList(new ProtocolVersionRecord(AgentEndpointProtocol.RESPONSES, "1.0.0")));
    }

    private static CodeFileDetails createCodeFileDetails(Path codeZipPath) {
        return new CodeFileDetails(codeZipPath.toString()).setFilename("responses-echo-agent.zip")
            .setContentType("application/zip");
    }

    private static void addZipEntry(ZipOutputStream zipOutputStream, String name, Path sourcePath) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(name));
        Files.copy(sourcePath, zipOutputStream);
        zipOutputStream.closeEntry();
    }
}
