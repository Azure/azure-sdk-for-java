// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for creating a call.
 */
@Fluent
public class StartRecordingOptions {
    /**
     * The channel property.
     */
    private final RecordingChannel channel;

    /**
     * The content property.
     */
    private final RecordingContent content;

    /**
     * The recording format.
     */
    private final RecordingFormat format;

    /**
     * Constructor
     *
     * @param content The source property.
     * @param format The targets of the call.
     * @param channel The call back URI.
     */
    public StartRecordingOptions(RecordingContent content, RecordingFormat format, RecordingChannel channel) {
        this.channel = channel;
        this.content = content;
        this.format = format;
    }

    /**
     * Get the channel.
     *
     * @return the channel value.
     */
    public RecordingChannel getRecordingChannel() {
        return channel;
    }

    /**
     * Get the content.
     *
     * @return the content value.
     */
    public RecordingContent getRecordingContent() {
        return content;
    }

    /**
     * Get the format.
     *
     * @return the format value.
     */
    public RecordingFormat getRecordingFormat() {
        return format;
    }
}
