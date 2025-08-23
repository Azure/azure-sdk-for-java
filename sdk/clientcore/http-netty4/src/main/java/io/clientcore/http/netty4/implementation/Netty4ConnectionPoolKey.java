// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * A composite key for the connection pool.
 * <p>
 * For direct connections, connectionTarget and finalDestination are the same.
 * For proxied connections, connectionTarget is the proxy's address. For plain HTTP through a proxy,
 * finalDestination is also the proxy's address to allow connection reuse. For HTTPS through a proxy,
 * finalDestination is the target server's address to create a dedicated pool for the tunnel.
 */
public final class Netty4ConnectionPoolKey {
    private final SocketAddress connectionTarget;
    private final SocketAddress finalDestination;

    public Netty4ConnectionPoolKey(SocketAddress connectionTarget, SocketAddress finalDestination) {
        this.connectionTarget = connectionTarget;
        this.finalDestination = finalDestination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Netty4ConnectionPoolKey poolKey = (Netty4ConnectionPoolKey) o;
        return Objects.equals(connectionTarget, poolKey.connectionTarget)
            && Objects.equals(finalDestination, poolKey.finalDestination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionTarget, finalDestination);
    }

    public SocketAddress getConnectionTarget() {
        return this.connectionTarget;
    }

    public SocketAddress getFinalDestination() {
        return this.finalDestination;
    }
}
