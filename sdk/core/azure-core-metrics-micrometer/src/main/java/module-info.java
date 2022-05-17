import com.azure.core.metrics.micrometer.MicrometerMeterProvider;
import com.azure.core.util.metrics.AzureMeterProvider;

module com.azure.core.metrics.micrometer {
    requires transitive com.azure.core;
    requires micrometer.core;

    opens com.azure.core.metrics.micrometer to com.fasterxml.jackson.databind;

    exports com.azure.core.metrics.micrometer;

    provides AzureMeterProvider
        with MicrometerMeterProvider;
}
