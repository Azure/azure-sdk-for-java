package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.MockHttpPipelineLogger;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequestPolicyOptionsTests {
    @Test
    public void shouldLogWithNullLogger() {
        final RequestPolicy.Options options = new RequestPolicy.Options(null);
        assertFalse(options.shouldLog(HttpPipeline.LogLevel.INFO));
    }

    @Test
    public void shouldLogWithNullLogLevel() {
        final RequestPolicy.Options options = new RequestPolicy.Options(new MockHttpPipelineLogger(HttpPipeline.LogLevel.INFO));
        assertFalse(options.shouldLog(null));
    }

    @Test
    public void shouldLogWithOFFLogLevelAndOFFMinimum() {
        final RequestPolicy.Options options = new RequestPolicy.Options(new MockHttpPipelineLogger(HttpPipeline.LogLevel.OFF));
        assertFalse(options.shouldLog(HttpPipeline.LogLevel.OFF));
    }

    @Test
    public void shouldLogWithOFFLogLevelAndINFOMinimum() {
        final RequestPolicy.Options options = new RequestPolicy.Options(new MockHttpPipelineLogger(HttpPipeline.LogLevel.INFO));
        assertFalse(options.shouldLog(HttpPipeline.LogLevel.OFF));
    }

    @Test
    public void shouldLogWithINFOLogLevelAndINFOMinimum() {
        final RequestPolicy.Options options = new RequestPolicy.Options(new MockHttpPipelineLogger(HttpPipeline.LogLevel.INFO));
        assertTrue(options.shouldLog(HttpPipeline.LogLevel.INFO));
    }

    @Test
    public void shouldLogWithINFOLogLevelAndWARNINGMinimum() {
        final RequestPolicy.Options options = new RequestPolicy.Options(new MockHttpPipelineLogger(HttpPipeline.LogLevel.WARNING));
        assertFalse(options.shouldLog(HttpPipeline.LogLevel.INFO));
    }

    @Test
    public void logWithNullLogger() {
        final RequestPolicy.Options options = new RequestPolicy.Options(null);
        options.log(HttpPipeline.LogLevel.INFO, "Test Log");
    }
}
