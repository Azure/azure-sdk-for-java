package com.azure.data.cosmos.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoggerConfigurationTest {
    /**
     * Netty logging is off. We expect error logging to be disabled.
     */
    @Test(groups = {"unit"})
    public void nettyIsDisabled() {
        final Logger logger = LoggerFactory.getLogger("io.netty");
        final Logger logger2 = LoggerFactory.getLogger("io.netty.channel");

        Assert.assertFalse(logger.isErrorEnabled());
        Assert.assertFalse(logger2.isErrorEnabled());
    }

    /**
     * Loggers in the cosmos package should inherit root configuration and log at info.
     */
    @Test(groups = {"unit"})
    public void defaultLoggersAtInfo() {
        final Logger logger = LoggerFactory.getLogger("com.azure.cosmos.internal.query.aggregation");
        final Logger logger2 = LoggerFactory.getLogger("com.azure.cosmos");

        Assert.assertTrue(logger.isInfoEnabled());
        Assert.assertTrue(logger2.isInfoEnabled());

        Assert.assertFalse(logger.isDebugEnabled());
        Assert.assertFalse(logger2.isDebugEnabled());
    }
}
