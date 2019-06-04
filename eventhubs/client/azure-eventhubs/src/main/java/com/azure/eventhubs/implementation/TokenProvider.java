// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import java.io.UnsupportedEncodingException;
import java.time.Duration;

/**
 * Generates tokens that can be used to authenticate with Azure Services.
 */
public interface TokenProvider {
    /**
     * Given the audience and time to live, gets an authorization token.
     *
     * @param tokenAudience Audience for the token.
     * @param tokenTimeToLive Token's time to live.
     * @return Token to authorize with Azure services.
     * @throws UnsupportedEncodingException If the token encoding is not supported, such as UTF-8.
     */
    String getToken(String tokenAudience, Duration tokenTimeToLive) throws UnsupportedEncodingException;
}
