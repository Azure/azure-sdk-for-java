// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.Optional;

public class ConditionalAccessResponseErrorHandlerTest {
    private ResponseErrorHandler azureHandler = new ConditionalAccessResponseErrorHandler();
    private ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(("{\n"
        + "    \"error\": \"fake_error\",\n"
        + "    \"error_description\": \" fake_error_description\",\n"
        + "    \"error_codes\": [\n"
        + "        53001\n"
        + "    ],\n"
        + "    \"timestamp\": \"fake_timestamp\",\n"
        + "    \"trace_id\": \"fake_trace_id\",\n"
        + "    \"correlation_id\": \"fake_correlation_id\",\n"
        + "    \"error_uri\": \"fake_error_uri\",\n"
        + "    \"suberror\": \"message_only\",\n"
        + "    \"claims\": \"{\\\"access_token\\\":{\\\"fake_token\\\":{\\\"essential\\\":true,"
        + "\\\"values\\\":[\\\"fake_values\\\"]}}}\"\n"
        + "}").getBytes(), HttpStatus.BAD_REQUEST);

    @Test
    public void azureResponseErrorHandleTest() throws IOException {
        AADOAuth2Error error = null;
        try {
            azureHandler.handleError(clientHttpResponse);
        } catch (OAuth2AuthorizationException exception) {
            error = (AADOAuth2Error) Optional.of(exception)
                                             .map(OAuth2AuthorizationException::getError).orElse(null);
        }
        Assert.assertNotNull(error);
    }

    @Test
    public void defaultErrorHandlerTest() throws IOException {
        clientHttpResponse = new MockClientHttpResponse("".getBytes(), HttpStatus.UNAUTHORIZED);
        try {
            azureHandler.handleError(clientHttpResponse);
        } catch (HttpClientErrorException exception) {
            Assert.assertNotNull(exception);
        }
    }
}
