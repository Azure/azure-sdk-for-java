import com.microsoft.agentserver.api.FoundryEnvironment;
import com.microsoft.agentserver.api.langchain4j.SupervisorAgentWithMemory;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;

public class Init {
    public SupervisorAgentWithMemory createAgent() {
        String openAiEndpoint = FoundryEnvironment.OPENAI_ENDPOINT;
        if (openAiEndpoint == null || openAiEndpoint.isBlank()) {
            throw new IllegalStateException("No endpoint configured. Set FOUNDRY_PROJECT_ENDPOINT or AZURE_OPENAI_ENDPOINT.");
        }

        ChatModel model = buildModel(openAiEndpoint);

        MathsAgent mathsAgent = AgenticServices
            .agentBuilder(MathsAgent.class)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
            .tools(new MathsTools())
            .chatModel(model)
            .outputKey("result")
            .build();

        GeneralAgent generalAgent = AgenticServices
            .agentBuilder(GeneralAgent.class)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
            .chatModel(model)
            .outputKey("result")
            .build();

        return AgenticServices
            .supervisorBuilder(SupervisorAgentWithMemory.class)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
            .chatModel(model)
            .subAgents(mathsAgent, generalAgent)
            .contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY)
            .build();
    }

    private static AzureOpenAiChatModel buildModel(String openAiEndpoint) {
        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
            .deploymentName(FoundryEnvironment.MODEL_DEPLOYMENT_NAME);

        String nonAzureApiKey = System.getenv("AZURE_CLIENT_KEY");
        if (nonAzureApiKey != null && !nonAzureApiKey.isBlank()) {
            builder.nonAzureApiKey(nonAzureApiKey);
        } else {
            builder.tokenCredential(FoundryEnvironment.resolveCredential());
        }
        return builder
            .endpoint(openAiEndpoint)
            .build();
    }
}
