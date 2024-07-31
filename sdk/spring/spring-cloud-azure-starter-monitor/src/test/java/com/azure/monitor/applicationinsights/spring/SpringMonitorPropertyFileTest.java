package com.azure.monitor.applicationinsights.spring;

import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SpringMonitorPropertyFileTest {

    @Test
    void should_contain_project_properties() {
        Map<String, String> properties =
            CoreUtils.getProperties("azure-spring-cloud-azure-starter-monitor.properties");

        assertThat(properties).hasFieldOrPropertyWithValue("name", "spring-cloud-azure-starter-monitor");
        assertThat(properties).hasEntrySatisfying("version", value -> assertThat(value).matches("[0-9].[0-9].[0-9].*"));
    }
}
