// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.collect.ImmutableList;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the time and duration of important events in the lifetime of a request.
 *
 * A {@link RequestTimeline} represents a timeline as a sequence of {@link Event} instances with name, time, and
 * duration properties. One might use this class to represent any timeline. Today we use it to represent
 * {@link RntbdTransportClient} request timelines. In the future it might also be used to represent
 * {@link HttpTransportClient} request timelines.
 *
 * A {@link RequestTimeline} serializes to JSON as an array of {@link Event} instances. This is the default
 * serialization for any class that implements {@link Iterable}.
 * <p>
 * <b>Example:</b>
 * <p>
 * <pre>{@code OffsetDateTime startTime = OffsetDateTime.parse("2020-01-07T11:24:12.842749-08:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
 * sys.out.println(RequestTimeline.of(
 *     new RequestTimeline.Event("foo", startTime, startTime.plusSeconds(1)),
 *     new RequestTimeline.Event("bar", startTime.plusSeconds(1), startTime.plusSeconds(2))));}</pre>
 * JSON serialization:<pre>{@code [{"name":"foo","time":"2020-01-07T11:24:12.842749-08:00","duration":"PT1S"},{"name":"bar","time":"2020-01-07T11:24:13.842749-08:00","duration":"PT1S"}])}</pre>
 */
public final class RequestTimeline implements Iterable<RequestTimeline.Event> {

    private static final RequestTimeline EMPTY = new RequestTimeline();
    private final ImmutableList<Event> events;

    private RequestTimeline() {
        this.events = ImmutableList.of();
    }

    private RequestTimeline(final ImmutableList<Event> events) {
        checkNotNull(events, "expected non-null events");
        this.events = events;
    }

    public static RequestTimeline empty() {
        return EMPTY;
    }

    @Override
    public Iterator<Event> iterator() {
        return this.events.iterator();
    }

    public static RequestTimeline of() {
        return EMPTY;
    }

    public static RequestTimeline of(final Event event) {
        return new RequestTimeline(ImmutableList.of(event));
    }

    public static RequestTimeline of(final Event e1, final Event e2) {
        return new RequestTimeline(ImmutableList.of(e1, e2));
    }

    public static RequestTimeline of(final Event e1, final Event e2, final Event e3) {
        return new RequestTimeline(ImmutableList.of(e1, e2, e3));
    }

    public static RequestTimeline of(final Event e1, final Event e2, final Event e3, final Event e4) {
        return new RequestTimeline(ImmutableList.of(e1, e2, e3, e4));
    }

    public static RequestTimeline of(final Event e1, final Event e2, final Event e3, final Event e4, final Event e5) {
        return new RequestTimeline(ImmutableList.of(e1, e2, e3, e4, e5));
    }

    public static RequestTimeline of(final Event... events) {
        return new RequestTimeline(ImmutableList.copyOf(events));
    }

    /**
     * Returns a textual representation of this {@link RequestTimeline}.
     * <p>
     * The textual representation returned is a string of the form {@code RequestTimeline(}<i> &lt;event-array&gt;</i>
     * {@code )}.
     */
    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    @JsonPropertyOrder({ "name", "time", "duration" })
    public static final class Event {

        private final Duration duration;
        private final String name;
        private final OffsetDateTime time;

        public Event(final String name, final OffsetDateTime from, final OffsetDateTime to) {

            checkNotNull(name, "expected non-null name");

            this.name = name;
            this.time = from;

            this.duration = from == null ? null : to == null ? Duration.ZERO : Duration.between(from, to);
        }

        @JsonSerialize(using = ToStringSerializer.class)
        public Duration getDuration() {
            return this.duration;
        }

        public String getName() {
            return name;
        }

        @JsonSerialize(using = ToStringSerializer.class)
        public OffsetDateTime getTime() {
            return time;
        }
    }
}
