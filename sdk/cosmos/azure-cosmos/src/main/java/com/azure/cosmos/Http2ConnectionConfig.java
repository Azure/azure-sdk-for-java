// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.util.Beta;

@Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class Http2ConnectionConfig {
    private static final int DEFAULT_MAX_CONCURRENT_STREAMS = 30;
    private static final int DEFAULT_MIN_CONNECTION_POOL_SIZE = 1;

    private int maxConnectionPoolSize;
    private int minConnectionPoolSize;
    private int maxConcurrentStreams;
    private boolean enabled;

    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig() {
        this.maxConnectionPoolSize = Configs.getDefaultHttpPoolSize(); // overlapping with the maxConnectionPoolSize in gateway connection config
        this.minConnectionPoolSize = DEFAULT_MIN_CONNECTION_POOL_SIZE;
        this.maxConcurrentStreams = DEFAULT_MAX_CONCURRENT_STREAMS;
        this.enabled = Configs.isHttp2Enabled();
    }

    /***
     * Get the maximum number of live connections to keep in the pool.
     *
     * @return the configured max number of live connections to keep in the pool.
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Integer getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    /***
     * Configures the maximum number of live connections to keep in the pool.
     * If not configured, will be default to 1000.
     *
     * @param maxConnectionPoolSize the maximum number of live connections to keep in the pool.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setMaxConnectionPoolSize(int maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
        return this;
    }

    /***
     * Get the maximum number of the concurrent streams that can be opened to the remote peer.
     * @return the maximum number of the concurrent streams that can be opened to the remote peer.
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }

    /***
     * Configures the maximum number of the concurrent streams that can be opened to the remote peer.
     * When evaluating how many streams can be opened to the remote peer, the minimum of this configuration and the remote peer configuration is taken (unless -1 is used).
     * Default to 30.
     *
     * @param maxConcurrentStreams the maximum number of the concurrent streams that can be opened to the remote peer.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setMaxConcurrentStreams(int maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams;
        return this;
    }

    /***
     * Get the minimum number of live connections to keep in the pool (can be the best effort).
     * @return the minimum number of live connections to keep in the pool (can be the best effort).
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getMinConnectionPoolSize() {
        return minConnectionPoolSize;
    }

    /***
     * Configures the minimum number of live connections to keep in the pool (can be the best effort). Default to 1.
     * @param minConnectionPoolSize the minimum number of live connections to keep in the pool (can be the best effort).
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setMinConnectionPoolSize(int minConnectionPoolSize) {
        this.minConnectionPoolSize = minConnectionPoolSize;
        return this;
    }

    /***
     * return the flag to indicate whether http2 is enabled.
     * @return the flag to indicate whether http2 is enabled.
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isEnabled() {
        return enabled;
    }

    /***
     * Configure the flag to indicate whether http2 is enabled.
     * @param enabled the flag to indicate whether http2 is enabled.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Override
    public String toString() {
        return "Http2ConnectionConfig{" +
            "isEnabled=" + enabled +
            ", maxConnectionPoolSize=" + maxConnectionPoolSize +
            ", minConnectionPoolSize=" + minConnectionPoolSize +
            ", maxConcurrentStreams=" + maxConcurrentStreams +
            '}';
    }
}
