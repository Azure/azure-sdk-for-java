module com.azure.resourcemanager.privatedns {
    requires transitive com.azure.resourcemanager.resources;

    exports com.azure.resourcemanager.privatedns;
    exports com.azure.resourcemanager.privatedns.fluent;
    exports com.azure.resourcemanager.privatedns.fluent.inner;
    exports com.azure.resourcemanager.privatedns.models;

    opens com.azure.resourcemanager.privatedns.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.privatedns.models to com.fasterxml.jackson.databind, com.azure.core;
}
