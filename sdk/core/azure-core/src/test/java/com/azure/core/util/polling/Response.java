// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.polling;

/**
 * A test response type for testing polling.
 */
public class Response {
    private final String response;

    /**
     * Creates a new TestPollResponse object.
     *
     * @param response The response string.
     */
    public Response(String response) {
        this.response = response;
    }

    /**
     * Gets the response string.
     *
     * @return The response string.
     */
    public String getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "Response: " + response;
    }
}
