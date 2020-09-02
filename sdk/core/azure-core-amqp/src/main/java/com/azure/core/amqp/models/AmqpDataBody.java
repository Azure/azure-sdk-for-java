// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.IterableStream;

public class AmqpDataBody implements AmqpMessageBody {
    public AmqpDataBody(Iterable<BinaryData> data) {

    }

    @Override public AmqpBodyType getBodyType() {

    }
    public IterableStream<BinaryData> getData() {

    }
}
