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

Replace:
* the `com.azure.monitor.opentelemetry.exporter.AzureMonitorExporter` class by `com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure`
* the `com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterOptions` class by `com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigureOptions`

