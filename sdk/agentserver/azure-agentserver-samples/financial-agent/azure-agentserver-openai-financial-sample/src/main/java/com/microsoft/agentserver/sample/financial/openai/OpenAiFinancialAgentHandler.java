// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.sample.financial.openai;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.CreateResponse;
import com.microsoft.agentserver.api.GenAiObservability;
import com.microsoft.agentserver.api.ResponseBuilder;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.microsoft.agentserver.api.implementation.IdGenerator;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.openai.models.completions.CompletionUsage;
import com.openai.models.responses.ResponseFunctionToolCallOutputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputText;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An Agent Server handler that runs a real, LLM-driven tool-calling loop against
 * Azure OpenAI using the raw OpenAI Java SDK — no agent framework.
 * <p>
 * This is the framework-free counterpart to the langchain4j financial sample. On
 * each turn the model may request one or more function calls; the handler
 * executes them via {@link FinancialToolbox}, feeds the results back as
 * {@code tool} messages, and loops until the model returns a final text answer
 * (or a safety cap is reached).
 * <p>
 * Crucially, the full tool trajectory is surfaced to the caller and to Azure AI
 * Foundry agent-run evaluators: each executed tool call is emitted as a
 * {@code function_call} output item followed by a {@code function_call_output}
 * item, ordered before the final assistant message. The sync path builds these
 * items through {@link ResponseBuilder#convertOutputToResponse}; the streaming
 * path emits them as {@code output_item} events.
 */
public class OpenAiFinancialAgentHandler implements ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiFinancialAgentHandler.class);

    private static final String SYSTEM_PROMPT =
        "You are a helpful banking assistant for a small demo bank. "
            + "Use the provided tools to look up balances, credit or withdraw funds, and convert "
            + "currencies. Always call a tool to obtain real values instead of guessing, and only "
            + "answer questions related to these banking operations. When you are done, reply with a "
            + "short natural-language summary of what you did and the resulting balances.";

    /** Safety cap so a misbehaving model cannot loop forever. */
    private static final int MAX_TURNS = 8;

    private final OpenAIClient client;
    private final String deploymentName;
    private final FinancialToolbox toolbox;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Creates the handler.
     *
     * @param client         the configured Azure OpenAI client
     * @param deploymentName the chat model deployment name to invoke
     * @param toolbox        the financial tools available to the model
     */
    public OpenAiFinancialAgentHandler(OpenAIClient client, String deploymentName, FinancialToolbox toolbox) {
        this.client = client;
        this.deploymentName = deploymentName;
        this.toolbox = toolbox;
    }

    @Override
    public CreateResponse createResponse(ResponseContext responseContext, AgentServerCreateResponse request) {
        AgentResult result = runToolLoop(request.inputText());

        ResponseOutputText outputText = ResponseOutputText.builder()
            .text(result.finalText)
            .annotations(new ArrayList<>())
            .build();

        return new CreateResponse(
            request.agent(),
            ResponseBuilder.convertOutputToResponse(request, outputText, result.toolCalls));
    }

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        ResponseEventStream stream = ResponseEventStream.create(responseContext, request);
        Context invocationContext = Context.current();

        executorService.execute(invocationContext.wrap(() -> {
            try {
                stream.awaitSubscription();
                stream.emitCreated();
                stream.emitInProgress();

                AgentResult result = runToolLoop(request.inputText());

                emitToolCalls(stream, result.toolCalls);

                String finalText = result.finalText;
                stream.addOutputMessage(msg -> msg
                    .emitAdded()
                    .addTextPart(text -> text.emitAdded().emitDelta(finalText).emitDone(finalText))
                    .emitDone());

                stream.emitCompleted();
            } catch (Exception e) {
                LOGGER.error("Error during streaming response", e);
                stream.emitFailed();
            }
        }));

        return stream;
    }

    /**
     * Drives the chat-completions tool loop and captures the tool trajectory.
     * <p>
     * Each model round-trip is wrapped in a GenAI {@code chat} span and every tool the
     * model requests is emitted as an {@code execute_tool} span, following the
     * OpenTelemetry GenAI semantic conventions (see {@link GenAiObservability}). The running
     * conversation, the assistant's tool-call decisions and the tool definitions are
     * recorded on the chat spans so Azure AI Foundry agent-run evaluators (for example
     * <em>task adherence</em>) can reconstruct the trajectory.
     */
    private AgentResult runToolLoop(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            throw new IllegalArgumentException("No text input provided in the request");
        }

        List<ChatCompletionMessageParam> messages = new ArrayList<>();
        messages.add(ChatCompletionMessageParam.ofSystem(
            ChatCompletionSystemMessageParam.builder().content(SYSTEM_PROMPT).build()));
        messages.add(ChatCompletionMessageParam.ofUser(
            ChatCompletionUserMessageParam.builder().content(userInput).build()));

        List<ResponseBuilder.ToolCallRecord> toolCalls = new ArrayList<>();

        // Sensitive message/tool content is built only when the operator explicitly opts in.
        boolean captureMessageContent = GenAiObservability.isMessageContentCaptureEnabled();
        String systemInstructionsJson = captureMessageContent
            ? GenAiMessageSerializer.systemInstructions(SYSTEM_PROMPT)
            : null;
        String toolDefinitionsJson = captureMessageContent
            ? GenAiMessageSerializer.toJson(toolbox.toolDefinitionsForTelemetry())
            : null;
        List<Map<String, Object>> genAiMessages = captureMessageContent ? new ArrayList<>() : null;
        if (genAiMessages != null) {
            genAiMessages.add(GenAiMessageSerializer.textMessage("user", userInput));
        }

        for (int turn = 0; turn < MAX_TURNS; turn++) {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(deploymentName)
                .messages(messages)
                .tools(toolbox.definitions())
                .build();

            // Snapshot the request-side conversation before the call so it is reported
            // as gen_ai.input.messages against the assistant response it produced.
            String inputMessagesJson = genAiMessages != null
                ? GenAiMessageSerializer.toJson(new ArrayList<>(genAiMessages))
                : null;

            Span chatSpan = GenAiObservability.startChatSpan(deploymentName);
            ChatCompletion completion;
            ChatCompletionMessage message;
            Optional<List<ChatCompletionMessageToolCall>> requestedCalls;
            boolean hasToolCalls;
            try {
                try (Scope ignored = chatSpan.makeCurrent()) {
                    completion = client.chat().completions().create(params);
                }
                if (completion.choices().isEmpty()) {
                    throw new IllegalStateException("The model returned no choices");
                }
                ChatCompletion.Choice choice = completion.choices().get(0);
                message = choice.message();
                requestedCalls = message.toolCalls();
                hasToolCalls = requestedCalls.isPresent() && !requestedCalls.get().isEmpty();

                String finishReason = choice.finishReason() != null ? choice.finishReason().toString() : null;
                recordChatTurn(chatSpan, completion, message, requestedCalls.orElseGet(List::of),
                    inputMessagesJson, systemInstructionsJson, toolDefinitionsJson, finishReason, genAiMessages);
            } catch (RuntimeException | Error error) {
                GenAiObservability.recordChatError(chatSpan, error);
                throw error;
            }

            if (!hasToolCalls) {
                // No more tools requested — this turn holds the final answer.
                return new AgentResult(message.content().orElse(""), toolCalls);
            }

            // Record the assistant turn (with its tool_calls) so the follow-up
            // tool results are correlated correctly by the model.
            messages.add(ChatCompletionMessageParam.ofAssistant(message.toParam()));

            for (ChatCompletionMessageToolCall toolCall : requestedCalls.get()) {
                if (!toolCall.isFunction()) {
                    continue;
                }
                var function = toolCall.asFunction();
                String callId = function.id();
                String name = function.function().name();
                String arguments = function.function().arguments();

                Span toolSpan = GenAiObservability.startExecuteToolSpan(name, callId, arguments);
                String output;
                try (Scope ignored = toolSpan.makeCurrent()) {
                    output = toolbox.invoke(function);
                    GenAiObservability.recordToolResult(toolSpan, output, null);
                } catch (RuntimeException toolError) {
                    GenAiObservability.recordToolResult(toolSpan, null, toolError);
                    LOGGER.warn("Tool '{}' failed", name, toolError);
                    output = "Error: " + toolError.getMessage();
                } catch (Error fatalError) {
                    GenAiObservability.recordToolResult(toolSpan, null, fatalError);
                    throw fatalError;
                }

                toolCalls.add(new ResponseBuilder.ToolCallRecord(callId, name, arguments, output));
                if (genAiMessages != null) {
                    genAiMessages.add(GenAiMessageSerializer.toolResultMessage(callId, output));
                }
                messages.add(ChatCompletionMessageParam.ofTool(ChatCompletionToolMessageParam.builder()
                    .toolCallId(callId)
                    .content(output)
                    .build()));
            }
        }

        LOGGER.warn("Tool loop hit the {}-turn cap without a final answer", MAX_TURNS);
        return new AgentResult(
            "I could not complete the request within the allotted number of steps.", toolCalls);
    }

    /**
     * Records the response content on the chat span, appends the assistant turn to the
     * accumulated GenAI conversation, and ends the span via
     * {@link GenAiObservability#recordChatResponse}.
     *
     */
    private static void recordChatTurn(Span chatSpan, ChatCompletion completion, ChatCompletionMessage message,
        List<ChatCompletionMessageToolCall> requestedCalls, String inputMessagesJson,
        String systemInstructionsJson, String toolDefinitionsJson, String finishReason,
        List<Map<String, Object>> genAiMessages) {

        if (genAiMessages != null) {
            Map<String, Object> assistantMessage;
            if (!requestedCalls.isEmpty()) {
                List<GenAiMessageSerializer.ToolCall> calls = new ArrayList<>();
                for (ChatCompletionMessageToolCall toolCall : requestedCalls) {
                    if (!toolCall.isFunction()) {
                        continue;
                    }
                    var function = toolCall.asFunction();
                    calls.add(new GenAiMessageSerializer.ToolCall(
                        function.id(), function.function().name(), function.function().arguments()));
                }
                assistantMessage = GenAiMessageSerializer.assistantToolCallsMessage(calls, finishReason);
            } else {
                assistantMessage = GenAiMessageSerializer.textMessage("assistant", message.content().orElse(""));
                if (finishReason != null && !finishReason.isEmpty()) {
                    assistantMessage.put("finish_reason", finishReason);
                }
            }

            String outputMessagesJson = GenAiMessageSerializer.toJson(List.of(assistantMessage));
            GenAiObservability.setChatMessages(chatSpan, inputMessagesJson, outputMessagesJson,
                systemInstructionsJson, toolDefinitionsJson);
            genAiMessages.add(assistantMessage);
        }

        Long inputTokens = null;
        Long outputTokens = null;
        Optional<CompletionUsage> usage = completion.usage();
        if (usage.isPresent()) {
            inputTokens = usage.get().promptTokens();
            outputTokens = usage.get().completionTokens();
        }
        List<String> finishReasons = finishReason == null ? List.of() : List.of(finishReason);
        GenAiObservability.recordChatResponse(chatSpan, completion.id(), completion.model(),
            inputTokens, outputTokens, finishReasons);
    }

    /**
     * Emits the collected tool calls onto the streaming response as
     * {@code function_call} (and, when present, {@code function_call_output})
     * output items, ordered before the final assistant text message.
     */
    private static void emitToolCalls(ResponseEventStream stream, List<ResponseBuilder.ToolCallRecord> toolCalls) {
        if (toolCalls.isEmpty()) {
            return;
        }
        IdGenerator idGen = new IdGenerator(null);
        for (ResponseBuilder.ToolCallRecord call : toolCalls) {
            String name = call.name() == null ? "" : call.name();
            String arguments = call.arguments() == null ? "" : call.arguments();
            stream.addOutputFunctionCall(fc -> fc
                .emitAdded(name, call.id())
                .emitArgumentsDone(name, arguments)
                .emitDone());

            if (call.output() != null) {
                ResponseFunctionToolCallOutputItem outputItem = ResponseFunctionToolCallOutputItem.builder()
                    .id(idGen.generateFunctionCallItemId())
                    .callId(call.id())
                    .output(call.output())
                    .status(ResponseFunctionToolCallOutputItem.Status.COMPLETED)
                    .build();
                ResponseOutputItem wrapped = ResponseOutputItem.ofFunctionCallOutput(outputItem);
                stream.addOutputItem(outputItem.id(), b -> b.emitAdded(wrapped).emitDone(wrapped));
            }
        }
    }

    /**
     * Result of a completed tool loop: the final assistant text and the ordered
     * tool calls that produced it.
     */
    private static final class AgentResult {
        private final String finalText;
        private final List<ResponseBuilder.ToolCallRecord> toolCalls;

        AgentResult(String finalText, List<ResponseBuilder.ToolCallRecord> toolCalls) {
            this.finalText = finalText;
            this.toolCalls = toolCalls;
        }
    }
}
