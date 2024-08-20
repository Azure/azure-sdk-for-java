// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.StatsbeatTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import okio.BufferedSource;
import okio.Okio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AttachStatsbeatTest {

    private AttachStatsbeat attachStatsbeat;

    @BeforeEach
    public void setup() {
        attachStatsbeat = new AttachStatsbeat(new CustomDimensions());
    }

    @Test
    public void testVirtualMachineResourceProviderId() throws IOException {
        assertThat(attachStatsbeat.getResourceProviderId()).isEqualTo("unknown");
        Path path
            = new File(getClass().getClassLoader().getResource("metadata_instance_linux.json").getPath()).toPath();
        InputStream in = Files.newInputStream(path);
        BufferedSource source = Okio.buffer(Okio.source(in));
        String result = source.readUtf8();
        source.close();
        CustomDimensions customDimensions = new CustomDimensions();
        AzureMetadataService azureMetadataService
            = new AzureMetadataService(attachStatsbeat, customDimensions, (response) -> {
            });
        azureMetadataService.updateMetadata(result);
        assertThat("2a1216c3-a2a0-4fc5-a941-b1f5acde7051/65b2f83e-7bf1-4be3-bafc-3a4163265a52")
            .isEqualTo(attachStatsbeat.getResourceProviderId());

        StatsbeatTelemetryBuilder telemetryBuilder = StatsbeatTelemetryBuilder.create("test", 1);
        customDimensions.populateProperties(telemetryBuilder, null);
        MetricsData data = (MetricsData) telemetryBuilder.build().getData().getBaseData();
        assertThat(data.getProperties()).containsEntry("os", "Linux");
    }

    @Test
    public void testAppSvcResourceProviderId() {
        Map<String, String> envVars = new HashMap<>();
        envVars.put("WEBSITE_SITE_NAME", "test_site_name");
        envVars.put("WEBSITE_HOME_STAMPNAME", "test_stamp_name");

        assertThat(AttachStatsbeat.initResourceProviderId(ResourceProvider.RP_APPSVC, null, envVars::get))
            .isEqualTo("test_site_name/test_stamp_name");
    }

    @Test
    public void testFunctionsResourceProviderId() {
        Map<String, String> envVars = new HashMap<>();
        envVars.put("WEBSITE_HOSTNAME", "test_function_name");

        assertThat(AttachStatsbeat.initResourceProviderId(ResourceProvider.RP_FUNCTIONS, null, envVars::get))
            .isEqualTo("test_function_name");
    }

    @Test
    public void testAksResourceProviderId() {
        Map<String, String> envVars = new HashMap<>();
        envVars.put("AKS_ARM_NAMESPACE_ID", "test_aks_rp_id");
        assertThat(AttachStatsbeat.initResourceProviderId(ResourceProvider.RP_AKS, null, envVars::get))
            .isEqualTo("test_aks_rp_id");
    }

    @Test
    public void testUnknownResourceProviderId() {
        assertThat(new CustomDimensions().getResourceProvider()).isEqualTo(ResourceProvider.UNKNOWN);
        assertThat(attachStatsbeat.getResourceProviderId()).isEqualTo("unknown");
    }
}
