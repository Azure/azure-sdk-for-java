package com.microsoft.agentserver.sample.financial;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AccountInfoAgent {

    @SystemMessage("""
        You are a banker that provides information about a users account,
        """)
    @UserMessage("""
        Get {{user}}'s account balance.
        """)
    @Agent("A banker that provides information about a users account")
    Double getBalance(@V("user") String user);
}
