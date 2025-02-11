// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

/** Channel affinity for a participant. */
@Fluent
public final class ChannelAffinity {
    /*
     * Channel number to which bitstream from a particular participant will be
     * written.
     */
    private Integer channel;

    /*
     * The identifier for the participant whose bitstream will be written to
     * the channel
     * represented by the channel number.
     */
    private CommunicationIdentifier participant;

    /**
     * Creates an instance of {@link ChannelAffinity}.
     */
    public ChannelAffinity() {
    }

    /**
     * Get the channel property: Channel number to which bitstream from a particular participant will be written.
     *
     * @return the channel value.
     */
    public Integer getChannel() {
        return this.channel;
    }

    /**
     * Set the channel property: Channel number to which bitstream from a particular participant will be written.
     *
     * @param channel the channel value to set.
     * @return the ChannelAffinityInternal object itself.
     */
    public ChannelAffinity setChannel(Integer channel) {
        this.channel = channel;
        return this;
    }

    /**
     * Get the participant property: The identifier for the participant whose bitstream will be written to the channel
     * represented by the channel number.
     *
     * @return the participant value.
     */
    public CommunicationIdentifier getParticipant() {
        return this.participant;
    }

    /**
     * Set the participant property: The identifier for the participant whose bitstream will be written to the channel
     * represented by the channel number.
     *
     * @param participant the participant value to set.
     * @return the ChannelAffinityInternal object itself.
     */
    public ChannelAffinity setParticipant(CommunicationIdentifier participant) {
        this.participant = participant;
        return this;
    }
}
