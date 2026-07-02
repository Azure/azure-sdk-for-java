package com.microsoft.agentserver.sample.financial;

import com.microsoft.agentserver.api.langchain4j.SupervisorAgentWithMemory;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountingAgent {

    public static SupervisorAgentWithMemory build(ChatModel model) {
        BankTool bankTool = new BankTool();
        bankTool.createAccount("Mario", 1000.0);
        bankTool.createAccount("Georgio", 1000.0);
        bankTool.createAccount("Alice", 1000.0);
        bankTool.createAccount("Bob", 1000.0);

        WithdrawAgent withdrawAgent = AgenticServices
            .agentBuilder(WithdrawAgent.class)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
            .chatModel(model)
            .tools(bankTool)
            .build();

        CreditAgent creditAgent = AgenticServices
            .agentBuilder(CreditAgent.class)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
            .chatModel(model)
            .tools(bankTool)
            .build();

        ExchangeAgent exchange = AgenticServices
            .agentBuilder(ExchangeAgent.class)
            .chatModel(model)
            .tools(new ExchangeTool())
            .build();

        AccountInfoAgent accountInfoAgent = AgenticServices
            .agentBuilder(AccountInfoAgent.class)
            .chatModel(model)
            .tools(bankTool)
            .build();

        return AgenticServices
            .supervisorBuilder(SupervisorAgentWithMemory.class)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
            .chatModel(model)
            .subAgents(withdrawAgent, creditAgent, exchange, accountInfoAgent)
            .responseStrategy(SupervisorResponseStrategy.SCORED)
            .contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY)
            .build();
    }

}
