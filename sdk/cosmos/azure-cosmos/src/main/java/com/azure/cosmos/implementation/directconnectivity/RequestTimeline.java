/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
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

    public static RequestTimeline from(RntbdRequestRecord requestRecord) {

        checkNotNull(requestRecord, "expected non-null requestRecord");

        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime timeCreated = requestRecord.timeCreated();
        OffsetDateTime timeQueued = requestRecord.timeQueued();
        OffsetDateTime timePipelined = requestRecord.timePipelined();
        OffsetDateTime timeSent = requestRecord.timeSent();
        OffsetDateTime timeCompleted = requestRecord.timeCompleted();
        OffsetDateTime timeCompletedOrNow = timeCompleted == null ? now : timeCompleted;

        return RequestTimeline.of(
            new Event("created",
                timeCreated, timeQueued == null ? timeCompletedOrNow : timeQueued),
            new Event("queued",
                timeQueued, timePipelined == null ? timeCompletedOrNow : timePipelined),
            new Event("pipelined",
                timePipelined, timeSent == null ? timeCompletedOrNow : timeSent),
            new Event("sent",
                timeSent, timeCompletedOrNow),
            new Event("completed",
                timeCompleted, now));
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
