module com.azure.storage.file {
    requires transitive com.azure.core;
    requires com.azure.storage.common;

    exports com.azure.storage.file;
    exports com.azure.storage.file.models;

    opens com.azure.storage.file.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.file.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
