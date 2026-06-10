package com.microsoft.agentserver.sample.financial.jersey;

import com.azure.core.credential.TokenCredential;
import com.microsoft.agentserver.api.FoundryEnvironment;
import com.microsoft.agentserver.api.TrustStoreInstaller;
import com.microsoft.agentserver.api.langchain4j.Langchain4jResponsesHandler;
import com.microsoft.agentserver.api.langchain4j.SupervisorAgentWithMemory;
import com.microsoft.agentserver.sample.financial.AccountingAgent;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;

public class Main {

    private static final Logger LOGGER;

    static {
        if (System.getProperty("java.util.logging.manager") == null) {
            System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        }

        BasicConfigurator.configure();

        LOGGER = org.slf4j.LoggerFactory.getLogger(Main.class);
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            TrustStoreInstaller.installAdcEgressProxyCertificate(LOGGER);
        } catch (Exception e) {
            LOGGER.warn("Failed to install ADC egress proxy CA certificate into the JVM truststore.", e);
        }

        String openAiEndpoint = FoundryEnvironment.OPENAI_ENDPOINT;
        if (openAiEndpoint == null || openAiEndpoint.isBlank()) {
            LOGGER.error("No endpoint configured. Set FOUNDRY_PROJECT_ENDPOINT or AZURE_OPENAI_ENDPOINT.");
            System.exit(1);
        }

        LOGGER.info("=== Resolved Configuration ===");
        LOGGER.info("  Agent Name:          {}", FoundryEnvironment.AGENT_NAME);
        LOGGER.info("  Model Deployment:    {}", FoundryEnvironment.MODEL_DEPLOYMENT_NAME);
        LOGGER.info("  Project Endpoint:    {}", FoundryEnvironment.PROJECT_ENDPOINT);
        LOGGER.info("  OpenAI Endpoint:     {}", openAiEndpoint);
        LOGGER.info("  Hosted:              {}", FoundryEnvironment.IS_HOSTED);
        LOGGER.info("=== End Configuration ===");

        AzureOpenAiChatModel model = getAzureOpenAiChatModel(openAiEndpoint);

        SupervisorAgentWithMemory agent = AccountingAgent.build(model);

        JerseyAgentServerAdaptorService.buildAgent(
            Langchain4jResponsesHandler.builder()
                .supervisorAgent(agent)
                .build());

        Thread.currentThread().join();
    }


    private static AzureOpenAiChatModel getAzureOpenAiChatModel(String openAiEndpoint) {
        AzureOpenAiChatModel.Builder modelBuilder = AzureOpenAiChatModel.builder()
            .deploymentName(FoundryEnvironment.MODEL_DEPLOYMENT_NAME);

        String nonAzureApiKey = System.getenv("AZURE_CLIENT_KEY");
        if (nonAzureApiKey != null && !nonAzureApiKey.isBlank()) {
            LOGGER.info("Using non-Azure API key for authentication");
            modelBuilder.nonAzureApiKey(nonAzureApiKey);
        } else {
            TokenCredential credential = FoundryEnvironment.resolveCredential();
            modelBuilder.tokenCredential(credential);
        }

        return modelBuilder
            .endpoint(openAiEndpoint)
            .build();
    }
}
