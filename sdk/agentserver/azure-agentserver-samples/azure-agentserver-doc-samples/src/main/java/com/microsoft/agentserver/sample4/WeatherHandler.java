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
 * A weather handler demonstrating function calling using the convenience API.
 * Turn 1: emits a function call for "get_weather".
 * Turn 2: reads the function output and returns a text message with the result.
 * Equivalent to the C# WeatherHandler (convenience version).
 */
public class WeatherHandler implements ResponseHandler {

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

                    stream.addOutputMessage(msg -> msg
                        .outputItemMessage("The weather is: " + weatherJson));

                    stream.emitCompleted();
                } else {
                    // Turn 1: emit a function call for "get_weather"
                    stream.emitCreated();
                    stream.emitInProgress();

                    String arguments = "{\"location\":\"Seattle\",\"unit\":\"fahrenheit\"}";

                    stream.addOutputFunctionCall(funcCall -> funcCall
                        .emitAdded("get_weather", "call_weather_1")
                        .emitArgumentsDelta(arguments)
                        .emitArgumentsDone("get_weather", arguments)
                        .emitDone());

                    stream.emitCompleted();
                }
            } catch (Exception e) {
                stream.emitFailed();
            }
        });

        return stream;
    }
}

