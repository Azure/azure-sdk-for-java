package com.microsoft.azure.eventhubs;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Objects;

public class ProxyConfiguration {
    private final String proxyAddress;
    private final ProxyConfiguration.ProxyAuthType authType;
    private final PasswordAuthentication credentials;

    /**
     * Creates a proxy configuration that uses the system configured proxy and authentication.
     */
    private ProxyConfiguration() {
        this.authType = ProxyAuthType.USE_DEFAULT_AUTHENTICATOR;
        this.proxyAddress = null;
        this.credentials = null;
    }

    /**
     * Creates a proxy configuration that uses the {@code proxyAddress} and uses system configured authenticator to
     * provide a username or password if one is needed.
     *
     * @throws NullPointerException if {@code proxyAddress} is {@code null}.
     */
    public ProxyConfiguration(String proxyAddress) {
        this(proxyAddress, null, null, ProxyAuthType.USE_DEFAULT_AUTHENTICATOR);

        Objects.requireNonNull(proxyAddress);
    }

    /**
     * Creates a proxy configuration that uses the {@code proxyAddress} and authenticates with provided
     * {@code username}, {@code password} and {@code authType}.
     *
     * @param proxyAddress URL of the proxy. If {@code null} is passed in, then the system configured proxy url is used.
     * @param username Username used to authenticate with proxy. Optional if {@code authType} is
     * {@link ProxyAuthType#NONE} or {@link ProxyAuthType#USE_DEFAULT_AUTHENTICATOR}.
     * @param password Password used to authenticate with proxy. Optional if {@code authType} is
     * {@link ProxyAuthType#NONE} or {@link ProxyAuthType#USE_DEFAULT_AUTHENTICATOR}.
     * @param authType Authentication method to use with proxy.
     *
     * @throws NullPointerException if {@code authType} is {@code null}.
     * @throws IllegalArgumentException if {@code authType} is {@link ProxyAuthType#BASIC} or
     * {@link ProxyAuthType#DIGEST} and {@code username} or {@code password} are {@code null}.
     */
    public ProxyConfiguration(String proxyAddress, String username, String password, ProxyAuthType authType) {
        Objects.requireNonNull(authType);

        // If the user is authenticating with BASIC or DIGEST, they do not want to use the system-configured
        // authenticator, so we require these values.
        if (authType == ProxyAuthType.BASIC || authType == ProxyAuthType.DIGEST) {
            Objects.requireNonNull(username);
            Objects.requireNonNull(password);

            this.credentials = new PasswordAuthentication(username, password.toCharArray());
        } else {
            this.credentials = null;
        }

        this.proxyAddress = proxyAddress;
        this.authType = authType;
    }

    public static ProxyConfiguration useSystemConfiguration() {
        return new ProxyConfiguration();
    }

    String proxyAddress() {
        return proxyAddress;
    }

    PasswordAuthentication credentials() {
        return credentials;
    }

    /**
     * Gets whether the proxy address has been configured.
     *
     * @return true if the proxy url has been set, and false otherwise.
     */
    boolean isProxyAddressConfigured() {
        return proxyAddress != null && !proxyAddress.equals("");
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
