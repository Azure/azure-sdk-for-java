package com.microsoft.azure.eventhubs;

import java.net.Authenticator;
import java.util.Objects;

public class ProxyConfiguration {
    private final String proxyAddress;
    private final String username;
    private final char[] password;
    private final ProxyConfiguration.ProxyAuthType authType;

    /**
     * Creates a proxy configuration that uses the system configured proxy and authentication.
     */
    private ProxyConfiguration() {
        this.authType = ProxyAuthType.USE_DEFAULT_AUTHENTICATOR;
        this.proxyAddress = null;
        this.username = null;
        this.password = null;
    }

    /**
     * Creates a proxy configuration that uses the {@code proxyAddress} and requires no authentication.
     * {@link ProxyAuthType#NONE} is used.
     *
     * @throws NullPointerException if {@code proxyAddress} is {@code null}.
     */
    public ProxyConfiguration(String proxyAddress) {
        this(proxyAddress, null, null, ProxyAuthType.NONE);
    }

    /**
     * Creates a proxy configuration that uses the {@code proxyAddress} and authenticates with provided
     * {@code username}, {@code password} and {@code authType}.
     *
     * @param proxyAddress Required. URL of the proxy
     * @param username Username used to authenticate with proxy. Optional if {@code authType} is
     * {@link ProxyAuthType#NONE} or {@link ProxyAuthType#USE_DEFAULT_AUTHENTICATOR}.
     * @param password Password used to authenticate with proxy. Optional if {@code authType} is
     * {@link ProxyAuthType#NONE} or {@link ProxyAuthType#USE_DEFAULT_AUTHENTICATOR}.
     * @param authType Authentication method to use with proxy.
     *
     * @throws NullPointerException if {@code proxyAddress} or {@code authType} is {@code null}.
     * @throws IllegalArgumentException if {@code authType} is {@link ProxyAuthType#BASIC} or
     * {@link ProxyAuthType#DIGEST} and {@code username} or {@code password} are {@code null}.
     */
    public ProxyConfiguration(String proxyAddress, String username, String password, ProxyAuthType authType) {
        Objects.requireNonNull(proxyAddress);
        Objects.requireNonNull(authType);

        if (authType == ProxyAuthType.BASIC || authType == ProxyAuthType.DIGEST) {
            Objects.requireNonNull(username);
            Objects.requireNonNull(password);
        }

        this.proxyAddress = proxyAddress;
        this.username = username;
        this.authType = authType;
        this.password = password != null
            ? password.toCharArray()
            : new char[0];
    }

    public static ProxyConfiguration useSystemConfiguration() {
        return new ProxyConfiguration();
    }

    String proxyAddress() {
        return proxyAddress;
    }

    String username() {
        return username;
    }

    /**
     * Supported methods of proxy authentication.
     */
    public enum ProxyAuthType {
        /**
         * Proxy requires no authentication. Service calls will fail if proxy demands authentication.
         */
        NONE,
        /**
         * Authenticates against proxy with provided {@code username} and {@code password}.
         */
        BASIC,
        /**
         * Authenticates against proxy with digest access authentication.
         */
        DIGEST,
        /**
         * Authenticates against proxy with {@link Authenticator}.
         */
        USE_DEFAULT_AUTHENTICATOR,
    }
}
