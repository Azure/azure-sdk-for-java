import com.azure.core.metrics.micrometer.MicrometerMeterProvider;

module com.azure.core.metrics.micrometer {
    requires transitive com.azure.core;
    requires micrometer.core;

    opens com.azure.core.metrics.micrometer to com.fasterxml.jackson.databind;

    exports com.azure.core.metrics.micrometer;

    provides com.azure.core.util.metrics.ClientMeterProvider
        with MicrometerMeterProvider;
}
