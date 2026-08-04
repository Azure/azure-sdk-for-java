import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

public class Main {

    public static void main(String[] args) {
        MathsAgent mathsAgent = AgenticServices
            .agentBuilder(MathsAgent.class)
            .tools(new MathsTools())
            .chatModel(AzureOpenAiChatModel.builder()
                .deploymentName("gpt-4o")
                .apiKey(System.getenv("AZURE_API_KEY"))
                .endpoint(System.getenv("AZURE_ENDPOINT"))
                .build())
            .outputKey("result")
            .build();

        UntypedAgent agent = AgenticServices
            .sequenceBuilder(UntypedAgent.class)
            .subAgents(mathsAgent)
            .outputKey("result")
            .build();

        SpringAgentServerService.buildAgent(agent);
    }
}

