// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.models.AgentProtocol;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeConfiguration;
import com.azure.ai.agents.models.CodeDependencyResolution;
import com.azure.ai.agents.models.CodeFileDetails;
import com.azure.ai.agents.models.CreateAgentVersionFromCodeContent;
import com.azure.ai.agents.models.CreateAgentVersionFromCodeMetadata;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.ProtocolVersionRecord;
import com.azure.core.util.BinaryData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class CodeAgentSampleUtils {
    static final String SAMPLE_AGENT_NAME = "java-code-agent-sample";

    private CodeAgentSampleUtils() {
    }

    static CreateAgentVersionFromCodeContent createAgentVersionFromCodeContent(BinaryData codeZip) {
        return new CreateAgentVersionFromCodeContent(createMetadata(), createCodeFileDetails(codeZip));
    }

    static BinaryData createCodeZip() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            addZipEntry(zipOutputStream, "main.py", createMainPy());
            addZipEntry(zipOutputStream, "requirements.txt", createRequirementsTxt());
        }
        return BinaryData.fromBytes(outputStream.toByteArray());
    }

    static String sha256(BinaryData data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.toBytes());
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 is not available.", e);
        }
    }

    static void printLatestVersion(AgentVersionDetails version) {
        System.out.printf("Agent version: %s%n", version.getVersion());
        System.out.printf("Status: %s%n", version.getStatus());
        if (version.getDefinition() instanceof HostedAgentDefinition) {
            HostedAgentDefinition definition = (HostedAgentDefinition) version.getDefinition();
            if (definition.getCodeConfiguration() != null) {
                System.out.printf("Code content hash: %s%n", definition.getCodeConfiguration().getContentSha256());
            }
        }
    }

    private static CreateAgentVersionFromCodeMetadata createMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("sample", "code-agent");

        return new CreateAgentVersionFromCodeMetadata(createHostedAgentDefinition())
            .setDescription("Code-based hosted agent sample created by the Azure AI Agents Java SDK.")
            .setMetadata(metadata);
    }

    private static HostedAgentDefinition createHostedAgentDefinition() {
        return new HostedAgentDefinition("0.5", "1Gi")
            .setCodeConfiguration(new CodeConfiguration(
                "python_3_11",
                Arrays.asList("python", "main.py"),
                CodeDependencyResolution.REMOTE_BUILD))
            .setProtocolVersions(Collections.singletonList(
                new ProtocolVersionRecord(AgentProtocol.RESPONSES, "1.0.0")));
    }

    private static CodeFileDetails createCodeFileDetails(BinaryData codeZip) {
        return new CodeFileDetails(codeZip)
            .setFilename("responses-echo-agent.zip")
            .setContentType("application/zip");
    }

    private static void addZipEntry(ZipOutputStream zipOutputStream, String name, String content) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(name));
        zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
        zipOutputStream.closeEntry();
    }

    private static String createMainPy() {
        return "import asyncio\n"
            + "import logging\n\n"
            + "from azure.ai.agentserver.responses import (\n"
            + "    CreateResponse,\n"
            + "    ResponseContext,\n"
            + "    ResponsesAgentServerHost,\n"
            + "    TextResponse,\n"
            + ")\n\n"
            + "logging.basicConfig(level=logging.INFO)\n"
            + "logger = logging.getLogger(__name__)\n"
            + "app = ResponsesAgentServerHost()\n\n"
            + "@app.create_handler\n"
            + "async def handler(request: CreateResponse, context: ResponseContext, "
            + "cancellation_signal: asyncio.Event):\n"
            + "    input_text = await context.get_input_text()\n"
            + "    logger.info('Received input: %s', input_text)\n"
            + "    return TextResponse(context, request, text=f'Echo: {input_text}')\n\n"
            + "def main() -> None:\n"
            + "    app.run()\n\n"
            + "if __name__ == '__main__':\n"
            + "    main()\n";
    }

    private static String createRequirementsTxt() {
        return "--index-url https://pkgs.dev.azure.com/azure-sdk/public/_packaging/azure-sdk-for-python/pypi/simple/\n"
            + "azure-ai-agentserver-core==2.0.0a20260410006\n"
            + "azure-ai-agentserver-invocations==1.0.0a20260410006\n"
            + "azure-ai-agentserver-responses==1.0.0a20260410006\n";
    }
}
