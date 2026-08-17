package com.microsoft.agentserver.api.langchain4j.noop;

import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.Map;

@Alternative
@Priority(Integer.MIN_VALUE)
@ApplicationScoped
public class NOOPUntypedAgent implements UntypedAgent {

    @Override
    public Object invoke(Map<String, Object> input) {
        return null;
    }

    @Override
    public ResultWithAgenticScope<String> invokeWithAgenticScope(Map<String, Object> input) {
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
