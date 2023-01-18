import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

public class MetricsDefinitionsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        ClassCustomization metricsDefinitions = libraryCustomization
                .getClass("com.azure.monitor.query.implementation.metricsdefinitions", "MetricDefinitions");
        metricsDefinitions.rename("MetricDefinitionsImpl");

        ClassCustomization metricsDefinitionsClient = libraryCustomization
                .getClass("com.azure.monitor.query.implementation.metricsdefinitions", "MonitorManagementClient");
        metricsDefinitionsClient.rename("MetricsDefinitionsClientImpl");

        ClassCustomization metricsDefinitionsClientBuilder = libraryCustomization
                .getClass("com.azure.monitor.query.implementation.metricsdefinitions", "MonitorManagementClientBuilder");
        metricsDefinitionsClientBuilder.rename("MetricsDefinitionsClientImplBuilder");
    }
}
