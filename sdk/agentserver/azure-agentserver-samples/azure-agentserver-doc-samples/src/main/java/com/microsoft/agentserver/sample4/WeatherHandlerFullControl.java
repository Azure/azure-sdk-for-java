package com.microsoft.agentserver.sample4;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * A weather handler with full control over each individual SSE event.
 * Demonstrates the builder API for fine-grained control over function call
 * and text message lifecycles.
 * Equivalent to the C# WeatherHandlerFullControl.
 */
public class WeatherHandlerFullControl implements ResponseHandler {

    public static final java.util.concurrent.ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private static ResponseInputItem.FunctionCallOutput findFunctionCallOutput(ResponseCreateParams.Body request) {
        Optional<ResponseCreateParams.Input> inputOpt = request.input();
        if (inputOpt.isEmpty()) {
            return null;
        }
        ResponseCreateParams.Input input = inputOpt.get();
        if (!input.isResponse()) {
            return null;
        }
        List<ResponseInputItem> items = input.asResponse();
        for (ResponseInputItem item : items) {
            if (item.isFunctionCallOutput()) {
                return item.asFunctionCallOutput();
            }
        }
        return null;
    }

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        ResponseEventStream stream = ResponseEventStream.create(responseContext, request);

        EXECUTOR_SERVICE.execute(() -> {
            try {
                // Check if the input contains a function call output (turn 2)
                ResponseInputItem.FunctionCallOutput toolOutput = findFunctionCallOutput(request.responseCreateParams());

                if (toolOutput != null) {
                    // Turn 2: function output received — return the weather as a text message
                    String weatherJson = toolOutput.output().isString()
                        ? toolOutput.output().asString()
                        : "{}";

                    stream.emitCreated();
                    stream.emitInProgress();

                    String reply = "The weather is: " + weatherJson;

                    stream.addOutputMessage(msg -> {
                        msg.emitAdded();              // response.output_item.added

                        msg.addTextPart(text -> {
                            text.emitAdded();         // response.content_part.added
                            text.emitDelta(reply);    // response.output_text.delta
                            text.emitDone(reply);     // response.output_text.done
                            // response.content_part.done (auto)
                        });

                        msg.emitDone();               // response.output_item.done
                    });

                    stream.emitCompleted();
                } else {
                    // Turn 1: emit a function call for "get_weather" with full control
                    stream.emitCreated();
                    stream.emitInProgress();

                    String arguments = "{\"location\":\"Seattle\",\"unit\":\"fahrenheit\"}";

                    stream.addOutputFunctionCall(funcCall -> {
                        funcCall.emitAdded("get_weather", "call_weather_1");  // response.output_item.added
                        funcCall.emitArgumentsDelta(arguments);                // response.function_call_arguments.delta
                        funcCall.emitArgumentsDone("get_weather", arguments);  // response.function_call_arguments.done
                        funcCall.emitDone();                                   // response.output_item.done
                    });

                    stream.emitCompleted();
                }
            } catch (Exception e) {
                stream.emitFailed();
            }
        });

        return stream;
    }
}

