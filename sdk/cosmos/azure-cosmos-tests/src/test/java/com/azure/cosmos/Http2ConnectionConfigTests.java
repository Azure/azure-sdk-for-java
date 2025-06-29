// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Http2ConnectionConfigTests {
    @BeforeMethod(groups = { "unit" })
    public void before_Method() throws Exception {
        this.cleanup();
    }

    @AfterMethod(groups = { "unit" })
    public void after_Method() throws Exception {
        this.cleanup();
    }

    private void cleanup() {
        System.clearProperty("COSMOS.HTTP2_ENABLED");
        System.clearProperty("COSMOS.HTTP2_MAX_CONNECTION_POOL_SIZE");
        System.clearProperty("COSMOS.HTTP2_MIN_CONNECTION_POOL_SIZE");
        System.clearProperty("COSMOS.HTTP2_MAX_CONCURRENT_STREAMS");
    }

    @Test(groups = { "unit" })
    public void defaultValues() {
        Http2ConnectionConfig cfg = new Http2ConnectionConfig();
        assertThat(cfg.isEnabled()).isNull();
        assertThat(cfg.isEffectivelyEnabled()).isEqualTo(false);
        assertThat(cfg.getMaxConnectionPoolSize()).isNull();
        assertThat(cfg.getEffectiveMaxConnectionPoolSize()).isEqualTo(1000);
        assertThat(cfg.getMinConnectionPoolSize()).isNull();
        assertThat(cfg.getEffectiveMinConnectionPoolSize())
            .isEqualTo(Math.max(8, Runtime.getRuntime().availableProcessors()));
        assertThat(cfg.getMaxConcurrentStreams()).isNull();
        assertThat(cfg.getEffectiveMaxConcurrentStreams()).isEqualTo(30);
    }

    @Test(groups = { "unit" })
    public void modifiedDefaultValues() {
        System.setProperty("COSMOS.HTTP2_ENABLED", "TRue");
        System.setProperty("COSMOS.HTTP2_MAX_CONNECTION_POOL_SIZE", "999");
        System.setProperty("COSMOS.HTTP2_MIN_CONNECTION_POOL_SIZE", "2");
        System.setProperty("COSMOS.HTTP2_MAX_CONCURRENT_STREAMS", "11");


        Http2ConnectionConfig cfg = new Http2ConnectionConfig();
        assertThat(cfg.isEnabled()).isNull();
        assertThat(cfg.isEffectivelyEnabled()).isEqualTo(true);
        assertThat(cfg.getMaxConnectionPoolSize()).isNull();
        assertThat(cfg.getEffectiveMaxConnectionPoolSize()).isEqualTo(999);
        assertThat(cfg.getMinConnectionPoolSize()).isNull();
        assertThat(cfg.getEffectiveMinConnectionPoolSize()).isEqualTo(2);
        assertThat(cfg.getMaxConcurrentStreams()).isNull();
        assertThat(cfg.getEffectiveMaxConcurrentStreams()).isEqualTo(11);
    }

    @Test(groups = { "unit" })
    public void settersHonoredAndNullResetsToDefaults() {
        System.setProperty("COSMOS.HTTP2_ENABLED", "TRue");
        System.setProperty("COSMOS.HTTP2_MAX_CONNECTION_POOL_SIZE", "999");
        System.setProperty("COSMOS.HTTP2_MIN_CONNECTION_POOL_SIZE", "2");
        System.setProperty("COSMOS.HTTP2_MAX_CONCURRENT_STREAMS", "11");


        Http2ConnectionConfig cfg = new Http2ConnectionConfig();
        assertThat(cfg.isEnabled()).isNull();
        assertThat(cfg.isEffectivelyEnabled()).isEqualTo(true);
        cfg.setEnabled(false);
        assertThat(cfg.isEnabled()).isEqualTo(false);
        assertThat(cfg.isEffectivelyEnabled()).isEqualTo(false);
        cfg.setEnabled(null);
        assertThat(cfg.isEnabled()).isNull();
        assertThat(cfg.isEffectivelyEnabled()).isEqualTo(true);

        assertThat(cfg.getMaxConnectionPoolSize()).isNull();
        assertThat(cfg.getEffectiveMaxConnectionPoolSize()).isEqualTo(999);
        cfg.setMaxConnectionPoolSize(998);
        assertThat(cfg.getMaxConnectionPoolSize()).isEqualTo(998);
        assertThat(cfg.getEffectiveMaxConnectionPoolSize()).isEqualTo(998);
        cfg.setMaxConnectionPoolSize(null);
        assertThat(cfg.getMaxConnectionPoolSize()).isNull();
        assertThat(cfg.getEffectiveMaxConnectionPoolSize()).isEqualTo(999);

        assertThat(cfg.getMinConnectionPoolSize()).isNull();
        assertThat(cfg.getEffectiveMinConnectionPoolSize()).isEqualTo(2);
        cfg.setMinConnectionPoolSize(3);
        assertThat(cfg.getMinConnectionPoolSize()).isEqualTo(3);
        assertThat(cfg.getEffectiveMinConnectionPoolSize()).isEqualTo(3);
        cfg.setMinConnectionPoolSize(null);
        assertThat(cfg.getMinConnectionPoolSize()).isNull();
        assertThat(cfg.getEffectiveMinConnectionPoolSize()).isEqualTo(2);

        assertThat(cfg.getMaxConcurrentStreams()).isNull();
        assertThat(cfg.getEffectiveMaxConcurrentStreams()).isEqualTo(11);
        cfg.setMaxConcurrentStreams(12);
        assertThat(cfg.getMaxConcurrentStreams()).isEqualTo(12);
        assertThat(cfg.getEffectiveMaxConcurrentStreams()).isEqualTo(12);
        cfg.setMaxConcurrentStreams(null);
        assertThat(cfg.getMaxConcurrentStreams()).isNull();
        assertThat(cfg.getEffectiveMaxConcurrentStreams()).isEqualTo(11);
    }
}

