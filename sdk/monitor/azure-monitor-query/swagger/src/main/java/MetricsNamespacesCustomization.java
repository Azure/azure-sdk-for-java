import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

public class MetricsNamespacesCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        ClassCustomization metricsNamespaces = libraryCustomization
                .getClass("com.azure.monitor.query.implementation.metricsnamespaces", "MetricNamespaces");
        metricsNamespaces.rename("MetricNamespacesImpl");

        ClassCustomization metricsNamespacesClient = libraryCustomization
                .getClass("com.azure.monitor.query.implementation.metricsnamespaces", "MonitorManagementClient");
        metricsNamespacesClient.rename("MetricsNamespacesClientImpl");

        ClassCustomization metricsNamespacesClientBuilder = libraryCustomization
                .getClass("com.azure.monitor.query.implementation.metricsnamespaces", "MonitorManagementClientBuilder");
        metricsNamespacesClientBuilder.rename("MetricsNamespacesClientImplBuilder");
    }
}
