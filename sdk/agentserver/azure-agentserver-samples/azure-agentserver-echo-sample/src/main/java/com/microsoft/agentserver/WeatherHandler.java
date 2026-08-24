package com.microsoft.agentserver;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

public class WeatherHandler implements ResponseHandler {

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        ResponseEventStream stream = ResponseEventStream.create(responseContext, request);

        Executors.newSingleThreadExecutor().execute(() -> {
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

                    stream.addOutputMessage(msg -> {
                        msg.emitAdded();
                        msg.addTextPart(text -> {
                            text.emitAdded();
                            String reply = "The weather is: " + weatherJson;
                            text.emitDelta(reply);
                            text.emitDone(reply);
                        });
                        msg.emitDone();
                    });

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
}
