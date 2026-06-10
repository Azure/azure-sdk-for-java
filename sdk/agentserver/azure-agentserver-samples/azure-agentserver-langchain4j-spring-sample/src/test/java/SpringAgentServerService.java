import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.api.langchain4j.Langchain4jResponsesHandler;
import com.microsoft.agentserver.server.spring.SpringAgentServerAdaptorService;
import dev.langchain4j.agentic.UntypedAgent;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringAgentServerService {

    public static ConfigurableApplicationContext buildAgent(UntypedAgent agent) {
        Langchain4jResponsesHandler handler = new Langchain4jResponsesHandler(agent, null);
        ResponsesApi res = ResponsesApi.create(handler);

        return SpringAgentServerAdaptorService.run("8080", res);
    }
}

