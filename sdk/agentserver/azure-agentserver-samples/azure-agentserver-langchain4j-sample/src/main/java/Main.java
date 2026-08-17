import com.microsoft.agentserver.api.langchain4j.Langchain4jResponsesHandler;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        JerseyAgentServerAdaptorService.buildAgent(
            Langchain4jResponsesHandler.builder()
                .supervisorAgent(new Init().createAgent())
                .build()
        );

        Thread.sleep(Long.MAX_VALUE);
    }
}
