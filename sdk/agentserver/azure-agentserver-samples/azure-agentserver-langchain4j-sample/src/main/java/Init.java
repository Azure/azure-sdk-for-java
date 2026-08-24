import com.microsoft.agentserver.api.langchain4j.SupervisorAgentWithMemory;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.Produces;

@Alternative
@Priority(1)
@ApplicationScoped
public class Init {
    private static final String DEPLOYMENT_NAME = System.getenv().getOrDefault("AZURE_DEPLOYMENT_NAME", "gpt-4o");
    private static final String AZURE_API_KEY = System.getenv("AZURE_API_KEY");
    private static final String AZURE_ENDPOINT = System.getenv("AZURE_ENDPOINT");

    @Produces
    @Alternative
    @Priority(1)
    @ApplicationScoped
    public SupervisorAgentWithMemory createAgent() {
        ChatModel model = AzureOpenAiChatModel.builder()
            .deploymentName(DEPLOYMENT_NAME)
            .apiKey(AZURE_API_KEY)
            .endpoint(AZURE_ENDPOINT)
            .build();

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
}
