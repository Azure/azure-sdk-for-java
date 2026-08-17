import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.api.langchain4j.Langchain4jResponsesHandler;
import dev.langchain4j.agentic.UntypedAgent;
import jakarta.enterprise.context.ApplicationScoped;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.inject.hk2.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class JerseyAgentServerService {

    public static class AgentBinder extends AbstractBinder {

        private final UntypedAgent supervisorAgent;

        public AgentBinder(UntypedAgent supervisorAgent) {
            this.supervisorAgent = supervisorAgent;
        }

        @Override
        protected void configure() {
            Langchain4jResponsesHandler handler = new Langchain4jResponsesHandler(supervisorAgent, null);
            ResponsesApi res = ResponsesApi.builder()
                .responseHandler(handler)
                .build();

            bind(res).to(ResponsesApi.class).in(ApplicationScoped.class);
        }
    }

    public static HttpServer buildAgent(UntypedAgent agent) {
        final ResourceConfig rc = new ResourceConfig()
            .register(new AgentBinder(agent))
            .packages(true, "com.microsoft");

        final String BASE_URI = "http://0.0.0.0:8080/";
        return build(URI.create(BASE_URI), rc);
    }

    public static HttpServer build(URI uri, ResourceConfig rc) {
        return GrizzlyHttpServerFactory.createHttpServer(uri, rc);
    }
}
