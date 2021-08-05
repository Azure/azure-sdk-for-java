// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdReporter.reportIssueUnless;

public class RntbdChannelAcquisitionContext {
    private static final Logger logger = LoggerFactory.getLogger(RntbdChannelAcquisitionContext.class);
    /**
     * Track each time the request being added to pending acquisition queue.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private final List<Instant> addedToPendingQueueTimestamps;

    /**
     * Track each time the request try to acquire channel.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private final List<Instant> attemptedToAcquireChannelTimestamps;

    /**
     * Track each time the request try to establish a new channel.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private final List<Pair<Instant, Instant>> attemptedToCreateNewChannelTimestamps;

    /**
     * Before establish a new connection to handle a request, we will first try to find an existing "GOOD" connection to handle the request.
     * This will track the channel states for all the channels evaluated in a single loop.
     * "GOOD" usually means the channel is open, has completed RntbdContext Negotiation and has not reach pending requests limit.
     */
    private final List<List<RntbdChannelState>> channelStates;

    public RntbdChannelAcquisitionContext() {
        this.addedToPendingQueueTimestamps = new ArrayList<>();
        this.attemptedToAcquireChannelTimestamps = new ArrayList<>();
        this.channelStates = new ArrayList<>();
        this.attemptedToCreateNewChannelTimestamps = new ArrayList<>();
    }

    public List<Instant> getAddedToPendingQueueTimestamps() {
        return this.addedToPendingQueueTimestamps;
    }

    public List<Instant> getAttemptedToAcquireChannelTimestamps() {
        return this.attemptedToAcquireChannelTimestamps;
    }

    public List<List<RntbdChannelState>> getChannelStates() {
        return this.channelStates;
    }

    public List<Pair<Instant, Instant>> getAttemptedToCreateNewChannelTimestamps() {
        return attemptedToCreateNewChannelTimestamps;
    }

    public static void recordAcquireChannelTime(RntbdChannelAcquisitionContext context) {
        if (context != null) {
            context.getAttemptedToAcquireChannelTimestamps().add(Instant.now());
        }
    }

    public static void startNewPollChannelCycle(RntbdChannelAcquisitionContext context) {
        if (context != null) {
            context.getChannelStates().add(new ArrayList<RntbdChannelState>());
        }
    }

    public static void recordChannelState(RntbdChannelAcquisitionContext context, RntbdChannelState state) {
        if (context != null) {
            context.getChannelStates().get(context.getChannelStates().size()-1)
                .add(state);
        }
    }

    public static void recordPendingAcquisitionTime(RntbdChannelAcquisitionContext context) {
        if (context != null) {
            context.getAddedToPendingQueueTimestamps().add(Instant.now());
        }
    }

    public static void recordNewChannelStartTime(RntbdChannelAcquisitionContext context) {
        if (context != null) {
            context.getAttemptedToCreateNewChannelTimestamps().add(Pair.of(Instant.now(), null));
        }
    }

    public static void recordNewChannelCompleteTime(RntbdChannelAcquisitionContext context) {
        if (context != null) {
            Pair<Instant, Instant> lastPair = context.getAttemptedToCreateNewChannelTimestamps().get(context.getAttemptedToCreateNewChannelTimestamps().size()-1);
            reportIssueUnless(logger, lastPair.getRight() == null, lastPair, "Mismatch new channel timestamp pair");
            context.getAttemptedToCreateNewChannelTimestamps().remove(lastPair);
            context.getAttemptedToCreateNewChannelTimestamps().add(Pair.of(lastPair.getLeft(), Instant.now()));
        }
    }
}
