package com.microsoft.agentserver;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.CreateResponse;
import com.microsoft.agentserver.api.ResponseBuilder;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.openai.client.OpenAIClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.openai.models.responses.ResponseOutputText;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An Agent Server handler that translates any input text to Italian by calling
 * the Azure OpenAI chat completions API directly via the stainless-generated OpenAI Java SDK.
 *
 * <p>The synchronous path ({@code createResponse}) calls the non-streaming completions endpoint
 * and returns a single response. The streaming path ({@code createAsync}) uses
 * {@code createStreaming} so each token from the LLM is forwarded to the caller as a delta
 * event in real time.
 *
 * <p>The {@link OpenAIClient} and model deployment name are supplied by the caller (see
 * {@link Main}), which builds them from the Foundry hosting environment.
 */
public class ItalianTranslatorHandler implements ResponseHandler {

    private static final String SYSTEM_PROMPT =
        "You are a translator. Translate the user's text to Italian. "
            + "Respond with only the Italian translation, nothing else.";
    // Small fixed pool so concurrent streaming requests are not serialized behind one another.
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

    private final OpenAIClient client;
    private final String deploymentName;

    /**
     * Creates a handler that translates input to Italian using the supplied Azure OpenAI client.
     *
     * @param client the configured Azure OpenAI client.
     * @param deploymentName the model deployment name to invoke.
     */
    public ItalianTranslatorHandler(OpenAIClient client, String deploymentName) {
        this.client = client;
        this.deploymentName = deploymentName;
    }

    private ChatCompletionCreateParams buildParams(String inputText) {
        return ChatCompletionCreateParams.builder()
            .model(ChatModel.of(deploymentName))
            .addMessage(ChatCompletionSystemMessageParam.builder()
                .content(SYSTEM_PROMPT)
                .build())
            .addMessage(ChatCompletionUserMessageParam.builder()
                .content(inputText)
                .build())
            .build();
    }

    /**
     * Synchronous path: call the completions endpoint and return a single response.
     */
    @Override
    public CreateResponse createResponse(ResponseContext responseContext, AgentServerCreateResponse request) {
        String inputText = request.inputText();
        if (inputText.isEmpty()) {
            throw new IllegalArgumentException("No text input provided in the request");
        }

        var choices = client.chat().completions().create(buildParams(inputText)).choices();
        if (choices.isEmpty()) {
            throw new IllegalStateException("The model returned no choices for the translation request");
        }
        String translation = choices.get(0).message().content().orElse("");

        ResponseOutputText responseOutputText = ResponseOutputText.builder()
            .text(translation)
            .annotations(new ArrayList<>())
            .build();

        return new CreateResponse(
            request.agent(),
            ResponseBuilder.convertOutputToResponse(request, responseOutputText)
        );
    }

    /**
     * Streaming path: open a streaming chat completion and forward each token delta
     * to the caller as it arrives, giving the client a real-time translation feed.
     */
    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        String inputText = request.inputText();
        if (inputText.isEmpty()) {
            throw new IllegalArgumentException("No text input provided in the request");
        }

        ResponseEventStream stream = ResponseEventStream.create(responseContext, request)
            .emitCreated()
            .emitInProgress();

        EXECUTOR_SERVICE.execute(() -> {
            try {
                StreamResponse<ChatCompletionChunk> translatedMessages = client.chat().completions().createStreaming(buildParams(inputText));
                stream.addOutputMessage(msg -> msg.streamChatCompletion(translatedMessages));
            } finally {
                stream.emitCompleted();
            }
        });

        return stream;
    }
}
