module com.azure.storage.blob {
    requires transitive com.azure.core;
    requires com.azure.storage.common;

    exports com.azure.storage.blob;
    exports com.azure.storage.blob.models;
}
