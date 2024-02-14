package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatCompletion;
import com.azure.ai.openai.models.ChatCompletionCollection;
import com.azure.ai.openai.models.ChatCompletionOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatUpdate;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The ChatClient class provides a client for interacting with the Azure OpenAI service.
 */
public final class ChatClient {
    private final OpenAIClient openAIClient;

    ChatClient(OpenAIClient openAIClient, String model) {
        this.openAIClient = openAIClient;
    }

    /**
     * Gets completions for the provided input prompts. Completions support a wide variety of tasks and generate text
     * that continues from or "completes" provided prompt data.
     *
     * <p>
     * <strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     prompt (Required): [
     *         String (Required)
     *     ]
     *     max_tokens: Integer (Optional)
     *     temperature: Double (Optional)
     *     top_p: Double (Optional)
     *     logit_bias (Optional): {
     *         String: int (Optional)
     *     }
     *     user: String (Optional)
     *     n: Integer (Optional)
     *     logprobs: Integer (Optional)
     *     echo: Boolean (Optional)
     *     stop (Optional): [
     *         String (Optional)
     *     ]
     *     presence_penalty: Double (Optional)
     *     frequency_penalty: Double (Optional)
     *     best_of: Integer (Optional)
     *     stream: Boolean (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p>
     * <strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     created: int (Required)
     *     choices (Required): [
     *          (Required){
     *             text: String (Required)
     *             index: int (Required)
     *             logprobs (Required): {
     *                 tokens (Required): [
     *                     String (Required)
     *                 ]
     *                 token_logprobs (Required): [
     *                     double (Required)
     *                 ]
     *                 top_logprobs (Required): [
     *                      (Required){
     *                         String: double (Required)
     *                     }
     *                 ]
     *                 text_offset (Required): [
     *                     int (Required)
     *                 ]
     *             }
     *             finish_reason: String(stopped/tokenLimitReached/contentFiltered) (Required)
     *         }
     *     ]
     *     usage (Required): {
     *         completion_tokens: int (Required)
     *         prompt_tokens: int (Required)
     *         total_tokens: int (Required)
     *     }
     * }
     * }</pre>
     *
     * @param requestBody The input prompts to generate chat completions from.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return completions for the provided input prompts. Completions support a wide variety of tasks and generate text
     * that continues from or "completes" provided prompt data along with {@link Response}.
     * @throws HttpResponseException         thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException     thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException     thrown if the request is rejected by server on status code 409.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> completeChatWithResponse(BinaryData requestBody, RequestOptions requestOptions) {
        return null;
    }

    /**
     * @param message
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ChatCompletion completeChat(String message) {
        return null;
    }

    /**
     * @param message
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ChatCompletion completeChat(String message, ChatCompletionOptions chatCompletionOptions) {
        return null;
    }

    /**
     * @param messages
     * @param chatCompletionOptions
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ChatCompletion completeChat(List<ChatRequestMessage> messages, ChatCompletionOptions chatCompletionOptions) {
        return null;
    }

    /**
     * @param messages
     * @param choiceCount
     * @param chatCompletionOptions
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ChatCompletionCollection completeChat(List<ChatRequestMessage> messages, int choiceCount, ChatCompletionOptions chatCompletionOptions) {
        return null;
    }

    /**
     * @param messages
     * @param choiceCount
     * @param chatCompletionOptions
     * @param requestOptions
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ChatCompletionCollection> completeChatWithResponse(List<ChatRequestMessage> messages,
                                                                       int choiceCount, ChatCompletionOptions chatCompletionOptions,
                                                                       RequestOptions requestOptions) {
        return null;
    }

    /**
     * @param message
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public IterableStream<ChatUpdate> completeChatStream(String message) {
        return null;
    }

    /**
     * @param messages
     * @param chatCompletionOptions
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public IterableStream<ChatUpdate> completeChatStream(List<ChatRequestMessage> messages, ChatCompletionOptions chatCompletionOptions) {
        return null;
    }
}
