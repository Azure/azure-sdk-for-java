import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.EmbeddingCreateParams;

public class EmbeddingSample {
    public static void main(String[] args) {
        // Your code to perform embedding generation goes here.
        // This is just a placeholder for the actual implementation.

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

        // Example usage of the client to generate embeddings
        String inputText = "This is a sample text for embedding generation.";
        String model = "text-embedding-ada-002";
        EmbeddingCreateParams params = EmbeddingCreateParams.builder().model(model).input(inputText).build();
        client
            .embeddings()
            .create(params)
            .data()
            .forEach(embedding -> {
                System.out.println("Row: " + embedding.embedding());
            });
    }
}
