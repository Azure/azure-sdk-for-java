// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FaultInjectionConfigTest {

    @Test(groups = {"unit"})
    public void parsesInheritedFaultInjectionConfig() throws Exception {
        BenchmarkConfig config = parseConfig(
            faultInjection("SERVICE_UNAVAILABLE", "PT5S", "PT10S", "0.25"),
            "");

        FaultInjectionConfig faultInjectionConfig = config.getTenantWorkloads().get(0).getFaultInjectionConfig();
        assertThat(faultInjectionConfig.getServerErrorType())
            .isEqualTo(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE);
        assertThat(faultInjectionConfig.getStartDelay()).isEqualTo(Duration.ofSeconds(5));
        assertThat(faultInjectionConfig.getDuration()).isEqualTo(Duration.ofSeconds(10));
        assertThat(faultInjectionConfig.getInjectionRate()).isEqualTo(0.25);
        assertThat(faultInjectionConfig.getRegion()).isNull();
    }

    @Test(groups = {"unit"})
    public void parsesRegionScopedFaultInjectionConfig() throws Exception {
        BenchmarkConfig config = parseConfig(
            faultInjection("SERVICE_UNAVAILABLE", "PT5S", "PT10S", "0.25", "West US"),
            "");

        FaultInjectionConfig faultInjectionConfig = config.getTenantWorkloads().get(0).getFaultInjectionConfig();
        assertThat(faultInjectionConfig.getRegion()).isEqualTo("West US");
    }

    @Test(groups = {"unit"})
    public void tenantFaultInjectionOverridesDefaults() throws Exception {
        BenchmarkConfig config = parseConfig(
            faultInjection("TIMEOUT", "PT1S", "PT2S", "0.1"),
            ",\"faultInjection\":" + faultInjection("SERVICE_UNAVAILABLE", "PT3S", "PT4S", "0.5"));

        FaultInjectionConfig faultInjectionConfig = config.getTenantWorkloads().get(0).getFaultInjectionConfig();
        assertThat(faultInjectionConfig.getServerErrorType())
            .isEqualTo(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE);
        assertThat(faultInjectionConfig.getStartDelay()).isEqualTo(Duration.ofSeconds(3));
        assertThat(faultInjectionConfig.getDuration()).isEqualTo(Duration.ofSeconds(4));
        assertThat(faultInjectionConfig.getInjectionRate()).isEqualTo(0.5);
    }

    @DataProvider(name = "invalidFaultInjectionConfigs")
    public Object[][] invalidFaultInjectionConfigs() {
        return new Object[][] {
            {"{\"startDelay\":\"PT1S\",\"duration\":\"PT2S\",\"injectionRate\":1}", "serverErrorType"},
            {faultInjection("TIMEOUT", "-PT1S", "PT2S", "1"), "startDelay"},
            {faultInjection("TIMEOUT", "PT1S", "PT0S", "1"), "duration"},
            {faultInjection("TIMEOUT", "PT1S", "PT2S", "0"), "injectionRate"},
            {faultInjection("TIMEOUT", "PT1S", "PT2S", "1.01"), "injectionRate"},
            {faultInjection("TIMEOUT", "PT1S", "PT2S", "1", ""), "region"}
        };
    }

    @Test(dataProvider = "invalidFaultInjectionConfigs", groups = {"unit"})
    public void rejectsInvalidFaultInjectionConfig(String faultInjection, String expectedField) {
        assertThatThrownBy(() -> parseConfig(faultInjection, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(expectedField);
    }

    @DataProvider(name = "unsupportedWorkloads")
    public Object[][] unsupportedWorkloads() {
        return new Object[][] {
            {",\"useSync\":true", "sync workloads"},
            {",\"encryptionEnabled\":true", "encryption workloads"},
            {",\"operation\":\"CtlWorkload\"", "CtlWorkload"}
        };
    }

    @Test(dataProvider = "unsupportedWorkloads", groups = {"unit"})
    public void rejectsUnsupportedWorkloadFamily(String tenantSetting, String expectedMessage) {
        assertThatThrownBy(() -> parseConfig(
            faultInjection("SERVICE_UNAVAILABLE", "PT1S", "PT2S", "1"),
            tenantSetting))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(expectedMessage);
    }

    private static BenchmarkConfig parseConfig(String defaultFaultInjection, String tenantSettings) throws Exception {
        String json = "{"
            + "\"orchestrator\":{"
            + "\"tenantDefaults\":{\"faultInjection\":" + defaultFaultInjection + "},"
            + "\"tenants\":[{"
            + "\"id\":\"tenant-1\","
            + "\"serviceEndpoint\":\"https://account.documents.azure.com:443/\","
            + "\"masterKey\":\"key\","
            + "\"databaseId\":\"database\","
            + "\"containerId\":\"container\""
            + tenantSettings
            + "}]}}";

        File file = File.createTempFile("fault-injection-config-", ".json");
        try {
            Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
            return BenchmarkConfig.fromFile(file);
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    private static String faultInjection(
        String serverErrorType,
        String startDelay,
        String duration,
        String injectionRate) {

        return faultInjection(serverErrorType, startDelay, duration, injectionRate, null);
    }

    private static String faultInjection(
        String serverErrorType,
        String startDelay,
        String duration,
        String injectionRate,
        String region) {

        return "{"
            + "\"serverErrorType\":\"" + serverErrorType + "\","
            + "\"startDelay\":\"" + startDelay + "\","
            + "\"duration\":\"" + duration + "\","
            + "\"injectionRate\":" + injectionRate
            + (region != null ? ",\"region\":\"" + region + "\"" : "")
            + "}";
    }
}