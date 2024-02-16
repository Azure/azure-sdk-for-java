// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.util.ExpandableStringEnum;

public final class OperationName extends ExpandableStringEnum<OperationName> {
    // https://github.com/open-telemetry/semantic-conventions/blob/main/docs/messaging/messaging-spans.md#operation-names
    public static final OperationName PUBLISH = fromString("publish", OperationName.class);
    public static final OperationName RECEIVE = fromString("receive", OperationName.class);
    public static final OperationName PROCESS = fromString("process", OperationName.class);
    public static final OperationName CREATE = fromString("create", OperationName.class);
    public static final OperationName SETTLE = fromString("settle", OperationName.class);
    // TODO (limolkova) we should document and standardize EventHubs operation names across languages
    // https://github.com/open-telemetry/semantic-conventions/issues/750
    public static final OperationName GET_EVENT_HUB_PROPERTIES = fromString("get_event_hub_properties");
    public static final OperationName GET_PARTITION_PROPERTIES = fromString("get_partition_properties");

    @Deprecated
    public OperationName() {
    }

    public static OperationName fromString(String name) {
        return fromString(name, OperationName.class);
    }
}
