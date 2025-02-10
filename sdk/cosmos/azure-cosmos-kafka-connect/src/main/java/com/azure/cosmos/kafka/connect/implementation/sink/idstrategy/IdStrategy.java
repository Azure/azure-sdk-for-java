// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idstrategy;

import org.apache.kafka.common.Configurable;
import org.apache.kafka.connect.sink.SinkRecord;

public interface IdStrategy extends Configurable {
    String generateId(SinkRecord record);
}
