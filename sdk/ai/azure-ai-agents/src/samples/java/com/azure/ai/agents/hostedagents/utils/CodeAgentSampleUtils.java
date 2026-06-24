// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents.utils;

import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.AgentEndpointProtocol;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeConfiguration;
import com.azure.ai.agents.models.CodeDependencyResolution;
import com.azure.ai.agents.models.CodeFileDetails;
import com.azure.ai.agents.models.CreateAgentVersionFromCodeContent;
import com.azure.ai.agents.models.CreateAgentVersionFromCodeMetadata;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.ProtocolVersionRecord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class CodeAgentSampleUtils {
    public static final String SAMPLE_AGENT_NAME = "java-code-agent-sample";
    private static final String CODE_AGENT_ASSETS_PATH = "assets/";
    private static final int READ_BUFFER_SIZE = 8192;

    private CodeAgentSampleUtils() {
    }

    public static CreateAgentVersionFromCodeContent createAgentVersionFromCodeContent(Path codeZipPath) {
        return new CreateAgentVersionFromCodeContent(createMetadata(), createCodeFileDetails(codeZipPath));
    }

    public static Path createCodeZip() throws IOException {
        Path codeZipPath = Files.createTempFile("responses-echo-agent-", ".zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(codeZipPath))) {
            addZipEntry(zipOutputStream, "main.py", SampleUtils.getResourcePath(CODE_AGENT_ASSETS_PATH + "main.py"));
            addZipEntry(zipOutputStream, "requirements.txt",
                SampleUtils.getResourcePath(CODE_AGENT_ASSETS_PATH + "requirements.txt"));
        }
        return codeZipPath;
    }

    public static String sha256(Path filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[READ_BUFFER_SIZE];
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            byte[] hash = digest.digest();
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 is not available.", e);
        }
    }

    public static void printLatestVersion(AgentVersionDetails version) {
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
                "python_3_13",
                Arrays.asList("python", "main.py"),
                CodeDependencyResolution.REMOTE_BUILD))
            .setProtocolVersions(Collections.singletonList(
                new ProtocolVersionRecord(AgentEndpointProtocol.RESPONSES, "1.0.0")));
    }

    private static CodeFileDetails createCodeFileDetails(Path codeZipPath) {
        return new CodeFileDetails(codeZipPath)
            .setFilename("responses-echo-agent.zip")
            .setContentType("application/zip");
    }

    private static void addZipEntry(ZipOutputStream zipOutputStream, String name, Path sourcePath) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(name));
        zipOutputStream.write(Files.readAllBytes(sourcePath));
        zipOutputStream.closeEntry();
    }
}
