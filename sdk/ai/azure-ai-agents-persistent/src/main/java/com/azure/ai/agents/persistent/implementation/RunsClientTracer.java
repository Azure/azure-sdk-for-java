// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.persistent.implementation;

import com.azure.ai.agents.persistent.RunsAsyncClient;
import com.azure.ai.agents.persistent.RunsClient;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageStatus;
import com.azure.ai.agents.persistent.models.RunStep;
import com.azure.ai.agents.persistent.models.RunStepBingGroundingToolCall;
import com.azure.ai.agents.persistent.models.RunStepCodeInterpreterToolCall;
import com.azure.ai.agents.persistent.models.RunStepFunctionToolCall;
import com.azure.ai.agents.persistent.models.RunStepMessageCreationDetails;
import com.azure.ai.agents.persistent.models.RunStepStatus;
import com.azure.ai.agents.persistent.models.RunStepToolCall;
import com.azure.ai.agents.persistent.models.RunStepToolCallDetails;
import com.azure.ai.agents.persistent.models.RunStepType;
import com.azure.ai.agents.persistent.models.StreamMessageCreation;
import com.azure.ai.agents.persistent.models.StreamRunCreation;
import com.azure.ai.agents.persistent.models.StreamThreadRunCreation;
import com.azure.ai.agents.persistent.models.StreamUpdate;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.ai.agents.persistent.models.ToolOutput;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracer for the convenience methods in {@link RunsClient} and
 * {@link RunsAsyncClient}.
 * <p>
 * For more about the OTel semantic conventions this type enables, see
 * <a href="https://github.com/open-telemetry/semantic-conventions/blob/v1.27.0/docs/gen-ai">Gen AI semantic conventions</a>.
 * </p>
 */
public class RunsClientTracer extends MessagesClientTracer {

    private static final ClientLogger LOGGER = new ClientLogger(RunsClientTracer.class);

    // Run-specific constants
    static final String GEN_AI_THREAD_ID_KEY = "gen_ai.thread.id";
    static final String GEN_AI_AGENT_ID_KEY = "gen_ai.agent.id";
    static final String GEN_AI_RUN_ID_KEY = "gen_ai.thread.run.id";
    static final String GEN_AI_RUN_STATUS_KEY = "gen_ai.thread.run.status";
    static final String GEN_AI_USAGE_INPUT_TOKENS_KEY = "gen_ai.usage.input_tokens";
    static final String GEN_AI_USAGE_OUTPUT_TOKENS_KEY = "gen_ai.usage.output_tokens";
    static final String GEN_AI_RESPONSE_MODEL_KEY = "gen_ai.response.model";

    static final String GEN_AI_RUN_STEP_START_TIMESTAMP_KEY = "gen_ai.run_step.start.timestamp";

    static final String OPERATION_CREATE_THREAD_RUN = "create_thread_run";
    static final String OPERATION_CREATE_THREAD_RUN_STREAMING = "create_thread_run_streaming";
    static final String OPERATION_SUBMIT_TOOL_OUTPUTS = "submit_tool_outputs";
    static final String OPERATION_LIST_RUN_STEPS = "list_run_steps";
    static final String EVENT_GEN_AI_TOOL_MESSAGE = "gen_ai.tool.message";
    static final String EVENT_GEN_AI_SYSTEM_MESSAGE = "gen_ai.system.message";
    static final String EVENT_GEN_AI_RUN_STEP_MESSAGE_CREATION = "gen_ai.run_step.message_creation";
    static final String EVENT_GEN_AI_RUN_STEP_TOOL_CALLS = "gen_ai.run_step.tool_calls";

    /**
     * Creates RunsClientTracer.
     *
     * @param endpoint the service endpoint.
     * @param configuration the {@link Configuration} instance to check if message content needs to be captured,
     *     if {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @param tracer the Tracer instance.
     */
    public RunsClientTracer(String endpoint, Configuration configuration, Tracer tracer, Meter meter) {
        super(endpoint, configuration, tracer, meter);
    }

    //<editor-fold desc="Tracing CreateRun">

    /**
     * Traces the synchronous convenience API - create run operation.
     *
     * @param options input options containing run creation parameters.
     * @param operation the operation performing the actual create run call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return thread run created from the request.
     */
    public ThreadRun traceCreateRunSync(CreateRunOptions options, Operation<ThreadRun> operation,
        RequestOptions requestOptions) {

        return this.traceSyncOperation(OPERATION_CREATE_THREAD_RUN, operation, requestOptions, (span) -> {
            traceCreateRunInvocationAttributes(options, span);
        }, this::traceCreateRunResponseAttributes);
    }

    /**
     * Traces the asynchronous convenience API - create run operation.
     *
     * @param options input options containing run creation parameters.
     * @param operation the operation performing the actual create run call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return thread run created from the request.
     */
    public Mono<ThreadRun> traceCreateRunAsync(CreateRunOptions options, Operation<Mono<ThreadRun>> operation,
        RequestOptions requestOptions) {

        return this.traceAsyncMonoOperation(OPERATION_CREATE_THREAD_RUN, operation, requestOptions,
            (span) -> traceCreateRunInvocationAttributes(options, span),
            (span, traceAttributes, result) -> result.flatMap(run -> {
                traceCreateRunResponseAttributes(span, traceAttributes, run);
                return Mono.empty();
            }));
    }

    /**
     * Trace the attributes for a create run request.
     *
     * @param options The run creation options.
     * @param span The current span context.
     */
    void traceCreateRunInvocationAttributes(CreateRunOptions options, Context span) {
        // Set request attributes
        if (options != null) {
            this.setAttributeIfNotNull(GEN_AI_THREAD_ID_KEY, options.getThreadId(), span);
            this.setAttributeIfNotNull(GEN_AI_AGENT_ID_KEY, options.getAssistantId(), span);

            // Record system instructions as an event if content capture is enabled (same as non-streaming)
            if (traceContent && !CoreUtils.isNullOrEmpty(options.getInstructions())) {
                Map<String, Object> eventAttributes = new HashMap<>();
                eventAttributes.put(GEN_AI_THREAD_ID_KEY, options.getThreadId());
                eventAttributes.put(GEN_AI_AGENT_ID_KEY, options.getAssistantId());

                Map<String, Object> contentMap = new HashMap<>();
                putIfNotNullOrEmpty(contentMap, "instructions", options.getInstructions());
                putIfNotNullOrEmpty(contentMap, "additional_instructions", options.getAdditionalInstructions());

                String eventContent = toJsonString(contentMap);
                if (eventContent != null) {
                    eventAttributes.put(GEN_AI_EVENT_CONTENT, eventContent);
                    tracer.addEvent(EVENT_GEN_AI_SYSTEM_MESSAGE, eventAttributes, null, span);
                }
            }
        }
    }

    /**
     * Record the response attributes from a create run operation.
     *
     * @param span The current span context.
     * @param run The thread run created.
     */
    void traceCreateRunResponseAttributes(Context span, Map<String, Object> traceAttributes, ThreadRun run) {
        traceThreadRun(span, traceAttributes, run);
    }

    //</editor-fold>

    //<editor-fold desc="Tracing CreateRunStreaming">

    /**
     * Traces the asynchronous convenience API - create run operation.
     *
     * @param options input options containing run creation parameters.
     * @param operation the operation performing the actual create run call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return thread run created from the request.
     */
    public Flux<StreamUpdate> traceCreateRunStreaming(CreateRunOptions options, Operation<Flux<StreamUpdate>> operation,
        RequestOptions requestOptions) {

        return this.traceAsyncFluxOperation(OPERATION_CREATE_THREAD_RUN_STREAMING, operation, requestOptions,
            (span) -> {
                traceCreateRunInvocationAttributes(options, span);
            }, (span, traceAttributes, result) -> result.flatMap(streamUpdate -> {
                traceStreamUpdate(span, traceAttributes, streamUpdate, true);
                return Flux.just(streamUpdate);
            }).then(Mono.empty()));
    }

    //</editor-fold>

    //<editor-fold desc="Tracing SubmitToolOutputs">

    /**
     * Traces the synchronous convenience API - submit tool outputs operation.
     *
     * @param threadId The thread ID.
     * @param runId The run ID.
     * @param toolOutputs The tool outputs being submitted.
     * @param operation the operation performing the actual submit tool outputs call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return thread run object after submitting tool outputs.
     */
    public ThreadRun traceSubmitToolOutputsSync(String threadId, String runId, List<ToolOutput> toolOutputs,
        Operation<ThreadRun> operation, RequestOptions requestOptions) {

        return this.traceSyncOperation(OPERATION_SUBMIT_TOOL_OUTPUTS, operation, requestOptions, (span) -> {
            traceSubmitToolOutputsInvocationAttributes(threadId, runId, toolOutputs, span);
        }, this::traceSubmitToolOutputsResponseAttributes);
    }

    /**
     * Traces the asynchronous convenience API - submit tool outputs operation.
     *
     * @param threadId The thread ID.
     * @param runId The run ID.
     * @param toolOutputs The tool outputs being submitted.
     * @param operation the operation performing the actual submit tool outputs call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return thread run object after submitting tool outputs.
     */
    public Mono<ThreadRun> traceSubmitToolOutputsAsync(String threadId, String runId, List<ToolOutput> toolOutputs,
        Operation<Mono<ThreadRun>> operation, RequestOptions requestOptions) {

        return this.traceAsyncMonoOperation(OPERATION_SUBMIT_TOOL_OUTPUTS, operation, requestOptions, (span) -> {
            traceSubmitToolOutputsInvocationAttributes(threadId, runId, toolOutputs, span);
        }, (span, traceAttributes, result) -> result.flatMap(run -> {
            traceSubmitToolOutputsResponseAttributes(span, traceAttributes, run);
            return Mono.empty();
        }));
    }

    /**
     * Trace the attributes for a submit tool outputs request.
     *
     * @param threadId The thread ID.
     * @param runId The run ID.
     * @param toolOutputs The tool outputs to trace.
     * @param span The current span context.
     */
    void traceSubmitToolOutputsInvocationAttributes(String threadId, String runId, List<ToolOutput> toolOutputs,
        Context span) {
        // Set request attributes
        this.setAttributeIfNotNull(GEN_AI_THREAD_ID_KEY, threadId, span);
        this.setAttributeIfNotNull(GEN_AI_RUN_ID_KEY, runId, span);

        // Record tool outputs as events if content capture is enabled
        if (toolOutputs != null && !toolOutputs.isEmpty()) {
            for (ToolOutput toolOutput : toolOutputs) {
                if (toolOutput == null) {
                    continue;
                }

                Map<String, Object> eventAttributes = new HashMap<>();
                eventAttributes.put(GEN_AI_THREAD_ID_KEY, threadId);
                eventAttributes.put(GEN_AI_RUN_ID_KEY, runId);

                Map<String, Object> contentMap = new HashMap<>();
                putIfNotNullOrEmpty(contentMap, "id", traceContent ? toolOutput.getToolCallId() : "");
                putIfNotNullOrEmpty(contentMap, "content", toolOutput.getOutput());

                String eventContent = toJsonString(contentMap);
                if (eventContent != null) {
                    eventAttributes.put(GEN_AI_EVENT_CONTENT, eventContent);
                    tracer.addEvent(EVENT_GEN_AI_TOOL_MESSAGE, eventAttributes, null, span);
                }
            }
        }
    }

    /**
     * Record the response attributes from a submit tool outputs operation.
     *
     * @param span The current span context.
     * @param run The thread run after submitting tool outputs.
     */
    void traceSubmitToolOutputsResponseAttributes(Context span, Map<String, Object> traceAttributes, ThreadRun run) {
        traceCreateRunResponseAttributes(span, traceAttributes, run);
    }
    //</editor-fold>

    //<editor-fold desc="Tracing SubmitToolOutputsStreaming">

    /**
     * Traces the asynchronous convenience API - create run operation.
     *
     * @param operation the operation performing the actual create run call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return thread run created from the request.
     */
    public Flux<StreamUpdate> traceSubmitToolOutputsStreaming(String threadId, String runId,
        List<ToolOutput> toolOutputs, Operation<Flux<StreamUpdate>> operation, RequestOptions requestOptions) {

        return this.traceAsyncFluxOperation(OPERATION_CREATE_THREAD_RUN_STREAMING, operation, requestOptions,
            (span) -> {
                traceSubmitToolOutputsInvocationAttributes(threadId, runId, toolOutputs, span);
            }, (span, traceAttributes, result) -> result.flatMap(streamUpdate -> {
                traceStreamUpdate(span, traceAttributes, streamUpdate, true);
                return Flux.just(streamUpdate);
            }).then(Mono.empty()));
    }

    //<editor-fold desc="Tracing ListRunSteps">

    /**
     * Traces the synchronous list run steps operation.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run.
     * @param operation the operation performing the actual list run steps call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return paged list of run steps.
     */
    public PagedIterable<RunStep> traceListRunStepsSync(String threadId, String runId,
        Operation<PagedIterable<RunStep>> operation, RequestOptions requestOptions) {

        return this.traceSyncOperation(OPERATION_LIST_RUN_STEPS, operation, requestOptions, (span) -> {
            traceListRunStepsInvocationAttributes(threadId, runId, span);
        }, (span, attributes, result) -> {
            // For paged collections, we trace thread and run IDs since we can't access actual items yet
            traceListRunStepsResponseAttributes(span, threadId, runId);
        });
    }

    /**
     * Traces the synchronous list run steps operation.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run.
     * @param operation the operation performing the actual list run steps call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return paged list of run steps.
     */
    public PagedFlux<RunStep> traceListRunStepsAsync(String threadId, String runId,
        Operation<PagedFlux<RunStep>> operation, RequestOptions requestOptions) {

        return this.traceAsyncFluxOperation(OPERATION_LIST_RUN_STEPS, operation, requestOptions, (span) -> {
            traceListRunStepsInvocationAttributes(threadId, runId, span);
        }, (span, traceAttributes, result) -> result.flatMap(runStep -> {
            traceListRunStepsResponseAttributes(span, threadId, runId);
            return Flux.just(runStep);
        }).then(Mono.empty()));
    }

    /**
     * Trace the attributes for a list run steps request.
     *
     * @param threadId The ID of the thread.
     * @param runId The ID of the run.
     * @param span The current span context.
     */
    void traceListRunStepsInvocationAttributes(String threadId, String runId, Context span) {
        // Set request attributes
        this.setAttributeIfNotNull(GEN_AI_THREAD_ID_KEY, threadId, span);
        this.setAttributeIfNotNull(GEN_AI_RUN_ID_KEY, runId, span);
    }

    /**
     * Record the response attributes from a list run steps operation.
     *
     * @param span The current span context.
     * @param threadId The ID of the thread.
     * @param runId The ID of the run.
     */
    void traceListRunStepsResponseAttributes(Context span, String threadId, String runId) {
        // For paged collections, we mainly just include the thread and run IDs
        // The actual run step details would be analyzed after pagination
        this.setAttributeIfNotNull(GEN_AI_THREAD_ID_KEY, threadId, span);
        this.setAttributeIfNotNull(GEN_AI_RUN_ID_KEY, runId, span);
    }
    //</editor-fold>

    void traceThreadRun(Context span, Map<String, Object> traceAttributes, ThreadRun run) {
        if (run != null) {
            this.setAttributeIfNotNullOrEmpty(GEN_AI_RUN_ID_KEY, run.getId(), span);
            this.setAttributeIfNotNullOrEmpty(GEN_AI_THREAD_ID_KEY, run.getThreadId(), span);
            this.setAttributeIfNotNullOrEmpty(GEN_AI_AGENT_ID_KEY, run.getAssistantId(), span);
            this.setAttributeIfNotNull(GEN_AI_RUN_STATUS_KEY, run.getStatus(), span);
            if (run.getUsage() != null) {
                traceAttributes.put(GEN_AI_USAGE_INPUT_TOKENS_KEY, run.getUsage().getPromptTokens());
                this.setAttributeIfNotNull(GEN_AI_USAGE_INPUT_TOKENS_KEY, run.getUsage().getPromptTokens(), span);
                tokensCounter.add(run.getUsage().getPromptTokens(), meter.createAttributes(traceAttributes), span);

                traceAttributes.put(GEN_AI_USAGE_OUTPUT_TOKENS_KEY, run.getUsage().getCompletionTokens());
                this.setAttributeIfNotNull(GEN_AI_USAGE_OUTPUT_TOKENS_KEY, run.getUsage().getCompletionTokens(), span);
                tokensCounter.add(run.getUsage().getCompletionTokens(), meter.createAttributes(traceAttributes), span);
            }
            this.setAttributeIfNotNullOrEmpty(GEN_AI_RESPONSE_MODEL_KEY, run.getModel(), span);
        }
    }

    void traceRunStep(Context span, Map<String, Object> traceAttributes, RunStep runStep, boolean stream) {
        if (runStep == null) {
            return;
        }

        String runStepType = runStep.getType().toString().toLowerCase();
        String eventName;

        if (stream && RunStepType.TOOL_CALLS == runStep.getType()) {
            eventName = EVENT_GEN_AI_TOOL_MESSAGE;
        } else {
            eventName = runStepType == "message_creation"
                ? EVENT_GEN_AI_RUN_STEP_MESSAGE_CREATION
                : runStepType == "tool_calls" ? EVENT_GEN_AI_RUN_STEP_TOOL_CALLS : "gen_ai.run_step." + runStepType;
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(GEN_AI_SYSTEM_KEY, GEN_AI_SYSTEM_VALUE);
        attributes.put(GEN_AI_THREAD_ID_KEY, runStep.getThreadId());
        attributes.put(GEN_AI_AGENT_ID_KEY, runStep.getAssistantId());
        attributes.put(GEN_AI_RUN_ID_KEY, runStep.getRunId());
        attributes.put(GEN_AI_RUN_STATUS_KEY, runStep.getStatus().toString());
        attributes.put(GEN_AI_RUN_STEP_START_TIMESTAMP_KEY, runStep.getCreatedAt().toEpochSecond());

        if (runStep.getCompletedAt() != null) {
            attributes.put(GEN_AI_RUN_STEP_START_TIMESTAMP_KEY, runStep.getCompletedAt().toEpochSecond());
        }

        if (runStep.getCancelledAt() != null) {
            attributes.put(GEN_AI_RUN_STEP_START_TIMESTAMP_KEY, runStep.getCancelledAt().toEpochSecond());
        }

        if (runStep.getFailedAt() != null) {
            attributes.put(GEN_AI_RUN_STEP_START_TIMESTAMP_KEY, runStep.getFailedAt().toEpochSecond());
        }

        if (runStep.getLastError() != null) {
            attributes.put(ERROR_TYPE_KEY, runStep.getLastError().getCode());
            attributes.put(ERROR_MESSAGE_KEY, runStep.getLastError().getMessage());
        }

        if (runStep.getUsage() != null) {
            attributes.put(GEN_AI_USAGE_INPUT_TOKENS_KEY, runStep.getUsage().getPromptTokens());
            attributes.put(GEN_AI_USAGE_OUTPUT_TOKENS_KEY, runStep.getUsage().getCompletionTokens());
        }

        if ("message_creation".equals(runStepType)
            && runStep.getStepDetails() instanceof RunStepMessageCreationDetails) {
            RunStepMessageCreationDetails messageDetails = (RunStepMessageCreationDetails) runStep.getStepDetails();
            attributes.put(GEN_AI_MESSAGE_ID_KEY, messageDetails.getMessageCreation().getMessageId());
        } else if ("tool_calls".equals(runStepType) && runStep.getStepDetails() instanceof RunStepToolCallDetails) {
            RunStepToolCallDetails toolCallDetails = (RunStepToolCallDetails) runStep.getStepDetails();
            List<Map<String, Object>> toolCalls = processToolCalls(toolCallDetails);
            if (toolCalls != null) {
                attributes.put(GEN_AI_EVENT_CONTENT, toJsonString(Collections.singletonMap("tool_calls", toolCalls)));
            }
        }

        tracer.addEvent(eventName, attributes, OffsetDateTime.now(), span);
    }

    private List<Map<String, Object>> processToolCalls(RunStepToolCallDetails toolCallDetails) {
        if (toolCallDetails == null || toolCallDetails.getToolCalls() == null) {
            return null;
        }

        List<Map<String, Object>> toolCalls = new ArrayList<>();
        for (RunStepToolCall toolCall : toolCallDetails.getToolCalls()) {
            Map<String, Object> toolCallAttributes = new HashMap<>();
            toolCallAttributes.put("id", toolCall.getId());
            toolCallAttributes.put("type", toolCall.getType());

            if (traceContent) {
                if (toolCall instanceof RunStepFunctionToolCall) {
                    RunStepFunctionToolCall functionToolCall = (RunStepFunctionToolCall) toolCall;
                    Map<String, Object> functionDetails = new HashMap<>();
                    functionDetails.put("name", functionToolCall.getFunction().getName());
                    functionDetails.put("arguments", parseJsonString(functionToolCall.getFunction().getArguments()));
                    toolCallAttributes.put("function", functionDetails);
                } else if (toolCall instanceof RunStepCodeInterpreterToolCall) {
                    RunStepCodeInterpreterToolCall codeInterpreterToolCall = (RunStepCodeInterpreterToolCall) toolCall;
                    Map<String, Object> interpreterDetails = new HashMap<>();
                    interpreterDetails.put("input", codeInterpreterToolCall.getCodeInterpreter().getInput());
                    interpreterDetails.put("outputs", codeInterpreterToolCall.getCodeInterpreter().getOutputs());
                    toolCallAttributes.put("code_interpreter", interpreterDetails);
                } else if (toolCall instanceof RunStepBingGroundingToolCall) {
                    RunStepBingGroundingToolCall bingGroundingToolCall = (RunStepBingGroundingToolCall) toolCall;
                    toolCallAttributes.put(toolCall.getType(), bingGroundingToolCall.getBingGrounding());
                } else {
                    Map<String, Object> otherDetails = convertObjectToMap(toolCall);
                    if (otherDetails != null) {
                        toolCallAttributes.put(toolCall.getType(), otherDetails);
                    }
                }
            }

            toolCalls.add(toolCallAttributes);
        }

        return toolCalls;
    }

    void traceStreamUpdate(Context span, Map<String, Object> traceAttributes, StreamUpdate streamUpdate,
        boolean stream) {
        if (streamUpdate == null) {
            return;
        }
        if (streamUpdate instanceof StreamThreadRunCreation) {
            StreamThreadRunCreation threadRunUpdate = (StreamThreadRunCreation) streamUpdate;
            ThreadRun run = threadRunUpdate.getMessage();
            traceThreadRun(span, traceAttributes, run);
        } else if (streamUpdate instanceof StreamMessageCreation) {
            StreamMessageCreation messageUpdate = (StreamMessageCreation) streamUpdate;
            if (messageUpdate.getMessage() != null
                && (messageUpdate.getMessage().getStatus() == MessageStatus.COMPLETED
                    || messageUpdate.getMessage().getStatus() == MessageStatus.fromString("failed"))) {
                this.setAttributeIfNotNullOrEmpty(GEN_AI_MESSAGE_ID_KEY, messageUpdate.getMessage().getId(), span);
            }
            traceThreadMessage(span, traceAttributes, messageUpdate.getMessage());
        } else if (streamUpdate instanceof StreamRunCreation) {
            StreamRunCreation runStepUpdate = (StreamRunCreation) streamUpdate;
            RunStep runStep = runStepUpdate.getMessage();
            if (runStep != null) {
                if (runStep.getStatus() == RunStepStatus.COMPLETED
                    && runStep.getType() == RunStepType.TOOL_CALLS
                    && runStep.getStepDetails() instanceof RunStepToolCallDetails) {
                    traceRunStep(span, traceAttributes, runStep, stream);
                } else if (runStep.getStatus() == RunStepStatus.COMPLETED
                    && runStep.getType() == RunStepType.MESSAGE_CREATION) {
                    traceRunStep(span, traceAttributes, runStep, stream);
                }
            }
        }
    }
}
