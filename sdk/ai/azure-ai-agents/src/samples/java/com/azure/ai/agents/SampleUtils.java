// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseOutputItem;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SampleUtils {

    private SampleUtils() {
    }

    /**
     * Creates an agent reference for a version returned by the service.
     *
     * @param agent the agent version.
     * @return the agent reference.
     */
    public static AgentReference toAgentReference(AgentVersionDetails agent) {
        return new AgentReference(agent.getName()).setVersion(agent.getVersion());
    }

    /**
     * Prints all text output in a response.
     *
     * @param response the response to print.
     */
    public static void printResponseText(Response response) {
        for (ResponseOutputItem item : response.output()) {
            item.message().ifPresent(message -> message.content().forEach(content ->
                content.outputText().ifPresent(text -> System.out.println(text.text()))));
        }
    }

    /**
     * Creates a temporary UTF-8 text file.
     *
     * @param prefix the file prefix.
     * @param suffix the file suffix.
     * @param content the file content.
     * @return the temporary file path.
     */
    public static Path createTempFile(String prefix, String suffix, String content) {
        try {
            Path path = Files.createTempFile(prefix, suffix);
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets the path to a file in the sample resource folder.
     * @param fileName the name of the file in the sample resource folder
     * @return Path to the sample resource file
     */
    public static Path getResourcePath(String fileName) {
        try {
            URL resourceUrl = SampleUtils.class.getClassLoader().getResource(fileName);
            if (resourceUrl != null) {
                return Paths.get(resourceUrl.toURI());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI for sample resource: " + fileName, e);
        }

        Path[] resourcePaths = new Path[] {
            Paths.get("src", "samples", "resources", fileName),
            Paths.get("sdk", "ai", "azure-ai-agents", "src", "samples", "resources", fileName)
        };
        for (Path resourcePath : resourcePaths) {
            if (Files.exists(resourcePath)) {
                return resourcePath;
            }
        }
        throw new RuntimeException("Sample resource file not found: " + fileName);
    }
}
