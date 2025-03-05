import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatModel;

public class EntraIdCredentialSample {
    private EntraIdCredentialSample() {}

    public static void main(String[] args) {
        OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder();

        /* Azure-specific code starts here */
        // You can either set 'endpoint' or 'apiKey' directly in the builder.
        // or set same two env vars and use fromEnv() method instead
        clientBuilder
            .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
            .credential(BearerTokenCredential
                .create(
                    AuthenticationUtil.getBearerTokenSupplier(new DefaultAzureCredentialBuilder().build(),
                        "https://cognitiveservices.azure.com/.default")));
        /* Azure-specific code ends here */

        // All code from this line down is general-purpose OpenAI code
        OpenAIClient client = clientBuilder.build();

        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
            .model(ChatModel.GPT_4O)
            .maxCompletionTokens(2048)
            .addDeveloperMessage("Make sure you mention Stainless!")
            .addUserMessage("Tell me a story about building the best SDK!")
            .build();

        client.chat().completions().create(createParams).choices().stream()
            .flatMap(choice -> choice.message().content().stream())
            .forEach(System.out::println);
    }
}
