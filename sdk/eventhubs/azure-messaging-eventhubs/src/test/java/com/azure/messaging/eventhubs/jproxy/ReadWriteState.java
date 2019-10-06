// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.jproxy;

import java.nio.ByteBuffer;

class ReadWriteState {
    // flag used by PROXY_CONNECTED state to decide which socketchannel to write bytes toz
    private final Boolean isClientWriter;
    private final ByteBuffer buffer;
    private volatile boolean isReading;

    /**
     * Creates a new instance.
     *
     * @param isClientWriter {@code true} if writing to the client socket. {@code false} if writing to the service
     *     socket.
     * @param buffer The buffer to write/read contents to/from.
     * @param isReading {@code true} if reading from the socket. {@code false} if we should be writing to socket.
     */
    ReadWriteState(boolean isClientWriter, ByteBuffer buffer, boolean isReading) {
        this.isClientWriter = isClientWriter;
        this.buffer = buffer;
        this.isReading = isReading;
    }

    @Override
    public String toString() {
        return String.format("ReadWriteState[%sWriter, %s]",
            isClientWriter ? "client" : "service",
            isReading ? "reading" : "writing");
    }

    boolean isClientWriter() {
        return isClientWriter;
    }

    /**
     * Gets whether the handler is reading from the client or service socket or writing to it.
     *
     * @return {@code true} if the handler is reading from the client or service socket, {@code false} otherwise.
     */
    boolean isReading() {
        return isReading;
    }

    void setIsReading(boolean isReading) {
        this.isReading = isReading;
    }

    ByteBuffer getBuffer() {
        return buffer;
    }
}
