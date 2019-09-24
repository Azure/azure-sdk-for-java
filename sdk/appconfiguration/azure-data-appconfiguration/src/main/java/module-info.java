module com.azure.data.appconfiguration {
    requires com.azure.core;

    opens com.azure.data.appconfiguration.implementation to com.fasterxml.jackson.databind;
    opens com.azure.data.appconfiguration.models to com.fasterxml.jackson.databind;
}
