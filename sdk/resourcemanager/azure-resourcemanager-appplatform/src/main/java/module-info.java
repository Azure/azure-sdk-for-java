module com.azure.resourcemanager.appplatform {
    requires transitive com.azure.resourcemanager.resources;
    requires transitive com.azure.storage.file.share;
    requires org.apache.commons.compress;

    exports com.azure.resourcemanager.appplatform;
    exports com.azure.resourcemanager.appplatform.fluent;
    exports com.azure.resourcemanager.appplatform.fluent.inner;
    exports com.azure.resourcemanager.appplatform.models;

    opens com.azure.resourcemanager.appplatform.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.appplatform.models to com.fasterxml.jackson.databind, com.azure.core;
}
