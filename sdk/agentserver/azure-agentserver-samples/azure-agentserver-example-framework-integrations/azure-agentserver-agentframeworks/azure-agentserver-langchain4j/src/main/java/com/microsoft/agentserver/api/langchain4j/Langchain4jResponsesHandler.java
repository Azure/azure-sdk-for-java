package com.microsoft.agentserver.api.langchain4j;

import com.microsoft.agentserver.api.ApiError;
import com.microsoft.agentserver.api.ApiException;
import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.CreateResponse;
import com.microsoft.agentserver.api.ResponseBuilder;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.api.ResponsesProvider;
import com.microsoft.agentserver.api.langchain4j.noop.NOOPSupervisorAgent;
import com.microsoft.agentserver.api.langchain4j.noop.NOOPUntypedAgent;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputMessageItem;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Langchain4j-specific {@link ResponseHandler} implementation.
 * <p>
 * Handles converting OpenAI Responses API requests into langchain4j
 * {@link dev.langchain4j.data.message.ChatMessage} format and invoking the
 * configured agent ({@link UntypedAgent} or {@link SupervisorAgent}).
 * <p>
 * Use the {@link #builder()} to obtain a ready-to-use {@link ResponsesApi}
 * backed by a {@link ResponsesApi} that delegates to this handler.
 */
public class Langchain4jResponsesHandler implements ResponseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Langchain4jResponsesHandler.class);

    private final UntypedAgent agent;
    private final SupervisorAgent supervisorAgent;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Inject
    public Langchain4jResponsesHandler(
        UntypedAgent agent,
        SupervisorAgent supervisorAgent) {
        this.agent = agent;
        if (supervisorAgent != null && !(supervisorAgent instanceof NOOPSupervisorAgent)) {
            this.supervisorAgent = supervisorAgent;
        } else {
            this.supervisorAgent = null;
        }
    }

    // --- ResponseHandler implementation ---

    @Override
    public CreateResponse createResponse(
        ResponseContext responseContext,
        AgentServerCreateResponse request) {
        try {
            ResultWithAgenticScope<String> result = validateAndInvoke(responseContext, request);

            ResponseOutputText responseOutputText = ResponseOutputText.builder()
                .text(result.result())
                .annotations(new ArrayList<>())
                .build();

            com.openai.models.responses.Response response =
                ResponseBuilder.convertOutputToResponse(request, responseOutputText);

            return new CreateResponse(request.agent(), response);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEventStream createAsync(
        ResponseContext responseContext,
        AgentServerCreateResponse request) {

        ResponseEventStream stream = ResponseEventStream.builder()
            .context(responseContext)
            .request(request)
            .build();

        executorService.execute(() -> {
            try {
                stream.awaitSubscription();
                stream.emitCreated();
                stream.emitInProgress();

                ResultWithAgenticScope<String> result = validateAndInvoke(responseContext, request);
                String responseText = result.result();

                //String[] tokens = responseText.split("(?<=\\s)|(?=\\s)");
                String[] tokens = new String[]{responseText};

                stream.addOutputMessage(msg -> msg
                    .emitAdded()
                    .addTextPart(text -> {
                        text = text.emitAdded();
                        for (String token : tokens) {
                            if (!token.isEmpty()) {
                                text = text.emitDelta(token);
                            }
                        }
                        text.emitDone(responseText);
                    })
                    .emitDone());

                stream.emitCompleted();
            } catch (Exception e) {
                LOGGER.error("Error during streaming response", e);
                stream.emitFailed();
            }
        });

        return stream;
    }

    // --- Langchain4j-specific agent invocation ---

    /**
     * Validates the request, loads conversation history from the ResponseContext,
     * and invokes the agent with the converted input including history.
     */
    public ResultWithAgenticScope<String> validateAndInvoke(ResponseContext responseContext, AgentServerCreateResponse createResponse) throws ApiException {
        ResponseCreateParams.Body responseCreateParams = getResponseCreateParams(createResponse);

        Map<String, Object> input = convert(responseCreateParams);

        // Load conversation history from the ResponseContext and prepend to messages
        List<ChatMessage> messages = (List<ChatMessage>) input.get("messages");
        List<ChatMessage> historyMessages = loadHistoryMessages(responseContext);
        if (!historyMessages.isEmpty()) {
            LOGGER.info("Loaded {} history message(s) from conversation context", historyMessages.size());
            List<ChatMessage> combined = new ArrayList<>(historyMessages.size() + messages.size());
            // Separate system messages (instructions) from the rest
            List<ChatMessage> systemMsgs = new ArrayList<>();
            List<ChatMessage> nonSystemMsgs = new ArrayList<>();
            for (ChatMessage msg : messages) {
                if (msg instanceof SystemMessage) {
                    systemMsgs.add(msg);
                } else {
                    nonSystemMsgs.add(msg);
                }
            }
            // Order: system instructions, then history, then current turn input
            combined.addAll(systemMsgs);
            combined.addAll(historyMessages);
            combined.addAll(nonSystemMsgs);
            input.put("messages", combined);
        }

        return invoke(
            createResponse.responseCreateParams(),
            input,
            responseContext);
    }

    /**
     * Loads conversation history from the ResponseContext and converts to langchain4j messages.
     * Uses {@link ResponseItem}'s union type to naturally determine roles:
     * {@code ResponseInputMessageItem} carries explicit role (user/system/developer),
     * {@code ResponseOutputMessage} is always assistant.
     */
    private List<ChatMessage> loadHistoryMessages(ResponseContext responseContext) {
        if (responseContext == null) {
            return List.of();
        }
        try {
            List<ResponseItem> historyItems = responseContext.getHistoryAsync().join();
            if (historyItems == null || historyItems.isEmpty()) {
                return List.of();
            }
            List<ChatMessage> result = new ArrayList<>();
            for (ResponseItem item : historyItems) {
                if (item.isResponseInputMessageItem()) {
                    // Input message with explicit role
                    ResponseInputMessageItem inputMsg = item.asResponseInputMessageItem();
                    String text = extractTextFromInputContent(inputMsg.content());
                    if (text != null && !text.isEmpty()) {
                        ResponseInputMessageItem.Role role = inputMsg.role();
                        if (role.equals(ResponseInputMessageItem.Role.USER)) {
                            result.add(new UserMessage(text));
                        } else if (role.equals(ResponseInputMessageItem.Role.SYSTEM)
                            || role.equals(ResponseInputMessageItem.Role.DEVELOPER)) {
                            result.add(new SystemMessage(text));
                        } else {
                            result.add(new UserMessage(text));
                        }
                    }
                } else if (item.isResponseOutputMessage()) {
                    // Output message — always assistant
                    ResponseOutputMessage outputMsg = item.asResponseOutputMessage();
                    String text = extractTextFromOutputMessage(outputMsg);
                    if (text != null && !text.isEmpty()) {
                        result.add(new AiMessage(text));
                    }
                }
                // Skip tool calls, reasoning, etc. — not directly mappable to chat messages
            }
            return result;
        } catch (Exception e) {
            LOGGER.warn("Failed to load conversation history from context", e);
            return List.of();
        }
    }

    /**
     * Extracts concatenated text from a list of {@link ResponseInputContent}.
     */
    private String extractTextFromInputContent(List<ResponseInputContent> content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (ResponseInputContent c : content) {
            if (c.isInputText()) {
                if (!sb.isEmpty()) {
                    sb.append("\n");
                }
                sb.append(c.asInputText().text());
            }
        }
        return sb.toString();
    }

    /**
     * Extracts the concatenated text content from a ResponseOutputMessage.
     */
    private String extractTextFromOutputMessage(ResponseOutputMessage message) {
        if (message.content() == null || message.content().isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (ResponseOutputMessage.Content content : message.content()) {
            if (content.isOutputText()) {
                if (!sb.isEmpty()) {
                    sb.append("\n");
                }
                sb.append(content.asOutputText().text());
            }
        }
        return sb.toString();
    }

    private static ResponseCreateParams.Body getResponseCreateParams(AgentServerCreateResponse createResponse) throws ApiException {
        if (!createResponse.responseCreateParams().isValid()) {
            throw new ApiException(400, ApiError.invalidRequest("Invalid request parameters"));
        }

        return createResponse.responseCreateParams();
    }

    private ResultWithAgenticScope<String> invoke(ResponseCreateParams.Body createResponse, Map<String, Object> input, ResponseContext responseContext) {

        boolean store = createResponse.store().orElse(true);
        boolean useMemory = store && supervisorAgent instanceof SupervisorAgentWithMemory;

        if (supervisorAgent != null) {
            if (useMemory) {
                String id = resolveMemoryKey(createResponse, responseContext);
                return ((SupervisorAgentWithMemory) supervisorAgent).invokeWithAgenticScope(id, input.get("messages").toString());
            }
            return supervisorAgent.invokeWithAgenticScope(input.get("messages").toString());
        } else if (agent != null && !(agent instanceof NOOPUntypedAgent)) {
            return agent.invokeWithAgenticScope(input);
        } else {
            throw new IllegalStateException("No agent or supervisorAgent available");
        }
    }

    /**
     * Resolves the memory partition key for a memory-aware supervisor invocation.
     * Priority: explicit {@code previous_response_id} → {@code conversation.id} →
     * the request-scoped session id resolved by {@code SessionIdResolver}
     * (payload {@code agent_session_id} → {@code FOUNDRY_AGENT_SESSION_ID} env →
     * SHA-256 derivation). Throws when none yields a non-empty value: a random
     * fallback would silently disable memory by allocating a fresh per-request
     * {@code ChatMemory} that is never reused or evicted.
     */
    private static String resolveMemoryKey(ResponseCreateParams.Body createResponse, ResponseContext responseContext) {
        if (createResponse.previousResponseId().isPresent()) {
            String prev = createResponse.previousResponseId().get();
            if (!prev.isEmpty()) {
                return prev;
            }
        }
        if (createResponse.conversation().isPresent()) {
            var conv = createResponse.conversation().get();
            if (conv.isId() && !conv.asId().isEmpty()) {
                return conv.asId();
            }
        }
        if (responseContext != null) {
            String sid = responseContext.getSessionId();
            if (sid != null && !sid.isEmpty()) {
                return sid;
            }
        }
        throw new IllegalStateException(
            "Cannot resolve memory key for memory-aware supervisor invocation. "
                + "Expected a non-empty value from previous_response_id, conversation.id, "
                + "or ResponseContext.getSessionId(). To run without memory, set store=false "
                + "or wire a plain SupervisorAgent.");
    }

    // --- Input conversion from OpenAI format to Langchain4j messages ---

    private static Map<String, Object> convert(ResponseCreateParams.Body data) {
        Map<String, Object> agentInput = new HashMap<>();
        List<ChatMessage> messages = new ArrayList<>();

        // Add system message if instructions exist
        if (data.instructions().isPresent() && data.instructions().isPresent()) {
            messages.add(new SystemMessage(data.instructions().get()));
        }

        if (data.input().isPresent()) {
            ResponseCreateParams.Input input = data.input().get();
            if (input.isText() &&
                !input.asText().isEmpty()) {
                messages.add(new UserMessage(input.asText()));
            } else if (!input.asResponse().isEmpty()) {
                for (ResponseInputItem inner : input.asResponse()) {
                    if (inner.isMessage()) {
                        messages.add(toLangChainMessage(inner.message().get()));
                    } else if (inner.isEasyInputMessage()) {
                        messages.add(toLangChainMessage(inner.easyInputMessage().get()));
                    } else {
                        throw new IllegalArgumentException("Unsupported input type: " + inner.getClass());
                    }
                }
            }
        }

        agentInput.put("messages", messages);
        return agentInput;
    }

    private static ChatMessage toLangChainMessage(EasyInputMessage easyInputMessage) {
        List<Content> content = toLangChainContent(easyInputMessage.content());

        // Workaround for probable bug in deserialization of EasyInputMessage role
        EasyInputMessage.Role.Value role = EasyInputMessage.Role.Value.valueOf(easyInputMessage.role().asString().toUpperCase(Locale.ROOT));
        if (role == EasyInputMessage.Role.Value.USER) {
            return new UserMessage(content);
        } else if (role == EasyInputMessage.Role.Value.ASSISTANT) {
            return new AiMessage(content.getFirst().toString());
        } else if (role == EasyInputMessage.Role.Value.DEVELOPER || role == EasyInputMessage.Role.Value.SYSTEM) {
            return new SystemMessage(content.getFirst().toString()); // No developer message in langchain4j, using system message as fallback
        } else {
            throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }

    private static List<Content> toLangChainContent(EasyInputMessage.Content content) {
        List<Content> result = new ArrayList<>();
        if (content.isResponseInputMessageContentList()) {
            return toLangChainContent(content.asResponseInputMessageContentList());
        } else if (content.isTextInput()) {
            result.add(new TextContent(content.asTextInput()));
        }
        return result;
    }

    private static ChatMessage toLangChainMessage(ResponseInputItem.Message message) {
        if (message.type().get() == ResponseInputItem.Message.Type.MESSAGE) {
            List<Content> content = toLangChainContent(message.content());

            return switch (message.role().value()) {
                case USER -> new UserMessage(content);
                case SYSTEM, DEVELOPER ->
                    new SystemMessage(content.getFirst().toString()); // No developer message in langchain4j, using system message as fallback
                default -> throw new IllegalStateException("Unexpected value: " + message.type().get());
            };
        }
        throw new IllegalStateException("Unexpected value: " + message.type().get());
    }

    private static List<Content> toLangChainContent(List<ResponseInputContent> content) {
        List<Content> result = new ArrayList<>();
        if (content != null) {
            for (ResponseInputContent item : content) {
                if (item.isInputText()) {
                    result.add(new TextContent(item.asInputText().text()));
                } /*else if (item.isInputAudio()) {
                    // TODO: handle mime type
                    result.add(new AudioContent(
                        item.asInputAudio().inputAudio().data(),
                        "" // ?
                    ));
                }*/ else if (item.isInputFile()) {
                    // TODO: handle mime type
                    result.add(
                        new PdfFileContent(
                            item.asInputFile().fileData().get(),
                            "" // ?
                        )
                    );
                } else if (item.isInputImage()) {
                    result.add(
                        new ImageContent(
                            URI.create(item.asInputImage().imageUrl().get())
                        )
                    );
                } else {
                    throw new IllegalArgumentException("Unsupported content type: " + item.getClass());
                }
            }
        }
        return result;
    }

    /**
     * Returns a builder that produces a ready-to-use {@link ResponsesApi}
     * backed by a {@link AgentServerResponsesApi} wrapping this handler.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UntypedAgent agent;
        private SupervisorAgentWithMemory supervisorAgent;
        private ResponsesProvider provider;

        public Builder agent(UntypedAgent agent) {
            this.agent = agent;
            return this;
        }

        public Builder supervisorAgent(SupervisorAgentWithMemory supervisorAgent) {
            this.supervisorAgent = supervisorAgent;
            return this;
        }

        public Builder provider(ResponsesProvider provider) {
            this.provider = provider;
            return this;
        }

        public ResponsesApi build() {
            if (agent == null && supervisorAgent == null) {
                throw new IllegalStateException("Either agent or supervisorAgent must be provided");
            } else if (agent != null && supervisorAgent != null) {
                throw new IllegalStateException("Only one of agent or supervisorAgent can be provided");
            }
            Langchain4jResponsesHandler handler = new Langchain4jResponsesHandler(agent, supervisorAgent);
            ResponsesApi.Builder b = ResponsesApi.builder().responseHandler(handler);
            if (provider != null) {
                b.provider(provider);
            }
            return b.build();
        }
    }
}
