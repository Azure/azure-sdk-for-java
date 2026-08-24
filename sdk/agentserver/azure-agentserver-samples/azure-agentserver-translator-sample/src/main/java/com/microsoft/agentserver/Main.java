package com.microsoft.agentserver;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.agentserver.api.FoundryEnvironment;
import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.api.TrustStoreInstaller;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;
import com.openai.azure.AzureOpenAIServiceVersion;
import com.openai.azure.credential.AzureApiKeyCredential;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /** Scope for Azure Cognitive Services (Azure OpenAI) access tokens. */
    private static final String COGNITIVE_SERVICES_SCOPE = "https://cognitiveservices.azure.com/.default";

    public static void main(String[] args) throws InterruptedException {
        // Install the ADC egress proxy CA certificate so outbound TLS calls to Azure OpenAI
        // succeed in hosted Foundry environments (avoids "PKIX path building failed").
        try {
            TrustStoreInstaller.installAdcEgressProxyCertificate(LOGGER);
        } catch (Exception e) {
            LOGGER.warn("Failed to install ADC egress proxy CA certificate into the JVM truststore.", e);
        }

        OpenAIClient client = buildClient();

        JerseyAgentServerAdaptorService.buildAgent(
            ResponsesApi.builder()
                .responseHandler(new ItalianTranslatorHandler(client, FoundryEnvironment.MODEL_DEPLOYMENT_NAME))
                .build()
        );

        Thread.sleep(Long.MAX_VALUE);
    }

    /**
     * Builds the Azure OpenAI client. The endpoint and model deployment are supplied by the
     * Foundry hosting environment; authentication prefers an {@code AZURE_CLIENT_KEY} for local
     * development, otherwise a managed-identity bearer token from the hosting environment.
     *
     * @return a configured {@link OpenAIClient}.
     * @throws IllegalStateException if {@code openAiEndpoint} is null or blank.
     */
    private static OpenAIClient buildClient() {
        if (FoundryEnvironment.OPENAI_ENDPOINT == null || FoundryEnvironment.OPENAI_ENDPOINT.isBlank()) {
            throw new IllegalStateException(
                "No endpoint configured. Set FOUNDRY_PROJECT_ENDPOINT or AZURE_OPENAI_ENDPOINT.");
        }

        LOGGER.info("=== Resolved Configuration ===");
        LOGGER.info("  Agent Name:          {}", FoundryEnvironment.AGENT_NAME);
        LOGGER.info("  Model Deployment:    {}", FoundryEnvironment.MODEL_DEPLOYMENT_NAME);
        LOGGER.info("  Project Endpoint:    {}", FoundryEnvironment.PROJECT_ENDPOINT);
        LOGGER.info("  OpenAI Endpoint:     {}", FoundryEnvironment.OPENAI_ENDPOINT);
        LOGGER.info("  Hosted:              {}", FoundryEnvironment.IS_HOSTED);
        LOGGER.info("=== End Configuration ===");

        OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder()
            .baseUrl(FoundryEnvironment.OPENAI_ENDPOINT)
            .azureServiceVersion(AzureOpenAIServiceVersion.latestStableVersion());

        String apiKey = System.getenv("AZURE_CLIENT_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            LOGGER.info("Using API key for authentication");
            clientBuilder.credential(AzureApiKeyCredential.create(apiKey));
        } else {
            LOGGER.info("Using managed-identity token for authentication");
            TokenCredential credential = FoundryEnvironment.resolveCredential();
            TokenRequestContext ctx = new TokenRequestContext().addScopes(COGNITIVE_SERVICES_SCOPE);
            clientBuilder.credential(
                BearerTokenCredential.create(() -> credential.getTokenSync(ctx).getToken()));
        }

        return clientBuilder.build();
    }
}
