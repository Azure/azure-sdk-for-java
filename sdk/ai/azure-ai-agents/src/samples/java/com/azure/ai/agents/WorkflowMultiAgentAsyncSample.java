// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WorkflowAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.ResponseCreateParams;
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
        ResponsesAsyncClient responsesClient = builder.buildResponsesAsyncClient();
        AtomicReference<AgentVersionDetails> teacherRef = new AtomicReference<>();
        AtomicReference<AgentVersionDetails> studentRef = new AtomicReference<>();
        AtomicReference<AgentVersionDetails> workflowRef = new AtomicReference<>();

        agentsClient.createAgentVersion("teacher-agent-async",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Check the student's answer, then explain the correct answer.")))
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
            .flatMap(workflow -> responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(workflow)),
                ResponseCreateParams.builder().input("What is 12 multiplied by 8?")))
            .doOnNext(SampleUtils::printResponseText)
            .then(Mono.defer(() -> cleanup(agentsClient, workflowRef, studentRef, teacherRef)))
            .onErrorResume(error -> cleanup(agentsClient, workflowRef, studentRef, teacherRef)
                .then(Mono.error(error)))
            .block();
    }

    @SafeVarargs
    private static Mono<Void> cleanup(AgentsAsyncClient client,
        AtomicReference<AgentVersionDetails>... references) {
        Mono<Void> cleanup = Mono.empty();
        for (AtomicReference<AgentVersionDetails> reference : references) {
            AgentVersionDetails agent = reference.get();
            if (agent != null) {
                cleanup = cleanup.then(client.deleteAgentVersion(agent.getName(), agent.getVersion()));
            }
        }
        return cleanup;
    }
}
