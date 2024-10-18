package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.RemoteDependencyTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

import static com.azure.monitor.opentelemetry.exporter.implementation.MappingsBuilder.MappingType.SPAN;
import static com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils.toRemoteDependencyData;
import static org.assertj.core.api.Assertions.assertThat;

class MappingsBuilderTest {

    @Test
    void testThatCosmosDbAttributesAreRetained() {
        Mappings mappings = new MappingsBuilder(SPAN).build();
        Attributes attributes = Attributes.builder()
            .put("db.one", "one")
            .put("db.cosmosdb.two", "two")
            .build();

        RemoteDependencyTelemetryBuilder telemetryBuilder = RemoteDependencyTelemetryBuilder.create();

        mappings.map(attributes, telemetryBuilder);

        TelemetryItem telemetryItem = telemetryBuilder.build();

        RemoteDependencyData remoteDependencyData = toRemoteDependencyData(telemetryItem.getData().getBaseData());

        assertThat(remoteDependencyData.getProperties()).hasSize(1);
        assertThat(remoteDependencyData.getProperties().get("db.cosmosdb.two")).isEqualTo("two");
    }
}
