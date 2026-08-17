package com.microsoft.agentserver.sample.financial;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CreditAgent {
    @SystemMessage("""
        You are a banker that can only credit US dollars (USD) to a user account,
        """)
    @UserMessage("""
        Credit {{amount}} USD to {{user}}'s account and return the new balance.
        """)
    @Agent("A banker that credit USD to an account")
    String credit(@V("user") String user, @V("amount") Double amount);
}
