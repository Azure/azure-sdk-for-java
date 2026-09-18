// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/** A server event whose complete JSON payload is retained for forward compatibility. */
@Beta(warningText = "Preview API. VoiceAgents=V1Preview")
public final class RawRealtimeServerEvent extends RealtimeServerEvent {
    private final BinaryData rawEvent;
    private final RealtimeServerEventType type;

    /**
     * Creates an event from a JSON object.
     * @param rawEvent the complete event payload.
     */
    public RawRealtimeServerEvent(BinaryData rawEvent) {
        this.rawEvent
            = BinaryData.fromString(Objects.requireNonNull(rawEvent, "'rawEvent' cannot be null.").toString());
        Object value = this.rawEvent.toObject(Map.class).get("type");
        this.type = value instanceof String ? RealtimeServerEventType.fromString((String) value) : null;
    }

    /**
     * Gets the complete event, including fields unknown to this SDK.
     * @return the JSON payload.
     */
    public BinaryData getRawEvent() {
        return rawEvent;
    }

    @Override
    public RealtimeServerEventType getType() {
        return type;
    }

    @Override
    public JsonWriter toJson(JsonWriter writer) throws IOException {
        return writer.writeRawValue(rawEvent.toString());
    }

    /**
     * Reads a raw event without discarding unknown properties.
     * @param reader the JSON reader.
     * @return the event, or null for JSON null.
     * @throws IOException if the JSON cannot be read.
     */
    public static RawRealtimeServerEvent fromJson(JsonReader reader) throws IOException {
        Object payload = reader.readUntyped();
        return payload == null ? null : new RawRealtimeServerEvent(BinaryData.fromObject(payload));
    }
}
