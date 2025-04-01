package com.azure.openai.tests;

import com.azure.ai.openai.AzureCompanionAsyncClient;
import com.azure.ai.openai.models.AzureChatDataSource;
import com.azure.ai.openai.models.AzureChatDataSourceApiKeyAuthenticationOptions;
import com.azure.ai.openai.models.AzureCreateChatCompletionRequest;
import com.azure.ai.openai.models.AzureCreateChatCompletionResponse;
import com.azure.ai.openai.models.AzureCreateChatCompletionResponseChoice;
import com.azure.ai.openai.models.AzureSearchChatDataSource;
import com.azure.ai.openai.models.AzureSearchChatDataSourceParameters;
import com.azure.ai.openai.models.ChatCompletionRequestMessageContentPartText;
import com.azure.ai.openai.models.ChatCompletionRequestUserMessage;
import com.azure.core.util.BinaryData;
import com.openai.azure.credential.AzureApiKeyCredential;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static com.azure.openai.tests.TestUtils.AZURE_OPEN_AI;
import static com.azure.openai.tests.TestUtils.GA;
import static com.azure.openai.tests.TestUtils.OPEN_AI;
import static com.azure.openai.tests.TestUtils.PREVIEW;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AzureCompanionAsyncClientTests extends  OpenAIOkHttpClientTestBase {

    private AzureCompanionAsyncClient client;

    private OpenAIOkHttpClientAsync.Builder setAzureServiceApiVersion(OpenAIOkHttpClientAsync.Builder clientBuilder,
                                                                      String apiVersion) {
        if (GA.equals(apiVersion)) {
            clientBuilder.azureServiceVersion(AZURE_OPENAI_SERVICE_VERSION_GA);
        } else if (PREVIEW.equals(apiVersion)) {
            clientBuilder.azureServiceVersion(AZURE_OPENAI_SERVICE_VERSION_PREVIEW);
        } else {
            throw new IllegalArgumentException("Invalid Azure API version");
        }
        return clientBuilder;
    }

    private AzureCompanionAsyncClient createAsyncClient(String apiType, String apiVersion) {
        OpenAIOkHttpClientAsync.Builder clientBuilder = OpenAIOkHttpClientAsync.builder();
        if (AZURE_OPEN_AI.equals(apiType)) {
            setAzureServiceApiVersion(clientBuilder, apiVersion)
                    .credential(AzureApiKeyCredential.create(System.getenv("AZURE_OPENAI_KEY")))
                    .baseUrl(getEndpoint());
        } else if (OPEN_AI.equals(apiType)) {
            clientBuilder.credential(BearerTokenCredential.create(System.getenv("NON_AZURE_OPENAI_KEY")));
        } else {
            throw new IllegalArgumentException("Invalid API type");
        }

        return new AzureCompanionAsyncClient(clientBuilder.build());
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureOnlyClient")
    public void testChatCompletionByod(String apiType, String apiVersion, String testModel) {
        client = createAsyncClient(apiType, apiVersion);

        AzureCreateChatCompletionRequest request = new AzureCreateChatCompletionRequest(
                Arrays.asList(new ChatCompletionRequestUserMessage(
                        BinaryData.fromString("What do most contributors to open source do?")))
        );
        AzureSearchChatDataSourceParameters parameters = new AzureSearchChatDataSourceParameters(
            System.getenv("AZURE_SEARCH_ENDPOINT"),
            System.getenv("AZURE_SEARCH_INDEX_NAME"),
            BinaryData.fromObject(new AzureChatDataSourceApiKeyAuthenticationOptions(
                System.getenv("AZURE_SEARCH_API_KEY")))
        );

        request.setDataSources(Arrays.asList(new AzureSearchChatDataSource(parameters)));

        AzureCreateChatCompletionResponse completion = client.createResponse("gpt-4o-mini", request).block();
        assertNotNull(completion);
        List<AzureCreateChatCompletionResponseChoice> choices = completion.getChoices();
        assertNotNull(choices);
        AzureCreateChatCompletionResponseChoice firstChoice = choices.get(0);
        assertNotNull(firstChoice);
//        assertChatCompletionByod(completion);
    }

    @ParameterizedTest
    @MethodSource("com.azure.openai.tests.TestUtils#azureOnlyClient")
    public void testChatCompletionByodErrorNoIndexName(String apiType, String apiVersion, String testModel) {

    }
}
