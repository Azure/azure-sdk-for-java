// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.Protocol;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigsTests {

    @Test(groups = { "unit" })
    public void maxHttpHeaderSize() {
        Configs config = new Configs();
        assertThat(config.getMaxHttpHeaderSize()).isEqualTo(32 * 1024);
    }

    @Test(groups = { "unit" })
    public void maxHttpBodyLength() {
        Configs config = new Configs();
        assertThat(config.getMaxHttpBodyLength()).isEqualTo(6 * 1024 * 1024);
    }

    @Test(groups = { "unit" })
    public void getProtocol() {
        Configs config = new Configs();
        assertThat(config.getProtocol()).isEqualTo(Protocol.valueOf(System.getProperty("azure.cosmos.directModeProtocol", "TCP").toUpperCase()));
    }

    @Test(groups = { "unit" })
    public void getDirectHttpsMaxConnectionLimit() {
        Configs config = new Configs();
        assertThat(config.getDirectHttpsMaxConnectionLimit()).isEqualTo(Runtime.getRuntime().availableProcessors() * 500);
    }

    @Test(groups = { "unit" })
    public void isVerboseMetricsEnabledDefault() {
        assertThat(Configs.isVerboseMetricsEnabled()).isEqualTo(false);
    }

    @Test(groups = { "unit" })
    public void isVerboseMetricsEnabled() {
        System.setProperty(Configs.VERBOSE_METRICS_ENABLED, "null");
        assertThat(Configs.isVerboseMetricsEnabled()).isEqualTo(false);

        System.setProperty(Configs.VERBOSE_METRICS_ENABLED, "");
        assertThat(Configs.isVerboseMetricsEnabled()).isEqualTo(false);

        System.setProperty(Configs.VERBOSE_METRICS_ENABLED, "false1");
        assertThat(Configs.isVerboseMetricsEnabled()).isEqualTo(false);

        System.setProperty(Configs.VERBOSE_METRICS_ENABLED, "TRue");
        assertThat(Configs.isVerboseMetricsEnabled()).isEqualTo(true);

        System.setProperty(Configs.VERBOSE_METRICS_ENABLED, "true");
        assertThat(Configs.isVerboseMetricsEnabled()).isEqualTo(true);

        System.setProperty(Configs.VERBOSE_METRICS_ENABLED, "true ");
        assertThat(Configs.isVerboseMetricsEnabled()).isEqualTo(false);

        System.clearProperty(Configs.VERBOSE_METRICS_ENABLED);
    }
}
