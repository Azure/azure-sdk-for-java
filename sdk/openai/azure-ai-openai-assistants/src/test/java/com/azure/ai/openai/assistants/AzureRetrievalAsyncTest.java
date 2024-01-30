// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfThreadMessage;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.http.HttpClient;
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

public class AzureRetrievalAsyncTest extends AssistantsClientTestBase {

    AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void basicRetrieval(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);

        createRetrievalRunner((uploadFileRequest, assistantCreationOptions) -> {
            // Upload file
            StepVerifier.create(client.uploadFile(uploadFileRequest)
                .flatMap(openAIFile -> {
                    // Create assistant
                    CleanUp cleanUp = new CleanUp();
                    assistantCreationOptions.setFileIds(Arrays.asList(openAIFile.getId()));
                    cleanUp.file = openAIFile;
                    return client.createAssistant(assistantCreationOptions).zipWith(Mono.just(cleanUp));
                }).flatMap(tuple -> {
                    Assistant assistant = tuple.getT1();
                    CleanUp cleanUp = tuple.getT2();
                    cleanUp.assistant = assistant;

                    return client.createThread(new AssistantThreadCreationOptions())
                        .zipWith(Mono.just(cleanUp));
                }).flatMap(tuple -> {
                    AssistantThread thread = tuple.getT1();
                    CleanUp cleanUp = tuple.getT2();
                    cleanUp.thread = thread;

                    return client.createMessage(
                        thread.getId(),
                        MessageRole.USER,
                        "Can you give me the documented codes for 'banana' and 'orange'?"
                    ).flatMap(_message ->
                        client.createRun(cleanUp.thread, cleanUp.assistant)
                            .flatMap(createdRun ->
                                client.getRun(cleanUp.thread.getId(), createdRun.getId()).zipWith(Mono.just(cleanUp))
                                    .repeat()
                                    .delayElements(Duration.ofMillis(500))
                                    .takeWhile(tuple2 -> {
                                        ThreadRun run = tuple2.getT1();

                                        return run.getStatus() == RunStatus.IN_PROGRESS
                                            || run.getStatus() == RunStatus.QUEUED;
                                    })
                                    .last()
                            )
                    );
                }).flatMap(tuple -> {
                    // we do one last request, that gets the Run with the Status that broke the above loop
                    ThreadRun run = tuple.getT1();
                    CleanUp cleanUp = tuple.getT2();

                    return client.getRun(cleanUp.thread.getId(), run.getId()).zipWith(Mono.just(cleanUp));
                })
                .flatMap(tuple -> {
                    ThreadRun run = tuple.getT1();
                    CleanUp cleanUp = tuple.getT2();

                    assertEquals(RunStatus.COMPLETED, run.getStatus());
                    assertEquals(cleanUp.assistant.getId(), run.getAssistantId());

                    return client.listMessages(cleanUp.thread.getId()).zipWith(Mono.just(cleanUp));
                }).map(tuple -> {
                    OpenAIPageableListOfThreadMessage messageList = tuple.getT1();
                    CleanUp cleanUp = tuple.getT2();

                    assertEquals(2, messageList.getData().size());
                    ThreadMessage firstMessage = messageList.getData().getFirst();

                    assertEquals(MessageRole.ASSISTANT, firstMessage.getRole());
                    assertFalse(firstMessage.getContent().isEmpty());

                    MessageTextContent firstMessageContent = (MessageTextContent) firstMessage.getContent().getFirst();
                    assertNotNull(firstMessageContent);
                    assertTrue(firstMessageContent.getText().getValue().contains("232323"));

                    return cleanUp;
                })
                .flatMap(cleanUp -> client.deleteAssistant(cleanUp.assistant.getId())
                    .flatMap(_unused -> client.deleteFile(cleanUp.file.getId()))
                    .flatMap(_unused -> client.deleteThread(cleanUp.thread.getId()))))
                // last deletion asserted
                .assertNext(threadDeletionStatus -> assertTrue(threadDeletionStatus.isDeleted()))
                .verifyComplete();
        });
    }

    private class CleanUp {
        Assistant assistant;
        AssistantThread thread;
        OpenAIFile file;
    }
}
