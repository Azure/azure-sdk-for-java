// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Defines the transport type used for streaming. Note that future values may be introduced that are not currently
 * documented.
 */
public final class StreamingTransport extends ExpandableStringEnum<StreamingTransport> {
    /**
     * Static value websocket for StreamingTransportType.
     */
    public static final StreamingTransport WEBSOCKET = fromString("websocket");

    /**
     * Creates a new instance of StreamingTransportType value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public StreamingTransport() {
    }

    /**
     * Creates or finds a StreamingTransportType from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding StreamingTransportType.
     */
    public static StreamingTransport fromString(String name) {
        return fromString(name, StreamingTransport.class);
    }

    /**
     * Gets known StreamingTransportType values.
     * 
     * @return known StreamingTransportType values.
     */
    public static Collection<StreamingTransport> values() {
        return values(StreamingTransport.class);
    }
}
