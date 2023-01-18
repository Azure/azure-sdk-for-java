module com.azure.ai.openai {
    requires transitive com.azure.core;

    exports com.azure.ai.openai;
    exports com.azure.ai.openai.models;

    opens com.azure.ai.openai.models to
            com.azure.core,
            com.fasterxml.jackson.databind;
}
