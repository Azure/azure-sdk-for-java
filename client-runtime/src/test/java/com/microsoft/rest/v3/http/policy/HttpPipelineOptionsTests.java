package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpPipelineLogLevel;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
import com.microsoft.rest.v3.http.MockHttpPipelineLogger;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpPipelineOptionsTests {
    @Test
    public void shouldLogWithNullLogger() {
        final HttpPipelineOptions options = new HttpPipelineOptions(null);
        assertFalse(options.shouldLog(HttpPipelineLogLevel.INFO));
    }

    @Test
    public void shouldLogWithNullLogLevel() {
        final HttpPipelineOptions options = new HttpPipelineOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.INFO));
        assertFalse(options.shouldLog(null));
    }

    @Test
    public void shouldLogWithOFFLogLevelAndOFFMinimum() {
        final HttpPipelineOptions options = new HttpPipelineOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.OFF));
        assertFalse(options.shouldLog(HttpPipelineLogLevel.OFF));
    }

    @Test
    public void shouldLogWithOFFLogLevelAndINFOMinimum() {
        final HttpPipelineOptions options = new HttpPipelineOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.INFO));
        assertFalse(options.shouldLog(HttpPipelineLogLevel.OFF));
    }

    @Test
    public void shouldLogWithINFOLogLevelAndINFOMinimum() {
        final HttpPipelineOptions options = new HttpPipelineOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.INFO));
        assertTrue(options.shouldLog(HttpPipelineLogLevel.INFO));
    }

    @Test
    public void shouldLogWithINFOLogLevelAndWARNINGMinimum() {
        final HttpPipelineOptions options = new HttpPipelineOptions(new MockHttpPipelineLogger(HttpPipelineLogLevel.WARNING));
        assertFalse(options.shouldLog(HttpPipelineLogLevel.INFO));
    }

    @Test
    public void logWithNullLogger() {
        final HttpPipelineOptions options = new HttpPipelineOptions(null);
        options.log(HttpPipelineLogLevel.INFO, "Test Log");
    }
}
