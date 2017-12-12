package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.MockHttpPipelineLogger;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequestPolicyOptionsTests {
    @Test
    public void shouldLogWithNullLogger() {
        final RequestPolicyOptions options = new RequestPolicyOptions(null);
        assertFalse(options.shouldLog(HttpPipelineLogLevel.INFO));
    }

    @Test
    public void shouldLogWithNullLogLevel() {
        final RequestPolicyOptions options = new RequestPolicyOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.INFO));
        assertFalse(options.shouldLog(null));
    }

    @Test
    public void shouldLogWithOFFLogLevelAndOFFMinimum() {
        final RequestPolicyOptions options = new RequestPolicyOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.OFF));
        assertFalse(options.shouldLog(HttpPipelineLogLevel.OFF));
    }

    @Test
    public void shouldLogWithOFFLogLevelAndINFOMinimum() {
        final RequestPolicyOptions options = new RequestPolicyOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.INFO));
        assertFalse(options.shouldLog(HttpPipelineLogLevel.OFF));
    }

    @Test
    public void shouldLogWithINFOLogLevelAndINFOMinimum() {
        final RequestPolicyOptions options = new RequestPolicyOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.INFO));
        assertTrue(options.shouldLog(HttpPipelineLogLevel.INFO));
    }

    @Test
    public void shouldLogWithINFOLogLevelAndWARNINGMinimum() {
        final RequestPolicyOptions options = new RequestPolicyOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.WARNING));
        assertFalse(options.shouldLog(HttpPipelineLogLevel.INFO));
    }

    @Test
    public void logWithNullLogger() {
        final RequestPolicyOptions options = new RequestPolicyOptions(null);
        options.log(HttpPipelineLogLevel.INFO, "Test Log");
    }
}
