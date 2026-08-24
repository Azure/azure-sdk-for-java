import com.microsoft.agentserver.api.langchain4j.Langchain4jResponsesHandler;
import com.microsoft.agentserver.server.spring.SpringAgentServerAdaptorService;

public class Main {
    public static void main(String[] args) {
        SpringAgentServerAdaptorService.run(
            Langchain4jResponsesHandler.builder()
                .supervisorAgent(new Init().createAgent())
                .build()
        );
    }
}

