# Migrating to Azure Monitor OpenTelemetry SDK Autoconfigure Distro

## Replace dependency

Replace the `azure-monitor-opentelemetry-exporter` dependency by the following one

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-opentelemetry-autoconfigure</artifactId>
</dependency>
```

## Update the Java code

Replace the `com.azure.monitor.opentelemetry.exporter.AzureMonitorExporter` method by the `com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure` one.
