// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.models.StructuredInputDefinition;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;

import java.util.LinkedHashMap;
import java.util.Map;

final class ToolSampleUtils {
    private ToolSampleUtils() {
    }

    static Map<String, StructuredInputDefinition> structuredInput(String name, String description) {
        Map<String, StructuredInputDefinition> definitions = new LinkedHashMap<>();
        definitions.put(name, new StructuredInputDefinition()
            .setDescription(description)
            .setRequired(true));
        return definitions;
    }

    static void printUrlCitations(Response response) {
        for (ResponseOutputItem item : response.output()) {
            if (!item.message().isPresent()) {
                continue;
            }
            for (ResponseOutputMessage.Content content : item.asMessage().content()) {
                if (!content.outputText().isPresent()) {
                    continue;
                }
                for (ResponseOutputText.Annotation annotation : content.asOutputText().annotations()) {
                    if (annotation.isUrlCitation()) {
                        ResponseOutputText.Annotation.UrlCitation citation = annotation.asUrlCitation();
                        System.out.printf("Citation: %s (%s)%n", citation.title(), citation.url());
                    }
                }
            }
        }
    }

    static ContainerFile findContainerFile(Response response) {
        for (ResponseOutputItem item : response.output()) {
            if (!item.message().isPresent()) {
                continue;
            }
            for (ResponseOutputMessage.Content content : item.asMessage().content()) {
                if (!content.outputText().isPresent()) {
                    continue;
                }
                for (ResponseOutputText.Annotation annotation : content.asOutputText().annotations()) {
                    if (annotation.isContainerFileCitation()) {
                        ResponseOutputText.Annotation.ContainerFileCitation citation
                            = annotation.asContainerFileCitation();
                        return new ContainerFile(citation.containerId(), citation.fileId(), citation.filename());
                    }
                }
            }
        }
        return null;
    }

    static final class ContainerFile {
        private final String containerId;
        private final String fileId;
        private final String filename;

        ContainerFile(String containerId, String fileId, String filename) {
            this.containerId = containerId;
            this.fileId = fileId;
            this.filename = filename;
        }

        String getContainerId() {
            return containerId;
        }

        String getFileId() {
            return fileId;
        }

        String getFilename() {
            return filename;
        }
    }
}
