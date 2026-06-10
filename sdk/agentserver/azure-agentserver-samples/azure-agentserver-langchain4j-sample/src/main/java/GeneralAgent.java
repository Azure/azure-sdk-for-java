import dev.langchain4j.agentic.Agent;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

/**
 * Fallback sub-agent that handles any general question for which no
 * specialized sub-agent applies. Keeps the supervisor from returning an empty
 * response when the user asks something outside the math domain.
 */
public interface GeneralAgent {

    @SystemMessage("""
        You are a helpful assistant that is part of an agent for answering maths questions. Confine your answers to
        answering questions about maths and what the agent can do
        """)
    @UserMessage("""
        Conversation:
        {{messages}}
        """)
    @Agent("Answers general questions when no specialised agent applies")
    String answer(@V("messages") List<ChatMessage> messages);
}
