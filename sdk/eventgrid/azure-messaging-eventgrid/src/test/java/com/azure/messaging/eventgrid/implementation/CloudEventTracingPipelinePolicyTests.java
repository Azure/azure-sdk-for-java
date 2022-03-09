// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.implementation;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class CloudEventTracingPipelinePolicyTests {
    private HttpRequest httpRequest;

    @BeforeEach
    public void setup() throws MalformedURLException {
        httpRequest = new HttpRequest(HttpMethod.POST, new URL("https://something.com"));
    }

    @Test
    void processBodyWithNoHeader() {
        String testBodyString = "[{\"id\":\"313ac785-2dca-467e-a6a7-623f1baa2890\",\"source\":\"source\","
            + "\"type\":\"json\",\"specversion\":\"1.0\",\"tracestate\":\"TS-14b6b15b-74b6-4178-847e-d142aa2727b2\","
            + "\"traceparent\":\"TP-14b6b15b-74b6-4178-847e-d142aa2727b2\"}]";
        String expectedNewBody = "[{\"id\":\"313ac785-2dca-467e-a6a7-623f1baa2890\",\"source\":\"source\","
            + "\"type\":\"json\",\"specversion\":\"1.0\"}]";

        httpRequest.setBody(testBodyString);
        httpRequest.setHeader(Constants.CONTENT_LENGTH, testBodyString);
        String newBody = CloudEventTracingPipelinePolicy.replaceTracingPlaceHolder(
            httpRequest, new StringBuilder(testBodyString));
        Assertions.assertEquals(expectedNewBody, newBody);
        Assertions.assertEquals(httpRequest.getHeaders().get(Constants.CONTENT_LENGTH).getValue(),
            String.valueOf(newBody.length()));
    }

    @Test
    void processBodyWithTraceParentHeader() {
        httpRequest.setHeader(Constants.TRACE_PARENT, "aTraceParent");
        String testBodyString = "[{\"id\":\"313ac785-2dca-467e-a6a7-623f1baa2890\",\"source\":\"source\","
            + "\"type\":\"json\",\"specversion\":\"1.0\",\"tracestate\":\"TS-14b6b15b-74b6-4178-847e-d142aa2727b2\","
            + "\"traceparent\":\"TP-14b6b15b-74b6-4178-847e-d142aa2727b2\"}]";
        String expectedNewBody = "[{\"id\":\"313ac785-2dca-467e-a6a7-623f1baa2890\",\"source\":\"source\","
            + "\"type\":\"json\",\"specversion\":\"1.0\","
            + "\"traceparent\":\"aTraceParent\"}]";
        httpRequest.setBody(testBodyString);
        httpRequest.setHeader(Constants.CONTENT_LENGTH, testBodyString);
        String newBody = CloudEventTracingPipelinePolicy.replaceTracingPlaceHolder(
            httpRequest, new StringBuilder(testBodyString));
        Assertions.assertEquals(expectedNewBody, newBody);
        Assertions.assertEquals(httpRequest.getHeaders().get(Constants.CONTENT_LENGTH).getValue(),
            String.valueOf(newBody.length()));
    }

    @Test
    void processBodyWithTraceStateHeader() {
        httpRequest.setHeader(Constants.TRACE_STATE, "aTraceState");
        String testBodyString = "[{\"id\":\"313ac785-2dca-467e-a6a7-623f1baa2890\",\"source\":\"source\","
            + "\"type\":\"json\",\"specversion\":\"1.0\",\"tracestate\":\"TS-14b6b15b-74b6-4178-847e-d142aa2727b2\","
            + "\"traceparent\":\"TP-14b6b15b-74b6-4178-847e-d142aa2727b2\"}]";
        String expectedNewBody = "[{\"id\":\"313ac785-2dca-467e-a6a7-623f1baa2890\",\"source\":\"source\","
            + "\"type\":\"json\",\"specversion\":\"1.0\","
            + "\"tracestate\":\"aTraceState\"}]";

        httpRequest.setBody(testBodyString);
        httpRequest.setHeader(Constants.CONTENT_LENGTH, testBodyString);
        String newBody = CloudEventTracingPipelinePolicy.replaceTracingPlaceHolder(
            httpRequest, new StringBuilder(testBodyString));
        Assertions.assertEquals(expectedNewBody, newBody);
        Assertions.assertEquals(httpRequest.getHeaders().get(Constants.CONTENT_LENGTH).getValue(),
            String.valueOf(newBody.length()));
    }
}
