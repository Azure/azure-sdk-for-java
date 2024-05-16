// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.jproxy;

import java.nio.ByteBuffer;

class ReadWriteState {
    // flag used by PROXY_CONNECTED state to decide which socketchannel to write bytes toz
    private final Target writeTarget;
    private final ByteBuffer buffer;
    private volatile boolean isReading;

    /**
     * Creates a new instance.
     *
     * @param writeTarget Target socket to write to if is in a write state.
     * @param buffer The buffer to write/read contents to/from.
     * @param isReading {@code true} if reading from the socket. {@code false} if we should be writing to socket.
     */
    ReadWriteState(Target writeTarget, ByteBuffer buffer, boolean isReading) {
        this.writeTarget = writeTarget;
        this.buffer = buffer;
        this.isReading = isReading;
    }

    @Override
    public String toString() {
        final String writer;
        switch (writeTarget) {
            case CLIENT:
                writer = "ClientWriter";
                break;
            case SERVICE:
                writer = "ServiceWriter";
                break;
            default:
                writer = "";
                break;
        }

        return String.format("ReadWriteState[%s, %s]", writer, isReading ? "reading" : "writing");
    }

    /**
     * Gets the target socket to write to.
     *
     * @return The target socket to write to.
     */
    Target getWriteTarget() {
        return writeTarget;
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

    /**
     * Indicates which socket to target when performing a read or write.
     */
    enum Target {
        CLIENT,
        SERVICE,
    }
}
