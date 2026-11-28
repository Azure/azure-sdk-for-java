// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * Supplies Microsoft Entra ID access tokens for the {@code AccessTokenProvider} auth type.
 * <p>
 * Use this when tokens are issued by an external system, such as a managed workload identity, and the connector
 * cannot acquire them itself. Configure the implementation with
 * {@code azure.cosmos.auth.type=AccessTokenProvider} and
 * {@code azure.cosmos.auth.accessTokenProvider.class=<fully qualified class name>}.
 * <p>
 * The connector creates one provider per Cosmos client, calls {@link #configure(Map)} once with all connector
 * configurations, and calls {@link #getAccessToken(List)} whenever the Cosmos client needs a new or refreshed token.
 * Implementations must therefore return a token that is valid at the time of the call, must be thread-safe, and
 * must have a public no-argument constructor. {@link #close()} is called when the Cosmos client is closed.
 * <p>
 * This interface only uses JDK types so that it can be implemented outside of the shaded connector package.
 */
public interface CosmosAccessTokenProvider extends Closeable {
    /**
     * Configures the provider.
     *
     * @param configs all connector configurations, including any provider-specific settings.
     */
    void configure(Map<String, ?> configs);

    /**
     * Gets an access token for the requested scopes, for example {@code https://cosmos.azure.com/.default}.
     *
     * @param scopes the scopes requested by the Cosmos client.
     * @return a valid access token. Must not be null.
     */
    CosmosAccessToken getAccessToken(List<String> scopes);

    /**
     * Releases resources held by the provider. The default implementation does nothing.
     */
    @Override
    default void close() {
    }
}
