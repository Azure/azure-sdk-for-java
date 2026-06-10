package com.microsoft.agentserver.sample5;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * A study tutor handler demonstrating conversation history.
 * Uses ResponseContext.getHistoryAsync() to resolve previous turns and
 * build context-aware responses.
 * Equivalent to the C# StudyTutorHandler.
 */
public class StudyTutorHandler implements ResponseHandler {

    public static final java.util.concurrent.ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private static String extractMessageText(ResponseOutputMessage message) {
        return message.content().stream()
            .filter(ResponseOutputMessage.Content::isOutputText)
            .map(content -> content.asOutputText().text())
            .findFirst()
            .orElse("(none)");
    }

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        ResponseEventStream stream = ResponseEventStream.create(responseContext, request)
            .emitCreated()
            .emitInProgress();

        EXECUTOR_SERVICE.execute(() -> {
            try {
                // Resolve conversation history from previous responses.
                // Returns empty list if no previous_response_id is set.
                List<ResponseItem> history = responseContext.getHistoryAsync().join();

                List<ResponseOutputMessage> messageHistory = history.stream()
                    .filter(ResponseItem::isResponseOutputMessage)
                    .map(ResponseItem::asResponseOutputMessage)
                    .toList();

                String currentInput = request.inputText();

                String reply;
                if (messageHistory.isEmpty()) {
                    reply = "Welcome! I'm your study tutor. You asked: \"" + currentInput + "\". " +
                        "Let me help you understand that topic.";
                } else {
                    // Find the last message in history
                    String lastText = extractMessageText(messageHistory.getLast());

                    String truncatedLastText = lastText.length() > 50
                        ? lastText.substring(0, 50) + "..."
                        : lastText + "...";

                    reply = "[Turn " + messageHistory.size() + "] Building on our previous discussion " +
                        "(last answer: \"" + truncatedLastText + "\"), " +
                        "you asked: \"" + currentInput + "\".";
                }

                stream.addOutputMessage(msg -> msg.outputItemMessage(reply));
                stream.emitCompleted();
            } catch (Exception e) {
                stream.emitFailed();
            }
        });

        return stream;
    }

}
