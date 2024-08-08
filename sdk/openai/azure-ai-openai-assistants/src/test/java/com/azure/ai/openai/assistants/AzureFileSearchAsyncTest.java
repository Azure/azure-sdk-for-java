// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CreateFileSearchToolResourceOptions;
import com.azure.ai.openai.assistants.models.CreateFileSearchToolResourceVectorStoreOptions;
import com.azure.ai.openai.assistants.models.CreateFileSearchToolResourceVectorStoreOptionsList;
import com.azure.ai.openai.assistants.models.CreateToolResourcesOptions;
import com.azure.ai.openai.assistants.models.FilePurpose;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.implementation.AsyncUtils;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureFileSearchAsyncTest extends AssistantsClientTestBase {

    AssistantsAsyncClient client;

    @Disabled("file_search tools are not supported in Azure")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void basicFileSearch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);

        createRetrievalRunner((fileDetails, assistantCreationOptions) -> {
            // Upload file
            StepVerifier.create(client.uploadFile(fileDetails, FilePurpose.ASSISTANTS)
                .flatMap(openAIFile -> {
                    // Create assistant
                    AsyncUtils cleanUp = new AsyncUtils();
                    CreateToolResourcesOptions createToolResourcesOptions = new CreateToolResourcesOptions();
                    createToolResourcesOptions.setFileSearch(
                        new CreateFileSearchToolResourceOptions(
                            new CreateFileSearchToolResourceVectorStoreOptionsList(
                                Arrays.asList(new CreateFileSearchToolResourceVectorStoreOptions(
                                    Arrays.asList(openAIFile.getId()),
                                    null
                                )))));
                    assistantCreationOptions.setToolResources(createToolResourcesOptions);
                    cleanUp.setFile(openAIFile);
                    return client.createAssistant(assistantCreationOptions).zipWith(Mono.just(cleanUp));
                }).flatMap(tuple -> {
                    Assistant assistant = tuple.getT1();
                    AsyncUtils cleanUp = tuple.getT2();
                    cleanUp.setAssistant(assistant);

                    return client.createThread(new AssistantThreadCreationOptions())
                        .zipWith(Mono.just(cleanUp));
                }).flatMap(tuple -> {
                    AssistantThread thread = tuple.getT1();
                    AsyncUtils cleanUp = tuple.getT2();
                    cleanUp.setThread(thread);

                    return client.createMessage(
                        thread.getId(),
                        new ThreadMessageOptions(
                            MessageRole.USER,
                            "Can you give me the documented codes for 'banana' and 'orange'?"
                    )).flatMap(_message ->
                        client.createRun(cleanUp.getThread(), cleanUp.getAssistant())
                            .flatMap(createdRun ->
                                client.getRun(cleanUp.getThread().getId(), createdRun.getId()).zipWith(Mono.just(cleanUp))
                                    .repeatWhen(completed -> completed.delayElements(Duration.ofMillis(1000)))
                                    .takeUntil(tuple2 -> {
                                        ThreadRun run = tuple2.getT1();

                                        return run.getStatus() != RunStatus.IN_PROGRESS
                                            && run.getStatus() != RunStatus.QUEUED;
                                    })
                                    .last()
                            )
                    );
                }).flatMap(tuple -> {
                    ThreadRun run = tuple.getT1();
                    AsyncUtils cleanUp = tuple.getT2();

                    assertEquals(RunStatus.COMPLETED, run.getStatus());
                    assertEquals(cleanUp.getAssistant().getId(), run.getAssistantId());

                    return client.listMessages(cleanUp.getThread().getId()).zipWith(Mono.just(cleanUp));
                }).map(tuple -> {
                    PageableList<ThreadMessage> messageList = tuple.getT1();
                    AsyncUtils cleanUp = tuple.getT2();

                    assertEquals(2, messageList.getData().size());
                    ThreadMessage firstMessage = messageList.getData().get(0);

                    assertEquals(MessageRole.ASSISTANT, firstMessage.getRole());
                    assertFalse(firstMessage.getContent().isEmpty());

                    MessageTextContent firstMessageContent = (MessageTextContent) firstMessage.getContent().get(0);
                    assertNotNull(firstMessageContent);
                    assertTrue(firstMessageContent.getText().getValue().contains("232323"));

                    return cleanUp;
                })
                .flatMap(cleanUp -> client.deleteAssistant(cleanUp.getAssistant().getId())
                    .flatMap(_unused -> client.deleteFile(cleanUp.getFile().getId()))
                    .flatMap(_unused -> client.deleteThread(cleanUp.getThread().getId()))))
                // last deletion asserted
                .assertNext(threadDeletionStatus -> assertTrue(threadDeletionStatus.isDeleted()))
                .verifyComplete();
        });
    }
}
