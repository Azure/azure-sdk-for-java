// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.HttpConstants.HttpMethod;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface AuthTokenCache {
    void setOrUpdateAuthToken(ExpiringAuthTokenCache.Request request, Consumer<ExpiringAuthTokenCache.Request> setTokenAction);
}
