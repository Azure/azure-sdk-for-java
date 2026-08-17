import dev.langchain4j.agentic.Agent;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public interface MathsAgent {
    /**
     * AI agent for solving mathematical queries.
     * <p>
     * Uses LangChain4j's @Agent annotation to define an AI agent that can leverage
     * available tools to solve math problems. The @UserMessage template defines the
     * prompt sent to the language model, and @V binds method parameters to template
     * placeholders.
     */
    @UserMessage("""
        You are a helpful bot that uses the tools available to solve simple maths queries.

        The query is:
        {{messages}}
        """)
    @Agent("Solves a simple math problem using the available tools")
    public String solveQuery(@V("messages") List<ChatMessage> messages);
}

