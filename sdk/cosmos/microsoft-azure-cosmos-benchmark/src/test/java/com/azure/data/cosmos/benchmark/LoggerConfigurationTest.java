package com.azure.data.cosmos.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggerConfigurationTest {
    /**
     * Netty logging is off. We expect error logging to be disabled.
     */
    @Test(groups = {"unit"})
    public void nettyIsDisabled() {
        final Logger logger = LoggerFactory.getLogger("io.netty");
        final Logger logger2 = LoggerFactory.getLogger("io.netty.channel");

        assertThat(logger.isErrorEnabled()).isFalse();
        assertThat(logger2.isErrorEnabled()).isFalse();
    }

    /**
     * Loggers in the cosmos package should inherit root configuration and log at info.
     */
    @Test(groups = {"unit"})
    public void defaultLoggersAtInfo() {
        final Logger logger = LoggerFactory.getLogger("com.azure.cosmos.internal.query.aggregation");
        final Logger logger2 = LoggerFactory.getLogger("com.azure.cosmos");

        assertThat(logger.isInfoEnabled()).isTrue();
        assertThat(logger2.isInfoEnabled()).isTrue();

        assertThat(logger.isDebugEnabled()).isFalse();
        assertThat(logger2.isDebugEnabled()).isFalse();
    }
}
