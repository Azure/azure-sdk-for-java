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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;

public class TracerUnderTest implements Tracer {

    private final static Logger LOGGER = LoggerFactory.getLogger(TracerUnderTest.class);
    private final static ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private final ConcurrentLinkedDeque<SpanRecord> spanStack = new ConcurrentLinkedDeque();
    private SpanRecord currentSpan = null;
    SpanKind kind = SpanKind.INTERNAL;

    private Context startCore(
        String methodName,
        OffsetDateTime startTimestamp,
        Map<String, Object> attributes,
        SpanKind kind,
        Context context
    ) {
        LOGGER.info("--> start {}", methodName);
        SpanRecord parent = this.currentSpan;
        if (parent != null) {
            this.spanStack.push(parent);
            this.currentSpan = null;
        }
        assertThat(this.currentSpan).isNull();

        SpanRecord newSpan = new SpanRecord(methodName, startTimestamp, attributes, kind, context);

        if (parent != null) {
            parent.addChild(newSpan);
        }

        this.currentSpan = newSpan;

        return context;
    }

    @Override
    public synchronized Context start(String methodName, Context context) {
        return this.startCore(methodName, OffsetDateTime.now(), null, null, context);
    }

    @Override
    public synchronized Context start(String methodName, StartSpanOptions options, Context context) {
        Map<String, Object> attributes = null;
        OffsetDateTime startTimestamp = OffsetDateTime.now();
        SpanKind kind = SpanKind.INTERNAL;

        if (options != null) {
            attributes = options.getAttributes();

            if (options.getStartTimestamp() != null) {
                startTimestamp = options.getStartTimestamp().atOffset(ZoneOffset.UTC);
            }

            if (options.getSpanKind() != null) {
                kind = options.getSpanKind();
            }
        }

        return this.startCore(methodName, startTimestamp, attributes, kind, context);
    }

    @Override
    public synchronized void end(String statusMessage, Throwable error, Context context) {
        assertThat(this.currentSpan).isNotNull();
        assertThat(this.currentSpan.error).isNull();
        assertThat(this.currentSpan.statusMessage).isNull();

        assertThat(this.currentSpan.endTime).isNull();
        this.currentSpan.setEndTime(OffsetDateTime.now());

        this.currentSpan.setError(error);
        this.currentSpan.setStatusMessage(statusMessage);

        SpanRecord parent = this.spanStack.poll();
        if (parent == null) {

            if (this.currentSpan.getError() != null) {
                LOGGER.info("Span-Error: {}", this.currentSpan.getError().getMessage(), this.currentSpan.getError());
            }

            if (this.currentSpan.getStatusMessage() != null) {
                LOGGER.info("Span-StatusMessage: {}", this.currentSpan.getStatusMessage());
            }

            LOGGER.info("Span-Json: {}", this.currentSpan.toJson());
        } else {
            this.currentSpan = parent;
        }
    }

    @Override
    public synchronized void setAttribute(String key, String value, Context context) {
        assertThat(this.currentSpan).isNotNull();
        this.currentSpan.attributes.put(key, value);
    }

    @Override
    public synchronized void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp, Context context) {
        assertThat(this.currentSpan).isNotNull();
        this.currentSpan.events.add(new EventRecord(name, timestamp, attributes));
    }

    public synchronized void reset() {
        this.spanStack.clear();
        this.currentSpan = null;;
    }

    public SpanRecord getCurrentSpan() {
        return this.currentSpan;
    }

    public synchronized String toJson() {
        SpanRecord rootSpan = null;
        if (this.spanStack.isEmpty()) {
            rootSpan = this.currentSpan;
        } else {
            for(SpanRecord s: this.spanStack) {
                rootSpan = s;
            }
        }

        assertThat(rootSpan).isNotNull();

        return rootSpan.toJson();
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

    public static class SpanRecord {
        private final String name;
        private final OffsetDateTime startTime;

        private final Map<String, Object> attributes;

        private final Collection<EventRecord> events;

        private final Collection<SpanRecord> children;

        public final Context context;
        public final SpanKind spanKind;

        private OffsetDateTime endTime;

        private String statusMessage;

        private Throwable error;

        public SpanRecord(String name, OffsetDateTime startTimestamp,  Map<String, Object> attributes, SpanKind kind, Context context) {
            this.name = name;
            this.startTime = startTimestamp;
            if (attributes != null) {
                this.attributes = new ConcurrentHashMap<>(attributes);
            } else {
                this.attributes = new ConcurrentHashMap<>();
            }
            this.events = new ConcurrentLinkedQueue<>();
            this.children = new ConcurrentLinkedQueue<>();
            this.context = context;
            if (kind == null) {
                this.spanKind = SpanKind.INTERNAL;
            } else {
                this.spanKind = kind;
            }
        }

        public Context getContext() {
            return this.context;
        }

        public Collection<EventRecord> getEvents() {
            return new ArrayList<>(this.events);
        }

        public String getName() {
            return this.name;
        }

        public OffsetDateTime getStartTime() {
            return this.startTime;
        }

        public OffsetDateTime getEndTime() {
            return this.endTime;
        }

        public SpanRecord setEndTime(OffsetDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(this.attributes);
        }

        public String getStatusMessage() {
            return this.statusMessage;
        }

        public SpanRecord setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;

            return this;
        }

        public Throwable getError() {
            return this.error;
        }

        public SpanRecord setError(Throwable throwable) {
            this.error = throwable;
            return this;
        }

        public void addEvent(EventRecord eventRecord) {
            assertThat(eventRecord).isNotNull();
            this.events.add(eventRecord);
        }

        public void addChild(SpanRecord child) {
            assertThat(child).isNotNull();
            this.children.add(child);
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append(this.name)
              .append(" - ")
              .append(this.startTime)
              .append(" - ")
              .append(this.endTime)
              .append(": { '");

            for(String key: this.attributes.keySet()) {
                sb.append(key).append("' : '").append(this.attributes.get(key)).append("'");
            }

            sb.append(" }");

            return sb.toString();
        }

        public String toJson() {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            node.put("name", "dependency");
            node.put("spanName", this.name);
            node.put("kind", this.spanKind.name());
            node.put("startTime", DateTimeFormatter.ISO_INSTANT.format(this.startTime));
            if (this.endTime != null) {
                node.put("endTime", DateTimeFormatter.ISO_INSTANT.format(this.endTime));
                node.put("duration", Duration.between(this.startTime, this.endTime).toString());
            }
            node.put("statusMessage", this.statusMessage);
            if (this.error != null){
                node.put("error", this.error.toString());
            }
            for (String attributeName : this.attributes.keySet()) {
                node.put(attributeName, OBJECT_MAPPER. valueToTree(this.attributes.get(attributeName)));
            }

            if (!this.children.isEmpty()) {
                ArrayNode childrenNode = node.putArray("spans");
                for (SpanRecord child : this.children) {
                    ObjectNode childNode  = null;
                    try {
                        childNode = (ObjectNode)OBJECT_MAPPER.readTree(child.toJson());
                        childrenNode.add(childNode);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
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
    }
}