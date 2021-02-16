// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents the startTimeUTC and duration of important events in the lifetime of a request.
 * <p>
 * A {@link RequestTimeline} represents a timeline as a sequence of {@link Event} instances with name, startTimeUTC, and
 * duration properties. Hence, one might use this class to represent any timeline. Today we use it to represent
 * request timelines for:
 * <p><ul>
 * <li>{@link com.azure.cosmos.implementation.http.HttpClient#send(HttpRequest, Duration)},
 * <li>{@link com.azure.cosmos.implementation.directconnectivity.HttpTransportClient#invokeStoreAsync}, and
 * <li>{@link com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient#invokeStoreAsync}.
 * </ul></p>
 * A {@link RequestTimeline} serializes to JSON as an array of {@link Event} instances. This is the default
 * serialization for any class that implements {@link Iterable}.
 * <p>
 * <b>Example:</b>
 * <pre>{@code OffsetDateTime startTimeUTC = OffsetDateTime.parse("2020-01-07T11:24:12.842749-08:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
 * sys.out.println(RequestTimeline.of(
 *     new RequestTimeline.Event("foo", startTimeUTC, startTimeUTC.plusSeconds(1)),
 *     new RequestTimeline.Event("bar", startTimeUTC.plusSeconds(1), startTimeUTC.plusSeconds(2))));}</pre>
 * JSON serialization:
 * <pre>{@code [{"name":"foo","startTimeUTC":"2020-01-07T11:24:12.842749-08:00","duration":"PT1S"},{"name":"bar","startTimeUTC":"2020-01-07T11:24:13.842749-08:00","duration":"PT1S"}])}</pre>
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

    /**
     * Returns an empty {@link RequestTimeline}.
     *
     * The empty startTimeUTC line returned is static.
     *
     * @return an empty {@link RequestTimeline}.
     */
    public static RequestTimeline empty() {
        return EMPTY;
    }

    /**
     * Returns an iterator for enumerating the {@link Event} instances in this {@link RequestTimeline}.
     *
     * @return an iterator for enumerating the {@link Event} instances in this {@link RequestTimeline}.
     */
    @Override
    public Iterator<Event> iterator() {
        return this.events.iterator();
    }

    /**
     * Returns an empty {@link RequestTimeline}.
     *
     * The empty startTimeUTC line returned is static and equivalent to calling {@link RequestTimeline#empty}.
     *
     * @return an empty request timeline.
     */
    public static RequestTimeline of() {
        return EMPTY;
    }

    /**
     * Returns a new {@link RequestTimeline} with a single event.
     *
     * @return a new {@link RequestTimeline} with a single event.
     */
    public static RequestTimeline of(final Event event) {
        return new RequestTimeline(ImmutableList.of(event));
    }

    /**
     * Returns a new {@link RequestTimeline} with a pair of events.
     *
     * @return a new {@link RequestTimeline} with a pair of events.
     */
    public static RequestTimeline of(final Event e1, final Event e2) {
        return new RequestTimeline(ImmutableList.of(e1, e2));
    }

    /**
     * Returns a new {@link RequestTimeline} with three events.
     *
     * @return a new {@link RequestTimeline} with three events.
     */
    public static RequestTimeline of(final Event e1, final Event e2, final Event e3) {
        return new RequestTimeline(ImmutableList.of(e1, e2, e3));
    }

    /**
     * Returns a new {@link RequestTimeline} with four events.
     *
     * @return a new {@link RequestTimeline} with four events.
     */
    public static RequestTimeline of(final Event e1, final Event e2, final Event e3, final Event e4) {
        return new RequestTimeline(ImmutableList.of(e1, e2, e3, e4));
    }

    /**
     * Returns a new {@link RequestTimeline} with five events.
     *
     * @return a new {@link RequestTimeline} with five events.
     */
    public static RequestTimeline of(final Event e1, final Event e2, final Event e3, final Event e4, final Event e5) {
        return new RequestTimeline(ImmutableList.of(e1, e2, e3, e4, e5));
    }

    /**
     * Returns a new {@link RequestTimeline} with an arbitrary number of events.
     *
     * @return a new {@link RequestTimeline} with an arbitrary number of events.
     */
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

    @JsonPropertyOrder({ "name", "startTimeUTC", "durationInMicroSec" })
    public static final class Event {

        @JsonIgnore
        private final Duration duration;

        @JsonProperty
        private final long durationInMicroSec;

        @JsonProperty("eventName")
        private final String name;

        @JsonSerialize(using = ToStringSerializer.class)
        @JsonProperty("startTimeUTC")
        private final Instant startTime;

        public Event(final String name, final Instant from, final Instant to) {

            checkNotNull(name, "expected non-null name");

            this.name = name;
            this.startTime = from;

            if (from == null) {
                this.duration = null;
            } else if (to == null) {
                this.duration = Duration.ZERO;
            } else {
                this.duration = Duration.between(from, to);
            }

            if (duration != null) {
                this.durationInMicroSec = duration.toNanos()/1000L;
            } else {
                this.durationInMicroSec = 0;
            }
        }

        public Duration getDuration() {
            return this.duration;
        }

        public String getName() {
            return name;
        }

        public Instant getStartTime() {
            return startTime;
        }
    }
}
