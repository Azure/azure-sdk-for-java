package com.microsoft.agentserver.api.langchain4j.noop;

import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@Alternative
@Priority(Integer.MIN_VALUE)
@ApplicationScoped
public class NOOPSupervisorAgent implements SupervisorAgent {

    @Override
    public String invoke(String request) {
        return "";
    }

    @Override
    public ResultWithAgenticScope<String> invokeWithAgenticScope(String request) {
        return null;
    }

    @Override
    public AgenticScope getAgenticScope(Object memoryId) {
        return null;
    }

    @Override
    public boolean evictAgenticScope(Object memoryId) {
        return false;
    }
}
