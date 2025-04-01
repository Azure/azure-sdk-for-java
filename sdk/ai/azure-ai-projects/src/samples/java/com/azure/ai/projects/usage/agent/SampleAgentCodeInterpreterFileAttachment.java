package com.azure.ai.projects.usage.agent;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentsClient;
import com.azure.ai.projects.models.*;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SampleAgentCodeInterpreterFileAttachment {

    @Test
    void codeInterpreterFileAttachmentExample() throws FileNotFoundException, URISyntaxException {
        AgentsClient agentsClient
            = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .subscriptionId(Configuration.getGlobalConfiguration().get("SUBSCRIPTIONID", "subscriptionid"))
            .resourceGroupName(Configuration.getGlobalConfiguration().get("RESOURCEGROUPNAME", "resourcegroupname"))
            .projectName(Configuration.getGlobalConfiguration().get("PROJECTNAME", "projectname"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAgentsClient();

        var htmlFile = getFile("sample_test.html");

        var agentName = "code_interpreter_file_attachment_example";
        var ciTool = new CodeInterpreterToolDefinition();
        var createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(List.of(ciTool));
        Agent agent = agentsClient.createAgent(createAgentOptions);

        OpenAIFile uploadedFile = agentsClient.uploadFile(
            new UploadFileRequest(
                new FileDetails(BinaryData.fromFile(htmlFile)).setFilename("sample_test.html"),
                FilePurpose.AGENTS));

        MessageAttachment messageAttachment = new MessageAttachment(
            List.of(BinaryData.fromObject(ciTool))
        ).setFileId(uploadedFile.getId());

        var thread = agentsClient.createThread();
        assertNotNull(thread);
        var createdMessage = agentsClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "What does the attachment say?",
            List.of(messageAttachment),
            null
        );
        assertNotNull(createdMessage);

        //run agent
        var createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
            .setAdditionalInstructions("");
        var threadRun = agentsClient.createRun(createRunOptions);
        assertNotNull(threadRun);

        try {
            do {
                Thread.sleep(500);
                threadRun = agentsClient.getRun(thread.getId(), threadRun.getId());
            }
            while (
                threadRun.getStatus() == RunStatus.QUEUED
                    || threadRun.getStatus() == RunStatus.IN_PROGRESS
                    || threadRun.getStatus() == RunStatus.REQUIRES_ACTION);

            if (threadRun.getStatus() == RunStatus.FAILED) {
                System.out.println(threadRun.getLastError().getMessage());
            }

            var runMessages = agentsClient.listMessages(thread.getId());
            for (ThreadMessage message : runMessages.getData())
            {
                System.out.print(String.format("%1$s - %2$s : ", message.getCreatedAt(), message.getRole()));
                for (MessageContent contentItem : message.getContent())
                {
                    if (contentItem instanceof MessageTextContent)
                    {
                        System.out.print((((MessageTextContent) contentItem).getText().getValue()));
                    }
                    else if (contentItem instanceof MessageImageFileContent)
                    {
                        String imageFileId = (((MessageImageFileContent) contentItem).getImageFile().getFileId());
                        System.out.print("Image from ID: " + imageFileId);
                    }
                    System.out.println();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            //cleanup
            agentsClient.deleteThread(thread.getId());
            agentsClient.deleteAgent(agent.getId());
        }
    }

    private Path getFile(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }
        File file = new File(resource.toURI());
        return file.toPath();
    }
}
