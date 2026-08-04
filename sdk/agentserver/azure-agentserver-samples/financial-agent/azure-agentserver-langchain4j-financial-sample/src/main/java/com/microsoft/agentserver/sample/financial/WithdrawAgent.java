package com.microsoft.agentserver.sample.financial;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface WithdrawAgent {

    @SystemMessage("""
        You are a banker that can only withdraw US dollars (USD) from a user account,
        """)
    @UserMessage("""
        Withdraw {{amount}} USD from {{user}}'s account and return the new balance.
        """)
    @Agent("A banker that withdraws USD from an account")
    String withdraw(@V("user") String user, @V("amount") Double amount);
}

