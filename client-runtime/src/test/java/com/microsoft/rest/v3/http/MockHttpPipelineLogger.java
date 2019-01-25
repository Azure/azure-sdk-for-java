package com.microsoft.rest.v3.http;

import java.util.ArrayList;
import java.util.List;

public class MockHttpPipelineLogger implements HttpPipelineLogger {
    private final HttpPipelineLogLevel minimumLogLevel;
    private final List<String> logs;

    public MockHttpPipelineLogger(HttpPipelineLogLevel minimumLogLevel) {
        this.minimumLogLevel = minimumLogLevel;
        this.logs = new ArrayList<>();
    }

    @Override
    public HttpPipelineLogLevel minimumLogLevel() {
        return minimumLogLevel;
    }

    @Override
    public void log(HttpPipelineLogLevel logLevel, String message, Object... formattedArguments) {
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
