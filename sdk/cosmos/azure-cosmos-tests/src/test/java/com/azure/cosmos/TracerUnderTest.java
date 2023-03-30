// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TracerUnderTest implements Tracer {

    private final static Logger LOGGER = LoggerFactory.getLogger(TracerUnderTest.class);
    private final static ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    public Map<String, Object> attributes = new HashMap<>();
    public String methodName;
    public String statusMessage;
    public Instant startTime;
    public Instant endTime;
    public Throwable error;
    public List<EventRecord> events = new ArrayList<>();
    public Context context;
    public SpanKind spanKind = SpanKind.INTERNAL;

    @Override
    public Context start(String methodName, Context context) {
        LOGGER.info("--> start {}", methodName);
        assertThat(this.methodName).isNull();
        this.methodName = methodName;
        this.startTime = Instant.now();
        return this.context = context;
    }

    @Override
    public Context start(String methodName, StartSpanOptions options, Context context) {
        Context ctx = Tracer.super.start(methodName, options, context);

        if (options != null && options.getStartTimestamp() != null) {
            this.startTime = options.getStartTimestamp();
        } else {
            this.startTime = Instant.now();
        }

        if (options != null && options.getSpanKind() != null) {
            this.spanKind = options.getSpanKind();
        } else {
            this.spanKind = SpanKind.INTERNAL;
        }

        if (options != null) {
            for (String key : options.getAttributes().keySet()) {
                this.attributes.put(key, options.getAttributes().get(key));
            }
        }

        return this.context = ctx;
    }

    @Override
    public void end(String statusMessage, Throwable error, Context context) {
        assertThat(this.error).isNull();
        assertThat(this.statusMessage).isNull();

        assertThat(this.endTime).isNull();
        this.endTime = Instant.now();

        if (error != null) {
            LOGGER.info("Span-Error: {}", error.getMessage(), error);
        }

        if (error != null) {
            LOGGER.info("Span-StatusMessage: {}", statusMessage);
        }

        LOGGER.info("Span-Json: {}", this.toJson());


        this.error = error;
        this.statusMessage = statusMessage;
        this.context = context;
    }

    @Override
    public void setAttribute(String key, String value, Context context) {
        this.attributes.put(key, value);
        this.context = context;
    }

    @Override
    public void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp, Context context) {
        this.events.add(new EventRecord(name, timestamp, attributes));
        this.context = context;
    }

    public void reset() {
        this.error = null;
        this.statusMessage = null;
        this.methodName = null;
        this.context = null;
        this.startTime = null;
        this.endTime = null;
        this.spanKind = SpanKind.INTERNAL;
        this.attributes.clear();
        this.events.clear();
    }

    public String toJson() {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("name", "dependency");
        node.put("spanName", this.methodName);
        node.put("kind", this.spanKind.name());
        node.put("startTime", DateTimeFormatter.ISO_INSTANT.format(this.startTime));
        node.put("endTime", DateTimeFormatter.ISO_INSTANT.format(this.endTime));
        node.put("duration", Duration.between(this.startTime, this.endTime).toString());
        node.put("statusMessage", this.statusMessage);
        if (this.error != null){
            node.put("error", this.error.toString());
        }
        for (String attributeName : this.attributes.keySet()) {
            node.put(attributeName, OBJECT_MAPPER. valueToTree(this.attributes.get(attributeName)));
        }

        if (!this.events.isEmpty()) {
            ArrayNode eventsNode = node.putArray("events");
            for (EventRecord event : events) {
                ObjectNode eventNode  = OBJECT_MAPPER.createObjectNode();
                eventNode.put("name", event.name);
                eventNode.put("timestamp", event.timestamp.format(DateTimeFormatter.ISO_INSTANT));
                for (String eventAttributeName : event.attributes.keySet()) {
                    eventNode.put(
                        eventAttributeName,
                        OBJECT_MAPPER. valueToTree(event.attributes.get(eventAttributeName)));
                }
                eventsNode.add(eventNode);
            }
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class EventRecord {
        private final String name;
        private final OffsetDateTime timestamp;
        private final Map<String, Object> attributes;

        public EventRecord(String name, OffsetDateTime timestamp,  Map<String, Object> attributes) {
            this.name = name;
            this.timestamp = timestamp;
            this.attributes = attributes;
        }

        public String getName() {
            return this.name;
        }

        public OffsetDateTime getTimestamp() {
            return this.timestamp;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(this.attributes);
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append(this.name)
              .append(" - ")
              .append(this.timestamp)
              .append(": { '");

            for(String key: this.attributes.keySet()) {
                sb.append(key).append("' : '").append(this.attributes.get(key)).append("'");
            }

            sb.append(" }");

            return sb.toString();
        }
    }

}