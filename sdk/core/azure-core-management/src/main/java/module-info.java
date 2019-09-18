module com.azure.core.management {
    requires com.azure.core;
    requires org.reactivestreams;

    exports com.azure.core.management.implementation to com.fasterxml.jackson.databind;

    opens com.azure.core.management to com.fasterxml.jackson.databind;
    opens com.azure.core.management.implementation to com.fasterxml.jackson.databind;

    uses com.azure.core.http.HttpClientProvider;
}
