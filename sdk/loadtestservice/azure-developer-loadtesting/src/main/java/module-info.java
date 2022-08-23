module com.azure.developer.loadtesting {
    requires transitive com.azure.core;

    exports com.azure.developer.loadtesting;

    opens com.azure.developer.loadtesting.implementation.models to
            com.azure.core,
            com.fasterxml.jackson.databind;
}
