// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;

import java.util.List;
import java.util.Objects;

/**
 * The options for creating a call.
 */
@Fluent
public class StartRecordingOptions {
    /**
     * Either a {@link GroupCallLocator} or {@link ServerCallLocator} for locating the call.
     */
    private final CallLocator callLocator;

    private String recordingStateCallbackUrl;

    private RecordingChannel recordingChannel;

    private RecordingContent recordingContent;

    private RecordingFormat recordingFormat;

    private List<ChannelAffinity> channelAffinity;

    /**
     * Constructor
     *
     * @param callLocator Either a {@link GroupCallLocator} or {@link ServerCallLocator} for locating the call.
     */
    public StartRecordingOptions(CallLocator callLocator) {
        Objects.requireNonNull(callLocator, "'callLocator' cannot be null.");

        this.callLocator = callLocator;
    }

    /**
     * Get the call locator.
     *
     * @return the call locator.
     */
    public CallLocator getCallLocator() {
        return this.callLocator;
    }

    /**
     * Uri to send state change callbacks.
     *
     * @return url to send state change callbacks.
     */
    public String getRecordingStateCallbackUrl() {
        return recordingStateCallbackUrl;
    }

    /**
     * Set the recordingStateCallbackUrl
     *
     * @param recordingStateCallbackUrl to send state change callbacks.
     * @return the {@link StartRecordingOptions}
     */
    public StartRecordingOptions setRecordingStateCallbackUrl(String recordingStateCallbackUrl) {
        this.recordingStateCallbackUrl = recordingStateCallbackUrl;
        return this;
    }

    /**
     * Get the channel property.
     *
     * @return the channel property.
     */
    public RecordingChannel getRecordingChannel() {
        return recordingChannel;
    }

    /**
     * Set the channel property.
     *
     * @param recordingChannel the {@link RecordingChannel}.
     * @return the {@link StartRecordingOptions}
     */
    public StartRecordingOptions setRecordingChannel(RecordingChannel recordingChannel) {
        this.recordingChannel = recordingChannel;
        return this;
    }

    /**
     * Get the content property.
     *
     * @return the content property.
     */
    public RecordingContent getRecordingContent() {
        return recordingContent;
    }

    /**
     * Set the content property.
     *
     * @param recordingContent the {@link RecordingContent}.
     * @return the {@link StartRecordingOptions}
     */
    public StartRecordingOptions setRecordingContent(RecordingContent recordingContent) {
        this.recordingContent = recordingContent;
        return this;
    }

    /**
     * Get the recording format.
     *
     * @return the recording format.
     */
    public RecordingFormat getRecordingFormat() {
        return recordingFormat;
    }

    /**
     * Set the recording format property.
     *
     * @param recordingFormat the {@link RecordingFormat}.
     * @return the {@link StartRecordingOptions}
     */
    public StartRecordingOptions setRecordingFormat(RecordingFormat recordingFormat) {
        this.recordingFormat = recordingFormat;
        return this;
    }

    /**
     * Get the channel affinity.
     *
     * @return the channel affinity.
     */
    public List<ChannelAffinity> getChannelAffinity() {
        return channelAffinity;
    }

    /**
     * Sets the channel affinity.
     *
     * @param channelAffinity the list of {@link ChannelAffinity}.
     * @return the {@link StartRecordingOptions}
     */
    public StartRecordingOptions setChannelAffinity(List<ChannelAffinity> channelAffinity) {
        this.channelAffinity = channelAffinity;
        return this;
    }
}
