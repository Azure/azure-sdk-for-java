// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

public final class OperationName {
    // https://github.com/open-telemetry/semantic-conventions/blob/main/docs/messaging/messaging-spans.md#operation-names
    public static final OperationName CREATE = new OperationName("create");
    public static final OperationName PROCESS = new OperationName("process");
    public static final OperationName PUBLISH = new OperationName("publish", "send");
    public static final OperationName RECEIVE = new OperationName("receive");
    public static final OperationName SETTLE = new OperationName("settle", "checkpoint");

    // TODO (limolkova) we should document and standardize EventHubs operation names across languages
    // https://github.com/open-telemetry/semantic-conventions/issues/750
    public static final OperationName GET_EVENT_HUB_PROPERTIES = new OperationName("get_event_hub_properties");
    public static final OperationName GET_PARTITION_PROPERTIES = new OperationName("get_partition_properties");

    private final String attributeValue;
    private final String friendlyName;

    /**
     * Creates a new instance of OperationName.
     * @param attributeValue used in `messaging.operation` attribute. Must follow OpenTelemetry semantic conventions.
     * @param friendlyName used in span name, should match EventHubs terminology.
     */
    private OperationName(String attributeValue, String friendlyName) {
        this.attributeValue = attributeValue;
        this.friendlyName = friendlyName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    private OperationName(String attributeName) {
        this(attributeName, attributeName);
    }
}
