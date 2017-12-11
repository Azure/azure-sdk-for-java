package com.microsoft.rest.v2.http;

import java.util.ArrayList;
import java.util.List;

public class MockHttpPipelineLogger implements HttpPipeline.Logger {
    private final HttpPipeline.LogLevel minimumLogLevel;
    private final List<String> logs;

    public MockHttpPipelineLogger(HttpPipeline.LogLevel minimumLogLevel) {
        this.minimumLogLevel = minimumLogLevel;
        this.logs = new ArrayList<>();
    }

    @Override
    public HttpPipeline.LogLevel minimumLogLevel() {
        return minimumLogLevel;
    }

    @Override
    public void log(HttpPipeline.LogLevel logLevel, String message, Object... formattedArguments) {
        this.logs.add(logLevel + ") " + String.format(message, formattedArguments));
    }

    public String[] logs() {
        final int logCount = logs.size();
        final String[] result = new String[logCount];
        for (int i = 0; i < logCount; ++i) {
            result[i] = logs.get(i);
        }
        return result;
    }
}
