// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

public class CosmosSourceMessageKeyConfig {
    private final boolean messageKeyEnabled;
    private final String messageKeyField;

    public CosmosSourceMessageKeyConfig(boolean messageKeyEnabled, String messageKeyField) {
        this.messageKeyEnabled = messageKeyEnabled;
        this.messageKeyField = messageKeyField;
    }

    public boolean isMessageKeyEnabled() {
        return messageKeyEnabled;
    }

    public String getMessageKeyField() {
        return messageKeyField;
    }
}
