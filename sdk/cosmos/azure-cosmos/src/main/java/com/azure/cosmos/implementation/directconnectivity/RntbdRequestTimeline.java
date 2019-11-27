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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;

import java.time.OffsetDateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the {@link OffsetDateTime} of important events in the lifetime of a request.
 */
@JsonPropertyOrder({ "timeCreated", "timeQueued", "timeSent", "timeCompleted" })
public final class RntbdRequestTimeline {

    private final OffsetDateTime timeCreated;
    private final OffsetDateTime timeQueued;
    private final OffsetDateTime timeSent;
    private final OffsetDateTime timeCompleted;

    public RntbdRequestTimeline(final RntbdRequestRecord requestRecord) {

        checkNotNull(requestRecord, "expected non-null requestRecord");

        this.timeCreated = requestRecord.timeCreated();
        this.timeQueued = requestRecord.timeQueued();
        this.timeSent = requestRecord.timeSent();
        this.timeCompleted = requestRecord.timeCompleted();
    }

    /**
     * Returns the time that a request was completed.
     *
     * @return the time that a request was completed.
     */
    public OffsetDateTime getTimeCompleted() {
        return this.timeCompleted;
    }

    /**
     * Returns the time that a request was completed.
     *
     * @return the time that a request was completed.
     */
    public OffsetDateTime getTimeCreated() {
        return this.timeCreated;
    }

    /**
     * Returns the time that a request was queued.
     *
     * @return the time that a request was queued.
     */
    public OffsetDateTime getTimeQueued() {
        return this.timeQueued;
    }

    /**
     * Returns the time that a request was sent.
     *
     * @return the time that a request was completed.
     */
    public OffsetDateTime getTimeSent() {
        return this.timeSent;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }
}
