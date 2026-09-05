// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WorkflowAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.async.ConversationServiceAsync;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates asynchronously running a workflow that invokes student and teacher prompt agents.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class WorkflowMultiAgentAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsAsyncClient agentsClient = builder.buildAgentsAsyncClient();
        OpenAIClientAsync openAIClient = builder.buildOpenAIAsyncClient();
        ResponsesAsyncClient responsesClient = new ResponsesAsyncClient(openAIClient);
        ConversationServiceAsync conversations = openAIClient.conversations();
        AtomicReference<AgentVersionDetails> teacherRef = new AtomicReference<>();
        AtomicReference<AgentVersionDetails> studentRef = new AtomicReference<>();
        AtomicReference<AgentVersionDetails> workflowRef = new AtomicReference<>();
        AtomicReference<String> conversationIdRef = new AtomicReference<>();

        try {
            agentsClient.createAgentVersion("teacher-agent-async",
                    new CreateAgentVersionInput(new PromptAgentDefinition(model)
                        .setInstructions("Create a preschool math question and check the student's answer. "
                            + "Say [COMPLETE] when the answer is correct.")))
                .doOnNext(teacherRef::set)
                .then(agentsClient.createAgentVersion("student-agent-async",
                    new CreateAgentVersionInput(new PromptAgentDefinition(model)
                        .setInstructions("Answer the teacher's math question."))))
                .doOnNext(studentRef::set)
                .flatMap(student -> agentsClient.createAgentVersion("student-teacher-workflow-async",
                    new CreateAgentVersionInput(new WorkflowAgentDefinition().setWorkflow(
                        WorkflowSampleUtils.createStudentTeacherWorkflow(student.getName(),
                            teacherRef.get().getName())))))
                .doOnNext(workflowRef::set)
                .flatMap(workflow -> Mono.fromFuture(conversations.create())
                    .doOnNext(conversation -> conversationIdRef.set(conversation.id()))
                    .flatMapMany(conversation -> {
                        ResponseAccumulator accumulator = ResponseAccumulator.create();
                        return responsesClient.createStreamingAzureResponse(
                                new AzureCreateResponseOptions()
                                    .setAgentReference(new AgentReference(workflow.getName())),
                                ResponseCreateParams.builder()
                                    .conversation(conversation.id())
                                    .input("1 + 1 = ?"))
                            .doOnNext(event -> {
                                accumulator.accumulate(event);
                                event.outputTextDelta()
                                    .ifPresent(textEvent -> System.out.print(textEvent.delta()));
                            })
                            .then(Mono.fromRunnable(() -> {
                                System.out.println();
                                SampleUtils.printResponseText(accumulator.response());
                            }));
                    })
                    .then())
                .then(Mono.defer(() -> cleanup(agentsClient, conversations, conversationIdRef,
                    workflowRef, studentRef, teacherRef)))
                .onErrorResume(error -> cleanup(agentsClient, conversations, conversationIdRef,
                        workflowRef, studentRef, teacherRef)
                    .then(Mono.error(error)))
                .block();
        } finally {
            openAIClient.close();
        }
    }

    @SafeVarargs
    private static Mono<Void> cleanup(AgentsAsyncClient client, ConversationServiceAsync conversations,
        AtomicReference<String> conversationIdRef, AtomicReference<AgentVersionDetails>... references) {
        Mono<Void> cleanup = conversationIdRef.get() == null
            ? Mono.empty()
            : Mono.fromFuture(conversations.delete(conversationIdRef.get())).then();
        for (AtomicReference<AgentVersionDetails> reference : references) {
            AgentVersionDetails agent = reference.get();
            if (agent != null) {
                cleanup = cleanup.then(client.deleteAgentVersion(agent.getName(), agent.getVersion()));
            }
        }
        return cleanup;
    }
}
