package com.microsoft.agentserver.api.langchain4j;

import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.V;

public interface SupervisorAgentWithMemory extends SupervisorAgent {
    ResultWithAgenticScope<String> invokeWithAgenticScope(@MemoryId String memoryId, @V("request") String var1);
}
