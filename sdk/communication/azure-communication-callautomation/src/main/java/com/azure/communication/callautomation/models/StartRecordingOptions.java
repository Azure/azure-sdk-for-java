// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.List;
import java.util.Objects;

/**
 * The options for creating a call.
 */
@Fluent
public final class StartRecordingOptions {
    /**
     * Either a {@link GroupCallLocator} or {@link ServerCallLocator} for locating the call.
     */
    private final CallLocator callLocator;

    private String recordingStateCallbackUrl;

    private RecordingChannel recordingChannel;

    private RecordingContent recordingContent;

    private RecordingFormat recordingFormat;

    private Boolean pauseOnStart;

    private List<CommunicationIdentifier> audioChannelParticipantOrdering;

    private List<ChannelAffinity> channelAffinity;

    private RecordingStorage recordingStorage;

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
     * Get pause on start.
     *
     * @return pause on start.
     */
    public Boolean getPauseOnStart() {
        return pauseOnStart;
    }

    /**
     * Set the pause on start property.
     *
     * @param pauseOnStart indicate if the recording should be paused on start.
     * @return the {@link StartRecordingOptions}
     */
    public StartRecordingOptions setPauseOnStart(Boolean pauseOnStart) {
        this.pauseOnStart = pauseOnStart;
        return this;
    }

    /**
     * Get the audioChannelParticipantOrdering property: The sequential order in which audio channels are assigned to
     * participants in the unmixed recording. When 'recordingChannelType' is set to 'unmixed' and
     * `audioChannelParticipantOrdering is not specified, the audio channel to participant mapping will be automatically
     * assigned based on the order in which participant first audio was detected. Channel to participant mapping details
     * can be found in the metadata of the recording.
     *
     * @return the audioChannelParticipantOrdering value.
     */
    public List<CommunicationIdentifier> getAudioChannelParticipantOrdering() {
        return audioChannelParticipantOrdering;
    }

    /**
     * Set the audioChannelParticipantOrdering property: The sequential order in which audio channels are assigned to
     * participants in the unmixed recording. When 'recordingChannelType' is set to 'unmixed' and
     * `audioChannelParticipantOrdering is not specified, the audio channel to participant mapping will be automatically
     * assigned based on the order in which participant first audio was detected. Channel to participant mapping details
     * can be found in the metadata of the recording.
     *
     * @param audioChannelParticipantOrdering the list of {@link CommunicationIdentifier}.
     * @return the {@link StartRecordingOptions}
     */
    public StartRecordingOptions setAudioChannelParticipantOrdering(List<CommunicationIdentifier> audioChannelParticipantOrdering) {
        this.audioChannelParticipantOrdering = audioChannelParticipantOrdering;
        return this;
    }

    /**
     * Get the externalStorage property: Used to specify external storage for call recording
     *
     * @return the externalStorage value.
     */
    public RecordingStorage getRecordingStorage() {
        return this.recordingStorage;
    }

    /**
     * Set the externalStorage property: Used to specify external storage for call recording
     *
     * @param recordingStorage the external storage for call recording
     * @return the StartRecordingOptions object itself.
     */
    public StartRecordingOptions setRecordingStorage(RecordingStorage recordingStorage) {
        this.recordingStorage = recordingStorage;
        return this;
    }

    /**
     * Get the channelAffinity property: The channel affinity of call recording When 'recordingChannelType' is set to
     * 'unmixed', if channelAffinity is not specified, 'channel' will be automatically assigned. Channel-Participant
     * mapping details can be found in the metadata of the recording. ///.
     *
     * @return the channelAffinity value.
     */
    public List<ChannelAffinity> getChannelAffinity() {
        return this.channelAffinity;
    }

    /**
     * Set the channelAffinity property: The channel affinity of call recording When 'recordingChannelType' is set to
     * 'unmixed', if channelAffinity is not specified, 'channel' will be automatically assigned. Channel-Participant
     * mapping details can be found in the metadata of the recording. ///.
     *
     * @param channelAffinity the channelAffinity value to set.
     * @return the StartRecordingOptions object itself.
     */
    public StartRecordingOptions setChannelAffinity(List<ChannelAffinity> channelAffinity) {
        this.channelAffinity = channelAffinity;
        return this;
    }
}
