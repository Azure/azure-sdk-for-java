// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.annotation.Immutable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents the host and optional port of an endpoint selected using storage layout information.
 *
 * <p>The endpoint may be created from a {@code host[:port]} authority or an absolute HTTP(S) URL containing only an
 * authority. The URL scheme is validated but isn't retained, as data locality routing preserves the scheme of the
 * original storage request.</p>
 */
@Immutable
public final class DataLocalityEndpoint {
    private static final String INVALID_ENDPOINT_MESSAGE = "Invalid data locality endpoint. The endpoint must be a "
        + "host[:port] or an absolute HTTP(S) URL containing only an authority.";

    private final String host;
    private final Integer port;

    private DataLocalityEndpoint(String host, Integer port) {
        this.host = host.toLowerCase(Locale.ROOT);
        this.port = port;
    }

    /**
     * Creates a data locality endpoint from a host and optional port.
     *
     * @param endpoint A {@code host[:port]} authority or an absolute HTTP(S) URL containing only an authority.
     * @return The parsed data locality endpoint.
     * @throws IllegalArgumentException If {@code endpoint} isn't a valid authority.
     */
    public static DataLocalityEndpoint fromString(String endpoint) {
        if (endpoint == null || endpoint.isEmpty() || !endpoint.equals(endpoint.trim())) {
            throw new IllegalArgumentException(INVALID_ENDPOINT_MESSAGE);
        }

        URI uri;
        try {
            uri = new URI(endpoint.contains("://") ? endpoint : "https://" + endpoint);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(INVALID_ENDPOINT_MESSAGE, ex);
        }

        String scheme = uri.getScheme();
        String path = uri.getRawPath();
        int parsedPort = uri.getPort();
        if ((!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
            || uri.getHost() == null
            || uri.getRawUserInfo() != null
            || (path != null && !path.isEmpty() && !"/".equals(path))
            || uri.getRawQuery() != null
            || uri.getRawFragment() != null
            || parsedPort == 0
            || parsedPort > 65535) {
            throw new IllegalArgumentException(INVALID_ENDPOINT_MESSAGE);
        }

        return new DataLocalityEndpoint(uri.getHost(), parsedPort == -1 ? null : parsedPort);
    }

    /**
     * Gets the endpoint host.
     *
     * @return The endpoint host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the endpoint port.
     *
     * @return The endpoint port, or {@code null} if no port was specified.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Returns the endpoint as a {@code host[:port]} authority.
     *
     * @return The endpoint authority.
     */
    @Override
    public String toString() {
        String formattedHost = host.indexOf(':') >= 0 && !host.startsWith("[") ? "[" + host + "]" : host;
        return port == null ? formattedHost : formattedHost + ":" + port;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DataLocalityEndpoint)) {
            return false;
        }
        DataLocalityEndpoint that = (DataLocalityEndpoint) object;
        return host.equals(that.host) && Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
