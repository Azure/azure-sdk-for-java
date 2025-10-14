// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.util.Beta;

/***
 * Represents the http2 connection config associated with Cosmos Client in the Azure Cosmos DB database service.
 */
@Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class Http2ConnectionConfig {
    private Integer maxConnectionPoolSize;
    private Integer minConnectionPoolSize;
    private Integer maxConcurrentStreams;
    private Boolean enabled;

    /***
     * The constructor of Http2ConnectionConfig.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig() {
    }

    /***
     * Get the maximum number of live connections to keep in the pool.
     *
     * @return the configured max number of live connections to keep in the pool.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Integer getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    int getEffectiveMaxConnectionPoolSize() {
        Integer snapshot = maxConnectionPoolSize;
        return snapshot != null ? snapshot : Configs.getHttp2MaxConnectionPoolSize();
    }

    /***
     * Configures the maximum number of live connections to keep in the pool.
     * If not configured, will be default to 1000.
     *
     * @param maxConnectionPoolSize the maximum number of live connections to keep in the pool.
     * If null, the default value `1000` will be applied for http/2.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setMaxConnectionPoolSize(Integer maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
        return this;
    }

    /***
     * Get the maximum number of the concurrent streams that can be opened to the remote peer.
     * @return the maximum number of the concurrent streams that can be opened to the remote peer.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Integer getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }

    int getEffectiveMaxConcurrentStreams() {
        Integer snapshot = this.maxConcurrentStreams;
        return snapshot != null ? snapshot : Configs.getHttp2MaxConcurrentStreams();
    }

    /***
     * Configures the maximum number of the concurrent streams that can be opened to the remote peer.
     * When evaluating how many streams can be opened to the remote peer, the minimum of this configuration and the remote peer configuration is taken (unless -1 is used).
     * Default to 30.
     *
     * @param maxConcurrentStreams the maximum number of the concurrent streams that can be opened to the remote peer.
     * If null, the default value `30` will be applied for http/2.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setMaxConcurrentStreams(Integer maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams;
        return this;
    }

    /***
     * Get the minimum number of live connections to keep in the pool (can be the best effort).
     * @return the minimum number of live connections to keep in the pool (can be the best effort).
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Integer getMinConnectionPoolSize() {
        return minConnectionPoolSize;
    }

    int getEffectiveMinConnectionPoolSize() {
        Integer snapshot = minConnectionPoolSize;
        return snapshot != null ? snapshot : Configs.getHttp2MinConnectionPoolSize();
    }

    /***
     * Configures the minimum number of live connections to keep in the pool (can be the best effort). Default to 1.
     * @param minConnectionPoolSize the minimum number of live connections to keep in the pool (can be the best effort).
     * If null, the default value `1` will be applied for http/2.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setMinConnectionPoolSize(Integer minConnectionPoolSize) {
        this.minConnectionPoolSize = minConnectionPoolSize;

        return this;
    }

    /***
     * return the flag to indicate whether http2 is enabled.
     * @return the flag to indicate whether http2 is enabled.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Boolean isEnabled() {
        return enabled;
    }

    boolean isEffectivelyEnabled() {
        Boolean snapshot = enabled;
        return snapshot != null ? snapshot : Configs.isHttp2Enabled();
    }

    /***
     * Configure the flag to indicate whether http2 is enabled. If null, the default value (`false` while
     * in preview, `true` later) will be applied for http/2.
     * @param enabled the flag to indicate whether http2 is enabled.
     * @return the current {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    String toDiagnosticsString() {
        return String.format("(enabled:%s, maxc:%s, minc:%s, maxs:%s)",
            isEffectivelyEnabled(),
            getEffectiveMaxConnectionPoolSize(),
            getEffectiveMinConnectionPoolSize(),
            getEffectiveMaxConcurrentStreams());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.Http2ConnectionConfigHelper.setHttp2ConnectionConfigAccessor(
            new ImplementationBridgeHelpers.Http2ConnectionConfigHelper.Http2ConnectionConfigAccessor() {
                @Override
                public String toDiagnosticsString(Http2ConnectionConfig cfg) {
                    return cfg.toDiagnosticsString();
                }

                @Override
                public int getEffectiveMaxConcurrentStreams(Http2ConnectionConfig cfg) {
                    return cfg.getEffectiveMaxConcurrentStreams();
                }

                @Override
                public int getEffectiveMaxConnectionPoolSize(Http2ConnectionConfig cfg) {
                    return cfg.getEffectiveMaxConnectionPoolSize();
                }

                @Override
                public int getEffectiveMinConnectionPoolSize(Http2ConnectionConfig cfg) {
                    return cfg.getEffectiveMinConnectionPoolSize();
                }

                @Override
                public boolean isEffectivelyEnabled(Http2ConnectionConfig cfg) {
                    return cfg.isEffectivelyEnabled();
                }
            }
        );
    }

    static { initialize(); }
}
