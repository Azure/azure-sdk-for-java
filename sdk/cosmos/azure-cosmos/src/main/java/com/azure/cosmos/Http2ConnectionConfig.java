// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.util.Beta;

/***
 * Represents the http2 connection config associated with Cosmos Client in the Azure Cosmos DB database service.
 */
@Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class Http2ConnectionConfig {
    private int maxConnectionPoolSize;
    private int minConnectionPoolSize;
    private int maxConcurrentStreams;
    private boolean enabled;

    /***
     * The constructor of Http2ConnectionConfig.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig() {
        this.maxConnectionPoolSize = Configs.getHttp2MaxConnectionPoolSize(); // overlapping with the maxConnectionPoolSize in gateway connection config
        this.minConnectionPoolSize = Configs.getHttp2MinConnectionPoolSize();
        this.maxConcurrentStreams = Configs.getHttp2MaxConcurrentStreams();
        this.enabled = Configs.isHttp2Enabled();
    }

    /***
     * Get the maximum number of live connections to keep in the pool.
     *
     * @return the configured max number of live connections to keep in the pool.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    /***
     * Configures the maximum number of live connections to keep in the pool.
     * If not configured, will be default to 1000.
     *
     * @param maxConnectionPoolSize the maximum number of live connections to keep in the pool.
     * If null, the default value `1000` will be applied.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setMaxConnectionPoolSize(Integer maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize != null
            ? maxConnectionPoolSize
            : Configs.getDefaultHttpPoolSize();
        return this;
    }

    /***
     * Get the maximum number of the concurrent streams that can be opened to the remote peer.
     * @return the maximum number of the concurrent streams that can be opened to the remote peer.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }

    /***
     * Configures the maximum number of the concurrent streams that can be opened to the remote peer.
     * When evaluating how many streams can be opened to the remote peer, the minimum of this configuration and the remote peer configuration is taken (unless -1 is used).
     * Default to 30.
     *
     * @param maxConcurrentStreams the maximum number of the concurrent streams that can be opened to the remote peer.
     * If null, the default value `30` will be used.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setMaxConcurrentStreams(Integer maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams != null
            ? maxConcurrentStreams
            : Configs.getHttp2MaxConcurrentStreams();
        return this;
    }

    /***
     * Get the minimum number of live connections to keep in the pool (can be the best effort).
     * @return the minimum number of live connections to keep in the pool (can be the best effort).
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getMinConnectionPoolSize() {
        return minConnectionPoolSize;
    }

    /***
     * Configures the minimum number of live connections to keep in the pool (can be the best effort). Default to 1.
     * @param minConnectionPoolSize the minimum number of live connections to keep in the pool (can be the best effort).
     * If null, the default value `1` will be applied.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setMinConnectionPoolSize(Integer minConnectionPoolSize) {
        this.minConnectionPoolSize = minConnectionPoolSize != null
            ? minConnectionPoolSize
            : Configs.getHttp2MinConnectionPoolSize();
        return this;
    }

    /***
     * return the flag to indicate whether http2 is enabled.
     * @return the flag to indicate whether http2 is enabled.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isEnabled() {
        return enabled;
    }

    /***
     * Configure the flag to indicate whether http2 is enabled.
     * @param enabled the flag to indicate whether http2 is enabled.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String toDiagnosticsString() {
        return String.format("(enabled:%s, maxc:%s, minc:%s, maxs:%s)",
            isEnabled(),
            maxConnectionPoolSize,
            minConnectionPoolSize,
            maxConcurrentStreams);
    }
}
