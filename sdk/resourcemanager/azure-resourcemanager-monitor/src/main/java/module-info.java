module com.azure.resourcemanager.monitor {
    requires transitive com.azure.resourcemanager.resources;

    exports com.azure.resourcemanager.monitor;
    exports com.azure.resourcemanager.monitor.fluent;
    exports com.azure.resourcemanager.monitor.fluent.inner;
    exports com.azure.resourcemanager.monitor.models;

    opens com.azure.resourcemanager.monitor.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.monitor.models to com.fasterxml.jackson.databind, com.azure.core;
}
