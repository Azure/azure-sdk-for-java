// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CodeInterpreterToolDefinition;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FileDetails;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.MessageAttachment;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.ai.agents.persistent.models.UploadFileRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessages;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion;

public class AgentCodeInterpreterFileAttachmentSample {

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {

        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        PersistentAgentsAdministrationClient administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
        ThreadsClient threadsClient = agentsClient.getThreadsClient();
        MessagesClient messagesClient = agentsClient.getMessagesClient();
        RunsClient runsClient = agentsClient.getRunsClient();
        FilesClient filesClient = agentsClient.getFilesClient();

        Path htmlFile = getFile("sample.html");

        String agentName = "code_interpreter_file_attachment_example";
        CodeInterpreterToolDefinition ciTool = new CodeInterpreterToolDefinition();
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini").setName(agentName).setInstructions("You are a helpful agent").setTools(Arrays.asList(ciTool));
        PersistentAgent agent = administrationClient.createAgent(createAgentOptions);

        FileInfo uploadedFile = filesClient.uploadFile(new UploadFileRequest(
            new FileDetails(BinaryData.fromFile(htmlFile))
            .setFilename("sample.html"), FilePurpose.AGENTS));

        MessageAttachment messageAttachment = new MessageAttachment(Arrays.asList(BinaryData.fromObject(ciTool))).setFileId(uploadedFile.getId());

        PersistentAgentThread thread = threadsClient.createThread();
        ThreadMessage createdMessage = messagesClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "What does the attachment say?",
            Arrays.asList(messageAttachment),
            null);

        try {
            //run agent
            CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId()).setAdditionalInstructions("");
            ThreadRun threadRun = runsClient.createRun(createRunOptions);

            waitForRunCompletion(thread.getId(), threadRun, runsClient);
            printRunMessages(messagesClient, thread.getId());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //cleanup
            threadsClient.deleteThread(thread.getId());
            administrationClient.deleteAgent(agent.getId());
        }
    }

    private static Path getFile(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = AgentCodeInterpreterFileAttachmentSample.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }
        File file = new File(resource.toURI());
        return file.toPath();
    }
}
