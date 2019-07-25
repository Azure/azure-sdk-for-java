// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface ITokenProvider {
    CompletableFuture<SecurityToken> getToken(String resource, Duration timeout);
}
