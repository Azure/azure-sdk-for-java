// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.agentserver.api.serialization.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Process-scoped, in-memory buffer of serialized SSE events keyed by response ID,
 * used to support SSE replay via {@code GET /responses/{id}?stream=true}.
 *
 * <p>
 * Each event is retained for a minimum of {@code ttl} from when it was buffered
 * (default 10 minutes). The per-event TTL means individual events may be
 * evicted independently — JSON GET is unaffected by replay-buffer eviction.
 * <p>
 * Events may be appended {@linkplain #append(String, ResponseEvent) one at a time}
 * (live, while the response is still streaming — supports mid-stream replay) or in
 * a single batch via {@linkplain #store(String, List)}. {@linkplain #initBuffer(String)}
 * registers an empty buffer so {@linkplain #hasBuffer(String)} returns {@code true}
 * even before the first event arrives — required so a replay request that races
 * the first emission is not mis-classified as "not created with stream=true".
 */
final class EventReplayStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventReplayStore.class);

    /**
     * Default per-event replay retention.
     */
    static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final Duration ttl;
    private final Map<String, List<StoredEvent>> buffers = new ConcurrentHashMap<>();

    EventReplayStore() {
        this(DEFAULT_TTL);
    }

    EventReplayStore(Duration ttl) {
        this.ttl = ttl;
    }

    /**
     * A single buffered event: its sequence number, name, JSON payload, and expiry.
     */
    record StoredEvent(long sequenceNumber, String eventName, String data, long expiresAtMillis) {
    }

    /**
     * Registers an empty replay buffer for a response. Used by the streaming
     * create path so {@link #hasBuffer(String)} reflects "was created with
     * stream=true" before any event has been appended.
     */
    void initBuffer(String responseId) {
        buffers.computeIfAbsent(responseId, k -> new CopyOnWriteArrayList<>());
    }

    /**
     * Appends a single event to the replay buffer (used for live mid-stream
     * buffering of background streaming responses). Idempotent on the buffer
     * existence — if no buffer was {@linkplain #initBuffer(String) initialised}
     * first, one is created on demand.
     */
    void append(String responseId, ResponseEvent event) {
        if (event == null) {
            return;
        }
        StoredEvent stored = toStoredEvent(event, buffers.getOrDefault(responseId, List.of()).size());
        if (stored == null) {
            return;
        }
        buffers.computeIfAbsent(responseId, k -> new CopyOnWriteArrayList<>()).add(stored);
    }

    /**
     * Replaces the buffer for {@code responseId} with the supplied events.
     * Convenience for the "buffer at terminal" pattern.
     */
    void store(String responseId, List<ResponseEvent> events) {
        if (events == null || events.isEmpty()) {
            buffers.put(responseId, new CopyOnWriteArrayList<>());
            return;
        }
        List<StoredEvent> stored = new CopyOnWriteArrayList<>();
        long index = 0;
        for (ResponseEvent event : events) {
            StoredEvent s = toStoredEvent(event, index);
            if (s != null) {
                stored.add(s);
            }
            index++;
        }
        buffers.put(responseId, stored);
    }

    private StoredEvent toStoredEvent(ResponseEvent event, long fallbackIndex) {
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        try {
            String data = mapper.writeValueAsString(event.streamEvent());
            long seq = fallbackIndex;
            JsonNode node = mapper.readTree(data);
            if (node.hasNonNull("sequence_number")) {
                seq = node.get("sequence_number").asLong(fallbackIndex);
            }
            long expiresAt = System.currentTimeMillis() + ttl.toMillis();
            return new StoredEvent(seq, event.eventName(), data, expiresAt);
        } catch (Exception e) {
            LOGGER.warn("Failed to serialize replay event {}", fallbackIndex, e);
            return null;
        }
    }

    /**
     * Returns {@code true} if a replay buffer exists for the response — i.e. it was
     * created with {@code stream=true} (even if all its events have since expired).
     */
    boolean hasBuffer(String responseId) {
        return buffers.containsKey(responseId);
    }

    /**
     * Returns the non-expired events for a response with {@code sequence_number}
     * greater than {@code startingAfter} (when provided), in order.
     */
    Optional<List<ResponseStreamReplay.ReplayEvent>> replay(String responseId, Integer startingAfter) {
        List<StoredEvent> all = buffers.get(responseId);
        if (all == null) {
            return Optional.empty();
        }
        long now = System.currentTimeMillis();
        long after = startingAfter != null ? startingAfter : Long.MIN_VALUE;
        List<ResponseStreamReplay.ReplayEvent> live = new ArrayList<>();
        for (StoredEvent e : all) {
            if (e.expiresAtMillis() <= now) {
                continue; // per-event TTL eviction
            }
            if (e.sequenceNumber() <= after) {
                continue; // starting_after cursor
            }
            live.add(new ResponseStreamReplay.ReplayEvent(e.sequenceNumber(), e.eventName(), e.data()));
        }
        return Optional.of(live);
    }

    /**
     * Removes any buffered events for a response (best-effort, on delete).
     */
    void delete(String responseId) {
        buffers.remove(responseId);
    }
}
