// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.models.ErrorContext;

import java.util.function.Consumer;

public class TestEventHubsErrorContextConsumer implements Consumer<ErrorContext> {

    @Override
    public void accept(ErrorContext errorContext) {

    }

}
