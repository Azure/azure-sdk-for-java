package com.azure.ai.openai;

import com.azure.ai.openai.models.GenerateImageResult;
import com.azure.ai.openai.models.GenerateImageResultCollection;
import com.azure.ai.openai.models.GenerateImagesOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;

/**
 * The ImageClient class provides a client for interacting with the Azure OpenAI service.
 */
public final class ImageClient {

    private final OpenAIClient openAIClient;
    private final String model;

    ImageClient(OpenAIClient openAIClient, String model) {
        this.openAIClient = openAIClient;
        this.model = model;
    }

    /**
     * Creates an image given a prompt.
     *
     * @param prompt The prompt to generate an image from.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of a successful image generation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public GenerateImageResult generateImage(String prompt) {
        return null;
    }

    /**
     * Creates an image given a prompt.
     *
     * @param prompt The prompt to generate an image from.
     * @param generateImagesOptions Represents the request data used to generate images.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of a successful image generation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public GenerateImageResultCollection generateImages(String prompt, GenerateImagesOptions generateImagesOptions) {
        return null;
    }

    /**
     * Creates an image given a prompt.
     *
     * @param generateImagesOptions Represents the request data used to generate images.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of a successful image generation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<GenerateImageResultCollection> generateImagesWithResponse(String prompt, GenerateImagesOptions generateImagesOptions, RequestOptions requestOptions) {
        return null;
    }

    /**
     * Creates an image given a prompt.
     * <p>
     * <strong>Request Body Schema</strong>
     * </p>
     * <pre>{@code
     * {
     *     model: String (Optional)
     *     prompt: String (Required)
     *     n: Integer (Optional)
     *     size: String(256x256/512x512/1024x1024/1792x1024/1024x1792) (Optional)
     *     response_format: String(url/b64_json) (Optional)
     *     quality: String(standard/hd) (Optional)
     *     style: String(natural/vivid) (Optional)
     *     user: String (Optional)
     * }
     * }</pre>
     * <p>
     * <strong>Response Body Schema</strong>
     * </p>
     * <pre>{@code
     * {
     *     created: long (Required)
     *     data (Required): [
     *          (Required){
     *             url: String (Optional)
     *             b64_json: String (Optional)
     *             revised_prompt: String (Optional)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param requestBody Represents the request data used to generate images.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the result of a successful image generation operation along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> generateImagesWithResponse(BinaryData requestBody, RequestOptions requestOptions) {
        return openAIClient.getImageGenerationsWithResponse(model, requestBody, requestOptions);
    }

}
