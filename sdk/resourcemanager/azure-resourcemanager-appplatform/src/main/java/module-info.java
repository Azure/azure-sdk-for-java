module com.azure.resourcemanager.appplatform {
    requires transitive com.azure.resourcemanager.resources;
    requires com.azure.storage.file.share;
    requires org.apache.commons.compress;

    exports com.azure.resourcemanager.appplatform;
    exports com.azure.resourcemanager.appplatform.fluent;
    exports com.azure.resourcemanager.appplatform.fluent.inner;
    exports com.azure.resourcemanager.appplatform.models;
}
