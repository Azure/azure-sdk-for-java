// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.ToolboxesAsyncClient;
import com.azure.ai.agents.ToolboxesClient;
import com.azure.ai.agents.models.McpToolboxTool;
import com.azure.ai.agents.models.ToolboxSearchPreviewToolboxTool;
import com.azure.ai.agents.models.ToolboxTool;
import com.azure.ai.agents.models.ToolboxToolType;
import com.azure.ai.agents.models.ToolboxVersionDetails;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

@Disabled("TODO: re-record once service no longer requires Foundry-Features opt-in keys for these operations.")
public class ToolboxSamplesTests extends ClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V1)));
        return argumentsList.stream();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void toolboxSearchToolboxSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ToolboxesClient toolboxesClient = getClientBuilder(httpClient, serviceVersion).buildToolboxesClient();
        String toolboxName = "toolbox-search-tool-java-test";

        try {
            toolboxesClient.deleteToolbox(toolboxName);
        } catch (ResourceNotFoundException ignored) {
            // The sample toolbox does not already exist.
        }

        try {
            ToolboxSearchPreviewToolboxTool toolboxSearchTool
                = new ToolboxSearchPreviewToolboxTool().setName("search_tools")
                    .setDescription("Search over available toolbox tools at runtime.");

            ToolboxVersionDetails version
                = toolboxesClient.createToolboxVersion(toolboxName, Collections.singletonList(toolboxSearchTool),
                    "Toolbox version with a Toolbox Search preview tool.", null, null, null);

            Assertions.assertNotNull(version);
            Assertions.assertEquals(toolboxName, version.getName());
            Assertions.assertFalse(version.getTools().isEmpty());
            Assertions.assertEquals(ToolboxToolType.TOOLBOX_SEARCH_PREVIEW, version.getTools().get(0).getType());
        } finally {
            try {
                toolboxesClient.deleteToolbox(toolboxName);
            } catch (ResourceNotFoundException ignored) {
                // The sample toolbox may not have been created.
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void toolboxesAsyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        ToolboxesAsyncClient toolboxesAsyncClient = builder.buildToolboxesAsyncClient();
        String toolboxName = "toolbox-with-mcp-tool-java-async-test";

        List<ToolboxTool> toolsWithMcpApprovalNever = Collections
            .singletonList(new McpToolboxTool("api_specs").setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
                .setRequireApproval(BinaryData.fromString("\"never\"")));

        List<ToolboxTool> toolsWithMcpApprovalAlways = Collections
            .singletonList(new McpToolboxTool("api_specs").setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
                .setRequireApproval(BinaryData.fromString("\"always\"")));

        Mono<Void> testFlow = toolboxesAsyncClient.deleteToolbox(toolboxName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(toolboxesAsyncClient.createToolboxVersion(toolboxName, toolsWithMcpApprovalNever,
                "Toolbox version with MCP require_approval set to 'never'.", null, null, null))
            .doOnNext(created -> {
                Assertions.assertEquals(toolboxName, created.getName());
                Assertions.assertEquals("1", created.getVersion());
            })
            .then(toolboxesAsyncClient.createToolboxVersion(toolboxName, toolsWithMcpApprovalAlways,
                "Toolbox version with MCP require_approval set to 'always'.", null, null, null))
            .doOnNext(created -> {
                Assertions.assertEquals(toolboxName, created.getName());
                Assertions.assertEquals("2", created.getVersion());
            })
            .then(toolboxesAsyncClient.updateToolbox(toolboxName, "2"))
            .flatMap(updated -> toolboxesAsyncClient.getToolboxVersion(updated.getName(), updated.getDefaultVersion()))
            .doOnNext(version -> assertMcpRequireApproval(version, "always"))
            .then(toolboxesAsyncClient.updateToolbox(toolboxName, "1"))
            .flatMap(updated -> toolboxesAsyncClient.getToolboxVersion(updated.getName(), updated.getDefaultVersion()))
            .doOnNext(version -> assertMcpRequireApproval(version, "never"))
            .thenMany(toolboxesAsyncClient.listToolboxes().take(10))
            .filter(toolbox -> toolboxName.equals(toolbox.getName()))
            .next()
            .doOnNext(toolbox -> Assertions.assertEquals(toolboxName, toolbox.getName()))
            .then(toolboxesAsyncClient.deleteToolbox(toolboxName));

        StepVerifier.create(testFlow).verifyComplete();
    }

    private static void assertMcpRequireApproval(ToolboxVersionDetails version, String expectedApproval) {
        Assertions.assertNotNull(version);
        Assertions.assertFalse(version.getTools().isEmpty());
        Assertions.assertTrue(version.getTools().get(0) instanceof McpToolboxTool);
        McpToolboxTool mcpTool = (McpToolboxTool) version.getTools().get(0);
        Assertions.assertEquals("\"" + expectedApproval + "\"", mcpTool.getRequireApproval().toString());
    }
}
