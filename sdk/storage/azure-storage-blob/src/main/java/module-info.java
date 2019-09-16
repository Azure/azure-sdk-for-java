module com.azure.storage.blob {
    requires transitive com.azure.core;
    requires transitive com.azure.storage.common;

    exports com.azure.storage.blob;
    exports com.azure.storage.blob.models;

    opens com.azure.storage.blob.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.blob.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
