package com.azure.ai.openai;

import com.azure.ai.openai.models.Embedding;
import com.azure.ai.openai.models.EmbeddingCollection;
import com.azure.ai.openai.models.EmbeddingOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;

import java.util.List;

/**
 * The EmbeddingClient class provides a client for interacting with the Azure OpenAI service.
 */
public final class EmbeddingClient {

    private final OpenAIClient openAIClient;
    private final String model;

    EmbeddingClient(OpenAIClient openAIClient, String model) {
        this.openAIClient = openAIClient;
        this.model = model;
    }

    /**
     * @param input
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Embedding generateEmbedding(String input) {
        return null;
    }

    /**
     * @param input
     * @param embeddingOptions
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Embedding generateEmbedding(String input, EmbeddingOptions embeddingOptions) {
        return null;
    }

    /**
     * Return the embeddings for a given prompt.
     *
     * @param embeddingOptions The configuration information for an embeddings request.
     * Embeddings measure the relatedness of text strings and are commonly used for search, clustering,
     * recommendations, and other similar scenarios.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return representation of the response data from an embeddings request.
     * Embeddings measure the relatedness of text strings and are commonly used for search, clustering,
     * recommendations, and other similar scenarios.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EmbeddingCollection generateEmbeddings(List<String> inputs, EmbeddingOptions embeddingOptions) {
        return null;
    }

    /**
     * @param inputs
     * @param embeddingOptions
     * @param requestOptions
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EmbeddingCollection> generateEmbeddingsWithResponse(List<String> inputs, EmbeddingOptions embeddingOptions, RequestOptions requestOptions) {
        return null;
    }

    /**
     * Return the embeddings for a given prompt.
     * <p>
     * <strong>Request Body Schema</strong>
     * </p>
     * <pre>{@code
     * {
     *     user: String (Optional)
     *     model: String (Optional)
     *     input (Required): [
     *         String (Required)
     *     ]
     *     input_type: String (Optional)
     * }
     * }</pre>
     * <p>
     * <strong>Response Body Schema</strong>
     * </p>
     * <pre>{@code
     * {
     *     data (Required): [
     *          (Required){
     *             embedding (Required): [
     *                 double (Required)
     *             ]
     *             index: int (Required)
     *         }
     *     ]
     *     usage (Required): {
     *         prompt_tokens: int (Required)
     *         total_tokens: int (Required)
     *     }
     * }
     * }</pre>
     *
     * @param requestBody The configuration information for an embeddings request.
     * Embeddings measure the relatedness of text strings and are commonly used for search, clustering,
     * recommendations, and other similar scenarios.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return representation of the response data from an embeddings request.
     * Embeddings measure the relatedness of text strings and are commonly used for search, clustering,
     * recommendations, and other similar scenarios along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> generateEmbeddingsWithResponse(BinaryData requestBody, RequestOptions requestOptions) {
        return openAIClient.getEmbeddingsWithResponse(model, requestBody, requestOptions);
    }
}
