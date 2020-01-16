// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * This represents proxy configuration to be used in http clients..
 */
public class ProxyOptions {
    private final InetSocketAddress address;
    private final Type type;
    private String username;
    private String password;


    /**
     * Creates ProxyOptions.
     *
     * @param type the proxy type
     * @param address the proxy address (ip and port number)
     */
    public ProxyOptions(Type type, InetSocketAddress address) {
        this.type = type;
        this.address = address;
    }

    /**
     * Set the proxy credentials.
     *
     * @param username proxy user name
     * @param password proxy password
     * @return the updated ProxyOptions object
     */
    public ProxyOptions setCredentials(String username, String password) {
        this.username = Objects.requireNonNull(username, "'username' cannot be null.");
        this.password = Objects.requireNonNull(password, "'password' cannot be null.");
        return this;
    }

    /**
     * @return the address of the proxy.
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * @return the type of the proxy.
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the proxy user name.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @return the proxy password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * The type of the proxy.
     */
    public enum Type {
        /**
         * HTTP proxy type.
         */
        HTTP(),
        /**
         * SOCKS4 proxy type.
         */
        SOCKS4(),
        /**
         * SOCKS5 proxy type.
         */
        SOCKS5()
    }
}
