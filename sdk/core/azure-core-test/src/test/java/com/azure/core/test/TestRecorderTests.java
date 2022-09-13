package com.azure.core.test;

import com.azure.core.implementation.util.EnvironmentConfiguration;
import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.test.policy.ProxyRecordPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.logging.ClientLogger;
import org.junit.After;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRecorderTests {

    @BeforeAll
    public static void setupClass() {
        Configuration.getGlobalConfiguration().put(TestingHelpers.AZURE_TEST_MODE, "RECORD");
        TestBase.setupClass();
    }

    @AfterAll
    public static void teardownClass() {
        TestBase.teardownClass();
        Configuration.getGlobalConfiguration().remove(TestingHelpers.AZURE_TEST_MODE);
    }

    @Test
    public void testContainerStarted() {
        assertTrue(TestBase.isTestContainerRunning());
    }

    @Test
    public void startProxyRecording() {
        ProxyRecordPolicy policy = new ProxyRecordPolicy(null);
        policy.startRecording("record-file");
    }
}
